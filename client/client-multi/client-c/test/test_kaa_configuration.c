/*
 * Copyright 2014-2015 CyberVision, Inc.
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

#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <stdint.h>
#include "kaa_status.h"
#include "kaa_configuration_manager.h"

#ifndef KAA_DISABLE_FEATURE_CONFIGURATION

#include "platform/sock.h"
#include "platform/ext_sha.h"
#include "platform/ext_configuration_persistence.h"
#include "platform/ext_configuration_receiver.h"
#include "kaa_test.h"
#include "kaa_platform_utils.h"
#include "kaa_defaults.h"
#include "kaa_channel_manager.h"
#include "utilities/kaa_mem.h"
#include "utilities/kaa_log.h"

#include "plugins/kaa_plugin.h"



extern kaa_error_t kaa_status_create(kaa_status_t **kaa_status_p);
extern void        kaa_status_destroy(kaa_status_t *self);

//extern kaa_error_t kaa_configuration_manager_create(kaa_configuration_manager_t **configuration_manager_p,
//                                                    kaa_channel_manager_t *channel_manager, kaa_status_t *status,
//                                                    kaa_logger_t *logger);
//extern void kaa_configuration_manager_destroy(kaa_configuration_manager_t *self);
kaa_plugin_t *kaa_configuration_plugin_create(kaa_context_t *context);

//extern kaa_error_t kaa_configuration_manager_get_size(kaa_configuration_manager_t *self, size_t *expected_size);
//extern kaa_error_t kaa_configuration_manager_request_serialize(kaa_configuration_manager_t *self, kaa_platform_message_writer_t *writer);
//extern kaa_error_t kaa_configuration_manager_handle_server_sync(kaa_configuration_manager_t *self, kaa_platform_message_reader_t *reader, uint16_t extension_options, size_t extension_length);


static kaa_logger_t *logger = NULL;
static kaa_status_t *status = NULL;
//static kaa_configuration_manager_t *config_manager = NULL;
static kaa_context_t kaa_context;
static kaa_plugin_t *kaa_plugin = NULL;

typedef struct {
    kaa_status_t *status_instance;
} mock_status_holder_t;

static mock_status_holder_t status_holder;

#define CONFIG_START_SEQ_N  5
#define CONFIG_NEW_SEQ_N  6
#define CONFIG_RESPONSE_FLAGS 0x02
#define CONFIG_DATA_FIELD "Basic configuration schema"

static const size_t CONFIG_UUID_SIZE = 16;
static const char CONFIG_UUID[] = { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F };

static kaa_error_t on_configuration_updated(void *context, const kaa_root_configuration_t *configuration)
{
    bool *result = (bool *) context;
    *result = true;
    return KAA_ERR_NONE;
}

void test_create_request()
{
    KAA_TRACE_IN(logger);

    kaa_plugin_t *plugin;
    ASSERT_EQUAL(kaa_plugin_find_by_type(&kaa_context, KAA_PLUGIN_CONFIGURATION, &plugin), KAA_ERR_NONE);

    size_t expected_size = 0;
    ASSERT_EQUAL(plugin->request_get_size_fn(plugin, &expected_size), KAA_ERR_NONE);
    ASSERT_EQUAL(expected_size, sizeof(uint32_t) + KAA_EXTENSION_HEADER_SIZE + SHA_1_DIGEST_LENGTH);

    char request_buffer[expected_size];
    kaa_platform_message_writer_t *writer = NULL;
    ASSERT_EQUAL(kaa_platform_message_writer_create(&writer, request_buffer, expected_size), KAA_ERR_NONE);
    ASSERT_EQUAL(plugin->request_serialize_fn(plugin, writer), KAA_ERR_NONE);

    char *cursor = writer->begin;
    ASSERT_EQUAL(KAA_HTONS(*((uint16_t *) cursor)), KAA_CONFIGURATION_EXTENSION_TYPE);
    cursor += sizeof(uint32_t);

    ASSERT_EQUAL(KAA_NTOHL(*((uint32_t *) cursor)), sizeof(uint32_t) + SHA_1_DIGEST_LENGTH);    // checking payload size
    cursor += sizeof(uint32_t);

    ASSERT_EQUAL(KAA_NTOHL(*((uint32_t *) cursor)), CONFIG_START_SEQ_N);    // checking sequence number
    cursor += sizeof(uint32_t);

    kaa_digest check_hash;
    ext_calculate_sha_hash(KAA_CONFIGURATION_DATA, KAA_CONFIGURATION_DATA_LENGTH, check_hash);  // checking configuration hash
    ASSERT_EQUAL(memcmp(cursor, check_hash, SHA_1_DIGEST_LENGTH), 0);
    cursor += SHA_1_DIGEST_LENGTH;

    ASSERT_EQUAL(cursor, writer->end);

    kaa_platform_message_writer_destroy(writer);
}

void test_response()
{
    KAA_TRACE_IN(logger);
    const size_t response_size = kaa_aligned_size_get(KAA_CONFIGURATION_DATA_LENGTH) + sizeof(uint32_t) + sizeof(uint32_t);
    char response[response_size];
    char *response_cursor = response;

    *((uint32_t *) response_cursor) = KAA_HTONL(CONFIG_NEW_SEQ_N);
    response_cursor += sizeof(uint32_t);

    *((uint32_t *) response_cursor) = KAA_HTONL(KAA_CONFIGURATION_DATA_LENGTH);
    response_cursor += sizeof(uint32_t);

    memcpy(response_cursor, KAA_CONFIGURATION_DATA, KAA_CONFIGURATION_DATA_LENGTH);

    kaa_platform_message_reader_t *reader = NULL;
    ASSERT_EQUAL(kaa_platform_message_reader_create(&reader, response, response_size), KAA_ERR_NONE);

    bool is_callback_invoked = false;
    kaa_configuration_root_receiver_t receiver = { &is_callback_invoked, &on_configuration_updated };
    ASSERT_EQUAL(kaa_configuration_plugin_set_root_receiver(kaa_plugin, &receiver), KAA_ERR_NONE);

    ASSERT_EQUAL(kaa_context.kaa_plugins[0]->request_handle_server_sync_fn(kaa_plugin, reader, 0, CONFIG_RESPONSE_FLAGS, response_size), KAA_ERR_NONE);

    ASSERT_EQUAL(is_callback_invoked, true);
    ASSERT_EQUAL(status->config_seq_n, CONFIG_NEW_SEQ_N);

    const kaa_root_configuration_t *root_config = kaa_configuration_plugin_get_configuration(kaa_plugin);
    ASSERT_EQUAL(strcmp(root_config->data->data, CONFIG_DATA_FIELD), 0);

    kaa_bytes_t *uuid = (kaa_bytes_t *) root_config->__uuid->data;
    ASSERT_EQUAL(uuid->size, CONFIG_UUID_SIZE);
    ASSERT_EQUAL(memcmp(uuid->buffer, CONFIG_UUID, uuid->size), 0);

    kaa_platform_message_reader_destroy(reader);
}


#endif


int test_init(void)
{
    kaa_error_t error = kaa_log_create(&logger, KAA_MAX_LOG_MESSAGE_LENGTH, KAA_MAX_LOG_LEVEL, NULL);
    if (error || !logger)
        return error;

#ifndef KAA_DISABLE_FEATURE_CONFIGURATION
    error = kaa_status_create(&status);
    if (error || !status)
        return error;

    status->config_seq_n = CONFIG_START_SEQ_N;
    status_holder.status_instance = status;

//    error = kaa_configuration_manager_create(&config_manager, NULL, status, logger);
//    if (error || !config_manager)
//        return error;
    kaa_context.kaa_plugins = (kaa_plugin_t*)KAA_CALLOC(1, sizeof(kaa_plugin_t*));
    kaa_plugin = kaa_configuration_plugin_create(&kaa_context);
    kaa_context.kaa_plugins[0] = kaa_plugin;
    kaa_context.kaa_plugin_count = 1;
    kaa_context.logger = logger;
    kaa_context.status = &status_holder;
    kaa_context.channel_manager = NULL;

    if (!kaa_context.kaa_plugins[0]) {
        return KAA_ERR_NOT_INITIALIZED;
    }

    kaa_context.kaa_plugins[0]->init_fn(kaa_context.kaa_plugins[0]);


    fprintf(stderr, "STEP 1\n");
    //kaa_context.kaa_plugins[0]->deinit_fn(kaa_context.kaa_plugins[0]);
//    KAA_FREE(kaa_context.kaa_plugins[0]);
//    KAA_FREE(kaa_context.kaa_plugins);
    fprintf(stderr, "STEP 2\n");

#endif
    return 0;
}



int test_deinit(void)
{
#ifndef KAA_DISABLE_FEATURE_CONFIGURATION
//    kaa_configuration_manager_destroy(config_manager);
    kaa_context.kaa_plugins[0]->deinit_fn(kaa_context.kaa_plugins[0]);
    KAA_FREE(kaa_context.kaa_plugins[0]);
    KAA_FREE(kaa_context.kaa_plugins);
    kaa_status_destroy(status);
#endif
    kaa_log_destroy(logger);

    return 0;
}



KAA_SUITE_MAIN(Log, test_init, test_deinit
#ifndef KAA_DISABLE_FEATURE_CONFIGURATION
       ,
       KAA_TEST_CASE(create_request, test_create_request)
       KAA_TEST_CASE(process_response, test_response)
#endif
        )
