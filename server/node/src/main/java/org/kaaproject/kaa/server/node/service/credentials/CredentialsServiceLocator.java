/**
 *  Copyright 2014-2016 CyberVision, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.kaaproject.kaa.server.node.service.credentials;

import java.util.List;

/**
 * Allows each application to have a credentials service of its own.
 *
 * @author Andrew Shvayka
 * @author Bohdan Khablenko
 *
 * @since v0.9.0
 */
public interface CredentialsServiceLocator {

    /**
     * Returns the service that is responsible for managing credentials for the
     * specified application.
     *
     * @param applicationId The application ID
     *
     * @return The service that is responsible for managing credentials for the
     *         specified application.
     */
    CredentialsService getCredentialsService(String applicationId);

    /**
     * Returns the names of credentials services configured. This method is used
     * to set acceptable values of the listbox used to specify a credentials
     * service for an application via the Admin UI.
     *
     * The default implementation loads all credentials services configured as
     * Spring beans and returns their names.
     *
     * @return The names of credentials services configured
     */
    default List<String> getCredentialsServiceNames() {
        throw new UnsupportedOperationException();
    }
}
