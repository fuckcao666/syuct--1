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

/**
 * @file kaa_channel_manager.h
 * @brief User defined channels manager for Kaa C SDK.
 *
 * Manages client transport channels.
 *
 * Notifies about new access points and indicates to user defined protocol
 * implementations that Kaa services have data to sync with Operations server.
 */

#ifndef KAA_CHANNEL_MANAGER_H_
#define KAA_CHANNEL_MANAGER_H_

#include "kaa_common.h"
#include "platform/ext_transport_channel.h"

#ifdef __cplusplus
extern "C" {
#endif

/**
 * @brief Kaa channel manager structure.
 */
typedef struct kaa_channel_manager_t kaa_channel_manager_t;



/**
 * @brief Adds user-defined transport channel implementation as a sync request
 * handler for the given list of services.
 *
 * Kaa library will call the channel's callback when there is data to be sent to
 * Operations server for one of the specified services.
 *
 * @b NOTE: It is possible to register more than one channel for the same service.
 * In such event Kaa library will use the last registered one.
 *
 * @param[in]   self       Channel manager.
 * @param[in]   channel    Client transport channel.
 *
 * @return                 Error code.
 */
kaa_error_t kaa_channel_manager_add_transport_channel(kaa_channel_manager_t *self
                                                    , kaa_transport_channel_interface_t *channel);

/**
 * @brief Removes user-defined transport channel implementation from
 * the currently registered list.
 *
 * @b NOTE: The channel manager is responsible to release all resources related
 * to this channel.
 *
 * @param[in]   self       Channel manager.
 * @param[in]   channel    Client transport channel.
 *
 * @return                 Error code.
 */
kaa_error_t kaa_channel_manager_remove_transport_channel(kaa_channel_manager_t *self
                                                       , kaa_transport_channel_interface_t *channel);

#ifdef __cplusplus
}      /* extern "C" */
#endif
#endif /* KAA_CHANNEL_MANAGER_H_ */
