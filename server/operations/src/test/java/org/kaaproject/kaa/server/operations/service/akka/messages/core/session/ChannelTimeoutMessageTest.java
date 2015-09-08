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

package org.kaaproject.kaa.server.operations.service.akka.messages.core.session;

import org.junit.Assert;
import org.junit.Test;

import java.util.UUID;

public class ChannelTimeoutMessageTest {

    @Test
    public void channelTimeoutMessageTest(){
        UUID uuid = UUID.randomUUID();
        long lastActivity = 13221;
        ChannelTimeoutMessage timeoutMessage = new ChannelTimeoutMessage(uuid, lastActivity);
        Assert.assertEquals(uuid, timeoutMessage.getChannelUuid());
        Assert.assertEquals(lastActivity, timeoutMessage.getLastActivityTime());
    }

}
