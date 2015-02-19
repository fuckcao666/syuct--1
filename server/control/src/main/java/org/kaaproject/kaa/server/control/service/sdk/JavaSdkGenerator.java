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

package org.kaaproject.kaa.server.control.service.sdk;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.tools.JavaFileObject.Kind;

import org.apache.avro.Schema;
import org.apache.avro.compiler.specific.SpecificCompiler;
import org.apache.avro.compiler.specific.SpecificCompiler.FieldVisibility;
import org.apache.avro.generic.GenericData.StringType;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.kaaproject.kaa.server.common.Version;
import org.kaaproject.kaa.server.common.thrift.gen.control.Sdk;
import org.kaaproject.kaa.server.common.thrift.gen.control.SdkPlatform;
import org.kaaproject.kaa.server.common.zk.ServerNameUtil;
import org.kaaproject.kaa.server.common.zk.gen.BootstrapNodeInfo;
import org.kaaproject.kaa.server.common.zk.gen.TransportMetaData;
import org.kaaproject.kaa.server.common.zk.gen.VersionConnectionInfoPair;
import org.kaaproject.kaa.server.control.service.sdk.compiler.JavaDynamicBean;
import org.kaaproject.kaa.server.control.service.sdk.compiler.JavaDynamicCompiler;
import org.kaaproject.kaa.server.control.service.sdk.compress.ZipEntryData;
import org.kaaproject.kaa.server.control.service.sdk.event.EventFamilyMetadata;
import org.kaaproject.kaa.server.control.service.sdk.event.JavaEventClassesGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;

/**
 * The Class JavaSdkGenerator.
 */
public class JavaSdkGenerator extends SdkGenerator {

    private static final String SEPARATOR = ":";

    /** The Constant logger. */
    private static final Logger LOG = LoggerFactory.getLogger(JavaSdkGenerator.class);

    /** The Constant JAVA_SDK_DIR. */
    private static final String JAVA_SDK_DIR = "sdk/java";

    /** The Constant JAVA_SDK_PREFIX. */
    private static final String JAVA_SDK_PREFIX = "kaa-client-sdk-";

    /** The Constant JAVA_SDK_NAME_PATTERN. */
    private static final String JAVA_SDK_NAME_PATTERN = JAVA_SDK_PREFIX + "p{}-c{}-n{}-l{}.jar";

    /** The Constant ANDROID_SDK_DIR. */
    private static final String ANDROID_SDK_DIR = "sdk/android";

    /** The Constant ANDROID_SDK_PREFIX. */
    private static final String ANDROID_SDK_PREFIX = "kaa-client-sdk-android-";

    /** The Constant ANDROID_SDK_NAME_PATTERN. */
    private static final String ANDROID_SDK_NAME_PATTERN = ANDROID_SDK_PREFIX + "p{}-c{}-n{}-l{}.jar";

    /** The Constant CLIENT_PROPERTIES. */
    private static final String CLIENT_PROPERTIES = "client.properties";

    /** The Constant BUILD_VERSION. */
    private static final String BUILD_VERSION = "build.version";

    /** The Constant BUILD_COMMIT_HASH. */
    private static final String BUILD_COMMIT_HASH = "build.commit_hash";

    /** The Constant BOOTSTRAP_SERVERS_PROPERTY. */
    private static final String BOOTSTRAP_SERVERS_PROPERTY = "transport.bootstrap.servers";

    /** The Constant APP_TOKEN_PROPERTY. */
    private static final String APP_TOKEN_PROPERTY = "application_token";

    /** The Constant CONFIG_VERSION_PROPERTY. */
    private static final String CONFIG_VERSION_PROPERTY = "config_version";

    /** The Constant PROFILE_VERSION_PROPERTY. */
    private static final String PROFILE_VERSION_PROPERTY = "profile_version";

    /** The Constant NOTIFICATION_VERSION_PROPERTY. */
    private static final String NOTIFICATION_VERSION_PROPERTY = "user_nt_version";

    /** The Constant LOGS_VERSION_PROPERTY. */
    private static final String LOGS_VERSION_PROPERTY = "logs_version";

    /** The Constant CONFIG_DATA_DEFAULT_PROPERTY. */
    private static final String CONFIG_DATA_DEFAULT_PROPERTY = "config.data.default";

