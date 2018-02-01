package org.reaktivity.reaktor.internal.memory;

import static org.junit.Assert.assertEquals;
import static org.reaktivity.reaktor.internal.memory.DefaultMemoryManager.sizeOfMetaData;

import java.io.File;

import org.agrona.concurrent.UnsafeBuffer;
import org.junit.Test;
import org.reaktivity.nukleus.buffer.MemoryManager;


public class DefaultMemoryManagerTest2
{

    // TODO RULE?
    private static MemoryManager buildMananger(
        int capacity,
        int smallestBlockSize)
    {
        MemoryLayout memoryLayout = new MemoryLayout.Builder()
                .capacity(capacity)
                .path(new File("target/nukleus-itests/memory").toPath())
                .smallestBlockSize(smallestBlockSize)
                .build();

        return new DefaultMemoryManager(memoryLayout);
    }

    @Test
    public void shouldCalculateMetaDataSize()
    {
        assertEquals(9, sizeOfMetaData(16, 16, 4));
        assertEquals(10, sizeOfMetaData(128, 128, 4));
    }

    @Test
    public void shouldAllocateAndReleaseLargestBlock()
    {
        int largetBlock = 1024;
        int smallestBlockSize = 8;
        MemoryManager mm = buildMananger(largetBlock, smallestBlockSize);
        UnsafeBuffer writeBuffer = new UnsafeBuffer(new byte[1]); // Better way?

        long addressOffset = mm.acquire(largetBlock);
        writeBuffer.wrap(addressOffset, largetBlock);
        long expected = 0xffffffffffffffffL;
        writeBuffer.putLong(0, expected);
        long actual = writeBuffer.getLong(0);
        assertEquals(expected, actual);

        assertEquals(-1, mm.acquire(largetBlock));
        assertEquals(-1, mm.acquire(largetBlock / 2));

        mm.release(addressOffset, largetBlock);

        addressOffset = mm.acquire(largetBlock);
        writeBuffer.wrap(addressOffset, largetBlock);
        expected = 0xffffffffffffffffL;
        writeBuffer.putLong(0, expected);
        actual = writeBuffer.getLong(0);
        assertEquals(expected, actual);
    }

    @Test
    public void shouldAllocateAndReleaseSmallestBlocks()
    {
        int largetBlock = 1024;
        int smallestBlockSize = 8;
        MemoryManager mm = buildMananger(largetBlock, smallestBlockSize);
        UnsafeBuffer writeBuffer = new UnsafeBuffer(new byte[1]); // Better way?

        long addressOffset = mm.acquire(largetBlock);
        writeBuffer.wrap(addressOffset, largetBlock);
        long expected = 0xffffffffffffffffL;
        writeBuffer.putLong(0, expected);
        long actual = writeBuffer.getLong(0);
        assertEquals(expected, actual);

        assertEquals(-1, mm.acquire(largetBlock));
        assertEquals(-1, mm.acquire(largetBlock / 2));

        mm.release(addressOffset, largetBlock);

        addressOffset = mm.acquire(largetBlock);
        writeBuffer.wrap(addressOffset, largetBlock);
        expected = 0xffffffffffffffffL;
        writeBuffer.putLong(0, expected);
        actual = writeBuffer.getLong(0);
        assertEquals(expected, actual);
    }

}
