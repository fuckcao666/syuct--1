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

package org.kaaproject.kaa.server.common.nosql.mongo.dao;


import com.mongodb.DBObject;

import org.kaaproject.kaa.common.dto.EndpointProfileDto;
import org.kaaproject.kaa.server.common.dao.impl.EndpointProfileDao;
import org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoEndpointProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.nio.ByteBuffer;
import java.util.List;

import static org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoModelConstants.ENDPOINT_PROFILE;
import static org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoModelConstants.EP_ACCESS_TOKEN;
import static org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoModelConstants.EP_APPLICATION_ID;
import static org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoModelConstants.EP_ENDPOINT_KEY_HASH;
import static org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoModelConstants.EP_SDK_TOKEN;
import static org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoModelConstants.EP_USER_ID;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Repository
public class EndpointProfileMongoDao extends AbstractMongoDao<MongoEndpointProfile, ByteBuffer> implements EndpointProfileDao<MongoEndpointProfile> {

    private static final Logger LOG = LoggerFactory.getLogger(EndpointProfileMongoDao.class);

    @Override
    protected String getCollectionName() {
        return ENDPOINT_PROFILE;
    }

    @Override
    protected Class<MongoEndpointProfile> getDocumentClass() {
        return MongoEndpointProfile.class;
    }

    @Override
    public MongoEndpointProfile findByKeyHash(byte[] endpointKeyHash) {
        LOG.debug("Find endpoint profile by endpoint key hash [{}] ", endpointKeyHash);
        DBObject dbObject = query(where(EP_ENDPOINT_KEY_HASH).is(endpointKeyHash)).getQueryObject();
        DBObject result = mongoTemplate.getDb().getCollection(getCollectionName()).findOne(dbObject);
        return mongoTemplate.getConverter().read(getDocumentClass(), result);
    }

    @Override
    public long getCountByKeyHash(byte[] endpointKeyHash) {
        LOG.debug("Get count of endpoint profiles by endpoint key hash [{}] ", endpointKeyHash);
        DBObject dbObject = query(where(EP_ENDPOINT_KEY_HASH).is(endpointKeyHash)).getQueryObject();
        return mongoTemplate.getDb().getCollection(getCollectionName()).count(dbObject);
    }

    @Override
    public void removeByKeyHash(byte[] endpointKeyHash) {
        LOG.debug("Remove endpoint profile by endpoint key hash [{}] ", endpointKeyHash);
        mongoTemplate.remove(query(where(EP_ENDPOINT_KEY_HASH).is(endpointKeyHash)), getCollectionName());
    }

    @Override
    public void removeByAppId(String appId) {
        LOG.debug("Remove endpoint profile by application id [{}] ", appId);
        remove(query(where(EP_APPLICATION_ID).is(appId)));
    }

    @Override
    public MongoEndpointProfile findByAccessToken(String endpointAccessToken) {
        LOG.debug("Find endpoint profile by access token [{}] ", endpointAccessToken);
        DBObject dbObject = query(where(EP_ACCESS_TOKEN).is(endpointAccessToken)).getQueryObject();
        DBObject result = mongoTemplate.getDb().getCollection(getCollectionName()).findOne(dbObject);
        return mongoTemplate.getConverter().read(getDocumentClass(), result);
    }

    @Override
    public List<MongoEndpointProfile> findByEndpointUserId(String endpointUserId) {
        LOG.debug("Find endpoint profiles by endpoint user id [{}] ", endpointUserId);
        return find(query(where(EP_USER_ID).is(endpointUserId)));
    }

    @Override
    public MongoEndpointProfile findById(ByteBuffer key) {
        MongoEndpointProfile profile = null;
        if (key != null) {
            profile = findByKeyHash(key.array());
        }
        return profile;
    }

    @Override
    public void removeById(ByteBuffer key) {
        if (key != null) {
            removeByKeyHash(key.array());
        }
    }

    @Override
    public MongoEndpointProfile save(EndpointProfileDto dto) {
        return save(new MongoEndpointProfile(dto));
    }

    @Override
    public List<MongoEndpointProfile> findBySdkToken(String sdkToken) {
        LOG.debug("Searching for endpoint profiles by SDK token {} ", sdkToken);
        return find(query(where(EP_SDK_TOKEN).is(sdkToken)));
    }

    @Override
    public boolean checkSdkToken(String sdkToken) {
        LOG.debug("Checking for endpoint profiles with SDK token {}", sdkToken);
        return findOne(query(where(EP_SDK_TOKEN).is(sdkToken))) != null;
    }
}
