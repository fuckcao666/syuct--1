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

#ifndef DEFAULTOPERATIONHTTPCHANNEL_HPP_
#define DEFAULTOPERATIONHTTPCHANNEL_HPP_

#include "kaa/KaaDefaults.hpp"

#ifdef KAA_DEFAULT_OPERATION_HTTP_CHANNEL

#include "kaa/channel/impl/AbstractHttpChannel.hpp"

namespace kaa {

class DefaultOperationHttpChannel: public AbstractHttpChannel {
public:
    DefaultOperationHttpChannel(IKaaChannelManager *channelManager, const KeyPair& clientKeys)
            : AbstractHttpChannel(channelManager, clientKeys)
    {
    }
    virtual ~DefaultOperationHttpChannel()
    {
    }

    virtual const std::string& getId() const
    {
        return CHANNEL_ID;
    }
    virtual const std::map<TransportType, ChannelDirection>& getSupportedTransportTypes() const
    {
        return SUPPORTED_TYPES;
    }

private:
    virtual std::shared_ptr<IHttpRequest> createRequest(IPTransportInfoPtr server,
                                                        const std::vector<std::uint8_t>& body)
    {
        return getHttpDataProcessor()->createOperationRequest(server->getURL() + getURLSuffix(), body);
    }

    virtual std::string retrieveResponse(const IHttpResponse& response)
    {
        if (response.getStatusCode() != 200) {
            throw TransportException(boost::format("Invalid response code %1%") % response.getStatusCode());
        }
        return getHttpDataProcessor()->retrieveOperationResponse(response);
    }

    virtual ServerType getServerType() const
    {
        return ServerType::OPERATIONS;
    }

    virtual std::string getURLSuffix()
    {
        return "/EP/Sync";
    }

private:
    static const std::string CHANNEL_ID;
    static const std::map<TransportType, ChannelDirection> SUPPORTED_TYPES;
};

}

#endif

#endif /* DEFAULTOPERATIONHTTPCHANNEL_HPP_ */
