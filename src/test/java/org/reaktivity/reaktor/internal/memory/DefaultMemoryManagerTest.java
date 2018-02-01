package org.reaktivity.reaktor.internal.memory;

import static org.junit.Assert.assertEquals;
import static org.reaktivity.reaktor.internal.memory.DefaultMemoryManager.sizeOfMetaData;

import java.io.File;

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
        MemoryLayout memoryLayout = new MemoryLayout.Builder()
                .capacity(1024)
                .path(new File("target/nukleus-itests/memory").toPath())
                .smallestBlockSize(8)
                .build();
        DefaultMemoryManager mm = new DefaultMemoryManager(memoryLayout);
        long largestBlock = mm.acquire(1024);
        System.out.println(largestBlock);
    }
}
