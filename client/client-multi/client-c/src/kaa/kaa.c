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

#include <stddef.h>
#include <stdbool.h>
#include <stdint.h>
#include "platform/stdio.h"
#include "platform/ext_sha.h"
#include "kaa_status.h"
#include "kaa.h"
#include "utilities/kaa_mem.h"
#include "utilities/kaa_log.h"

#include "kaa_common.h"

#include "kaa_context.h"
#include "kaa_defaults.h"
#include "platform/ext_transport_channel.h"
#include "platform/ext_key_utils.h"
#include "plugins/kaa_plugin.h"

/*
 * External constructors and destructors from around the Kaa SDK
 */

extern kaa_error_t kaa_status_create(kaa_status_t **kaa_status_p);
extern void kaa_status_destroy(kaa_status_t *self);

extern bool kaa_profile_manager_is_profile_set(kaa_context_t *context);

extern kaa_error_t kaa_channel_manager_create(kaa_channel_manager_t **channel_manager_p, kaa_context_t *context);
extern void kaa_channel_manager_destroy(kaa_channel_manager_t *self);
extern kaa_transport_channel_interface_t *kaa_channel_manager_get_transport_channel(kaa_channel_manager_t *self,
                                                                                    uint16_t plugin_type);


extern kaa_error_t kaa_platform_protocol_create(kaa_platform_protocol_t **platform_protocol_p, kaa_context_t *context,
                                                kaa_status_t *status);
extern void kaa_platform_protocol_destroy(kaa_platform_protocol_t *self);

struct kaa_status_holder_t {
    kaa_status_t *status_instance;
};

extern kaa_error_t kaa_status_set_registered(kaa_status_t *self, bool is_registered);

#ifndef KAA_DISABLE_FEATURE_NOTIFICATION
extern kaa_error_t kaa_notification_manager_create(kaa_notification_manager_t **self, kaa_status_t *status
                                                 , kaa_channel_manager_t *channel_manager
                                                 , kaa_logger_t *logger);
extern void kaa_notification_manager_destroy(kaa_notification_manager_t *self);
#endif

extern kaa_error_t kaa_failover_strategy_create(kaa_failover_strategy_t** strategy, kaa_logger_t *logger);
extern void kaa_failover_strategy_destroy(kaa_failover_strategy_t* strategy);
extern bool kaa_bootstrap_manager_process_failover(kaa_context_t *context);

/* Forward declaration */
static kaa_error_t kaa_context_destroy(kaa_context_t *context);

static kaa_error_t kaa_context_create(kaa_context_t **context_p, kaa_logger_t *logger)
{
    KAA_RETURN_IF_NIL2(context_p, logger, KAA_ERR_BADPARAM);

    *context_p = (kaa_context_t *) KAA_MALLOC(sizeof(kaa_context_t));
    KAA_RETURN_IF_NIL(*context_p, KAA_ERR_NOMEM);

    (*context_p)->logger = logger;

    kaa_error_t error = KAA_ERR_NONE;
    (*context_p)->status = (kaa_status_holder_t *) KAA_MALLOC(sizeof(kaa_status_holder_t));
    if (!(*context_p)->status)
        error = KAA_ERR_NOMEM;

    if (!error)
        error = kaa_status_create(&((*context_p)->status->status_instance));

    if (!error)
        error = kaa_platform_protocol_create(&((*context_p)->platform_protocol), *context_p,
                                             (*context_p)->status->status_instance);

    if (!error)
        error = kaa_failover_strategy_create(&((*context_p)->failover_strategy), logger);

    if (!error)
        error = kaa_channel_manager_create(&((*context_p)->channel_manager), (*context_p));


    error = kaa_create_plugins(*context_p);

    if (error) {
        kaa_context_destroy(*context_p);
        *context_p = NULL;
    }

    return error;
}

static kaa_error_t kaa_context_destroy(kaa_context_t *context)
{
    KAA_RETURN_IF_NIL(context, KAA_ERR_BADPARAM);

    kaa_channel_manager_destroy(context->channel_manager);
    kaa_status_destroy(context->status->status_instance);
    kaa_failover_strategy_destroy(context->failover_strategy);
    KAA_FREE(context->status);
    kaa_platform_protocol_destroy(context->platform_protocol);

    kaa_destroy_plugins(context);

    KAA_FREE(context);
    return KAA_ERR_NONE;
}

