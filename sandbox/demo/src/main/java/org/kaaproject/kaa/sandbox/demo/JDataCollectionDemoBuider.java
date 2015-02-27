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
package org.kaaproject.kaa.sandbox.demo;


import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.admin.SdkPlatform;
import org.kaaproject.kaa.common.dto.logs.LogSchemaDto;
import org.kaaproject.kaa.common.dto.user.UserVerifierDto;
import org.kaaproject.kaa.sandbox.demo.projects.Platform;
import org.kaaproject.kaa.sandbox.demo.projects.Project;
import org.kaaproject.kaa.server.common.admin.AdminClient;
import org.kaaproject.kaa.server.common.core.algorithms.generation.DefaultRecordGenerationAlgorithm;
import org.kaaproject.kaa.server.common.core.algorithms.generation.DefaultRecordGenerationAlgorithmImpl;
import org.kaaproject.kaa.server.common.core.configuration.RawData;
import org.kaaproject.kaa.server.common.core.configuration.RawDataFactory;
import org.kaaproject.kaa.server.common.core.schema.RawSchema;
import org.kaaproject.kaa.server.verifiers.trustful.config.TrustfulVerifierConfig;

public class JDataCollectionDemoBuider extends AbstractDemoBuilder{
    @Override
    protected void buildDemoApplicationImpl(AdminClient client) throws Exception {
        ApplicationDto notificationApplication = new ApplicationDto();
        notificationApplication.setName("Java data collection");
        notificationApplication = client.editApplication(notificationApplication);

        sdkKey.setApplicationId(notificationApplication.getId());
        sdkKey.setProfileSchemaVersion(1);
        sdkKey.setConfigurationSchemaVersion(1);
        sdkKey.setNotificationSchemaVersion(1);
        sdkKey.setTargetPlatform(SdkPlatform.JAVA);


        LogSchemaDto logSchemaDto = new LogSchemaDto();
        logSchemaDto.setApplicationId(notificationApplication.getId());
        logSchemaDto.setName("Log schema");
        logSchemaDto.setDescription("Log schema describing incoming logs");
        logSchemaDto = client.createLogSchema(logSchemaDto, "demo/jdatacollection/logSchema.json");
        sdkKey.setLogSchemaVersion(logSchemaDto.getMajorVersion());


        loginTenantDeveloper(client);

        TrustfulVerifierConfig trustfulVerifierConfig = new TrustfulVerifierConfig();
        UserVerifierDto trustfulUserVerifier = new UserVerifierDto();
        trustfulUserVerifier.setApplicationId(notificationApplication.getId());
        trustfulUserVerifier.setName("Trustful verifier");
        trustfulUserVerifier.setPluginClassName(trustfulVerifierConfig.getPluginClassName());
        trustfulUserVerifier.setPluginTypeName(trustfulVerifierConfig.getPluginTypeName());
        RawSchema rawSchema = new RawSchema(trustfulVerifierConfig.getPluginConfigSchema().toString());
        DefaultRecordGenerationAlgorithm<RawData> algotithm =
                new DefaultRecordGenerationAlgorithmImpl<>(rawSchema, new RawDataFactory());
        RawData rawData = algotithm.getRootData();
        trustfulUserVerifier.setJsonConfiguration(rawData.getRawData());
        trustfulUserVerifier = client.editUserVerifierDto(trustfulUserVerifier);
        sdkKey.setDefaultVerifierToken(trustfulUserVerifier.getVerifierToken());
    }

    @Override
    protected void setupProjectConfigs() {
        Project projectConfig = new Project();
        projectConfig.setId("jdatacollection_demo");
        projectConfig.setName("Java Data Collection Demo");
        projectConfig.setDescription("Application on java platform demonstrating data collection subsystem (IoT)");
        projectConfig.setPlatform(Platform.JAVA);
        projectConfig.setSourceArchive("java/jdatacollection_demo.tar.gz");
        projectConfig.setProjectFolder("jdatacollection_demo/JDataCollectionDemo");
        projectConfig.setSdkLibDir("jdatacollection_demo/JDataCollectionDemo/lib");
        projectConfig.setDestBinaryFile("jdatacollection_demo/JDataCollectionDemo/bin/DataCollectionDemo.jar");
        projectConfigs.add(projectConfig);
    }
}
