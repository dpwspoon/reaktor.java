package org.reaktivity.reaktor.internal.memory;

import static org.junit.Assert.assertEquals;
import static org.reaktivity.reaktor.internal.memory.DefaultMemoryManager.sizeOfMetaData;

import org.junit.Test;

public class DefaultMemoryManagerTest
{

    @Test
    public void shouldCalculateMetaDataSize()
    {
        assertEquals(9, sizeOfMetaData(16, 16, 4));
        assertEquals(10, sizeOfMetaData(128, 128, 4));
    }
}
