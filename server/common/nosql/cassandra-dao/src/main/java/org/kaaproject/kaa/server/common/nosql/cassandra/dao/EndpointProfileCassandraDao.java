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

package org.kaaproject.kaa.server.common.nosql.cassandra.dao;

import org.apache.commons.codec.binary.Base64;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.QueryBuilder;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.kaaproject.kaa.server.common.dao.impl.DaoUtil.getDto;

import org.kaaproject.kaa.common.dto.EndpointProfileDto;
import org.kaaproject.kaa.common.dto.EndpointProfilesPageDto;
import org.kaaproject.kaa.common.dto.PageLinkDto;
import org.kaaproject.kaa.server.common.dao.impl.EndpointProfileDao;
import org.kaaproject.kaa.server.common.nosql.cassandra.dao.filter.CassandraEPByAccessTokenDao;
import org.kaaproject.kaa.server.common.nosql.cassandra.dao.filter.CassandraEPByAppIdDao;
import org.kaaproject.kaa.server.common.nosql.cassandra.dao.filter.CassandraEPByEndpointGroupIdDao;
import org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraEPByAccessToken;
import org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraEPByAppId;
import org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraEPByEndpointGroupId;
import org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraEndpointProfile;
import org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraEndpointUser;
import org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.type.CassandraEndpointGroupState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.base.Predicates;
import com.google.common.collect.Sets;

import static com.datastax.driver.core.querybuilder.QueryBuilder.delete;
import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static com.datastax.driver.core.querybuilder.QueryBuilder.in;
import static com.datastax.driver.core.querybuilder.QueryBuilder.select;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.CassandraDaoUtil.convertKeyHashToString;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.CassandraDaoUtil.convertStringToKeyHash;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.CassandraDaoUtil.getByteBuffer;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.CassandraDaoUtil.getBytes;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.EP_BY_APP_ID_APPLICATION_ID_PROPERTY;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.EP_BY_APP_ID_COLUMN_FAMILY_NAME;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.EP_COLUMN_FAMILY_NAME;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.EP_EP_KEY_HASH_PROPERTY;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.EP_BY_ENDPOINT_GROUP_ID_COLUMN_FAMILY_NAME;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.EP_BY_ENDPOINT_GROUP_ID_ENDPOINT_GROUP_ID_PROPERTY;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.EP_BY_ENDPOINT_GROUP_ID_ENDPOINT_KEY_HASH_PROPERTY;

@Repository(value = "endpointProfileDao")
public class EndpointProfileCassandraDao extends AbstractCassandraDao<CassandraEndpointProfile, ByteBuffer> implements EndpointProfileDao<CassandraEndpointProfile> {

    private static final Logger LOG = LoggerFactory.getLogger(EndpointProfileCassandraDao.class);

    @Autowired
    private CassandraEPByAppIdDao cassandraEPByAppIdDao;
    @Autowired
    private CassandraEPByAccessTokenDao cassandraEPByAccessTokenDao;
    @Autowired
    private CassandraEPByEndpointGroupIdDao cassandraEPByEndpointGroupIdDao;

    private EndpointUserCassandraDao endpointUserDao;

    @Override
    protected Class<CassandraEndpointProfile> getColumnFamilyClass() {
        return CassandraEndpointProfile.class;
    }

    @Override
    protected String getColumnFamilyName() {
        return EP_COLUMN_FAMILY_NAME;
    }

    @Override
    public CassandraEndpointProfile save(EndpointProfileDto dto) {
        if (isBlank(dto.getId())) {
            return save(new CassandraEndpointProfile(dto));
        } else {
            return update(new CassandraEndpointProfile(dto));
        }
    }

