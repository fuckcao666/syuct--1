/*
 * Copyright 2014 CyberVision, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kaaproject.kaa.client.logging;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.kaaproject.kaa.client.channel.KaaChannelManager;
import org.kaaproject.kaa.client.channel.LogTransport;
import org.kaaproject.kaa.client.channel.TransportConnectionInfo;
import org.kaaproject.kaa.client.context.ExecutorContext;
import org.kaaproject.kaa.common.TransportType;
import org.kaaproject.kaa.common.endpoint.gen.LogDeliveryErrorCode;
import org.kaaproject.kaa.common.endpoint.gen.LogDeliveryStatus;
import org.kaaproject.kaa.common.endpoint.gen.LogEntry;
import org.kaaproject.kaa.common.endpoint.gen.LogSyncRequest;
import org.kaaproject.kaa.common.endpoint.gen.LogSyncResponse;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponseResultType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reference implementation of @see LogCollector
 * 
 * @author Andrew Shvayka
 */
public abstract class AbstractLogCollector implements LogCollector, LogProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractLogCollector.class);

    public static final long MAX_BATCH_VOLUME = 512 * 1024; // Framework
                                                            // limitation
    private final static long DELAY = 60L;
    private final static long PERIOD = 5 * 60L;

    protected final ExecutorContext executorContext;
    private final LogTransport transport;
    private final Map<Integer, Long> timeoutMap = new ConcurrentHashMap<>();
    private final KaaChannelManager channelManager;

    protected LogStorage storage;
    private LogUploadStrategy strategy;
    private LogFailoverCommand controller;

    private volatile boolean isUploading = false;

    public AbstractLogCollector(LogTransport transport, ExecutorContext executorContext, KaaChannelManager manager) {
        this.strategy = new DefaultLogUploadStrategy();
        this.storage = new MemoryLogStorage(strategy.getBatchSize());
        this.controller = new DefaultLogUploadController();
        this.channelManager = manager;
        this.transport = transport;
        this.executorContext = executorContext;
    }

    @Override
    public void setStrategy(LogUploadStrategy strategy) {
        if (strategy == null) {
            throw new IllegalArgumentException("Strategy is null!");
        }
        this.strategy = strategy;
        LOG.info("New log upload strategy was set: {}", strategy);
    }

    @Override
    public void setStorage(LogStorage storage) {
        if (storage == null) {
            throw new IllegalArgumentException("Storage is null!");
        }
        this.storage = storage;
        LOG.info("New log storage was set {}", storage);
    }

    @Override
    public void fillSyncRequest(LogSyncRequest request) {
        LogBlock group = null;
        synchronized (storage) {
            isUploading = false;
            if (storage.getStatus().getRecordCount() == 0) {
                LOG.debug("Log storage is empty");
                return;
            }
            group = storage.getRecordBlock(strategy.getBatchSize());
        }

        if (group != null) {
            List<LogRecord> recordList = group.getRecords();

            if (!recordList.isEmpty()) {
                LOG.trace("Sending {} log records", recordList.size());

                List<LogEntry> logs = new LinkedList<>();
                for (LogRecord record : recordList) {
                    logs.add(new LogEntry(ByteBuffer.wrap(record.getData())));
                }

                request.setRequestId(group.getBlockId());
                request.setLogEntries(logs);

                timeoutMap.put(group.getBlockId(), System.currentTimeMillis() + strategy.getTimeout() * 1000);
            }
        } else {
            LOG.warn("Log group is null: log group size is too small");
        }
    }

    @Override
    public synchronized void onLogResponse(LogSyncResponse logSyncResponse) throws IOException {
        if (logSyncResponse.getDeliveryStatuses() != null) {
            boolean isAlreadyScheduled = false;
            for (LogDeliveryStatus response : logSyncResponse.getDeliveryStatuses()) {
                if (response.getResult() == SyncResponseResultType.SUCCESS) {
                    storage.removeRecordBlock(response.getRequestId());
                } else {
                    storage.notifyUploadFailed(response.getRequestId());
                    final LogDeliveryErrorCode errorCode = response.getErrorCode();
                    final LogFailoverCommand controller = this.controller;
                    executorContext.getCallbackExecutor().execute(new Runnable() {
                        @Override
                        public void run() {
                            strategy.onFailure(controller, errorCode);
                        }
                    });
                    isAlreadyScheduled = true;
                }

                timeoutMap.remove(response.getRequestId());
            }

            if (!isAlreadyScheduled) {
                processUploadDecision(strategy.isUploadNeeded(storage.getStatus()));
            }
        }
    }

    @Override
    public void stop() {

    }

    private void processUploadDecision(LogUploadStrategyDecision decision, boolean scheduleUpload) {
        switch (decision) {
            case UPLOAD:
                if (!isUploading) {
                    if (scheduleUpload) {
                        scheduleLogUpload();
                    }
                    isUploading = true;
                    transport.sync();
                }
                break;
            case NOOP:
            default:
                break;
        }
    }

    private void processUploadDecision(LogUploadStrategyDecision decision) {
        processUploadDecision(decision, true);
    }

    protected boolean isDeliveryTimeout() {
        boolean isTimeout = false;
        long currentTime = System.currentTimeMillis();

        for (Map.Entry<Integer, Long> logRequest : timeoutMap.entrySet()) {
            if (currentTime >= logRequest.getValue()) {
                isTimeout = true;
                break;
            }
        }

        if (isTimeout) {
            LOG.info("Log delivery timeout detected");

            for (Map.Entry<Integer, Long> logRequest : timeoutMap.entrySet()) {
                storage.notifyUploadFailed(logRequest.getKey());
            }

            timeoutMap.clear();
            final LogFailoverCommand controller = this.controller;
            executorContext.getCallbackExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    strategy.onTimeout(controller);
                }
            });
        }

        return isTimeout;
    }

    protected void uploadIfNeeded(boolean scheduleUpload) {
        processUploadDecision(strategy.isUploadNeeded(storage.getStatus()), scheduleUpload);
    }

    protected void uploadIfNeeded() {
        processUploadDecision(strategy.isUploadNeeded(storage.getStatus()), true);
    }

    private class DefaultLogUploadController implements LogFailoverCommand {
        @Override
        public void switchAccessPoint() {
            TransportConnectionInfo server = channelManager.getActiveServer(TransportType.LOGGING);
            if (server != null) {
                channelManager.onServerFailed(server);
            } else {
                LOG.warn("Failed to switch Operation server. No channel is used for logging transport");
            }
        }

        @Override
        public void retryLogUpload() {
            uploadIfNeeded();
        }

        @Override
        public void retryLogUpload(int delay) {
            executorContext.getScheduledExecutor().schedule(new Runnable() {
                @Override
                public void run() {
                    uploadIfNeeded();
                }
            }, delay, TimeUnit.SECONDS);
        }
    }

    public final void scheduleLogUpload() {
        executorContext.getScheduledExecutor().schedule(new Runnable() {
            @Override
            public void run() {
                if (!isDeliveryTimeout()) {
                    uploadIfNeeded(false);
                }
            }
        }, DELAY, TimeUnit.SECONDS);
    }
}
