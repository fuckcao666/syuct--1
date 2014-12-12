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

package org.kaaproject.kaa.server.operations.service.akka.messages.core.logs;

import java.util.List;

import org.kaaproject.kaa.server.common.log.shared.appender.LogEvent;
import org.kaaproject.kaa.server.common.log.shared.appender.LogEventPack;
import org.kaaproject.kaa.server.common.log.shared.appender.LogSchema;

import akka.actor.ActorRef;

/**
 * The Class LogEventPackMessage.
 */
public class LogEventPackMessage {

    private final String requestId;

    private final ActorRef originator;

    /** Log Event Pack. */
    private final LogEventPack logEventPack;

    /**
     * Instantiates a new log event pack message.
     * 
     * @param logEventPack
     *            the log event pack
     */
    public LogEventPackMessage(String requestId, ActorRef originator, LogEventPack logEventPack) {
        this.requestId = requestId;
        this.originator = originator;
        this.logEventPack = logEventPack;
    }

    public String getRequestId() {
        return requestId;
    }

    public ActorRef getOriginator() {
        return originator;
    }

    public LogEventPack getLogEventPack() {
        return logEventPack;
    }

    public String getEndpointKey() {
        return logEventPack.getEndpointKey();
    }

    public long getDateCreated() {
        return logEventPack.getDateCreated();
    }

    public LogSchema getLogSchema() {
        return logEventPack.getLogSchema();
    }

    public int getLogSchemaVersion() {
        return logEventPack.getLogSchemaVersion();
    }

    public List<LogEvent> getEvents() {
        return logEventPack.getEvents();
    }

    public void setLogSchema(LogSchema logSchema) {
        logEventPack.setLogSchema(logSchema);
    }
}
