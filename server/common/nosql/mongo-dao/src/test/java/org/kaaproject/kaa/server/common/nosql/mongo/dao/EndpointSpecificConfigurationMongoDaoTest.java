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

package org.kaaproject.kaa.server.common.nosql.mongo.dao;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kaaproject.kaa.common.dto.EndpointSpecificConfigurationDto;
import org.kaaproject.kaa.server.common.dao.exception.KaaOptimisticLockingFailureException;
import org.kaaproject.kaa.server.common.dao.model.EndpointSpecificConfiguration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/mongo-dao-test-context.xml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class EndpointSpecificConfigurationMongoDaoTest extends AbstractMongoTest {
    private static final String KEY = "key";
    private static final String KEY_2 = "key2";
    private static final String BODY = "body";
    private EndpointSpecificConfigurationDto saved1;
    private EndpointSpecificConfigurationDto saved2;
    private EndpointSpecificConfigurationDto saved3;

    @BeforeClass
    public static void init() throws Exception {
        MongoDBTestRunner.setUp();
    }

    @AfterClass
    public static void after() throws Exception {
        MongoDBTestRunner.tearDown();
    }

    @Test
    public void testRemoveByEndpointKeyHash() throws Exception {
        List<EndpointSpecificConfiguration> found = endpointSpecificConfigurationDao.find();
        Assert.assertTrue(found.size() == 3);
        endpointSpecificConfigurationDao.removeByEndpointKeyHash(KEY);
        found = endpointSpecificConfigurationDao.find();
        Assert.assertTrue(found.size() == 1);
    }

    @Test
    public void testFindByEndpointKeyHashAndConfigurationVersion() throws Exception {
        List<EndpointSpecificConfiguration> found = endpointSpecificConfigurationDao.find();
        Assert.assertTrue(found.size() == 3);
        EndpointSpecificConfigurationDto found1 = endpointSpecificConfigurationDao.findByEndpointKeyHashAndConfigurationVersion(KEY, 0).toDto();
        EndpointSpecificConfigurationDto found2 = endpointSpecificConfigurationDao.findByEndpointKeyHashAndConfigurationVersion(KEY, 1).toDto();
        EndpointSpecificConfigurationDto found3 = endpointSpecificConfigurationDao.findByEndpointKeyHashAndConfigurationVersion(KEY_2, 0).toDto();
        EndpointSpecificConfiguration found4 = endpointSpecificConfigurationDao.findByEndpointKeyHashAndConfigurationVersion(KEY_2, 4);
        Assert.assertEquals(saved1, found1);
        Assert.assertEquals(saved2, found2);
        Assert.assertEquals(saved3, found3);
        Assert.assertNull(found4);
    }

    @Test(expected = KaaOptimisticLockingFailureException.class)
    public void testLocking() throws Exception {
        saved1 = generateEndpointSpecificConfigurationDto(KEY, 1, BODY, 8L);
        saved2 = generateEndpointSpecificConfigurationDto(KEY, 1, BODY, 8L);
    }

    @Before
    public void setUp() throws Exception {
        saved1 = generateEndpointSpecificConfigurationDto(KEY, 0, BODY, null);
        saved2 = generateEndpointSpecificConfigurationDto(KEY, 1, BODY, null);
        saved3 = generateEndpointSpecificConfigurationDto(KEY_2, 0, BODY, null);
    }

    @After
    public void tearDown() throws Exception {
        endpointSpecificConfigurationDao.removeAll();
    }
}