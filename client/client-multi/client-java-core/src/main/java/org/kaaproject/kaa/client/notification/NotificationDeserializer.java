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
package org.kaaproject.kaa.client.notification;

import java.io.IOException;
import java.util.Collection;

import javax.annotation.Generated;

import org.kaaproject.kaa.common.avro.AvroByteArrayConverter;
import org.kaaproject.kaa.common.endpoint.gen.Topic;
import org.kaaproject.kaa.schema.base.Notification;

/**
 * This class deserialize binary data to notification object.
 * 
 * This implementation is auto-generated. Please modify corresponding template file.
 * 
 * @author Andrew Shvayka
 *
 */
@Generated("NotificationDeserializer.java.template")
class NotificationDeserializer {

    private final AvroByteArrayConverter<Notification> converter = new AvroByteArrayConverter<Notification>(Notification.class);

    void notify(Collection<NotificationListener> listeners, Topic topic, byte[] notificationData) throws IOException{
        Notification notification = fromByteArray(notificationData);
        for(NotificationListener listener : listeners){
            listener.onNotification(topic.getId(), notification);
        }
    }
    
    private Notification fromByteArray(byte[] data) throws IOException {
        return converter.fromByteArray(data);
    }

}
