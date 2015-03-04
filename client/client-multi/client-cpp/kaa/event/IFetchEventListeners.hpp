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

#ifndef IFETCHEVENTLISTENERS_HPP_
#define IFETCHEVENTLISTENERS_HPP_

#include "kaa/KaaDefaults.hpp"

#ifdef KAA_USE_EVENTS

#include <vector>
#include <string>

namespace kaa {

/**
 * Listener interface for retrieving endpoints list
 * which supports requested event class FQNs
 *
 * @see EventListenersResolver
 *
 */
class IFetchEventListeners {
public:

    /**
     * Called when resolve was successful
     *
     * @param eventListeners    List of endpoints
     */
    virtual void onEventListenersReceived(const std::vector<std::string>& eventListeners) = 0;

    /**
     * Called when some error occured during resolving endpoints
     * via event class FQNs.
     */
    virtual void onRequestFailed() = 0;

    virtual ~IFetchEventListeners()
    {
    }
};

} /* namespace kaa */

#endif

#endif /* IFETCHEVENTLISTENERS_HPP_ */
