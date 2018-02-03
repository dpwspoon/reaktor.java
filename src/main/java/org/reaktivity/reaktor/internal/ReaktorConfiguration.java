/**
 * Copyright 2016-2017 The Reaktivity Project
 *
 * The Reaktivity Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package org.reaktivity.reaktor.internal;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import org.reaktivity.nukleus.Configuration;

public class ReaktorConfiguration extends Configuration
{
    public static final String DIRECTORY_PROPERTY_NAME = "reaktor.directory";

    public static final String STREAMS_BUFFER_CAPACITY_PROPERTY_NAME = "reaktor.streams.buffer.capacity";

    public static final String THROTTLE_BUFFER_CAPACITY_PROPERTY_NAME = "reaktor.throttle.buffer.capacity";

    public static final String COMMAND_BUFFER_CAPACITY_PROPERTY_NAME = "reaktor.command.buffer.capacity";

    public static final String RESPONSE_BUFFER_CAPACITY_PROPERTY_NAME = "reaktor.response.buffer.capacity";

    public static final String COUNTERS_BUFFER_CAPACITY_PROPERTY_NAME = "reaktor.counters.buffer.capacity";

    public static final String MEMORY_CAPACITY_PROPERTY = "reaktor.memory.capacity";

    public static final String MEMORY_BLOCK_CAPACITY_PROPERTY = "reaktor.memory.block.capacity";

    public static final String ROUTES_BUFFER_CAPACITY_PROPERTY_NAME = "reaktor.routes.buffer.capacity";

    public static final int MEMORY_CAPACITY_DEFAULT = 128 * 1024 * 1024;

    public static final int MEMORY_BLOCK_CAPACITY_DEFAULT = 8 * 1024;

    public static final int STREAMS_BUFFER_CAPACITY_DEFAULT = 1024 * 1024;

    public static final int THROTTLE_BUFFER_CAPACITY_DEFAULT = 64 * 1024;

    public static final int COMMAND_BUFFER_CAPACITY_DEFAULT = 1024 * 1024;

    public static final int RESPONSE_BUFFER_CAPACITY_DEFAULT = 1024 * 1024;

    public static final int COUNTERS_BUFFER_CAPACITY_DEFAULT = 1024 * 1024;

    public static final int ROUTES_BUFFER_CAPACITY_DEFAULT = 1024 * 1024;

    private static final int ROUTES_ENTRY_CAPACITY_DEFAULT = 1024 * 1024;

    public ReaktorConfiguration(
        Configuration config)
    {
        super(config);
    }

    public ReaktorConfiguration(
        Properties properties)
    {
        super(properties);
    }

    public final Path directory()
    {
        return Paths.get(getProperty(DIRECTORY_PROPERTY_NAME, "."));
    }

    public int memoryCapacity()
    {
        return getInteger(MEMORY_CAPACITY_PROPERTY, MEMORY_CAPACITY_DEFAULT);
    }

    public int memoryBlockCapacity()
    {
        return getInteger(MEMORY_BLOCK_CAPACITY_PROPERTY, MEMORY_BLOCK_CAPACITY_DEFAULT);
    }

    public int maximumStreamsCount()
    {
        return memoryCapacity() / memoryBlockCapacity();
    }

    public int streamsBufferCapacity()
    {
        return getInteger(STREAMS_BUFFER_CAPACITY_PROPERTY_NAME, STREAMS_BUFFER_CAPACITY_DEFAULT);
    }

    public int throttleBufferCapacity()
    {
        return getInteger(THROTTLE_BUFFER_CAPACITY_PROPERTY_NAME, THROTTLE_BUFFER_CAPACITY_DEFAULT);
    }

    public int commandBufferCapacity()
    {
        return getInteger(COMMAND_BUFFER_CAPACITY_PROPERTY_NAME, COMMAND_BUFFER_CAPACITY_DEFAULT);
    }

    public int responseBufferCapacity()
    {
        return getInteger(RESPONSE_BUFFER_CAPACITY_PROPERTY_NAME, RESPONSE_BUFFER_CAPACITY_DEFAULT);
    }

    public int routesBufferCapacity()
    {
        return getInteger(ROUTES_BUFFER_CAPACITY_PROPERTY_NAME, ROUTES_BUFFER_CAPACITY_DEFAULT);
    }

    public int counterValuesBufferCapacity()
    {
        return getInteger(COUNTERS_BUFFER_CAPACITY_PROPERTY_NAME, COUNTERS_BUFFER_CAPACITY_DEFAULT);
    }

    public int counterLabelsBufferCapacity()
    {
        return getInteger(COUNTERS_BUFFER_CAPACITY_PROPERTY_NAME, COUNTERS_BUFFER_CAPACITY_DEFAULT) * 2;
    }
}
