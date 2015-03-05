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

#include "kaa/event/EventManager.hpp"

#ifdef KAA_USE_EVENTS

#include <utility>
#include <algorithm>

#include "kaa/common/UuidGenerator.hpp"
#include "kaa/logging/Log.hpp"
#include "kaa/gen/EndpointGen.hpp"
#include "kaa/event/IEventFamily.hpp"
#include "kaa/event/IFetchEventListeners.hpp"
#include "kaa/common/exception/KaaException.hpp"

namespace kaa {

void EventManager::registerEventFamily(IEventFamily* eventFamily)
{
    if (eventFamily) {
        auto it = eventFamilies_.insert(eventFamily);
        if (!it.second) {
            KAA_LOG_WARN("Failed to register event family: already exists");
        }
    } else {
        KAA_LOG_WARN("Failed to register event family: bad input data");
    }
}

void EventManager::produceEvent(const std::string& fqn, const std::vector<std::uint8_t>& data,
                                const std::string& target, TransactionIdPtr trxId)
{
    if (fqn.empty() || data.empty()) {
        KAA_LOG_WARN("Failed to process outgoing event: bad input data");
        return;
    }

    KAA_LOG_DEBUG(
            boost::format("Going to produce Event [FQN: %1%, target: %2%, data_size = %3%]") % fqn
            % (target.empty() ? "broadcast" : target) % data.size());

    Event event;
    event.eventClassFQN = fqn;
    event.eventData.assign(data.begin(), data.end());

    if (target.empty()) {
        event.target.set_null();
    } else {
        event.target.set_string(target);
    }

    if (trxId) {
        getContainerByTrxId(trxId).push_back(event);
        return;
    }

    KAA_MUTEX_UNIQUE_DECLARE(lock, sequenceGuard_);
    event.seqNum = eventSequenceNumber_++;
    status_->setEventSequenceNumber(eventSequenceNumber_);

    KAA_UNLOCK(lock);

    KAA_LOG_TRACE(boost::format("New event %1% is produced for %2%") % fqn % target);
    {
        KAA_MUTEX_UNIQUE_DECLARE(internal_lock, pendingEventsGuard_);
        pendingEvents_.push_back(event);
    }
    if (eventTransport_) {
        eventTransport_->sync();
    } else {
        KAA_LOG_WARN("Event postponed: transport was not set");
    }
}

std::list<Event> EventManager::releasePendingEvents()
{
    KAA_MUTEX_UNIQUE_DECLARE(lock, pendingEventsGuard_);
    std::list<Event> result(std::move(pendingEvents_));
    pendingEvents_ = std::list<Event>();
    return result;
}

bool EventManager::hasPendingEvents() const
{
    KAA_MUTEX_UNIQUE_DECLARE(lock, pendingEventsGuard_);
    return !pendingEvents_.empty();
}

std::map<std::int32_t, std::list<std::string> > EventManager::getPendingListenerRequests()
{
    KAA_MUTEX_UNIQUE_DECLARE(lock, eventListenersGuard_);
    std::map<std::int32_t, std::list<std::string> > result;
    for (const auto& idToFqnList : eventListenersRequests_) {
        result.insert(std::make_pair(idToFqnList.first, idToFqnList.second->eventFQNs_));
    }
    return result;
}

bool EventManager::hasPendingListenerRequests() const
{
    KAA_MUTEX_UNIQUE_DECLARE(lock, eventListenersGuard_);
    return !eventListenersRequests_.empty();
}

void EventManager::onEventFromServer(const std::string& eventClassFQN, const std::vector<std::uint8_t>& data,
                                     const std::string& source)
{
    if (eventClassFQN.empty() || data.empty()) {
        KAA_LOG_WARN("Failed to process incoming event: bad input data");
        return;
    }

    bool isProcessed = false;

    for (auto* family : eventFamilies_) {
        const auto& list = family->getSupportedEventClassFQNs();
        auto it = std::find(list.begin(), list.end(), eventClassFQN);
        if (it != list.end()) {
            KAA_LOG_TRACE(boost::format("Processing event for %1%") % eventClassFQN);
            family->onGenericEvent(eventClassFQN, data, source);
            isProcessed = true;
        }
    }

    if (!isProcessed) {
        KAA_LOG_WARN(boost::format("Event '%1%' wasn't processed: could "
                                   "not find appropriate family")
                     % eventClassFQN);
    }
}

void EventManager::onEventsReceived(const EventSyncResponse::events_t& events)
{
    auto eventContainer = events.get_array();
    std::sort(eventContainer.begin(), eventContainer.end(),
              [&](const Event& l, const Event& r) -> bool {return l.seqNum < r.seqNum;});
    for (const auto& event : eventContainer) {
        std::string source;
        if (!event.source.is_null()) {
            source = event.source.get_string();
        }
        onEventFromServer(event.eventClassFQN, event.eventData, source);
    }
}

void EventManager::onEventListenersReceived(const EventSyncResponse::eventListenersResponses_t& listenersResponses)
{
    if (!listenersResponses.is_null()) {
        const auto& container = listenersResponses.get_array();

        KAA_MUTEX_UNIQUE_DECLARE(lock, eventListenersGuard_);

        for (const auto& response : container) {
            auto it = eventListenersRequests_.find(response.requestId);

            if (it != eventListenersRequests_.end()) {
                KAA_UNLOCK(lock);
                if (response.result == SyncResponseResultType::SUCCESS) {
                    std::vector<std::string> listeners;
                    if (!response.listeners.is_null()) {
                        const auto& result = response.listeners.get_array();
                        listeners.assign(result.begin(), result.end());
                    }

                    it->second->listener_->onEventListenersReceived(listeners);
                } else {
                    it->second->listener_->onRequestFailed();
                }

                KAA_LOCK(lock);
                // Removing by request id, because the iterator could become outdated
                // after the user's callback processing.
                eventListenersRequests_.erase(response.requestId);
            } else {
                KAA_LOG_WARN(
                        boost::format("Failed to find requester for event listeners (request id = %1%)") % response
                                .requestId);
            }
        }
    }
}

std::int32_t EventManager::findEventListeners(const std::list<std::string>& eventFQNs, IFetchEventListeners* listener)
{
    if (eventFQNs.empty() || !listener) {
        KAA_LOG_WARN("Failed to add event listeners request: bad input data");
        throw KaaException("Bad event listeners data");
    }

    std::int32_t requestId = UuidGenerator::generateRandomInt();

    std::shared_ptr<EventListenersInfo> info(new EventListenersInfo);
    info->eventFQNs_ = eventFQNs;
    info->listener_ = listener;

    KAA_MUTEX_UNIQUE_DECLARE(lock, eventListenersGuard_);
    eventListenersRequests_.insert(std::make_pair(requestId, info));
    KAA_UNLOCK(lock);

    KAA_LOG_TRACE("Added event listeners resolving request");

    if (eventTransport_) {
        eventTransport_->sync();
    } else {
        KAA_LOG_WARN("Event listener resolve request postponed: transport was not set");
    }

    return requestId;
}

void EventManager::setTransport(EventTransport *transport)
{
    eventTransport_ = transport;
    if (eventTransport_) {
        bool needSync = false;
        KAA_MUTEX_UNIQUE_DECLARE(eventsLock, pendingEventsGuard_);
        needSync = !pendingEvents_.empty();
        KAA_UNLOCK(eventsLock);
        if (!needSync) {
            KAA_MUTEX_UNIQUE_DECLARE(listenersLock, eventListenersGuard_);
            needSync = !eventListenersRequests_.empty();
        }
        if (needSync) {
            eventTransport_->sync();
        }
    }
}

void EventManager::commit(TransactionIdPtr trxId)
{
    auto it = transactions_.find(trxId);
    if (it != transactions_.end()) {
        KAA_MUTEX_UNIQUE_DECLARE(lock, pendingEventsGuard_);
        std::list<Event> & events = it->second;
        for (Event &e : events) {
            e.seqNum = eventSequenceNumber_++;
            pendingEvents_.push_back(e);
            status_->setEventSequenceNumber(e.seqNum);
        }
        transactions_.erase(it);
        KAA_UNLOCK(lock);
        if (eventTransport_) {
            eventTransport_->sync();
        }
    }
}

} /* namespace kaa */

#endif

