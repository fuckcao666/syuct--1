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

#include <boost/test/unit_test.hpp>

#include "kaa/ClientStatus.hpp"
#include "kaa/KaaDefaults.hpp"
#include "kaa/KaaClientContext.hpp"
#include "kaa/KaaClientProperties.hpp"
#include "kaa/logging/DefaultLogger.hpp"

#include "headers/context/MockExecutorContext.hpp"
#include "headers/MockKaaClientStateStorage.hpp"

#include <map>
#include <cstdlib>
#include <fstream>
#include <sstream>
#include <utility>
#include <memory>
#include <string>

#ifdef RESOURCE_DIR
const char * const directory = RESOURCE_DIR;
#else
const char *const directory ="./";
#endif
const char * const filename = "kaa_status.file";


void cleanfile() {
  std::remove(std::string(std::string(directory) + std::string("/") + std::string(filename)).c_str());
}

namespace kaa {

static MockExecutorContext context;
static KaaClientProperties properties;
static DefaultLogger tmp_logger(properties.getClientId());

BOOST_AUTO_TEST_SUITE(ClientStatusSuite);

BOOST_AUTO_TEST_CASE(checkDefaults)
{
    cleanfile();
    IKaaClientStateStoragePtr stateMock(new MockKaaClientStateStorage);
    properties.setStateFileName(filename);
    properties.setWorkingDirectoryPath(directory);
    KaaClientContext clientContext(properties, tmp_logger, context, stateMock);

    ClientStatus cs(clientContext);
    BOOST_CHECK_EQUAL(cs.getAppSeqNumber().configurationSequenceNumber, 0);
    BOOST_CHECK_EQUAL(cs.getAppSeqNumber().notificationSequenceNumber, 0);
    BOOST_CHECK_EQUAL(cs.isRegistered(), false);
    BOOST_CHECK_EQUAL(cs.getProfileHash().empty(), true);
    BOOST_CHECK_EQUAL(cs.getAttachedEndpoints().size(), 0);
    BOOST_CHECK_EQUAL(cs.getEndpointAccessToken().empty(), false);
    BOOST_CHECK_EQUAL(cs.getEndpointAttachStatus(), false);

    cleanfile();
}

BOOST_AUTO_TEST_CASE(checkSetAndSaveParameters)
{
    IKaaClientStateStoragePtr stateMock(new MockKaaClientStateStorage);
    properties.setStateFileName(filename);
    properties.setWorkingDirectoryPath(directory);
    KaaClientContext clientContext(properties, tmp_logger, context, stateMock);
    ClientStatus cs(clientContext);
    cs.setAppSeqNumber({1,2,3});
    BOOST_CHECK_EQUAL(cs.getAppSeqNumber().configurationSequenceNumber, 1);
    BOOST_CHECK_EQUAL(cs.getAppSeqNumber().notificationSequenceNumber, 2);
    cs.setRegistered(true);
    BOOST_CHECK_EQUAL(cs.isRegistered(), true);

    HashDigest sdb;
    for (uint8_t i = 0; i < 5; ++i) {
        sdb.push_back(i);
    }
    cs.setProfileHash(sdb);
    auto checkHash = cs.getProfileHash();
    BOOST_CHECK_EQUAL_COLLECTIONS(checkHash.begin(), checkHash.end(), sdb.begin(), sdb.end());

    DetailedTopicStates empty_ts = cs.getTopicStates();
    BOOST_CHECK_EQUAL(empty_ts.size(), 0);

    DetailedTopicStates ts;
    DetailedTopicState ts1;
    ts1.topicId = "topic1";
    ts1.sequenceNumber = 100;
    ts1.topicName = "topicName1";
    ts1.subscriptionType = SubscriptionType::MANDATORY_SUBSCRIPTION;

    DetailedTopicState ts2;
    ts2.topicId = "topic2";
    ts2.sequenceNumber = 200;
    ts2.topicName = "topicName2";
    ts2.subscriptionType = SubscriptionType::OPTIONAL_SUBSCRIPTION;

    ts.insert(std::make_pair(ts1.topicId, ts1));
    ts.insert(std::make_pair(ts2.topicId, ts2));

    cs.setTopicStates(ts);
    DetailedTopicStates act_ts = cs.getTopicStates();
    BOOST_CHECK_EQUAL(act_ts.size(), 2);

    BOOST_CHECK_EQUAL(act_ts[ts1.topicId].topicId, ts1.topicId);
    BOOST_CHECK_EQUAL(act_ts[ts1.topicId].sequenceNumber, ts1.sequenceNumber);
    BOOST_CHECK_EQUAL(act_ts[ts1.topicId].topicName, ts1.topicName);
    BOOST_CHECK_EQUAL(act_ts[ts1.topicId].subscriptionType, ts1.subscriptionType);

    BOOST_CHECK_EQUAL(act_ts[ts2.topicId].topicId, ts2.topicId);
    BOOST_CHECK_EQUAL(act_ts[ts2.topicId].sequenceNumber, ts2.sequenceNumber);
    BOOST_CHECK_EQUAL(act_ts[ts2.topicId].topicName, ts2.topicName);
    BOOST_CHECK_EQUAL(act_ts[ts2.topicId].subscriptionType, ts2.subscriptionType);

    AttachedEndpoints attachedEndpoints;
    std::string token1("Token1"), hash1("hash1");
    std::string token2("Token2"), hash2("hash2");

    attachedEndpoints.insert(std::make_pair(token1, hash1));
    attachedEndpoints.insert(std::make_pair(token2, hash2));

    cs.setAttachedEndpoints(attachedEndpoints);

    bool isAttached = true;
    cs.setEndpointAttachStatus(isAttached);

    std::string endpointKeyHash = "thisEndpointKeyHash";
    cs.setEndpointKeyHash(endpointKeyHash);

    cs.save();
    ClientStatus cs_restored(clientContext);

    DetailedTopicStates act_ts1 = cs_restored.getTopicStates();
    BOOST_CHECK_EQUAL(act_ts1.size(), 2);

    BOOST_CHECK_EQUAL(act_ts1[ts1.topicId].topicId, ts1.topicId);
    BOOST_CHECK_EQUAL(act_ts1[ts1.topicId].sequenceNumber, ts1.sequenceNumber);
    BOOST_CHECK_EQUAL(act_ts1[ts1.topicId].topicName, ts1.topicName);
    BOOST_CHECK_EQUAL(act_ts1[ts1.topicId].subscriptionType, ts1.subscriptionType);

    BOOST_CHECK_EQUAL(act_ts1[ts2.topicId].topicId, ts2.topicId);
    BOOST_CHECK_EQUAL(act_ts1[ts2.topicId].sequenceNumber, ts2.sequenceNumber);
    BOOST_CHECK_EQUAL(act_ts1[ts2.topicId].topicName, ts2.topicName);
    BOOST_CHECK_EQUAL(act_ts1[ts2.topicId].subscriptionType, ts2.subscriptionType);

    AttachedEndpoints restoredAttachedEndpoints = cs.getAttachedEndpoints();
    BOOST_CHECK_EQUAL(restoredAttachedEndpoints[token1], hash1);
    BOOST_CHECK_EQUAL(restoredAttachedEndpoints[token2], hash2);

    BOOST_CHECK_EQUAL(cs_restored.getEndpointAttachStatus(), isAttached);
    BOOST_CHECK_EQUAL(cs_restored.getEndpointKeyHash(), endpointKeyHash);

    cleanfile();
}

}  // namespace kaa

BOOST_AUTO_TEST_SUITE_END()

