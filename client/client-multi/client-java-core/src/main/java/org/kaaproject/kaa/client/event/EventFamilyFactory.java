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

package org.kaaproject.kaa.client.event;

import java.util.HashSet;
import java.util.Set;
import org.kaaproject.kaa.client.transact.TransactionId;

import javax.annotation.Generated;

/**
 * Factory for accessing supported event families.
 * DO NOT edit it, this class is auto-generated.
 *
 * @author Taras Lemkin
 *
 */
@Generated("EventFamilyFactory.java.template")
public class EventFamilyFactory {
    private EventManager eventManager;
    private final Set<EventFamily> eventFamilies = new HashSet<EventFamily>();

    public EventFamilyFactory(EventManager eventManager) {
        this.eventManager = eventManager;
    }

    public TransactionId startEventsBlock() {
        return eventManager.beginTransaction();
    }

    public void submitEventsBlock(TransactionId trxId) {
        eventManager.commit(trxId);
    }

    public void removeEventsBlock(TransactionId trxId) {
        eventManager.rollback(trxId);
    }
}
