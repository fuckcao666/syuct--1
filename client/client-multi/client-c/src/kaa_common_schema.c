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

#include <string.h>
#include "platform/stdio.h"
#include "kaa_common_schema.h"


#include "kaa_common_schema.h"


#include "avro_src/avro/io.h"
#include "avro_src/encoding.h"

#include "utilities/kaa_mem.h"
#include "kaa_error.h"

void kaa_string_serialize(avro_writer_t writer, void* data)
{
    if (data) {
        kaa_string_t* str = (kaa_string_t*)data;
        if (str->data) {
            avro_binary_encoding.write_string(writer, str->data);
        }
    }
}

kaa_string_t* kaa_string_move_create(const char* data, destroy_fn destroy)
{
    kaa_string_t* str = NULL;
    if (data) {
        str = (kaa_string_t*)KAA_MALLOC(sizeof(kaa_string_t));
        KAA_RETURN_IF_NIL(str, NULL);

        str->data = (char*)data;
        str->destroy = destroy;
    }

    return str;
}

kaa_string_t* kaa_string_copy_create(const char* data)
{
    kaa_string_t* str = NULL;
    if (data) {
        str = (kaa_string_t*)KAA_MALLOC(sizeof(kaa_string_t));
        KAA_RETURN_IF_NIL(str, NULL);

        size_t len = strlen(data) + 1;
        str->data = (char*)KAA_MALLOC(len * sizeof(char));
        if (!str->data) {
            KAA_FREE(str);
            return NULL;
        }

        memcpy(str->data, data, len);
        str->destroy = &kaa_data_destroy;
    }

    return str;
}

void kaa_string_destroy(void *data)
{
    if (data) {
        kaa_string_t* str = (kaa_string_t*)data;
        if (str->data && str->destroy) {
            str->destroy(str->data);
        }
        KAA_FREE(str);
    }
}

kaa_string_t* kaa_string_deserialize(avro_reader_t reader)
{
    kaa_string_t* str = (kaa_string_t*)KAA_MALLOC(sizeof(kaa_string_t));
    if (str) {
        avro_binary_encoding.read_string(reader, &str->data, NULL);
        str->destroy = kaa_data_destroy;
    }
    return str;
}

size_t kaa_string_get_size(void *data)
{
    if (data) {
        kaa_string_t* str = (kaa_string_t*)data;
        if (str->data) {
            size_t len = strlen(str->data);
            return kaa_long_get_size(len) + len;
        }
    }
    return 0;
}

kaa_bytes_t* kaa_bytes_move_create(const uint8_t* data, size_t data_len, destroy_fn destroy)
{
    kaa_bytes_t* bytes_array = NULL;
    if (data && data_len > 0) {
        bytes_array = (kaa_bytes_t*)KAA_MALLOC(sizeof(kaa_bytes_t));
        KAA_RETURN_IF_NIL(bytes_array, NULL);

        bytes_array->buffer = (uint8_t*)data;
        bytes_array->size = data_len;
        bytes_array->destroy = destroy;
    }

    return bytes_array;
}

kaa_bytes_t* kaa_bytes_copy_create(const uint8_t* data, size_t data_len)
{
    kaa_bytes_t* bytes_array = NULL;
    if (data && data_len > 0) {
        bytes_array = (kaa_bytes_t*)KAA_MALLOC(sizeof(kaa_bytes_t));
        KAA_RETURN_IF_NIL(bytes_array, NULL);

        bytes_array->buffer = (uint8_t*)KAA_MALLOC(sizeof(uint8_t) * data_len);
        if (!bytes_array->buffer) {
            KAA_FREE(bytes_array);
            return NULL;
        }

        memcpy(bytes_array->buffer, data, data_len);
        bytes_array->size = data_len;
        bytes_array->destroy = &kaa_data_destroy;
    }

    return bytes_array;
}

void kaa_bytes_destroy(void *data)
{
    if (data) {
        kaa_bytes_t* bytes = (kaa_bytes_t*)data;
        if (bytes->buffer && bytes->destroy) {
            bytes->destroy(bytes->buffer);
        }
        KAA_FREE(bytes);
    }
}

kaa_bytes_t* kaa_bytes_deserialize(avro_reader_t reader)
{
    kaa_bytes_t* bytes = (kaa_bytes_t*)KAA_MALLOC(sizeof(kaa_bytes_t));
    if (bytes) {
        int64_t size;
        avro_binary_encoding.read_bytes(reader, (char**)&bytes->buffer, &size);
        bytes->size = size;
        bytes->destroy = kaa_data_destroy;
    }
    return bytes;
}