    @Override
    public CassandraEndpointProfile save(CassandraEndpointProfile profile) {
        if (profile.getId() == null) {
            profile.setId(convertKeyHashToString(profile.getEndpointKeyHash()));
        }
        LOG.debug("Saving endpoint profile with id {}", profile.getId());
        ByteBuffer epKeyHash = profile.getEndpointKeyHash();
        List<Statement> statementList = new ArrayList<>();
        statementList.add(cassandraEPByAppIdDao.getSaveQuery(new CassandraEPByAppId(profile.getApplicationId(), epKeyHash)));
        String accessToken = profile.getAccessToken();
        if (accessToken != null) {
            statementList.add(cassandraEPByAccessTokenDao.getSaveQuery(new CassandraEPByAccessToken(accessToken, epKeyHash)));
        }
        statementList.add(getSaveQuery(profile));
        List<CassandraEndpointGroupState> cfGroupState = new ArrayList<>();
        cfGroupState = profile.getCfGroupState();
        if (cfGroupState != null) {
            for (CassandraEndpointGroupState cf : cfGroupState) {
                statementList.add(cassandraEPByEndpointGroupIdDao.getSaveQuery(
                        new CassandraEPByEndpointGroupId(cf.getEndpointGroupId(), epKeyHash)));
            }
        }
        Statement[] st = new Statement[statementList.size()];
        statementList.toArray(st);
        executeBatch(st);
        LOG.debug("[{}] Endpoint profile saved", profile.getId());
        return profile;
    }

    private CassandraEndpointProfile update(CassandraEndpointProfile profile) {
        LOG.debug("Updating endpoint profile with id {}", profile.getId());
        ByteBuffer epKeyHash = profile.getEndpointKeyHash();
        CassandraEndpointProfile storedProfile = findByKeyHash(getBytes(epKeyHash));
        List<CassandraEndpointGroupState> oldCfGroupState = new ArrayList<>();
        List<CassandraEndpointGroupState> newCfGroupState = new ArrayList<>();
        Set<String> oldEndpointGroupIds = new HashSet<String>();
        Set<String> newEndpointGroupIds = new HashSet<String>();
        List<Statement> statementList = new ArrayList<>();
        statementList.add(getSaveQuery(profile));
        oldCfGroupState = storedProfile.getCfGroupState();
        for (CassandraEndpointGroupState cf : oldCfGroupState) {
            oldEndpointGroupIds.add(cf.getEndpointGroupId());
        }
        newCfGroupState = profile.getCfGroupState();
        for (CassandraEndpointGroupState cf : newCfGroupState) {
            newEndpointGroupIds.add(cf.getEndpointGroupId());
        }
        Set<String> removeEndpointGroupIds = Sets.filter(oldEndpointGroupIds, Predicates.not(Predicates.in(newEndpointGroupIds)));
        Set<String> addEndpointGroupIds = Sets.filter(newEndpointGroupIds, Predicates.not(Predicates.in(oldEndpointGroupIds)));
        if (addEndpointGroupIds != null) {
            for (String id : addEndpointGroupIds) {
                statementList.add(cassandraEPByEndpointGroupIdDao.getSaveQuery(
                        new CassandraEPByEndpointGroupId(id, epKeyHash)));
            }
        }
        if (removeEndpointGroupIds != null) {
            for (String id : removeEndpointGroupIds) {
                statementList.add(delete().from(EP_BY_ENDPOINT_GROUP_ID_COLUMN_FAMILY_NAME)
                        .where(eq(EP_BY_ENDPOINT_GROUP_ID_ENDPOINT_GROUP_ID_PROPERTY, id))
                        .and(eq(EP_BY_ENDPOINT_GROUP_ID_ENDPOINT_KEY_HASH_PROPERTY, epKeyHash)));
            }
        }
        Statement[] st = new Statement[statementList.size()];
        statementList.toArray(st);
        executeBatch(st);
        LOG.debug("[{}] Endpoint profile updated", profile.getId());
        return profile;
    }