    /** The Constant CONFIG_SCHEMA_DEFAULT_PROPERTY. */
    private static final String CONFIG_SCHEMA_DEFAULT_PROPERTY = "config.schema.default";

    /** The Constant EVENT_CLASS_FAMILY_VERSION_PROPERTY. */
    private static final String EVENT_CLASS_FAMILY_VERSION_PROPERTY = "event_cf_version";

    /** The Constant KAA_CLIENT_SOURCE_TEMPLATE. */
    private static final String KAA_CLIENT_SOURCE_TEMPLATE = "sdk/java/KaaClient.java.template";

    /** The Constant BASE_KAA_CLIENT_SOURCE_TEMPLATE. */
    private static final String BASE_KAA_CLIENT_SOURCE_TEMPLATE = "sdk/java/BaseKaaClient.java.template";

    /** The Constant CONFIGURATION_MANAGER_SOURCE_TEMPLATE. */
    private static final String CONFIGURATION_MANAGER_SOURCE_TEMPLATE = "sdk/java/cf/ConfigurationManager.java.template";
    
    /** The Constant CONFIGURATION_MANAGER_IMPL_SOURCE_TEMPLATE. */
    private static final String CONFIGURATION_MANAGER_IMPL_SOURCE_TEMPLATE = "sdk/java/cf/ResyncConfigurationManager.java.template";

    /** The Constant CONFIGURATION_LISTENER_SOURCE_TEMPLATE. */
    private static final String CONFIGURATION_LISTENER_SOURCE_TEMPLATE = "sdk/java/cf/ConfigurationListener.java.template";

    /** The Constant CONFIGURATION_DESERIALIZER_SOURCE_TEMPLATE. */
    private static final String CONFIGURATION_DESERIALIZER_SOURCE_TEMPLATE = "sdk/java/cf/ConfigurationDeserializer.java.template";

    /** The Constant NOTIFICATION_LISTENER_SOURCE_TEMPLATE. */
    private static final String NOTIFICATION_LISTENER_SOURCE_TEMPLATE = "sdk/java/nf/NotificationListener.java.template";

    /** The Constant NOTIFICATION_DESERIALIZER_SOURCE_TEMPLATE. */
    private static final String NOTIFICATION_DESERIALIZER_SOURCE_TEMPLATE = "sdk/java/nf/NotificationDeserializer.java.template";

    /** The Constant PROFILE_CONTAINER_SOURCE_TEMPLATE. */
    private static final String PROFILE_CONTAINER_SOURCE_TEMPLATE = "sdk/java/profile/ProfileContainer.java.template";

    /** The Constant PROFILE_SERIALIZER_SOURCE_TEMPLATE. */
    private static final String PROFILE_SERIALIZER_SOURCE_TEMPLATE = "sdk/java/profile/ProfileSerializer.java.template";

    /** The Constant DEFAULT_PROFILE_SERIALIZER_SOURCE_TEMPLATE. */
    private static final String DEFAULT_PROFILE_SERIALIZER_SOURCE_TEMPLATE = "sdk/java/profile/DefaultProfileSerializer.java.template";

    /** The Constant LOG_RECORD_SOURCE_TEMPLATE. */
    private static final String LOG_RECORD_SOURCE_TEMPLATE = "sdk/java/log/LogRecord.java.template";

    /** The Constant LOG_COLLECTOR_INTERFACE_TEMPLATE. */
    private static final String LOG_COLLECTOR_INTERFACE_TEMPLATE = "sdk/java/log/LogCollector.java.template";

    /** The Constant LOG_COLLECTOR_SOURCE_TEMPLATE. */
    private static final String LOG_COLLECTOR_SOURCE_TEMPLATE = "sdk/java/log/DefaultLogCollector.java.template";

    /** The Constant USER_VERIFIER_CONSTANTS_SOURCE_TEMPLATE. */
    private static final String USER_VERIFIER_CONSTANTS_SOURCE_TEMPLATE = "sdk/java/event/UserVerifierConstants.java.template";

    /** The Constant ABSTRACT_PROFILE_CONTAINER. */
    private static final String PROFILE_CONTAINER = "ProfileContainer";

