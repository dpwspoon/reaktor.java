package org.reaktivity.reaktor.internal.memory;

import static org.junit.Assert.assertEquals;
import static org.reaktivity.reaktor.internal.memory.DefaultMemoryManager.sizeOfMetaData;

import org.agrona.collections.LongArrayList;
import org.agrona.concurrent.UnsafeBuffer;
import org.junit.Rule;
import org.junit.Test;
import org.reaktivity.nukleus.buffer.MemoryManager;
import org.reaktivity.reaktor.test.ConfigureMemoryLayout;
import org.reaktivity.reaktor.test.DefaultMemoryManagerRule;


public class DefaultMemoryManagerTest
{

    private UnsafeBuffer writeBuffer = new UnsafeBuffer(new byte[1]);
    private static final int KB = 1024;
    private static final int BYTES_8 = 64;

    @Rule
    public DefaultMemoryManagerRule memoryManagerRule = new DefaultMemoryManagerRule();


    @Test
    @ConfigureMemoryLayout(capacity = KB, smallestBlockSize = BYTES_8)
    public void shouldCalculateMetaDataSize()
    {
        assertEquals(9, sizeOfMetaData(16, 16, 4));
        assertEquals(10, sizeOfMetaData(128, 128, 4));
    }

    @Test
    @ConfigureMemoryLayout(capacity = KB, smallestBlockSize = BYTES_8)
    public void shouldAllocateAndReleaseLargestBlock()
    {
        final MemoryManager mm = memoryManagerRule.memoryManager();
        final MemoryLayout layout = memoryManagerRule.layout();
        final long baseAddressOffset = layout.memoryBuffer().addressOffset();

        long addressOffset = mm.acquire(KB);
        assertEquals(baseAddressOffset, addressOffset);
        writeBuffer.wrap(addressOffset, KB);
        long expected = 0xffffffffffffffffL;
        writeBuffer.putLong(0, expected);
        long actual = writeBuffer.getLong(0);
        assertEquals(expected, actual);

        assertEquals(-1, mm.acquire(KB));
        assertEquals(-1, mm.acquire(KB / 2));

        mm.release(addressOffset, KB);

        addressOffset = mm.acquire(KB);
        writeBuffer.wrap(addressOffset, KB);
        expected = 0xffffffffffffffffL;
        writeBuffer.putLong(0, expected);
        actual = writeBuffer.getLong(0);
        assertEquals(expected, actual);
    }

    @Test
    @ConfigureMemoryLayout(capacity = KB, smallestBlockSize = BYTES_8)
    public void shouldAllocateAndReleaseSmallestBlocks()
    {
        final MemoryManager mm = memoryManagerRule.memoryManager();
        final MemoryLayout layout = memoryManagerRule.layout();
        final long baseAddressOffset = layout.memoryBuffer().addressOffset();

        LongArrayList acquiredAddresses = new LongArrayList();

        for (int i = 0; (i * BYTES_8) < KB; i++)
        {
            long addressOffset = mm.acquire(BYTES_8);
            assertEquals(baseAddressOffset + (i * BYTES_8), addressOffset);
            writeBuffer.wrap(addressOffset, BYTES_8);
            writeBuffer.putLong(0, i % BYTES_8);
            acquiredAddresses.add(addressOffset);
        }
        for (int i = 0; (i * BYTES_8) < KB; i++)
        {
            Long addressOffset = acquiredAddresses.get(i);
            writeBuffer.wrap(addressOffset, BYTES_8);
            assertEquals(i % BYTES_8, writeBuffer.getLong(0));
        }
    }

}
