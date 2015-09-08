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
package org.kaaproject.kaa.server.transport.message;

import java.util.UUID;

import org.kaaproject.kaa.server.transport.channel.ChannelContext;
import org.kaaproject.kaa.server.transport.channel.ChannelType;
import org.kaaproject.kaa.server.transport.platform.PlatformAware;

/**
 * An abstract class that represents a platform aware message. It includes
 * references to response and error converters that are used to convert objects
 * into the channel specific data.
 * 
 * @author Andrew Shvayka
 *
 */
public abstract class AbstractMessage implements PlatformAware {
    private final UUID uuid;
    private final int platformId;
    private final ChannelContext channelContext;
    private final ChannelType channelType;
    private final MessageBuilder messageBuilder;
    private final ErrorBuilder errorBuilder;

    protected AbstractMessage(UUID uuid, Integer platformId, ChannelContext channelContext, ChannelType channelType,
            MessageBuilder messageBuilder, ErrorBuilder errorBuilder) {
        super();
        this.uuid = uuid;
        this.platformId = platformId;
        this.channelContext = channelContext;
        this.channelType = channelType;
        this.messageBuilder = messageBuilder;
        this.errorBuilder = errorBuilder;
    }

    public UUID getChannelUuid() {
        return uuid;
    }

    public ChannelContext getChannelContext() {
        return channelContext;
    }

    public ChannelType getChannelType() {
        return channelType;
    }

    public MessageBuilder getMessageBuilder() {
        return messageBuilder;
    }

    public ErrorBuilder getErrorBuilder() {
        return errorBuilder;
    }

    @Override
    public int getPlatformId() {
        return platformId;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("AbstractMessage [uuid=");
        builder.append(uuid);
        builder.append(", channelContext=");
        builder.append(channelContext);
        builder.append(", channelType=");
        builder.append(channelType);
        builder.append(", responseConverter=");
        builder.append(messageBuilder);
        builder.append(", errorBuilder=");
        builder.append(errorBuilder);
        builder.append("]");
        return builder.toString();
    }
}
