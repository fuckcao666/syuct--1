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

#ifndef DEFAULTLOGUPLOADCONFIGURATION_HPP_
#define DEFAULTLOGUPLOADCONFIGURATION_HPP_

#include "kaa/KaaDefaults.hpp"

#ifdef KAA_USE_LOGGING

#include <cstdint>
#include "kaa/log/ILogUploadConfiguration.hpp"

namespace kaa {

class DefaultLogUploadConfiguration: public ILogUploadConfiguration {
public:
    DefaultLogUploadConfiguration()
            : blockSize_(DEFAULT_BLOCK_SIZE), maxStorageVolume_(DEFAULT_MAX_STORAGE_VOLUME),
              volumeThreshold_(DEFAULT_VOLUME_THRESHOLD), logUploadTimeout_(DEFAULT_LOG_UPLOAD_TIMEOUT)
    {
    }

    DefaultLogUploadConfiguration(std::size_t blockSize, std::size_t maxStorageVolume, std::size_t volumeThreshold,
                                  std::size_t logUploadTimeout)
            : blockSize_(blockSize), maxStorageVolume_(maxStorageVolume), volumeThreshold_(volumeThreshold),
              logUploadTimeout_(logUploadTimeout)
    {
    }

    std::size_t getBlockSize() const
    {
        return blockSize_;
    }
    std::size_t getMaxStorageVolume() const
    {
        return maxStorageVolume_;
    }
    std::size_t getVolumeThreshold() const
    {
        return volumeThreshold_;
    }
    std::size_t getLogUploadTimeout() const
    {
        return logUploadTimeout_;
    }

private:
    std::size_t blockSize_;
    std::size_t maxStorageVolume_;
    std::size_t volumeThreshold_;
    std::size_t logUploadTimeout_;

    static const std::size_t DEFAULT_BLOCK_SIZE = 8192;                               // 8 Kb
    static const std::size_t DEFAULT_MAX_STORAGE_VOLUME = 1024 * 1024;                // 1 Mb
    static const std::size_t DEFAULT_VOLUME_THRESHOLD = DEFAULT_BLOCK_SIZE * 4;       // 32 Kb
    static const std::size_t DEFAULT_LOG_UPLOAD_TIMEOUT = 120;                        // 120 sec
};

}  // namespace kaa

#endif

#endif /* DEFAULTLOGUPLOADCONFIGURATION_HPP_ */
