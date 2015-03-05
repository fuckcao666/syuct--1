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

#include "kaa/log/LoggingTransport.hpp"

#ifdef KAA_USE_LOGGING

#include "kaa/log/LogCollector.hpp"

#include "kaa/logging/Log.hpp"

namespace kaa {

LoggingTransport::LoggingTransport(IKaaChannelManager& manager, LogCollector& collector)
        : AbstractKaaTransport(manager), collector_(collector)
{
}

void LoggingTransport::sync()
{
    syncByType();
}

std::shared_ptr<LogSyncRequest> LoggingTransport::createLogSyncRequest()
{
    std::shared_ptr<LogSyncRequest> request(new LogSyncRequest());

    LogSyncRequest logBlockRequest = collector_.getLogUploadRequest();
    if (logBlockRequest.requestId != 0) {
        request.reset(new LogSyncRequest(logBlockRequest));
    }

    return request;
}

void LoggingTransport::onLogSyncResponse(const LogSyncResponse& response)
{
    collector_.onLogUploadResponse(response);
}

}  // namespace kaa

#endif
