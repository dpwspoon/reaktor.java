package org.reaktivity.reaktor.internal.memory;

import static org.junit.Assert.assertEquals;
import static org.reaktivity.reaktor.internal.memory.DefaultMemoryManager.sizeOfMetaData;

import java.io.File;

import org.agrona.concurrent.UnsafeBuffer;
import org.junit.Test;

public class DefaultMemoryManagerTest
{

    @Test
    public void shouldCalculateMetaDataSize()
    {
        assertEquals(9, sizeOfMetaData(16, 16, 4));
        assertEquals(10, sizeOfMetaData(128, 128, 4));
    }

    @Test
    public void shouldAllocateMemory()
    {
        final UnsafeBuffer writeBuffer = new UnsafeBuffer(new byte[1]);
        final int capacity = 1024;
        MemoryLayout memoryLayout = new MemoryLayout.Builder()
                .capacity(capacity)
                .path(new File("target/nukleus-itests/memory").toPath())
                .smallestBlockSize(8)
                .build();

        DefaultMemoryManager mm = new DefaultMemoryManager(memoryLayout);
        long addressOffset = mm.acquire(capacity);
        writeBuffer.wrap(addressOffset, capacity);
        long expected = 0xffffffffffffffffL;
        writeBuffer.putLong(0, expected);
        long actual = writeBuffer.getLong(0);
        assertEquals(expected, actual);

        actual = memoryLayout.memoryBuffer().getLong(0);
        assertEquals(expected, actual);

        assertEquals(-1, mm.acquire(1024));
        mm.release(0, 1024);
    }
}