    @Override
    public CassandraEndpointProfile findByKeyHash(byte[] endpointKeyHash) {
        LOG.debug("Try to find endpoint profile by key hash [{}]", endpointKeyHash);
        CassandraEndpointProfile endpointProfile = (CassandraEndpointProfile) getMapper().get(getByteBuffer(endpointKeyHash));
        LOG.debug("[{}] Found endpoint profile {}", endpointKeyHash, endpointProfile);
        return endpointProfile;
    }

    @Override
    public long getCountByKeyHash(byte[] endpointKeyHash) {
        LOG.debug("Try to check if endpoint profile exists with key hash [{}]", endpointKeyHash);
        long count = 0;
        ResultSet resultSet = execute(select().countAll().from(getColumnFamilyName())
                .where(eq(EP_EP_KEY_HASH_PROPERTY, getByteBuffer(endpointKeyHash))));
        Row row = resultSet.one();
        if (row != null) {
            count = row.getLong(0);
        }
        LOG.debug("{} endpoint profile exists with key hash [{}]", count);
        return count;
    }

    @Override
    public void removeByKeyHash(byte[] endpointKeyHash) {
        LOG.debug("Remove endpoint profile by key hash [{}]", endpointKeyHash);
        getMapper().delete(getByteBuffer(endpointKeyHash));
    }

    @Override
    public void removeByAppId(String appId) {
        LOG.debug("Remove endpoint profile by application id [{}]", appId);
        Statement deleteEps = delete().from(getColumnFamilyName()).where(in(EP_EP_KEY_HASH_PROPERTY, cassandraEPByAppIdDao.getEPIdsListByAppId(appId)));
        Statement deleteEpsByAppId = delete().from(EP_BY_APP_ID_COLUMN_FAMILY_NAME).where(eq(EP_BY_APP_ID_APPLICATION_ID_PROPERTY, appId));
        executeBatch(deleteEps, deleteEpsByAppId);
        LOG.trace("Execute statements {}, {} like batch", deleteEps, deleteEpsByAppId);
    }

    @Override
    public CassandraEndpointProfile findByAccessToken(String endpointAccessToken) {
        LOG.debug("Try to find endpoint profile by access token id [{}]", endpointAccessToken);
        CassandraEndpointProfile endpointProfile = null;
        ByteBuffer epKeyHash = cassandraEPByAccessTokenDao.findEPIdByAccessToken(endpointAccessToken);
        if (epKeyHash != null) {
            endpointProfile = (CassandraEndpointProfile) getMapper().get(epKeyHash);
        }
        LOG.trace("Found endpoint profile {} by access token [{}]", endpointProfile, endpointAccessToken);
        return endpointProfile;
    }

    private List<EndpointProfileDto> findEndpointProfilesList(ByteBuffer[] keyHashList, String endpointGroupId) {
        List<EndpointProfileDto> cassandraEndpointProfileList = new ArrayList<>();
        CassandraEndpointProfile profile = null;
        LOG.debug("Found {} endpoint profiles by group id {}", keyHashList != null ? keyHashList.length : 0, endpointGroupId);
        for (ByteBuffer keyHash : keyHashList) {
            profile = findByKeyHash(getBytes(keyHash));
            if (profile != null) {
                cassandraEndpointProfileList.add(getDto(profile));
            } else {
                LOG.debug("Can't find endpoint profile by id {}", keyHash);
            }
        }
        return cassandraEndpointProfileList;
    }

    private EndpointProfilesPageDto createNextPage(List<EndpointProfileDto> cassandraEndpointProfileList, String endpointGroupId, String limit) {
        EndpointProfilesPageDto endpointProfilesPageDto = new EndpointProfilesPageDto();
        PageLinkDto pageLinkDto = new PageLinkDto();
        String next;
        int lim = Integer.valueOf(limit);
        if (cassandraEndpointProfileList.size() == (lim + 1)) {
            pageLinkDto.setEndpointGroupId(endpointGroupId);
            pageLinkDto.setLimit(limit);
            pageLinkDto.setOffset(Base64.encodeBase64String(cassandraEndpointProfileList.get(lim).getEndpointKeyHash()));
            cassandraEndpointProfileList.remove(lim);
            next = null;
        } else {
            next = "It is the last page";
        }
        pageLinkDto.setNext(next);
        endpointProfilesPageDto.setPageLinkDto(pageLinkDto);
        endpointProfilesPageDto.setEndpointProfiles(cassandraEndpointProfileList);
        return endpointProfilesPageDto;
    }

