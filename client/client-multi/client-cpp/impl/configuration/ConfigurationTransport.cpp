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

#include "kaa/configuration/ConfigurationTransport.hpp"
#include "kaa/configuration/IConfigurationProcessor.hpp"
#include "kaa/configuration/storage/IConfigurationPersistenceManager.hpp"
#include "kaa/configuration/IConfigurationHashContainer.hpp"

#ifdef KAA_USE_CONFIGURATION

#include "kaa/logging/Log.hpp"

namespace kaa {

ConfigurationTransport::ConfigurationTransport(IKaaChannelManager& channelManager,
                                               IConfigurationProcessor *configProcessor,
                                               IConfigurationHashContainer *hashContainer,
                                               IKaaClientStateStoragePtr status)
        : AbstractKaaTransport(channelManager), configurationProcessor_(configProcessor), hashContainer_(hashContainer)
{
    setClientState(status);
}

void ConfigurationTransport::sync()
{
    syncByType();
}

std::shared_ptr<ConfigurationSyncRequest> ConfigurationTransport::createConfigurationRequest()
{
    if (!clientStatus_) {
        throw KaaException("Can not generate ConfigurationSyncRequest: Status was not provided");
    }

    std::shared_ptr<ConfigurationSyncRequest> request(new ConfigurationSyncRequest);
    request->appStateSeqNumber = clientStatus_->getConfigurationSequenceNumber();
    request->configurationHash.set_bytes(hashContainer_->getConfigurationHash());
    request->resyncOnly.set_bool(true); // Only full resyncs are currently supported
    return request;
}

void ConfigurationTransport::onConfigurationResponse(const ConfigurationSyncResponse &response)
{
    if (response.responseStatus != SyncResponseStatus::NO_DELTA) {
        clientStatus_->setConfigurationSequenceNumber(response.appStateSeqNumber);
        if (!response.confDeltaBody.is_null()) {
            std::vector<std::uint8_t> data = response.confDeltaBody.get_bytes();
            configurationProcessor_->processConfigurationData(data.data(), data.size(),
                                                              response.responseStatus == SyncResponseStatus::RESYNC);
        }
        syncAck();
    }
}

}  // namespace kaa

#endif