    /** The Constant ABSTRACT_PROFILE_CONTAINER. */
    private static final String PROFILE_SERIALIZER = "ProfileSerializer";

    /** The Constant CONFIGURATION_MANAGER. */
    private static final String CONFIGURATION_MANAGER = "ConfigurationManager";
    
    /** The Constant CONFIGURATION_MANAGER. */
    private static final String CONFIGURATION_MANAGER_IMPL = "ResyncConfigurationManager";

    /** The Constant CONFIGURATION_LISTENER. */
    private static final String CONFIGURATION_LISTENER = "ConfigurationListener";

    /** The Constant CONFIGURATION_DESERIALIZER. */
    private static final String CONFIGURATION_DESERIALIZER = "ConfigurationDeserializer";

    /** The Constant NOTIFICATION_LISTENER. */
    private static final String NOTIFICATION_LISTENER = "NotificationListener";

    /** The Constant NOTIFICATION_DESERIALIZER. */
    private static final String NOTIFICATION_DESERIALIZER = "NotificationDeserializer";

    /** The Constant USER_VERIFIER_CONSTANTS. */
    private static final String USER_VERIFIER_CONSTANTS = "UserVerifierConstants";

    /** The Constant DEFAULT_SCHEMA_VERSION. */
    private static final int DEFAULT_SCHEMA_VERSION = 1;

    /** The Constant KAA_CLIENT. */
    private static final String KAA_CLIENT = "KaaClient";

    /** The Constant BASE_KAA_CLIENT. */
    private static final String BASE_KAA_CLIENT = "BaseKaaClient";

    /** The Constant LOG_RECORD. */
    private static final String LOG_RECORD = "LogRecord";

    /** The Constant LOG_COLLECTOR_INTERFACE. */
    private static final String LOG_COLLECTOR_INTERFACE = "LogCollector";

    /** The Constant DEFAULT_LOG_COLLECTOR. */
    private static final String LOG_COLLECTOR_SOURCE = "DefaultLogCollector";

    /** The Constant PROFILE_CLASS_PACKAGE_VAR. */
    private static final String PROFILE_CLASS_PACKAGE_VAR = "\\$\\{profile_class_package\\}";

    /** The Constant PROFILE_CLASS_VAR. */
    private static final String PROFILE_CLASS_VAR = "\\$\\{profile_class\\}";

    /** The Constant CONFIGURATION_CLASS_PACKAGE_VAR. */
    private static final String CONFIGURATION_CLASS_PACKAGE_VAR = "\\$\\{configuration_class_package\\}";

    /** The Constant CONFIGURATION_CLASS_VAR. */
    private static final String CONFIGURATION_CLASS_VAR = "\\$\\{configuration_class\\}";

    /** The Constant NOTIFICATION_CLASS_PACKAGE_VAR. */
    private static final String NOTIFICATION_CLASS_PACKAGE_VAR = "\\$\\{notification_class_package\\}";

    /** The Constant NOTIFICATION_CLASS_VAR. */
    private static final String NOTIFICATION_CLASS_VAR = "\\$\\{notification_class\\}";

    /** The Constant LOG_RECORD_CLASS_PACKAGE_VAR. */
    private static final String LOG_RECORD_CLASS_PACKAGE_VAR = "\\$\\{log_record_class_package\\}";

    /** The Constant LOG_RECORD_CLASS_VAR. */
    private static final String LOG_RECORD_CLASS_VAR = "\\$\\{log_record_class\\}";

    /** The Constant DEFAULT_USER_VERIFIER_TOKEN_VAR. */
    private static final String DEFAULT_USER_VERIFIER_TOKEN_VAR = "\\$\\{default_user_verifier_token\\}";

    /** The Constant random. */
    private static final SecureRandom RANDOM = new SecureRandom();

    private final SdkPlatform sdkPlatform;

