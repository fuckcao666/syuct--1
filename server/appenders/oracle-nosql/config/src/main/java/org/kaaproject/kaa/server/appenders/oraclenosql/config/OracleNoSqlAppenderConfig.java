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
package org.kaaproject.kaa.server.appenders.oraclenosql.config;

import java.io.IOException;

import org.kaaproject.kaa.server.common.log.shared.annotation.KaaAppenderConfig;
import org.kaaproject.kaa.server.common.log.shared.config.AppenderConfig;
import org.kaaproject.kaa.server.common.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@KaaAppenderConfig
public class OracleNoSqlAppenderConfig implements AppenderConfig{

    private static final Logger LOG = LoggerFactory.getLogger(OracleNoSqlAppenderConfig.class);
    
    private String defaultConfig;
    
    public OracleNoSqlAppenderConfig() {
        try {
            defaultConfig = FileUtils.readResource("oracle-nosql-appender-default.properties");
        } catch (IOException e) {
            LOG.error("Unable to load default config!", e);
        }
    }
    
    @Override
    public String getName() {
        return "Oracle NoSQL";
    }

    @Override
    public String getLogAppenderClass() {
        return "org.kaaproject.kaa.server.appenders.oraclenosql.appender.OracleNoSqlLogAppender";
    }

    @Override
    public String getDefaultConfig() {
        return defaultConfig;
    }

}