kaa_error_t kaa_init(kaa_context_t **kaa_context_p)
{
    KAA_RETURN_IF_NIL(kaa_context_p, KAA_ERR_BADPARAM);

    // Initialize logger
    kaa_logger_t *logger = NULL;
    kaa_error_t error = kaa_log_create(&logger, KAA_MAX_LOG_MESSAGE_LENGTH, KAA_MAX_LOG_LEVEL, NULL); // TODO: make log destination configurable
    if (error)
        return error;

    KAA_LOG_INFO(logger, KAA_ERR_NONE, "Kaa SDK version %s, commit hash %s", KAA_BUILD_VERSION, KAA_BUILD_COMMIT_HASH);

    // Initialize general Kaa context
    error = kaa_context_create(kaa_context_p, logger);
    if (error) {
        KAA_LOG_FATAL(logger, error, "Failed to create Kaa context");
        kaa_log_destroy(logger);
        *kaa_context_p = NULL;
        return error;
    }

    // Initialize endpoint identity
    char *pub_key_buffer = NULL;
    size_t pub_key_buffer_size = 0;
    bool need_deallocation = false;

    ext_get_endpoint_public_key(&pub_key_buffer, &pub_key_buffer_size, &need_deallocation);

    kaa_digest pub_key_hash;
    error = ext_calculate_sha_hash(pub_key_buffer, pub_key_buffer_size, pub_key_hash);

    if (need_deallocation && pub_key_buffer_size > 0) {
        KAA_FREE(pub_key_buffer);
    }

    if (error) {
        KAA_LOG_FATAL(logger, error, "Failed to calculate EP ID");
        kaa_context_destroy(*kaa_context_p);
        *kaa_context_p = NULL;
        kaa_log_destroy(logger);
        return error;
    }

    error = ext_copy_sha_hash((*kaa_context_p)->status->status_instance->endpoint_public_key_hash, pub_key_hash);
    if (error) {
        KAA_LOG_FATAL(logger, error, "Failed to set Endpoint public key");
        kaa_context_destroy(*kaa_context_p);
        *kaa_context_p = NULL;
        kaa_log_destroy(logger);
        return error;
    }

    return KAA_ERR_NONE;
}

kaa_error_t kaa_start(kaa_context_t *kaa_context)
{
    KAA_RETURN_IF_NIL(kaa_context, KAA_ERR_BADPARAM);

    KAA_LOG_INFO(kaa_context->logger, KAA_ERR_NONE, "Going to start Kaa endpoint");

    kaa_transport_channel_interface_t *bootstrap_channel = kaa_channel_manager_get_transport_channel(
            kaa_context->channel_manager, KAA_PLUGIN_BOOTSTRAP);
    if (bootstrap_channel) {
        const uint16_t bootstrap_plugin[] = { KAA_PLUGIN_BOOTSTRAP };
        kaa_error_t error = bootstrap_channel->sync_handler(bootstrap_channel->context, bootstrap_plugin, 1);
        if (error) {
            KAA_LOG_ERROR(kaa_context->logger, error, "Failed to sync Bootstrap plugin. Try again later");
            return error;
        }
    } else {
        KAA_LOG_FATAL(kaa_context->logger, KAA_ERR_NOT_FOUND, "Could not find Bootstrap transport channel");
        return KAA_ERR_NOT_FOUND;
    }

    return KAA_ERR_NONE;
}

kaa_error_t kaa_deinit(kaa_context_t *kaa_context)
{
    KAA_RETURN_IF_NIL(kaa_context, KAA_ERR_BADPARAM);

    kaa_logger_t *logger = kaa_context->logger;
    kaa_error_t error = kaa_context_destroy(kaa_context);
    if (error)
        KAA_LOG_ERROR(logger, error, "Failed to destroy Kaa context");
    kaa_log_destroy(logger);
    return error;
}

bool kaa_process_failover(kaa_context_t *kaa_context)
{
    KAA_RETURN_IF_NIL(kaa_context, false);
    return kaa_bootstrap_manager_process_failover(kaa_context);
}

kaa_error_t kaa_context_set_status_registered(kaa_context_t *kaa_context, bool is_registered)
{
    KAA_RETURN_IF_NIL(kaa_context, KAA_ERR_BADPARAM);
    return kaa_status_set_registered(kaa_context->status->status_instance, is_registered);
}

kaa_error_t kaa_check_readiness(kaa_context_t *kaa_context)
{
    KAA_RETURN_IF_NIL(kaa_context, KAA_ERR_BADPARAM);
    if (!kaa_profile_manager_is_profile_set(kaa_context)) {
        KAA_LOG_ERROR(kaa_context->logger, KAA_ERR_PROFILE_IS_NOT_SET, "Profile isn't set");
        return KAA_ERR_PROFILE_IS_NOT_SET;
    }

    return KAA_ERR_NONE;
}

kaa_status_t *kaa_get_status(kaa_context_t *kaa_context)
{
    return kaa_context->status->status_instance;
}
