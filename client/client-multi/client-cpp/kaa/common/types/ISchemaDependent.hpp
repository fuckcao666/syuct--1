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

#ifndef I_SCHEMA_DEPENDENT_HPP_
#define I_SCHEMA_DEPENDENT_HPP_

#include "kaa/KaaDefaults.hpp"

#ifdef KAA_USE_CONFIGURATION

#include <avro/Schema.hh>

namespace kaa {

/**
 * Interface for objects whose serialization depends on schema
 */
class ISchemaDependent {
public:
    /**
     * @return schema object
     * @see NodePtr
     */
    virtual const avro::NodePtr &getSchema() const = 0;

    virtual ~ISchemaDependent()
    {
    }
};

}  // namespace kaa

#endif

#endif /* I_SCHEMA_DEPENDENT_HPP_ */
