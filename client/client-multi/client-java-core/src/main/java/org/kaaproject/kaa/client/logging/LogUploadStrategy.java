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

import org.kaaproject.kaa.common.endpoint.gen.LogDeliveryErrorCode;

/**
 * <p>
 * Interface for log upload strategy.
 * </p>
 *
 * <p>
 * Used by log collector on each adding of the new log record in order to check
 * whether to send logs to server or clean up local storage.
 * </p>
 *
 * <p>
 * Reference implementation used by default ({@link DefaultLogUploadStrategy}).
 * </p>
 */
public interface LogUploadStrategy {
    /**
     * Retrieves log upload decision based on current storage status and defined
     * upload configuration.
     *
     * @param status
     *            Log storage status
     *
     * @return Upload decision ({@link LogUploadStrategyDecision})
     */
    LogUploadStrategyDecision isUploadNeeded(LogStorageStatus status);
    
    /**
     * Retrieves maximum size of the report pack 
     * that will be delivered in single request to server 
     * @return size of the batch
     */
    long getBatchSize();

    /**
     * Maximum time to wait log delivery response.
     *
     * @return Time in seconds.
     */
    int getTimeout();

    /**
     * Handles timeout of log delivery
     * @param controller
     */
    void onTimeout(LogFailoverCommand controller);

    /**
     * Handles failure of log delivery
     * @param controller
     */
    void onFailure(LogFailoverCommand controller, LogDeliveryErrorCode code);
}