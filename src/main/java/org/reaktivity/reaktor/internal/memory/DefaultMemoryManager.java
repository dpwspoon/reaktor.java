package org.reaktivity.reaktor.internal.memory;

import org.agrona.BitUtil;
import org.reaktivity.nukleus.buffer.MemoryManager;
import org.reaktivity.reaktor.internal.Context;

public class DefaultMemoryManager implements MemoryManager
{
    private static final int BITS_PER_LONG = BitUtil.SIZE_OF_LONG * 8;
    private static final int BITS_PER_ENTRY = 2;

    private static final int EMPTY = 0x00;
    private static final int ALLOCATED = 0x01;
    private static final int SPLIT = 0x10;
    private static final int SPLIT_AND_FULL = 0x11;

    private final int smallestBlockSize;
    private final int largestBlockSize, totalSize;  // same fore now
    private final int numOfOrders;
    private final long[] bTree;
    private final int largetOrder;

    class BuddyEntryFlyWeight
    {
        int arrayIndex;
        int bitIndex;
    }

    public DefaultMemoryManager(Context context)
    {
        this.smallestBlockSize                          = 0x2000; // 2^13 / 8KB
        this.largestBlockSize = this.totalSize          = 0x40000000; // 2 GB
        this.numOfOrders = numOfOrders(largestBlockSize, smallestBlockSize);
        this.largetOrder = numOfOrders - 1;

        int bTreeLength = (int) ((orderLength(largetOrder << 2) * BITS_PER_ENTRY) / BITS_PER_LONG);
        this.bTree = new long[bTreeLength];
    }


    @Override
    public long acquire(int capacity)
    {
        // TODO Auto-generated method stub
        return 0;
    }


    @Override
    public void release(long offset, int capacity)
    {
        // TODO Auto-generated method stub
    }

    static int leftChild(int index)
    {
        return 2 * index + 1;
    }

    static int rightChild(int index)
    {
        return 2 * index + 2;
    }

    static int parent(int index)
    {
        return (index - 1) >> 1;
    }

    static int numOfOrders(
        long largest,
        int smallest)
    {
        int result = 0;
        while(largest >>> result != smallest)
        {
            result++;
        }
        return result + 1;
    }

    static long orderLength(int order)
    {
        return  (0x1L << order + (BITS_PER_ENTRY - 1));
    }

    static int arrayIndex(int entryIndex)
    {
        return (int) entryIndex / (BITS_PER_LONG >> (BITS_PER_ENTRY - 1));
    }

    static int bitIndex(int entryIndex)
    {
        return (entryIndex % (BITS_PER_LONG >> (BITS_PER_ENTRY - 1))) * BITS_PER_ENTRY;
    }
}