    @Override
    public EndpointProfilesPageDto findByEndpointGroupId(PageLinkDto pageLink) {
        LOG.debug("Try to find endpoint profile by endpoint group id [{}]", pageLink.getEndpointGroupId());
        EndpointProfilesPageDto endpointProfilesPageDto;
        List<EndpointProfileDto> cassandraEndpointProfileList;
        ByteBuffer[] keyHashList;
        if ("0".equals(pageLink.getOffset())) {
            keyHashList = cassandraEPByEndpointGroupIdDao.findFirstPageEPByEndpointGroupId(pageLink.getEndpointGroupId(), pageLink.getLimit());
            cassandraEndpointProfileList = findEndpointProfilesList(keyHashList, pageLink.getEndpointGroupId());
        } else {
            keyHashList = cassandraEPByEndpointGroupIdDao.findEPByEndpointGroupId(pageLink.getEndpointGroupId(),
                    pageLink.getLimit(), Base64.decodeBase64(pageLink.getOffset()));
            cassandraEndpointProfileList = findEndpointProfilesList(keyHashList, pageLink.getEndpointGroupId());
        }
        endpointProfilesPageDto = createNextPage(cassandraEndpointProfileList, pageLink.getEndpointGroupId(), pageLink.getLimit());
        return endpointProfilesPageDto;
    }

    @Override
    public List<CassandraEndpointProfile> findByEndpointUserId(String endpointUserId) {
        LOG.debug("Try to find endpoint profiles by endpoint user id [{}]", endpointUserId);
        List<CassandraEndpointProfile> profileList = Collections.emptyList();
        CassandraEndpointUser endpointUser = endpointUserDao.findById(endpointUserId);
        if (endpointUser != null) {
            List<String> ids = endpointUser.getEndpointIds();
            if (ids != null && !ids.isEmpty()) {
                Statement select = select().from(getColumnFamilyName()).where(QueryBuilder.in(EP_EP_KEY_HASH_PROPERTY, convertStringIds(ids)));
                LOG.trace("Execute statements {}", select);
                profileList = findListByStatement(select);
            }
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace("Found endpoint profiles {}", Arrays.toString(profileList.toArray()));
        }
        return profileList;
    }

    @Override
    public CassandraEndpointProfile findById(ByteBuffer key) {
        LOG.debug("Try to find endpoint profiles by key [{}]", key);
        CassandraEndpointProfile profile = null;
        if (key != null) {
            profile = findByKeyHash(key.array());
        }
        LOG.trace("Found endpoint profiles {}", profile);
        return profile;
    }

    @Override
    public void removeById(ByteBuffer key) {
        LOG.debug("Remove endpoint profiles by key [{}]", key);
        if (key != null) {
            removeByKeyHash(key.array());
        }
    }

    private ByteBuffer[] convertStringIds(List<String> ids) {
        ByteBuffer[] keyHashArray = new ByteBuffer[ids.size()];
        for (int i = 0; i < ids.size(); i++) {
            keyHashArray[i] = convertStringToKeyHash(ids.get(i));
        }
        return keyHashArray;
    }

    public EndpointUserCassandraDao getEndpointUserDao() {
        return endpointUserDao;
    }

    public void setEndpointUserDao(EndpointUserCassandraDao endpointUserDao) {
        this.endpointUserDao = endpointUserDao;
    }
}
