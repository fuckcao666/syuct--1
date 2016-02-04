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

#include "kaa/notification/NotificationTransport.hpp"

#ifdef KAA_USE_NOTIFICATIONS

#include <vector>
#include <string>
#include <algorithm>

#include "kaa/logging/Log.hpp"
#include "kaa/logging/LoggingUtils.hpp"
#include "kaa/ClientStatus.hpp"

namespace kaa {

NotificationTransport::NotificationTransport(IKaaChannelManager& manager, IKaaClientContext &context)
    : AbstractKaaTransport(manager, context), notificationProcessor_(nullptr)
{
    const DetailedTopicStates& detailedStatesContainer = context_.getStatus().getTopicStates();
    for (const auto& state : detailedStatesContainer) {
        notificationSubscriptions_.insert(std::make_pair(state.second.topicId, state.second.sequenceNumber));
    }
}

NotificationSyncRequestPtr NotificationTransport::createEmptyNotificationRequest()
{
    NotificationSyncRequestPtr request(new NotificationSyncRequest);

    request->appStateSeqNumber = context_.getStatus().getNotificationSequenceNumber();

    /*TODO: topic list hash is a future feature */
    request->topicListHash.set_null();

    const DetailedTopicStates& detailedStatesContainer = context_.getStatus().getTopicStates();
    if (!detailedStatesContainer.empty()) {
        std::vector<TopicState> container(detailedStatesContainer.size());
        auto it = detailedStatesContainer.begin();

        for (auto& state : container) {
            state.topicId = it->second.topicId;
            state.seqNumber = it->second.sequenceNumber;
            ++it;
        }

        request->topicStates.set_array(container);
    } else {
        request->topicStates.set_null();
    }

    request->acceptedUnicastNotifications.set_null();
    request->subscriptionCommands.set_null();

    return request;
}

NotificationSyncRequestPtr NotificationTransport::createNotificationRequest()
{
    NotificationSyncRequestPtr request(new NotificationSyncRequest);

    request->appStateSeqNumber = context_.getStatus().getNotificationSequenceNumber();

    /*TODO: topic list hash is a future feature */
    request->topicListHash.set_null();

    if (!acceptedUnicastNotificationIds_.empty()) {
        request->acceptedUnicastNotifications.set_array(std::vector<std::string>(
                acceptedUnicastNotificationIds_.begin(), acceptedUnicastNotificationIds_.end()));
    } else {
        request->acceptedUnicastNotifications.set_null();
    }

    const DetailedTopicStates& detailedStatesContainer = context_.getStatus().getTopicStates();
    if (!detailedStatesContainer.empty()) {
        std::vector<TopicState> container(detailedStatesContainer.size());
        auto it = detailedStatesContainer.begin();

        for (auto& state : container) {
            state.topicId = it->second.topicId;
            state.seqNumber = it->second.sequenceNumber;
            ++it;
        }

        request->topicStates.set_array(container);
    } else {
        request->topicStates.set_null();
    }

    if (!subscriptions_.empty()) {
        request->subscriptionCommands.set_array(std::vector<SubscriptionCommand>(subscriptions_.begin(),
                                                                                 subscriptions_.end()));
    } else {
        request->subscriptionCommands.set_null();
    }

    return request;
}

void NotificationTransport::onNotificationResponse(const NotificationSyncResponse& response)
{
    subscriptions_.clear();

    if (response.responseStatus == SyncResponseStatus::NO_DELTA) {
        acceptedUnicastNotificationIds_.clear();
    }

    context_.getStatus().setNotificationSequenceNumber(response.appStateSeqNumber);

    DetailedTopicStates detailedStatesContainer = context_.getStatus().getTopicStates();

    if (!response.availableTopics.is_null()) {
        const auto& topics = response.availableTopics.get_array();

        detailedStatesContainer.clear();

        for (const auto& topic : topics) {
            DetailedTopicState dts;

            dts.topicId = topic.id;
            dts.topicName = topic.name;
            dts.subscriptionType = topic.subscriptionType;
            dts.sequenceNumber = 0;

            auto insertResult = notificationSubscriptions_.insert(std::make_pair(topic.id, 0));
            if (!insertResult.second) {
                dts.sequenceNumber = notificationSubscriptions_[topic.id];
            }

            detailedStatesContainer[topic.id] = dts;
        }

        if (notificationProcessor_) {
            notificationProcessor_->topicsListUpdated(topics);
        }
    }

    if (!response.notifications.is_null()) {

        KAA_LOG_INFO(boost::format("Received notifications array: %1%") % LoggingUtils::NotificationToString(response.notifications));

        const auto& notifications = response.notifications.get_array();

        Notifications unicast = getUnicastNotifications(notifications);
        Notifications multicast = getMulticastNotifications(notifications);

        Notifications newNotifications;

        for (const auto& n : unicast) {
            const std::string& uid = n.uid.get_string();
            KAA_LOG_INFO(boost::format("Adding '%1%' to unicast accepted notifications") % uid);
            auto addResultPair = acceptedUnicastNotificationIds_.insert(uid);
            if (addResultPair.second) {
                newNotifications.push_back(n);
            } else {
                KAA_LOG_INFO(boost::format("Notification with uid [%1%] was already received") % uid);
            }
        }

        for (const auto& n : multicast) {
            KAA_LOG_DEBUG(boost::format("Notification: %1%, Stored sequence number: %2%")
                    % LoggingUtils::SingleNotificationToString(n)
                    % notificationSubscriptions_[n.topicId]);
            auto& sequenceNumber = notificationSubscriptions_[n.topicId];
            std::int32_t notificationSequenceNumber =
                        (n.seqNumber.is_null()) ? 0 : n.seqNumber.get_int();
            if (notificationSequenceNumber > sequenceNumber) {
                newNotifications.push_back(n);
                detailedStatesContainer[n.topicId].sequenceNumber = sequenceNumber = notificationSequenceNumber;
            }
        }

        if (notificationProcessor_) {
            notificationProcessor_->notificationReceived(newNotifications);
        }
    }    
    context_.getStatus().setTopicStates(detailedStatesContainer);
    if (response.responseStatus != SyncResponseStatus::NO_DELTA) {
        syncAck();
    }
}

Notifications NotificationTransport::getUnicastNotifications(const Notifications & notifications)
{
    Notifications result;
    for (const auto& n : notifications) {
        if (!n.uid.is_null()) {
            result.push_back(n);
        }
    }
    return result;
}

Notifications NotificationTransport::getMulticastNotifications(const Notifications & notifications)
{
    Notifications result;
    for (const auto& n : notifications) {
        if (n.uid.is_null()) {
            result.push_back(n);
        }
    }

    std::sort(result.begin(), result.end(), [&](const Notification& l, const Notification& r) -> bool { return l.seqNumber.get_int() < r.seqNumber.get_int(); });

    return result;
}

void NotificationTransport::onSubscriptionChanged(SubscriptionCommands&& commands)
{
    if (!commands.empty()) {
        subscriptions_.splice(subscriptions_.end(), commands, commands.begin(), commands.end());
    }
}

} /* namespace kaa */

#endif