void kaa_bytes_serialize(avro_writer_t writer, void* data)
{
    if (data) {
        kaa_bytes_t* bytes = (kaa_bytes_t*)data;
        if (bytes->buffer && bytes->size > 0) {
            avro_binary_encoding.write_bytes(writer, (char*)bytes->buffer, bytes->size);
        }
    }
}

size_t kaa_bytes_get_size(void *data)
{
    if (data) {
        kaa_bytes_t* bytes = (kaa_bytes_t*)data;
        if (bytes->buffer && bytes->size > 0) {
            return kaa_long_get_size(bytes->size) + bytes->size;
        }
    }
    return 0;
}

void kaa_boolean_serialize(avro_writer_t writer, void* data)
{
    if (data) {
        int8_t* val = (int8_t*)data;
        avro_binary_encoding.write_boolean(writer, *val);
    }
}

int8_t* kaa_boolean_deserialize(avro_reader_t reader)
{
    int8_t* data = (int8_t*)KAA_MALLOC(sizeof(int8_t));
    if (data) {
        avro_binary_encoding.read_boolean(reader, data);
    }
    return data;
}

void kaa_int_serialize(avro_writer_t writer, void* data)
{
    if (data) {
        int32_t* val = (int32_t*)data;
        avro_binary_encoding.write_int(writer, *val);
    }
}

int32_t* kaa_int_deserialize(avro_reader_t reader)
{
    int32_t* data = (int32_t*)KAA_MALLOC(sizeof(int32_t));
    if (data) {
        avro_binary_encoding.read_int(reader, data);
    }
    return data;
}

void kaa_long_serialize(avro_writer_t writer, void* data)
{
    if (data) {
        int64_t* val = (int64_t*)data;
        avro_binary_encoding.write_long(writer, *val);
    }
}

int64_t* kaa_long_deserialize(avro_reader_t reader)
{
    int64_t* data = (int64_t*)KAA_MALLOC(sizeof(int64_t));
    if (data) {
        avro_binary_encoding.read_long(reader, data);
    }
    return data;
}

size_t kaa_long_get_size(int64_t l)
{
    int64_t len = 0;
    uint64_t n = (l << 1) ^ (l >> 63);
    while (n & ~0x7F) {
        len++;
        n >>= 7;
    }
    len++;
    return len;
}

void kaa_array_serialize(avro_writer_t writer, kaa_list_t* array, serialize_fn serialize)
{
    if (array) {
        int64_t element_count = kaa_list_get_size(array);

        if (element_count > 0) {
            avro_binary_encoding.write_long(writer, element_count);
            if (serialize) {
                while (array) {
                    serialize(writer, kaa_list_get_data(array));
                    array = kaa_list_next(array);
                }
            }
        }
    }

    avro_binary_encoding.write_long(writer, 0);
}

kaa_list_t *kaa_array_deserialize(avro_reader_t reader, deserialize_fn deserialize)
{
    kaa_list_t *array = NULL;

    if (deserialize) {
        int64_t temp;
        int64_t element_count;

        avro_binary_encoding.read_long(reader, &element_count);

        while (element_count != 0) {
            if (element_count < 0) {
                element_count *= (-1);
                avro_binary_encoding.read_long(reader, &temp);
            }

            array = kaa_list_create(deserialize(reader));

            while (--element_count > 0) {
                array = kaa_list_push_front(array, deserialize(reader));
            }

            avro_binary_encoding.read_long(reader, &element_count);
        }
    }

    return array;
}

size_t kaa_array_get_size(kaa_list_t* cursor, get_size_fn get_size)
{
    size_t array_size = 0;

    if (cursor && get_size) {
        size_t count = 0;
        while (cursor) {
            array_size += get_size(kaa_list_get_data(cursor));
            ++count;
            cursor = kaa_list_next(cursor);
        }

        array_size += kaa_long_get_size(count);
    }

    array_size += kaa_long_get_size(0);

    return array_size;
}

void kaa_null_serialize(avro_writer_t writer, void* data)
{
}

void* kaa_null_deserialize(avro_reader_t reader)
{
    return NULL;
}

void kaa_null_destroy(void *data)
{
}

size_t kaa_null_get_size()
{
    return 0;
}

void kaa_data_destroy(void *data)
{
    if (data) {
        KAA_FREE(data);
    }
}

