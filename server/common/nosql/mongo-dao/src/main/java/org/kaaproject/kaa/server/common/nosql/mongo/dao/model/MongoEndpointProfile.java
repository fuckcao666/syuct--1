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

package org.kaaproject.kaa.server.common.nosql.mongo.dao.model;

import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import org.kaaproject.kaa.common.dto.EndpointGroupStateDto;
import org.kaaproject.kaa.common.dto.EndpointProfileDto;
import org.kaaproject.kaa.common.dto.EventClassFamilyVersionStateDto;
import org.kaaproject.kaa.server.common.dao.impl.DaoUtil;
import org.kaaproject.kaa.server.common.dao.model.EndpointProfile;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import static org.kaaproject.kaa.server.common.dao.impl.DaoUtil.getArrayCopy;
import static org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoModelConstants.ENDPOINT_PROFILE;
import static org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoModelConstants.EP_ACCESS_TOKEN;
import static org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoModelConstants.EP_APPLICATION_ID;
import static org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoModelConstants.EP_CF_GROUP_STATE;
import static org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoModelConstants.EP_CF_SEQ_NUM;
import static org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoModelConstants.EP_CHANGED_FLAG;
import static org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoModelConstants.EP_CONFIGURATION_HASH;
import static org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoModelConstants.EP_USER_CONFIGURATION_HASH;
import static org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoModelConstants.EP_CONFIGURATION_VERSION;
import static org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoModelConstants.EP_ECF_VERSION_STATE;
import static org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoModelConstants.EP_ENDPOINT_KEY;
import static org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoModelConstants.EP_ENDPOINT_KEY_HASH;
import static org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoModelConstants.EP_LOG_SCHEMA_VERSION;
import static org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoModelConstants.EP_NF_GROUP_STATE;
import static org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoModelConstants.EP_NF_HASH;
import static org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoModelConstants.EP_NF_SEQ_NUM;
import static org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoModelConstants.EP_NOTIFICATION_VERSION;
import static org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoModelConstants.EP_PROFILE_HASH;
import static org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoModelConstants.EP_PROFILE_SCHEMA_ID;
import static org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoModelConstants.EP_PROFILE_VERSION;
import static org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoModelConstants.EP_SDK_TOKEN;
import static org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoModelConstants.EP_SERVER_HASH;
import static org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoModelConstants.EP_SYSTEM_NF_VERSION;
import static org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoModelConstants.EP_USER_ID;
import static org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoModelConstants.EP_USER_NF_VERSION;

@Document(collection = ENDPOINT_PROFILE)
public final class MongoEndpointProfile implements EndpointProfile, Serializable {

    private static final long serialVersionUID = -3227246639864687299L;

    @Id
    private String id;
    @Field(EP_APPLICATION_ID)
    private String applicationId;
    @Field(EP_ENDPOINT_KEY)
    private byte[] endpointKey;
    @Indexed
    @Field(EP_ENDPOINT_KEY_HASH)
    private byte[] endpointKeyHash;
    @Indexed
    @Field(EP_USER_ID)
    private String endpointUserId;
    @Indexed
    @Field(EP_ACCESS_TOKEN)
    private String accessToken;
    @Field(EP_PROFILE_SCHEMA_ID)
    private String profileSchemaId;
    @Field(EP_CF_GROUP_STATE)
    private List<EndpointGroupState> cfGroupState;
    @Field(EP_NF_GROUP_STATE)
    private List<EndpointGroupState> nfGroupState;
    @Field(EP_CF_SEQ_NUM)
    private int cfSequenceNumber;
    @Field(EP_NF_SEQ_NUM)
    private int nfSequenceNumber;
    @Field(EP_CHANGED_FLAG)
    private Boolean changedFlag;
    private DBObject profile;
    @Field(EP_PROFILE_HASH)
    private byte[] profileHash;
    @Field(EP_PROFILE_VERSION)
    private int profileVersion;
    @Field(EP_CONFIGURATION_HASH)
    private byte[] configurationHash;
    @Field(EP_USER_CONFIGURATION_HASH)
    private byte[] userConfigurationHash;
    @Field(EP_CONFIGURATION_VERSION)
    private int configurationVersion;
    @Field(EP_NOTIFICATION_VERSION)
    private int notificationVersion;
    private List<String> subscriptions;
    @Field(EP_NF_HASH)
    private byte[] ntHash;
    @Field(EP_SYSTEM_NF_VERSION)
    private int systemNfVersion;
    @Field(EP_USER_NF_VERSION)
    private int userNfVersion;
    @Field(EP_LOG_SCHEMA_VERSION)
    private int logSchemaVersion;
    @Field(EP_ECF_VERSION_STATE)
    private List<EventClassFamilyVersionState> ecfVersionStates;
    @Field(EP_SERVER_HASH)
    private String serverHash;
    @Indexed
    @Field(EP_SDK_TOKEN)
    private String sdkToken;


