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
 * @file kaa_context.h
 * @brief Kaa endpoint context definition
 *
 * Defines the general Kaa endpoint context.
 */

#ifndef KAA_CONTEXT_H_
#define KAA_CONTEXT_H_

#ifdef __cplusplus
extern "C" {
#endif

#ifndef KAA_STATUS_T
# define KAA_STATUS_T
    typedef struct kaa_status_t                     kaa_status_t;
#endif

#ifndef KAA_PLATFORM_PRTOCOL_T
# define KAA_PLATFORM_PRTOCOL_T
    typedef struct kaa_platform_protocol_t          kaa_platform_protocol_t;
#endif

typedef struct kaa_status_holder_t                  kaa_status_holder_t;

#ifndef KAA_BOOTSTRAP_MANAGER_T
# define KAA_BOOTSTRAP_MANAGER_T
    typedef struct kaa_bootstrap_manager_t          kaa_bootstrap_manager_t;
#endif

#ifndef KAA_CHANNEL_MANAGER_T
# define KAA_CHANNEL_MANAGER_T
    typedef struct kaa_channel_manager_t            kaa_channel_manager_t;
#endif

#ifndef KAA_PROFILE_MANAGER_T
# define KAA_PROFILE_MANAGER_T
    typedef struct kaa_profile_manager_t            kaa_profile_manager_t;
#endif

#ifndef KAA_USER_MANAGER_T
# define KAA_USER_MANAGER_T
    typedef struct kaa_user_manager_t               kaa_user_manager_t;
#endif

#ifndef KAA_EVENT_MANAGER_T
# define KAA_EVENT_MANAGER_T
    typedef struct kaa_event_manager_t              kaa_event_manager_t;
#endif

#ifndef KAA_LOG_COLLECTOR_T
# define KAA_LOG_COLLECTOR_T
    typedef struct kaa_log_collector                kaa_log_collector_t;
#endif

#ifndef KAA_CONFIGURATION_MANAGER_T
# define KAA_CONFIGURATION_MANAGER_T
    typedef struct kaa_configuration_manager_t        kaa_configuration_manager_t;
#endif

#ifndef KAA_NOTIFICATION_MANAGER_T
# define KAA_NOTIFICATION_MANAGER_T
    typedef struct kaa_notification_manager_t       kaa_notification_manager_t;
#endif

#ifndef KAA_LOGGER_T
# define KAA_LOGGER_T
    typedef struct kaa_logger_t                     kaa_logger_t;
#endif

#ifndef KAA_FAILOVER_STRATEGY
# define KAA_FAILOVER_STRATEGY
    typedef struct kaa_failover_strategy_t       kaa_failover_strategy_t;
#endif

#ifndef KAA_PLUGIN_T
# define KAA_PLUGIN_T
typedef struct kaa_plugin_t kaa_plugin_t;
#endif

/**
 * General Kaa endpoint context. Contains private structures of all Kaa endpoint SDK subsystems that can be used
 * independently to perform API calls to specific subsystems.
 */
typedef struct kaa_context_s {
    kaa_status_holder_t         *status;                 /**< See @link kaa_status.h @endlink. */
    kaa_platform_protocol_t     *platform_protocol;      /**< See @link kaa_platform_protocol.h @endlink. */
    kaa_channel_manager_t       *channel_manager;        /**< See @link kaa_channel_manager.h @endlink. */
    kaa_configuration_manager_t *configuration_manager;  /**< See @link kaa_configuration_manager.h @endlink. */
    kaa_logger_t                *logger;                 /**< See @link kaa_log.h @endlink. */
    kaa_failover_strategy_t     *failover_strategy;
    kaa_plugin_t               **kaa_plugins;
    size_t                       kaa_plugin_count;
} kaa_context_t;

#ifdef __cplusplus
}      /* extern "C" */
#endif
#endif /* KAA_CONTEXT_H_ */
