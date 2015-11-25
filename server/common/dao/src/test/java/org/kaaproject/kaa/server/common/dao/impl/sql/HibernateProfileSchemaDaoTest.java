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

package org.kaaproject.kaa.server.common.dao.impl.sql;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kaaproject.kaa.server.common.dao.model.sql.Application;
import org.kaaproject.kaa.server.common.dao.model.sql.ProfileSchema;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/common-dao-test-context.xml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Transactional
public class HibernateProfileSchemaDaoTest extends HibernateAbstractTest {

    @Test
    public void findByApplicationId() {
        List<ProfileSchema> schemas = generateProfSchema(null, 3);
        Assert.assertEquals(3, schemas.size());
        ProfileSchema schema  = schemas.get(0);
        Application app = schema.getApplication();
        List<ProfileSchema> found = profileSchemaDao.findByApplicationId(app.getId().toString());
        Assert.assertEquals(3, found.size());
        Assert.assertEquals(schemas, found);
    }

    @Test
    public void findByAppIdAndVersion() {
        List<ProfileSchema> schemas = generateProfSchema(null, 4);
        Assert.assertEquals(4, schemas.size());
        ProfileSchema schema = schemas.get(2);
        Application app = schema.getApplication();
        ProfileSchema found = profileSchemaDao.findByAppIdAndVersion(app.getId().toString(), schema.getMajorVersion());
        Assert.assertNotNull(found);
        Assert.assertEquals(3, found.getMajorVersion());
    }

    @Test
    public void findLatestByAppId() {
        List<ProfileSchema> schemas = generateProfSchema(null, 4);
        Assert.assertEquals(4, schemas.size());
        ProfileSchema schema  = schemas.get(0);
        Application app = schema.getApplication();
        ProfileSchema found = profileSchemaDao.findLatestByAppId(app.getId().toString());
        Assert.assertNotNull(found);
        Assert.assertEquals(4, found.getMajorVersion());
    }

    @Test
    public void findVacantSchemas() {
        //TODO : implement me
    }
}
