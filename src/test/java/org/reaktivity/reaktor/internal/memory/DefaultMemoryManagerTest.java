package org.reaktivity.reaktor.internal.memory;

import static org.junit.Assert.assertEquals;
import static org.reaktivity.reaktor.internal.memory.DefaultMemoryManager.arrayIndex;
import static org.reaktivity.reaktor.internal.memory.DefaultMemoryManager.bitIndex;
import static org.reaktivity.reaktor.internal.memory.DefaultMemoryManager.leftChild;
import static org.reaktivity.reaktor.internal.memory.DefaultMemoryManager.numOfOrders;
import static org.reaktivity.reaktor.internal.memory.DefaultMemoryManager.orderLength;
import static org.reaktivity.reaktor.internal.memory.DefaultMemoryManager.parent;
import static org.reaktivity.reaktor.internal.memory.DefaultMemoryManager.rightChild;

import org.junit.Test;

public class DefaultMemoryManagerTest
{
    @Test
    public void shouldAllocateMemoryManager()
    {

    }

    @Test
    public void shouldCalculateNumOfOrders()
    {
        assertEquals(1, numOfOrders(16, 16));
        assertEquals(5, numOfOrders(16, 1));
        assertEquals(5, numOfOrders(16, 1));
        assertEquals(3, numOfOrders(16, 4));
        assertEquals(4, numOfOrders(16, 2));
    }

    @Test
    public void shouldCalculateOrderBitLength()
    {
        assertEquals(2, orderLength(0));
        assertEquals(4, orderLength(1));
        assertEquals(8, orderLength(2));
        assertEquals(16, orderLength(3));
    }

    @Test
    public void shouldFindChildren()
    {
        assertEquals(1, leftChild(0));
        assertEquals(2, rightChild(0));
        assertEquals(3, leftChild(1));
        assertEquals(4, rightChild(1));
        assertEquals(7, leftChild(3));
        assertEquals(8, rightChild(3));
    }


    @Test
    public void shouldFindParent()
    {
        assertEquals(0, parent(1));
        assertEquals(0, parent(2));
        assertEquals(1, parent(3));
        assertEquals(1, parent(4));
        assertEquals(2, parent(5));
        assertEquals(2, parent(6));
        assertEquals(3, parent(7));
        assertEquals(3, parent(8));
        assertEquals(6, parent(13));
        assertEquals(6, parent(14));
    }
    @Test
    public void shouldCalculateArrayIndex()
    {
        assertEquals(0, arrayIndex(0));
        assertEquals(0, arrayIndex(1));
        assertEquals(0, arrayIndex(4));
        assertEquals(0, arrayIndex(31));
        assertEquals(1, arrayIndex(32));
        assertEquals(1, arrayIndex((32 * 2) - 1));
        assertEquals(2, arrayIndex(32 * 2));
        assertEquals(2, arrayIndex((32 * 3) - 1));
        assertEquals(3, arrayIndex((32 * 3)));
    }

    @Test
    public void shouldCalculateBitIndex()
    {
        assertEquals(0, bitIndex(0));
        assertEquals(2, bitIndex(1));
        assertEquals(8, bitIndex(4));
        assertEquals(62, bitIndex(31));
        assertEquals(0, bitIndex(32));
        assertEquals(62, bitIndex((32 * 2) - 1));
        assertEquals(0, bitIndex(32 * 2));
        assertEquals(62, bitIndex((32 * 3) - 1));
        assertEquals(0, bitIndex((32 * 3)));
    }
}
