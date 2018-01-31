package org.reaktivity.reaktor.internal.memory;

import java.nio.ByteBuffer;

import org.agrona.BitUtil;
import org.agrona.concurrent.UnsafeBuffer;
import org.reaktivity.nukleus.buffer.MemoryManager;
import org.reaktivity.reaktor.internal.layouts.Layout;

// NOTE, order 0 is largest in terms of size
public class DefaultMemoryManager extends Layout implements MemoryManager
{
    private static final int SIZE_OF_LOCK = BitUtil.SIZE_OF_INT;
    private final BtreeFlyweight bef = new BtreeFlyweight(this);

    static final int BITS_PER_LONG = BitUtil.SIZE_OF_LONG * 8;
    private static final int BITS_PER_ENTRY = 2;

    private static final int EMPTY = 0x00;
    private static final int ALLOCATED = 0x01;
    private static final int SPLIT = 0x02;
    private static final int SPLIT_AND_FULL = 0x3;

    enum NodeType
    {
        EMPTY,
        ALLOCATED,
        SPLIT,
        SPLIT_AND_FULL;

        public static NodeType valueOf(byte b)
        {
            switch(b)
            {
                case (byte) 0x00:
                    return EMPTY;
                case (byte) 0x01:
                    return ALLOCATED;
                case (byte) 0x02:
                    return SPLIT;
                case (byte) 0x03:
                    return SPLIT_AND_FULL;
            }
            throw new RuntimeException("Invalid Parameter");
        }
    }

    private final int smallestBlockSize;
    private final int numOfOrders;
    private final long[] bTree;
    private final int largetOrder;
    private final int largestBlock;

    private final UnsafeBuffer buffer;

    protected DefaultMemoryManager(
        UnsafeBuffer buffer,
        int memoryOffset,
        int lockOffset,
        int bTreeOffset,
        int numOfOrders,
        int smallestBlockSize,
        int largestBlock)
    {
        this.buffer = buffer;
        this.smallestBlockSize = smallestBlockSize;
        this.numOfOrders = numOfOrders;
        this.largetOrder = numOfOrders - 1;
        this.largestBlock = largestBlock;

        int bTreeLength = bTreeLength(largetOrder);
        this.bTree = new long[bTreeLength];
    }


    private static int bTreeLength(int largetOrder)
    {
        return (int) ((orderLength(largetOrder << 2) * BITS_PER_ENTRY) / BITS_PER_LONG);
    }


    @Override
    public long acquire(int capacity)
    {
        if (capacity > this.largestBlock)
        {
            return -1;
        }
        int requestedOrder = calculateOrder(capacity);

        int currentOrder = 0;
        BtreeFlyweight node = root();
        while(currentOrder != node.order() || !node.isFull())
        {
            if (node.isEmpty())
            {
                node.split();
            }
            if(node.walkLeftChild().isFull())
            {
                node.walkParent().walkRightChild();
            }
        }
        if (node.order() != requestedOrder && node.isFull())
        {
            return -1;
        }
        node.allocate();
        node.getAddress();
        // TODO search optimization on full
        return 0;
    }

    public BtreeFlyweight root()
    {
        return bef.wrap(buffer, 0);
    }

    @Override
    public void release(long offset, int capacity)
    {
        // TODO Auto-generated method stub
    }

    private int calculateOrder(
        int size)
    {
        // TODOBitUtil.findNextPositivePowerOfTwo(value);
        
        // TOOD we should be able to hav a faster approach
        int order = 0;
        while(size > (smallestBlockSize << order))
        {
            order++;
        }
        return numOfOrders - 1 - order;
    }

    @Override
    public void close()
    {
        // NOOP (not currently mapped?)
    }

//  this.smallestBlockSize                          = 0x2000; // 2^13 / 8KB
//  this.largestBlockSize = this.totalSize          = 0x40000000; // 2 GB
    public static final class Builder extends Layout.Builder<DefaultMemoryManager>
    {
        private int memoryCapacity;
        private int smallestBlockSize;

        public Builder memoryCapacity(
            int memoryCapacity)
        {
            this.memoryCapacity = memoryCapacity;
            return this;
        }

        public Builder smallestBlockSize(int smallestBlockSize)
        {
            this.smallestBlockSize = smallestBlockSize;
            return this;
        }

        @Override
        public DefaultMemoryManager build()
        {
            final int numOfOrders = numOfOrders(memoryCapacity, smallestBlockSize);
            final int capacity = memoryCapacity + SIZE_OF_LOCK + bTreeLength(numOfOrders);
            final ByteBuffer mappedMemory = ByteBuffer.allocateDirect(capacity);
            final UnsafeBuffer mutableBuffer = new UnsafeBuffer(mappedMemory);
            final int memoryOffset = 0;
            final int lockOffset = memoryCapacity;
            final int bTreeOffset = memoryOffset + lockOffset;

            return new DefaultMemoryManager(
                mutableBuffer,
                memoryOffset,
                lockOffset,
                bTreeOffset,
                numOfOrders,
                smallestBlockSize,
                memoryCapacity);
        }
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