    public MongoEndpointProfile() {
    }

    public MongoEndpointProfile(EndpointProfileDto dto) {
        this.id = dto.getId();
        this.applicationId = dto.getApplicationId();
        this.endpointKey = dto.getEndpointKey();
        this.endpointKeyHash = dto.getEndpointKeyHash();
        this.endpointUserId = dto.getEndpointUserId();
        this.accessToken = dto.getAccessToken();
        this.profileSchemaId = dto.getProfileSchemaId();
        this.cfGroupState = MongoDaoUtil.convertDtoToModelList(dto.getCfGroupStates());
        this.nfGroupState = MongoDaoUtil.convertDtoToModelList(dto.getNfGroupStates());
        this.cfSequenceNumber = dto.getCfSequenceNumber();
        this.nfSequenceNumber = dto.getNfSequenceNumber();
        this.changedFlag = dto.getChangedFlag();
        this.profile = (DBObject) JSON.parse(dto.getProfile());
        this.profileHash = dto.getProfileHash();
        this.profileVersion = dto.getProfileVersion();
        this.configurationHash = dto.getConfigurationHash();
        this.userConfigurationHash = dto.getUserConfigurationHash();
        this.configurationVersion = dto.getConfigurationVersion();
        this.subscriptions = dto.getSubscriptions();
        this.notificationVersion = dto.getNotificationVersion();
        this.ntHash = dto.getNtHash();
        this.systemNfVersion = dto.getSystemNfVersion();
        this.userNfVersion = dto.getUserNfVersion();
        this.logSchemaVersion = dto.getLogSchemaVersion();
        this.ecfVersionStates = MongoDaoUtil.convertECFVersionDtoToModelList(dto.getEcfVersionStates());
        this.serverHash = dto.getServerHash();
        this.sdkToken = dto.getSdkToken();
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public byte[] getEndpointKey() {
        return endpointKey;
    }

    public void setEndpointKey(byte[] endpointKey) {
        this.endpointKey = getArrayCopy(endpointKey);
    }

    public byte[] getEndpointKeyHash() {
        return endpointKeyHash;
    }

    public void setEndpointKeyHash(byte[] endpointKeyHash) {
        this.endpointKeyHash = getArrayCopy(endpointKeyHash);
    }

    public String getEndpointUserId() {
        return endpointUserId;
    }

    public void setEndpointUserId(String endpointUserId) {
        this.endpointUserId = endpointUserId;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getProfileSchemaId() {
        return profileSchemaId;
    }

    public List<EndpointGroupState> getCfGroupState() {
        return cfGroupState;
    }

    public void setCfGroupState(List<EndpointGroupState> cfGroupState) {
        this.cfGroupState = cfGroupState;
    }

    public List<EndpointGroupState> getNfGroupState() {
        return nfGroupState;
    }

    public void setNfGroupState(List<EndpointGroupState> nfGroupState) {
        this.nfGroupState = nfGroupState;
    }

    public int getCfSequenceNumber() {
        return cfSequenceNumber;
    }

    public void setCfSequenceNumber(int cfSequenceNumber) {
        this.cfSequenceNumber = cfSequenceNumber;
    }

    public int getNfSequenceNumber() {
        return nfSequenceNumber;
    }

    public void setNfSequenceNumber(int nfSequenceNumber) {
        this.nfSequenceNumber = nfSequenceNumber;
    }

    public Boolean getChangedFlag() {
        return changedFlag;
    }

    public void setChangedFlag(Boolean changedFlag) {
        this.changedFlag = changedFlag;
    }

    public DBObject getProfile() {
        return profile;
    }

    public void setProfile(DBObject profile) {
        this.profile = profile;
    }

    public byte[] getProfileHash() {
        return profileHash;
    }

    public void setProfileHash(byte[] profileHash) {
        this.profileHash = getArrayCopy(profileHash);
    }

    public int getProfileVersion() {
        return profileVersion;
    }

    public void setProfileVersion(int profileVersion) {
        this.profileVersion = profileVersion;
    }

    public byte[] getConfigurationHash() {
        return configurationHash;
    }

    public void setConfigurationHash(byte[] configurationHash) {
        this.configurationHash = getArrayCopy(configurationHash);
    }

    public byte[] getUserConfigurationHash() {
        return userConfigurationHash;
    }

    public void setUserConfigurationHash(byte[] userConfigurationHash) {
        this.userConfigurationHash = getArrayCopy(userConfigurationHash);
    }

    public int getConfigurationVersion() {
        return configurationVersion;
    }

    public void setConfigurationVersion(int configurationVersion) {
        this.configurationVersion = configurationVersion;
    }

    public int getNotificationVersion() {
        return notificationVersion;
    }

    public void setNotificationVersion(int notificationVersion) {
        this.notificationVersion = notificationVersion;
    }

    @Override
    public List<String> getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(List<String> subscriptions) {
        this.subscriptions = subscriptions;
    }

    public byte[] getNtHash() {
        return ntHash;
    }

    public void setNtHash(byte[] ntHash) {
        this.ntHash = getArrayCopy(ntHash);
    }

    public int getSystemNfVersion() {
        return systemNfVersion;
    }

    public void setSystemNfVersion(int systemNfVersion) {
        this.systemNfVersion = systemNfVersion;
    }

    public int getUserNfVersion() {
        return userNfVersion;
    }

    public void setUserNfVersion(int userNfVersion) {
        this.userNfVersion = userNfVersion;
    }

    public int getLogSchemaVersion() {
        return logSchemaVersion;
    }

    public void setLogSchemaVersion(int logSchemaVersion) {
        this.logSchemaVersion = logSchemaVersion;
    }

    public List<EventClassFamilyVersionState> getEcfVersionStates() {
        return ecfVersionStates;
    }

    public void setEcfVersionStates(List<EventClassFamilyVersionState> ecfVersionStates) {
        this.ecfVersionStates = ecfVersionStates;
    }

    public String getServerHash() {
        return serverHash;
    }

    public void setServerHash(String serverHash) {
        this.serverHash = serverHash;
    }

    public String getSdkToken() {
        return sdkToken;
    }

    public void setSdkToken(String sdkToken) {
        this.sdkToken = sdkToken;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MongoEndpointProfile)) {
            return false;
        }

        MongoEndpointProfile that = (MongoEndpointProfile) o;

        if (configurationVersion != that.configurationVersion) {
            return false;
        }
        if (notificationVersion != that.notificationVersion) {
            return false;
        }
        if (profileVersion != that.profileVersion) {
            return false;
        }
        if (cfSequenceNumber != that.cfSequenceNumber) {
            return false;
        }
        if (nfSequenceNumber != that.nfSequenceNumber) {
            return false;
        }
        if (systemNfVersion != that.systemNfVersion) {
            return false;
        }
        if (userNfVersion != that.userNfVersion) {
            return false;
        }
        if (applicationId != null ? !applicationId.equals(that.applicationId) : that.applicationId != null) {
            return false;
        }
        if (changedFlag != null ? !changedFlag.equals(that.changedFlag) : that.changedFlag != null) {
            return false;
        }
        if (!Arrays.equals(configurationHash, that.configurationHash)) {
            return false;
        }
        if (!Arrays.equals(userConfigurationHash, that.userConfigurationHash)) {
            return false;
        }
        if (cfGroupState != null ? !cfGroupState.equals(that.cfGroupState) : that.cfGroupState != null) {
            return false;
        }
        if (nfGroupState != null ? !nfGroupState.equals(that.nfGroupState) : that.nfGroupState != null) {
            return false;
        }
        if (!Arrays.equals(endpointKey, that.endpointKey)) {
            return false;
        }
        if (!Arrays.equals(endpointKeyHash, that.endpointKeyHash)) {
            return false;
        }
        if (!Arrays.equals(ntHash, that.ntHash)) {
            return false;
        }
        if (profile != null ? !profile.equals(that.profile) : that.profile != null) {
            return false;
        }
        if (!Arrays.equals(profileHash, that.profileHash)) {
            return false;
        }
        if (profileSchemaId != null ? !profileSchemaId.equals(that.profileSchemaId) : that.profileSchemaId != null) {
            return false;
        }
        if (subscriptions != null ? !subscriptions.equals(that.subscriptions) : that.subscriptions != null) {
            return false;
        }
        if (sdkToken != null ? !sdkToken.equals(that.sdkToken) : that.sdkToken != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = applicationId != null ? applicationId.hashCode() : 0;
        result = 31 * result + (endpointKey != null ? Arrays.hashCode(endpointKey) : 0);
        result = 31 * result + (endpointKeyHash != null ? Arrays.hashCode(endpointKeyHash) : 0);
        result = 31 * result + (profileSchemaId != null ? profileSchemaId.hashCode() : 0);
        result = 31 * result + (cfGroupState != null ? cfGroupState.hashCode() : 0);
        result = 31 * result + (nfGroupState != null ? nfGroupState.hashCode() : 0);
        result = 31 * result + cfSequenceNumber;
        result = 31 * result + nfSequenceNumber;
        result = 31 * result + (changedFlag != null ? changedFlag.hashCode() : 0);
        result = 31 * result + (profile != null ? profile.hashCode() : 0);
        result = 31 * result + (profileHash != null ? Arrays.hashCode(profileHash) : 0);
        result = 31 * result + profileVersion;
        result = 31 * result + (configurationHash != null ? Arrays.hashCode(configurationHash) : 0);
        result = 31 * result + (userConfigurationHash != null ? Arrays.hashCode(userConfigurationHash) : 0);
        result = 31 * result + configurationVersion;
        result = 31 * result + notificationVersion;
        result = 31 * result + (subscriptions != null ? subscriptions.hashCode() : 0);
        result = 31 * result + (ntHash != null ? Arrays.hashCode(ntHash) : 0);
        result = 31 * result + systemNfVersion;
        result = 31 * result + userNfVersion;
        result = 31 * result + (sdkToken != null ? sdkToken.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "EndpointProfile{" +
                "id='" + id + '\'' +
                ", applicationId=" + applicationId +
                ", endpointKey=" + Arrays.toString(endpointKey) +
                ", endpointKeyHash=" + Arrays.toString(endpointKeyHash) +
                ", profileSchemaId=" + profileSchemaId +
                ", cfGroupState=" + cfGroupState +
                ", nfGroupState=" + nfGroupState +
                ", cfSequenceNumber=" + cfSequenceNumber +
                ", nfSequenceNumber=" + nfSequenceNumber +
                ", changedFlag=" + changedFlag +
                ", profile=" + profile +
                ", profileHash=" + Arrays.toString(profileHash) +
                ", profileVersion=" + profileVersion +
                ", configurationHash=" + Arrays.toString(configurationHash) +
                ", userConfigurationHash=" + Arrays.toString(userConfigurationHash) +
                ", configurationVersion=" + configurationVersion +
                ", notificationVersion=" + notificationVersion +
                ", subscriptions=" + subscriptions +
                ", ntHash=" + Arrays.toString(ntHash) +
                ", systemNfVersion=" + systemNfVersion +
                ", userNfVersion=" + userNfVersion +
                ", sdkToken=" + sdkToken +
                '}';
    }

    @Override
    public EndpointProfileDto toDto() {
        EndpointProfileDto dto = new EndpointProfileDto();
        dto.setId(id);
        dto.setCfGroupStates(DaoUtil.<EndpointGroupStateDto>convertDtoList(cfGroupState));
        dto.setNfGroupStates(DaoUtil.<EndpointGroupStateDto>convertDtoList(nfGroupState));
        dto.setChangedFlag(changedFlag);
        dto.setCfSequenceNumber(cfSequenceNumber);
        dto.setNfSequenceNumber(nfSequenceNumber);
        dto.setConfigurationHash(configurationHash);
        dto.setUserConfigurationHash(userConfigurationHash);
        dto.setConfigurationVersion(configurationVersion);
        dto.setApplicationId(applicationId);
        dto.setEndpointKey(endpointKey);
        dto.setEndpointKeyHash(endpointKeyHash);
        dto.setEndpointUserId(endpointUserId);
        dto.setAccessToken(accessToken);
        dto.setProfile(profile != null ? profile.toString() : "");
        dto.setProfileHash(profileHash);
        dto.setProfileVersion(profileVersion);
        dto.setProfileSchemaId(profileSchemaId);
        dto.setNotificationVersion(notificationVersion);
        dto.setSubscriptions(subscriptions);
        dto.setNtHash(ntHash);
        dto.setSystemNfVersion(systemNfVersion);
        dto.setUserNfVersion(userNfVersion);
        dto.setLogSchemaVersion(logSchemaVersion);
        dto.setEcfVersionStates(DaoUtil.<EventClassFamilyVersionStateDto>convertDtoList(ecfVersionStates));
        dto.setServerHash(serverHash);
        dto.setSdkToken(sdkToken);
        return dto;
    }
}
