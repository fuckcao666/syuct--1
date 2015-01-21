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

#include <stdint.h>
#include <stdio.h>
#include <string.h>
#include "platform/sha.h"
#include "kaa_user.h"

#include "kaa_test.h"
#include "kaa.h"
#include "kaa_platform_protocol.h"
#include "kaa_channel_manager.h"
#include "kaa_profile.h"
#include "kaa_platform_utils.h"
#include "utilities/kaa_mem.h"
#include "utilities/kaa_log.h"

extern kaa_error_t kaa_status_create(kaa_status_t **kaa_status_p);
extern void        kaa_status_destroy(kaa_status_t *self);

extern kaa_error_t kaa_channel_manager_create(kaa_channel_manager_t **channel_manager_p, kaa_logger_t *logger);
extern void        kaa_channel_manager_destroy(kaa_channel_manager_t *self);

extern kaa_error_t kaa_user_manager_create(kaa_user_manager_t **user_manager_p, kaa_status_t *status
        , kaa_channel_manager_t *channel_manager, kaa_logger_t *logger);
extern void kaa_user_manager_destroy(kaa_user_manager_t *self);

extern kaa_error_t kaa_user_request_get_size(kaa_user_manager_t *self, size_t *expected_size);
extern kaa_error_t kaa_user_request_serialize(kaa_user_manager_t *self, kaa_platform_message_writer_t* writer);
extern kaa_error_t kaa_user_handle_server_sync(kaa_user_manager_t *self, kaa_platform_message_reader_t *reader, uint32_t extension_options, size_t extension_length);


#define USER_EXTERNAL_ID    "user@id"
#define ACCESS_TOKEN        "token"

static kaa_user_manager_t *user_manager = NULL;
static kaa_logger_t *logger = NULL;
static kaa_status_t *status = NULL;
static kaa_channel_manager_t *channel_manager = NULL;

static bool is_on_attached_invoked = false;
static bool is_on_detached_invoked = false;
static bool is_on_response_invoked = false;
static bool last_is_attached_result = false;

static void on_attached(const char *user_external_id, const char *endpoint_access_token)
{
    ASSERT_EQUAL(strcmp(ACCESS_TOKEN, endpoint_access_token), 0);
    ASSERT_EQUAL(strcmp(USER_EXTERNAL_ID, user_external_id), 0);
    is_on_attached_invoked = true;
}

static void on_detached(const char *endpoint_access_token)
{
    ASSERT_EQUAL(strcmp(ACCESS_TOKEN, endpoint_access_token), 0);
    is_on_detached_invoked = true;
}

static void on_response(bool is_attached)
{
    last_is_attached_result = is_attached;
    is_on_response_invoked = true;
}

void test_create_request()
{
    ASSERT_EQUAL(kaa_user_manager_attach_to_user(user_manager, USER_EXTERNAL_ID, ACCESS_TOKEN), KAA_ERR_NONE);

    size_t expected_size = 0;
    ASSERT_EQUAL(kaa_user_request_get_size(user_manager, &expected_size), KAA_ERR_NONE);

    char buffer[expected_size];
    kaa_platform_message_writer_t *writer = NULL;
    ASSERT_EQUAL(kaa_platform_message_writer_create(&writer, buffer, expected_size), KAA_ERR_NONE);
    ASSERT_NOT_NULL(writer);

    ASSERT_EQUAL(kaa_user_request_serialize(user_manager, writer), KAA_ERR_NONE);

    char *buf_cursor = buffer;
    ASSERT_EQUAL(KAA_USER_EXTENSION_TYPE, *buf_cursor);
    ++buf_cursor;

    char options[] = { 0x00, 0x00, 0x01 };
    ASSERT_EQUAL(memcmp(buf_cursor, options, 3), 0);
    buf_cursor += 3;

    ASSERT_EQUAL(*(uint32_t * ) buf_cursor,
            KAA_HTONL(sizeof(uint32_t) + kaa_aligned_size_get(strlen(USER_EXTERNAL_ID)) + kaa_aligned_size_get(strlen(ACCESS_TOKEN))));
    buf_cursor += sizeof(uint32_t);

    ASSERT_EQUAL(0, *buf_cursor);
    ++buf_cursor;

    ASSERT_EQUAL(strlen(USER_EXTERNAL_ID), *buf_cursor);
    ++buf_cursor;

    ASSERT_EQUAL(KAA_HTONS(strlen(ACCESS_TOKEN)), *(uint16_t *) buf_cursor);
    buf_cursor += sizeof(uint16_t);

    ASSERT_EQUAL(memcmp(buf_cursor, USER_EXTERNAL_ID, strlen(USER_EXTERNAL_ID)), 0);
    buf_cursor += kaa_aligned_size_get(strlen(USER_EXTERNAL_ID));

    ASSERT_EQUAL(memcmp(buf_cursor, ACCESS_TOKEN, strlen(ACCESS_TOKEN)), 0);
}

void test_response()
{
    char response[] = {
            /*  bit 0   */   0x00, 0x00, 0x00, 0x00,    /* User attach response field. Result - success */
            /*  bit 32  */   0x01, 0x07, 0x00, 0x05,    /* User attach notification field */
            /*  bit 64  */   'u', 's', 'e', 'r',
            /*  bit 96  */   '@', 'i', 'd', 0x00,
            /*  bit 128 */   't', 'o', 'k', 'e',
            /*  bit 160 */   'n', 0x00, 0x00, 0x00,
            /*  bit 192 */   0x02, 0x00, 0x00, 0x05,    /* User detach notification field */
            /*  bit 224 */   't', 'o', 'k', 'e',
            /*  bit 256 */   'n', 0x00, 0x00, 0x00

    };
    kaa_platform_message_reader_t *reader = NULL;
    ASSERT_EQUAL(kaa_platform_message_reader_create(&reader, response, 36), KAA_ERR_NONE);
    ASSERT_NOT_NULL(reader);

    ASSERT_EQUAL(kaa_user_handle_server_sync(user_manager, reader, 0, 36), KAA_ERR_NONE);
    ASSERT_TRUE(is_on_attached_invoked);
    ASSERT_TRUE(is_on_detached_invoked);
    ASSERT_TRUE(is_on_response_invoked);
    ASSERT_TRUE(last_is_attached_result);
}

int test_init(void)
{
    kaa_error_t error = kaa_log_create(&logger, KAA_MAX_LOG_MESSAGE_LENGTH, KAA_MAX_LOG_LEVEL, NULL);
    if (error || !logger) {
        return error;
    }

    error = kaa_status_create(&status);
    if (error || !status) {
        return error;
    }

    error = kaa_channel_manager_create(&channel_manager, logger);
    if (error || !channel_manager) {
        return error;
    }

    error = kaa_user_manager_create(&user_manager, status, channel_manager, logger);
    if (error || !user_manager) {
        return error;
    }

    kaa_attachment_status_listeners_t listeners = { &on_attached, &on_detached, &on_response };
    error = kaa_user_manager_set_attachment_listeners(user_manager, &listeners);
    if (error) {
        return error;
    }

    return 0;
}

int test_deinit(void)
{
    kaa_user_manager_destroy(user_manager);
    kaa_channel_manager_destroy(channel_manager);
    kaa_status_destroy(status);
    kaa_log_destroy(logger);

    return 0;
}

KAA_SUITE_MAIN(Log, test_init, test_deinit
       ,
       KAA_TEST_CASE(create_request, test_create_request)
       KAA_TEST_CASE(process_response, test_response)
        )
