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

#ifndef ICONFIGURATIONDELTAFACTORY_HPP_
#define ICONFIGURATIONDELTAFACTORY_HPP_

#include "kaa/KaaDefaults.hpp"

#ifdef KAA_USE_CONFIGURATION

#include <avro/Generic.hh>

#include "kaa/configuration/delta/IConfigurationDelta.hpp"

namespace kaa {

/**
 * Interface for the delta factory
 */
class IConfigurationDeltaFactory {
public:
    /**
     * Creates configuration delta from the given Avro Generic delta
     * @param genericDelta avro generic delta object
     * @return new configuration delta (\ref IConfigurationDelta)
     */
    virtual ConfigurationDeltaPtr createDelta(const avro::GenericDatum& genericDelta) = 0;

    virtual ~IConfigurationDeltaFactory()
    {
    }
};

} /* namespace kaa */

#endif

#endif /* ICONFIGURATIONDELTAFACTORY_HPP_ */
