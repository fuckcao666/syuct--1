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
#include "platform/sha.h"

#include "kaa.h"
#include "utilities/kaa_mem.h"
#include "utilities/kaa_log.h"
#include "kaa_common.h"
#include "kaa_context.h"
#include "kaa_external.h"
#include "kaa_defaults.h"
#include "kaa_status.h"



/*
 * External constructors and destructors from around the Kaa SDK
 */
extern kaa_error_t kaa_user_manager_create(kaa_user_manager_t **user_manager_p, kaa_status_t *status
        , kaa_channel_manager_t *channel_manager, kaa_logger_t *logger);

extern void        kaa_user_manager_destroy(kaa_user_manager_t *user_manager);

extern kaa_error_t kaa_status_create(kaa_status_t **kaa_status_p);
extern void        kaa_status_destroy(kaa_status_t *self);

extern kaa_error_t kaa_profile_manager_create(kaa_profile_manager_t **profile_manager_p, kaa_status_t *status
        , kaa_channel_manager_t *channel_manager, kaa_logger_t *logger);
extern void        kaa_profile_manager_destroy(kaa_profile_manager_t *self);

extern kaa_error_t kaa_channel_manager_create(kaa_channel_manager_t **channel_manager_p, kaa_logger_t *logger);
extern void        kaa_channel_manager_destroy(kaa_channel_manager_t *self);

#ifndef KAA_DISABLE_FEATURE_EVENTS
extern kaa_error_t kaa_event_manager_create(kaa_event_manager_t **event_manager_p, kaa_status_t *status
        , kaa_channel_manager_t *channel_manager, kaa_logger_t *logger);
extern void        kaa_event_manager_destroy(kaa_event_manager_t *self);
#endif

#ifndef KAA_DISABLE_FEATURE_LOGGING
extern kaa_error_t kaa_log_collector_create(kaa_log_collector_t ** log_collector_p, kaa_status_t *status
        , kaa_channel_manager_t *channel_manager, kaa_logger_t *logger);
extern void        kaa_log_collector_destroy(kaa_log_collector_t *self);
#endif

extern kaa_error_t kaa_bootstrap_manager_create(kaa_bootstrap_manager_t **bootstrap_manager_p, kaa_logger_t *logger);
extern void        kaa_bootstrap_manager_destroy(kaa_bootstrap_manager_t *self);

extern kaa_error_t kaa_platform_protocol_create(kaa_platform_protocol_t **platform_protocol_p, kaa_context_t *context
        , kaa_logger_t *logger);
extern void        kaa_platform_protocol_destroy(kaa_platform_protocol_t *self);


/* Forward declaration */
static kaa_error_t kaa_context_destroy(kaa_context_t *context);



static kaa_error_t kaa_context_create(kaa_context_t **context_p, kaa_logger_t *logger)
{
    KAA_RETURN_IF_NIL2(context_p, logger, KAA_ERR_BADPARAM);

    *context_p = (kaa_context_t *) KAA_MALLOC(sizeof(kaa_context_t));
    KAA_RETURN_IF_NIL(*context_p, KAA_ERR_NOMEM);

    (*context_p)->logger = logger;

    kaa_error_t error = kaa_status_create(&((*context_p)->status));

    if (!error)
        error = kaa_channel_manager_create(&((*context_p)->channel_manager), logger);

    if (!error)
        error = kaa_profile_manager_create(&((*context_p)->profile_manager), (*context_p)->status
                , (*context_p)->channel_manager, (*context_p)->logger);

#ifndef KAA_DISABLE_FEATURE_EVENTS
    if (!error)
        error = kaa_event_manager_create(&((*context_p)->event_manager)
                , (*context_p)->status, (*context_p)->channel_manager, (*context_p)->logger);
#endif

    if (!error)
        error = kaa_bootstrap_manager_create(&((*context_p)->bootstrap_manager), (*context_p)->logger);

#ifndef KAA_DISABLE_FEATURE_LOGGING
    if (!error)
        error = kaa_log_collector_create(&((*context_p)->log_collector)
                , (*context_p)->status, (*context_p)->channel_manager, (*context_p)->logger);
#endif

    if (!error)
        error = kaa_user_manager_create(&((*context_p)->user_manager)
                , (*context_p)->status, (*context_p)->channel_manager, (*context_p)->logger);

    if (!error)
        error = kaa_platform_protocol_create(&((*context_p)->platfrom_protocol), *context_p, (*context_p)->logger);

    if (error) {
        kaa_context_destroy(*context_p);
        *context_p = NULL;
    }

    return error;
}



static kaa_error_t kaa_context_destroy(kaa_context_t *context)
{
    KAA_RETURN_IF_NIL(context, KAA_ERR_BADPARAM);

    kaa_user_manager_destroy(context->user_manager);
#ifndef KAA_DISABLE_FEATURE_EVENTS
    kaa_event_manager_destroy(context->event_manager);
#endif
    kaa_profile_manager_destroy(context->profile_manager);
    kaa_bootstrap_manager_destroy(context->bootstrap_manager);
    kaa_channel_manager_destroy(context->channel_manager);
    kaa_status_destroy(context->status);
#ifndef KAA_DISABLE_FEATURE_LOGGING
    kaa_log_collector_destroy(context->log_collector);
#endif
    kaa_platform_protocol_destroy(context->platfrom_protocol);
    KAA_FREE(context);
    return KAA_ERR_NONE;
}



kaa_error_t kaa_init(kaa_context_t **kaa_context_p)
{
    KAA_RETURN_IF_NIL(kaa_context_p, KAA_ERR_BADPARAM);

    // Initialize logger
    kaa_logger_t *logger = NULL;
    kaa_error_t error = kaa_log_create(&logger, KAA_MAX_LOG_MESSAGE_LENGTH, KAA_MAX_LOG_LEVEL, NULL);  // TODO: make log destination configurable
    if (error)
        return error;

    KAA_LOG_INFO(logger, KAA_ERR_NONE, "Kaa SDK version %s, commit hash %s", BUILD_VERSION, BUILD_COMMIT_HASH);

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

    kaa_get_endpoint_public_key(&pub_key_buffer, &pub_key_buffer_size, &need_deallocation);
    kaa_digest d;
    error = kaa_calculate_sha_hash(pub_key_buffer, pub_key_buffer_size, d);

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

    error = kaa_status_set_endpoint_public_key_hash((*kaa_context_p)->status, d);
    if (error) {
        KAA_LOG_FATAL(logger, error, "Failed to set Endpoint public key");
        kaa_context_destroy(*kaa_context_p);
        *kaa_context_p = NULL;
        kaa_log_destroy(logger);
        return error;
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
