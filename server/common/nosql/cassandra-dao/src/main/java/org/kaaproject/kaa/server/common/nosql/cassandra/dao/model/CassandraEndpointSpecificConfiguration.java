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

package org.kaaproject.kaa.server.common.nosql.cassandra.dao.model;

import com.datastax.driver.mapping.annotations.ClusteringColumn;
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import com.datastax.driver.mapping.annotations.Transient;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.kaaproject.kaa.common.dto.EndpointSpecificConfigurationDto;
import org.kaaproject.kaa.server.common.dao.model.EndpointSpecificConfiguration;

import java.io.Serializable;

import static org.kaaproject.kaa.server.common.dao.DaoConstants.OPT_LOCK;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.EPS_CONFIGURATION_CONFIGURATION_BODY_PROPERTY;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.EPS_CONFIGURATION_CONFIGURATION_VERSION_PROPERTY;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.EPS_CONFIGURATION_KEY_HASH_PROPERTY;

@Table(name = CassandraModelConstants.EPS_CONFIGURATION_COLUMN_FAMILY_NAME)
public final class CassandraEndpointSpecificConfiguration implements EndpointSpecificConfiguration, Serializable {
    @Transient
    private static final long serialVersionUID = -8639669282952330290L;

    @PartitionKey
    @Column(name = EPS_CONFIGURATION_KEY_HASH_PROPERTY)
    private String endpointKeyHash;
    @ClusteringColumn
    @Column(name = EPS_CONFIGURATION_CONFIGURATION_VERSION_PROPERTY)
    private Integer configurationVersion;
    @Column(name = EPS_CONFIGURATION_CONFIGURATION_BODY_PROPERTY)
    private String configuration;
    @Column(name = OPT_LOCK)
    private Long version;

    public CassandraEndpointSpecificConfiguration() {
    }

    public CassandraEndpointSpecificConfiguration(EndpointSpecificConfigurationDto dto) {
        this.endpointKeyHash = dto.getEndpointKeyHash();
        this.configurationVersion = dto.getConfigurationVersion();
        this.configuration = dto.getConfiguration();
        this.version = dto.getVersion();
    }

    @Override
    public EndpointSpecificConfigurationDto toDto() {
        EndpointSpecificConfigurationDto dto = new EndpointSpecificConfigurationDto();
        dto.setEndpointKeyHash(this.getEndpointKeyHash());
        dto.setConfiguration(this.getConfiguration());
        dto.setConfigurationVersion(this.getConfigurationVersion());
        dto.setVersion(this.getVersion());
        return dto;
    }

    public String getEndpointKeyHash() {
        return endpointKeyHash;
    }

    public void setEndpointKeyHash(String endpointKeyHash) {
        this.endpointKeyHash = endpointKeyHash;
    }

    public Integer getConfigurationVersion() {
        return configurationVersion;
    }

    public void setConfigurationVersion(Integer configurationVersion) {
        this.configurationVersion = configurationVersion;
    }

    public String getConfiguration() {
        return configuration;
    }

    public void setConfiguration(String configuration) {
        this.configuration = configuration;
    }

    @Override
    public Long getVersion() {
        return version;
    }

    @Override
    public void setVersion(Long version) {
        this.version = version;
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public boolean equals(Object other) {
        return EqualsBuilder.reflectionEquals(this, other);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