    public JavaSdkGenerator(SdkPlatform sdkPlatform) {
        this.sdkPlatform = sdkPlatform;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.control.service.sdk.SdkGenerator#generateSdk
     * (java.lang.String, java.util.List, java.lang.String, int, int, int,
     * java.lang.String, java.lang.String, java.lang.String, byte[],
     * java.util.List)
     */
    @Override
    public Sdk generateSdk(String buildVersion, List<BootstrapNodeInfo> bootstrapNodes, String appToken, int profileSchemaVersion,
            int configurationSchemaVersion, int notificationSchemaVersion, int logSchemaVersion, String profileSchemaBody,
            String notificationSchemaBody, String configurationProtocolSchemaBody, String configurationSchemaBody,
            byte[] defaultConfigurationData, List<EventFamilyMetadata> eventFamilies, String logSchemaBody, String defaultVerifierToken)
            throws Exception {

        String sdkTemplateLocation;
        if (sdkPlatform == SdkPlatform.JAVA) {
            sdkTemplateLocation = System.getProperty("server_home_dir") + "/" + JAVA_SDK_DIR + "/" + JAVA_SDK_PREFIX + buildVersion
                    + ".jar";
            LOG.debug("Lookup Java SDK template: {}", sdkTemplateLocation);
        } else { // ANDROID
            sdkTemplateLocation = System.getProperty("server_home_dir") + "/" + ANDROID_SDK_DIR + "/" + ANDROID_SDK_PREFIX + buildVersion
                    + ".jar";
            LOG.debug("Lookup Android SDK template: {}", sdkTemplateLocation);
        }

        File sdkTemplateFile = new File(sdkTemplateLocation);
        ZipFile templateArhive = new ZipFile(sdkTemplateFile);

        Map<String, ZipEntryData> replacementData = new HashMap<String, ZipEntryData>();

        ZipEntry clientPropertiesEntry = templateArhive.getEntry(CLIENT_PROPERTIES);
        byte[] clientPropertiesData = generateClientProperties(templateArhive.getInputStream(clientPropertiesEntry), bootstrapNodes,
                appToken, configurationSchemaVersion, profileSchemaVersion, notificationSchemaVersion, logSchemaVersion,
                configurationProtocolSchemaBody, defaultConfigurationData, eventFamilies);

        replacementData.put(CLIENT_PROPERTIES, new ZipEntryData(new ZipEntry(CLIENT_PROPERTIES), clientPropertiesData));

        List<JavaDynamicBean> javaSources = new ArrayList<JavaDynamicBean>();

        Schema configurationSchema = new Schema.Parser().parse(configurationSchemaBody);
        String configurationClassName = configurationSchema.getName();
        String configurationClassPackage = configurationSchema.getNamespace();

        javaSources.addAll(generateSchemaSources(configurationSchema));

        String configurationManagerImplTemplate = readResource(CONFIGURATION_MANAGER_IMPL_SOURCE_TEMPLATE);
        String configurationManagerImplSource = configurationManagerImplTemplate.replaceAll(CONFIGURATION_CLASS_PACKAGE_VAR,
                configurationClassPackage).replaceAll(CONFIGURATION_CLASS_VAR, configurationClassName);
        
        JavaDynamicBean configurationManagerImplClassBean = new JavaDynamicBean(CONFIGURATION_MANAGER_IMPL, configurationManagerImplSource);
        javaSources.add(configurationManagerImplClassBean);
        
        String configurationManagerTemplate = readResource(CONFIGURATION_MANAGER_SOURCE_TEMPLATE);
        String configurationManagerSource = configurationManagerTemplate.replaceAll(CONFIGURATION_CLASS_PACKAGE_VAR,
                configurationClassPackage).replaceAll(CONFIGURATION_CLASS_VAR, configurationClassName);
        
        JavaDynamicBean configurationManagerClassBean = new JavaDynamicBean(CONFIGURATION_MANAGER, configurationManagerSource);
        javaSources.add(configurationManagerClassBean);

        String configurationListenerTemplate = readResource(CONFIGURATION_LISTENER_SOURCE_TEMPLATE);
        String configurationListenerSource = configurationListenerTemplate.replaceAll(CONFIGURATION_CLASS_PACKAGE_VAR,
                configurationClassPackage).replaceAll(CONFIGURATION_CLASS_VAR, configurationClassName);

        JavaDynamicBean configurationListenerClassBean = new JavaDynamicBean(CONFIGURATION_LISTENER, configurationListenerSource);
        javaSources.add(configurationListenerClassBean);


        String configurationDeserializerSourceTemplate = readResource(CONFIGURATION_DESERIALIZER_SOURCE_TEMPLATE);
        String configurationDeserializerSource = configurationDeserializerSourceTemplate.replaceAll(CONFIGURATION_CLASS_PACKAGE_VAR,
                configurationClassPackage).replaceAll(CONFIGURATION_CLASS_VAR, configurationClassName);

        JavaDynamicBean configurationDeserializerClassBean = new JavaDynamicBean(CONFIGURATION_DESERIALIZER,
                configurationDeserializerSource);
        javaSources.add(configurationDeserializerClassBean);

        Schema profileSchema = new Schema.Parser().parse(profileSchemaBody);
        String profileClassName = profileSchema.getName();
        String profileClassPackage = profileSchema.getNamespace();

        if (profileSchemaVersion != DEFAULT_SCHEMA_VERSION) {
            javaSources.addAll(generateSchemaSources(profileSchema));
        }

        String profileContainerTemplate = readResource(PROFILE_CONTAINER_SOURCE_TEMPLATE);
        String profileContainerSource = profileContainerTemplate.replaceAll(PROFILE_CLASS_PACKAGE_VAR, profileClassPackage).replaceAll(
                PROFILE_CLASS_VAR, profileClassName);
        JavaDynamicBean profileContainerClassBean = new JavaDynamicBean(PROFILE_CONTAINER, profileContainerSource);
        javaSources.add(profileContainerClassBean);

        String profileSerializerTemplate;
        if (profileSchemaVersion == DEFAULT_SCHEMA_VERSION) {
            profileSerializerTemplate = readResource(DEFAULT_PROFILE_SERIALIZER_SOURCE_TEMPLATE);
        } else {
            profileSerializerTemplate = readResource(PROFILE_SERIALIZER_SOURCE_TEMPLATE);
        }
        String profileSerializerSource = profileSerializerTemplate.replaceAll(PROFILE_CLASS_PACKAGE_VAR, profileClassPackage).replaceAll(
                PROFILE_CLASS_VAR, profileClassName);
        JavaDynamicBean profileSerializerClassBean = new JavaDynamicBean(PROFILE_SERIALIZER, profileSerializerSource);
        javaSources.add(profileSerializerClassBean);

        Schema notificationSchema = new Schema.Parser().parse(notificationSchemaBody);
        String notificationClassName = notificationSchema.getName();
        String notificationClassPackage = notificationSchema.getNamespace();

        if (notificationSchemaVersion != DEFAULT_SCHEMA_VERSION) {
            javaSources.addAll(generateSchemaSources(notificationSchema));
        }

        String notificationListenerTemplate = readResource(NOTIFICATION_LISTENER_SOURCE_TEMPLATE);
        String notificationListenerSource = notificationListenerTemplate.replaceAll(NOTIFICATION_CLASS_PACKAGE_VAR,
                notificationClassPackage).replaceAll(NOTIFICATION_CLASS_VAR, notificationClassName);

        JavaDynamicBean notificationListenerClassBean = new JavaDynamicBean(NOTIFICATION_LISTENER, notificationListenerSource);
        javaSources.add(notificationListenerClassBean);

        String notificationDeserializerSourceTemplate = readResource(NOTIFICATION_DESERIALIZER_SOURCE_TEMPLATE);
        String notificationDeserializerSource = notificationDeserializerSourceTemplate.replaceAll(NOTIFICATION_CLASS_PACKAGE_VAR,
                notificationClassPackage).replaceAll(NOTIFICATION_CLASS_VAR, notificationClassName);

        JavaDynamicBean notificationDeserializerClassBean = new JavaDynamicBean(NOTIFICATION_DESERIALIZER, notificationDeserializerSource);
        javaSources.add(notificationDeserializerClassBean);

        Schema logSchema = new Schema.Parser().parse(logSchemaBody);
        if (logSchemaVersion != DEFAULT_SCHEMA_VERSION) {
            javaSources.addAll(generateSchemaSources(logSchema));
        }

        String logRecordTemplate = readResource(LOG_RECORD_SOURCE_TEMPLATE);
        String logRecordSource = logRecordTemplate.replaceAll(LOG_RECORD_CLASS_PACKAGE_VAR, logSchema.getNamespace()).replaceAll(
                LOG_RECORD_CLASS_VAR, logSchema.getName());

        String logCollectorInterfaceTemplate = readResource(LOG_COLLECTOR_INTERFACE_TEMPLATE);
        String logCollectorInterface = logCollectorInterfaceTemplate.replaceAll(LOG_RECORD_CLASS_PACKAGE_VAR, logSchema.getNamespace())
                .replaceAll(LOG_RECORD_CLASS_VAR, logSchema.getName());

        String logCollectorSourceTemplate = readResource(LOG_COLLECTOR_SOURCE_TEMPLATE);
        String logCollectorSource = logCollectorSourceTemplate.replaceAll(LOG_RECORD_CLASS_PACKAGE_VAR, logSchema.getNamespace())
                .replaceAll(LOG_RECORD_CLASS_VAR, logSchema.getName());

        JavaDynamicBean logRecordClassBean = new JavaDynamicBean(LOG_RECORD, logRecordSource);
        JavaDynamicBean logCollectorInterfaceClassBean = new JavaDynamicBean(LOG_COLLECTOR_INTERFACE, logCollectorInterface);
        JavaDynamicBean logCollectorSourceClassBean = new JavaDynamicBean(LOG_COLLECTOR_SOURCE, logCollectorSource);

        javaSources.add(logRecordClassBean);
        javaSources.add(logCollectorInterfaceClassBean);
        javaSources.add(logCollectorSourceClassBean);

        if (eventFamilies != null && !eventFamilies.isEmpty()) {
            for (EventFamilyMetadata eventFamily : eventFamilies) {
                Schema eventFamilySchema = new Schema.Parser().parse(eventFamily.getEcfSchema());
                javaSources.addAll(generateSchemaSources(eventFamilySchema));
            }
            javaSources.addAll(JavaEventClassesGenerator.generateEventClasses(eventFamilies));
        }

        String userVerifierConstantsTemplate = readResource(USER_VERIFIER_CONSTANTS_SOURCE_TEMPLATE);
        if (defaultVerifierToken == null) {
            defaultVerifierToken = "null";
        } else {
            defaultVerifierToken = "\"" + defaultVerifierToken + "\"";
        }
        String userVerifierConstantsSource = userVerifierConstantsTemplate
                .replaceAll(DEFAULT_USER_VERIFIER_TOKEN_VAR, defaultVerifierToken);

        JavaDynamicBean userVerifierConstantsClassBean = new JavaDynamicBean(USER_VERIFIER_CONSTANTS, userVerifierConstantsSource);
        javaSources.add(userVerifierConstantsClassBean);

        String kaaClientTemplate = readResource(KAA_CLIENT_SOURCE_TEMPLATE);
        String kaaClientSource = kaaClientTemplate.replaceAll(LOG_RECORD_CLASS_PACKAGE_VAR, logSchema.getNamespace())
                .replaceAll(LOG_RECORD_CLASS_VAR, logSchema.getName())
                .replaceAll(CONFIGURATION_CLASS_PACKAGE_VAR, configurationClassPackage)
                .replaceAll(CONFIGURATION_CLASS_VAR, configurationClassName);
        JavaDynamicBean kaaClientClassBean = new JavaDynamicBean(KAA_CLIENT, kaaClientSource);
        javaSources.add(kaaClientClassBean);

        String baseKaaClientTemplate = readResource(BASE_KAA_CLIENT_SOURCE_TEMPLATE);
        String baseKaaClientSource = baseKaaClientTemplate.replaceAll(LOG_RECORD_CLASS_PACKAGE_VAR, logSchema.getNamespace())
                .replaceAll(LOG_RECORD_CLASS_VAR, logSchema.getName())
                .replaceAll(CONFIGURATION_CLASS_PACKAGE_VAR, configurationClassPackage)
                .replaceAll(CONFIGURATION_CLASS_VAR, configurationClassName);
        JavaDynamicBean baseKaaClientClassBean = new JavaDynamicBean(BASE_KAA_CLIENT, baseKaaClientSource);
        javaSources.add(baseKaaClientClassBean);

        packageSources(javaSources, replacementData);

        ByteArrayOutputStream sdkOutput = new ByteArrayOutputStream();
        ZipOutputStream sdkFile = new ZipOutputStream(sdkOutput);

        Enumeration<? extends ZipEntry> entries = templateArhive.entries();

        while (entries.hasMoreElements()) {
            ZipEntry e = entries.nextElement();
            if (replacementData.containsKey(e.getName())) {
                ZipEntryData replacementEntry = replacementData.remove(e.getName());
                sdkFile.putNextEntry(replacementEntry.getEntry());
                sdkFile.write(replacementEntry.getData());
            } else {
                sdkFile.putNextEntry(e);
                if (!e.isDirectory()) {
                    IOUtils.copy(templateArhive.getInputStream(e), sdkFile);
                }
            }
            sdkFile.closeEntry();
        }
        templateArhive.close();

        for (String entryName : replacementData.keySet()) {
            ZipEntryData replacementEntry = replacementData.get(entryName);
            sdkFile.putNextEntry(replacementEntry.getEntry());
            sdkFile.write(replacementEntry.getData());
            sdkFile.closeEntry();
        }

        sdkFile.close();

        String sdkFileName = MessageFormatter.arrayFormat(
                sdkPlatform == SdkPlatform.JAVA ? JAVA_SDK_NAME_PATTERN : ANDROID_SDK_NAME_PATTERN,
                new Object[] { profileSchemaVersion, configurationSchemaVersion, notificationSchemaVersion, logSchemaVersion })
                .getMessage();

        byte[] sdkData = sdkOutput.toByteArray();

        Sdk sdk = new Sdk();
        sdk.setFileName(sdkFileName);
        sdk.setData(sdkData);
        return sdk;
    }

    /**
     * Generate schema class.
     *
     * @param schema
     *            the schema
     * @return the list
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public static List<JavaDynamicBean> generateSchemaSources(Schema schema) throws IOException {
        SpecificCompiler compiler = new SpecificCompiler(schema);
        compiler.setStringType(StringType.String);
        compiler.setFieldVisibility(FieldVisibility.PRIVATE);

        File tmpdir = new File(System.getProperty("java.io.tmpdir"));
        long n = RANDOM.nextLong();
        if (n == Long.MIN_VALUE) {
            // corner case
            n = 0;
        } else {
            n = Math.abs(n);
        }
        File tmpOutputDir = new File(tmpdir, "tmp-gen-" + Long.toString(n));
        tmpOutputDir.mkdirs();

        compiler.compileToDestination(null, tmpOutputDir);

        List<JavaDynamicBean> sources = getJavaSources(tmpOutputDir);

        tmpOutputDir.delete();

        return sources;
    }

    /**
     * Package sources.
     *
     * @param javaSources
     *            the java sources
     * @param data
     *            the data
     */
    private void packageSources(List<JavaDynamicBean> javaSources, Map<String, ZipEntryData> data) {
        JavaDynamicCompiler dynamicCompiler = new JavaDynamicCompiler();
        dynamicCompiler.init();
        for (JavaDynamicBean bean : javaSources) {
            LOG.debug("Compiling bean {} with source: {}", bean.getName(), bean.getCharContent(true));
        }
        Collection<JavaDynamicBean> compiledObjects = dynamicCompiler.compile(javaSources);
        for (JavaDynamicBean compiledObject : compiledObjects) {
            String className = compiledObject.getName();
            String classFileName = className.replace('.', '/') + Kind.CLASS.extension;
            ZipEntry classFile = new ZipEntry(classFileName);
            ZipEntryData zipEntryData = new ZipEntryData(classFile, compiledObject.getBytes());
            data.put(classFileName, zipEntryData);
        }
    }

    /**
     * Gets the java sources.
     *
     * @param srcDir
     *            the src dir
     * @return the java sources
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private static List<JavaDynamicBean> getJavaSources(File srcDir) throws IOException {
        List<JavaDynamicBean> result = new ArrayList<JavaDynamicBean>();
        File[] files = srcDir.listFiles();
        for (File f : files) {
            if (f.isDirectory()) {
                result.addAll(getJavaSources(f));
            } else if (f.getName().endsWith(Kind.SOURCE.extension)) {
                int index = f.getName().indexOf('.');
                String className = f.getName().substring(0, index);
                String sourceCode = readFile(f);
                JavaDynamicBean sourceObject = new JavaDynamicBean(className, sourceCode);
                result.add(sourceObject);
            }
        }
        return result;
    }

    /**
     * Generate client properties.
     *
     * @param clientPropertiesStream
     *            the client properties stream
     * @param bootstrapNodes
     *            the bootstrap nodes
     * @param appToken
     *            the app token
     * @param configurationSchemaVersion
     *            the configuration schema version
     * @param profileSchemaVersion
     *            the profile schema version
     * @param notificationSchemaVersion
     *            the notification schema version
     * @param logSchemaVersion
     *            the log schema version
     * @param configurationProtocolSchemaBody
     *            the configuration protocol schema body
     * @param defaultConfigurationData
     *            the default configuration data
     * @param eventFamilies
     *            the event families meta information
     * @return the byte[]
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private byte[] generateClientProperties(InputStream clientPropertiesStream, List<BootstrapNodeInfo> bootstrapNodes, String appToken,
            int configurationSchemaVersion, int profileSchemaVersion, int notificationSchemaVersion, int logSchemaVersion,
            String configurationProtocolSchemaBody, byte[] defaultConfigurationData, List<EventFamilyMetadata> eventFamilies)
            throws IOException {

        Properties clientProperties = new Properties();
        clientProperties.load(clientPropertiesStream);

        String bootstrapServers = "";

        LOG.debug("[sdk generateClientProperties] bootstrapNodes.size(): {}", bootstrapNodes.size());
        for (int nodeIndex = 0; nodeIndex < bootstrapNodes.size(); ++nodeIndex) {
            BootstrapNodeInfo node = bootstrapNodes.get(nodeIndex);
            List<TransportMetaData> supportedChannels = node.getTransports();

            int accessPointId = ServerNameUtil.crc32(node.getConnectionInfo());

            for (int chIndex = 0; chIndex < supportedChannels.size(); ++chIndex) {
                TransportMetaData transport = supportedChannels.get(chIndex);
                for (VersionConnectionInfoPair pair : transport.getConnectionInfo()) {
                    bootstrapServers += accessPointId;
                    bootstrapServers += SEPARATOR;
                    bootstrapServers += transport.getId();
                    bootstrapServers += SEPARATOR;
                    bootstrapServers += pair.getVersion();
                    bootstrapServers += SEPARATOR;
                    bootstrapServers += Base64.encodeBase64String(pair.getConenctionInfo().array());
                    bootstrapServers += ";";
                }
            }
        }

        String ecfs = "";
        if (eventFamilies != null) {
            for (int i = 0; i < eventFamilies.size(); i++) {
                if (i > 0) {
                    ecfs += ";";
                }
                ecfs += eventFamilies.get(i).getEcfName() + SEPARATOR + eventFamilies.get(i).getVersion();
            }
        }

        clientProperties.put(BUILD_VERSION, Version.PROJECT_VERSION);
        clientProperties.put(BUILD_COMMIT_HASH, Version.COMMIT_HASH);
        clientProperties.put(BOOTSTRAP_SERVERS_PROPERTY, bootstrapServers);
        clientProperties.put(APP_TOKEN_PROPERTY, appToken);
        clientProperties.put(CONFIG_VERSION_PROPERTY, "" + configurationSchemaVersion);
        clientProperties.put(PROFILE_VERSION_PROPERTY, "" + profileSchemaVersion);
        clientProperties.put(NOTIFICATION_VERSION_PROPERTY, "" + notificationSchemaVersion);
        clientProperties.put(LOGS_VERSION_PROPERTY, "" + logSchemaVersion);
        clientProperties.put(CONFIG_SCHEMA_DEFAULT_PROPERTY, configurationProtocolSchemaBody);
        clientProperties.put(CONFIG_DATA_DEFAULT_PROPERTY, Base64.encodeBase64String(defaultConfigurationData));
        clientProperties.put(EVENT_CLASS_FAMILY_VERSION_PROPERTY, ecfs);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        clientProperties.store(baos, "");

        return baos.toByteArray();
    }

}
