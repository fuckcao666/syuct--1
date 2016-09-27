/*
 * Copyright 2014-2016 CyberVision, Inc.
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

package org.kaaproject.kaa.server.common.dao;

import org.kaaproject.kaa.common.dto.EndpointProfileDto;
import org.kaaproject.kaa.common.dto.EndpointSpecificConfigurationDto;

import java.util.Optional;

/**
 * The interface Endpoint specific configuration service.
 */
public interface EndpointSpecificConfigurationService {

    /**
     * Find endpoint specific configuration by endpoint key hash
     *
     * @param endpointKeyHash
     * @return the endpoint specific configuration
     */
    Optional<EndpointSpecificConfigurationDto> findByEndpointKeyHash(String endpointKeyHash);

    /**
     * Find endpoint specific configuration by endpoint profile
     *
     * @param endpointProfileDto
     * @return the endpoint specific configuration
     */
    Optional<EndpointSpecificConfigurationDto> findByEndpointProfile(EndpointProfileDto endpointProfileDto);

    /**
     * Delete endpoint specific configuration by endpoint key hash
     *
     * @param endpointKeyHash
     * @return deleted endpoint specific configuration
     */
    Optional<EndpointSpecificConfigurationDto> deleteByEndpointKeyHash(String endpointKeyHash);

    /**
     * Save endpoint specific configuration by endpoint key hash
     *
     * @param configurationDto endpoint specific configuration
     * @return saved endpoint specific configuration
     */
    EndpointSpecificConfigurationDto save(EndpointSpecificConfigurationDto configurationDto);

}
