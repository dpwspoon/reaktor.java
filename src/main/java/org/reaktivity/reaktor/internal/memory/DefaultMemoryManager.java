package org.reaktivity.reaktor.internal.memory;

import static org.agrona.BitUtil.findNextPositivePowerOfTwo;

import org.agrona.BitUtil;
import org.agrona.concurrent.UnsafeBuffer;
import org.reaktivity.nukleus.buffer.MemoryManager;

// NOTE, order 0 is largest in terms of size
public class DefaultMemoryManager implements MemoryManager
{

    public static final int BITS_PER_LONG = BitUtil.SIZE_OF_LONG * 8;
    public static final int BITS_PER_ENTRY = 2;
    public static final int SIZE_OF_LOCK_FIELD = BitUtil.SIZE_OF_LONG;

    private final BtreeFlyweight btree;

    private final int smallestBlock;
    private final int numOfOrders;
    private final int largestBlock;

    private final UnsafeBuffer buffer;
    private final int metaDataOffset;


    public DefaultMemoryManager(MemoryLayout memoryLayout)
    {
        this.buffer = new UnsafeBuffer(memoryLayout.memoryBuffer());
        this.metaDataOffset = memoryLayout.capacity();
        this.smallestBlock = memoryLayout.smallestBlock();
        this.largestBlock = memoryLayout.largestBlock();
        this.numOfOrders = numOfOrders(largestBlock, smallestBlock);
        this.btree = new BtreeFlyweight(largestBlock, metaDataOffset + SIZE_OF_LOCK_FIELD);
    }


    @Override
    public long acquire(int capacity)
    {
        if (capacity > this.largestBlock)
        {
            return -1;
        }
        int requestedBlockSize = calculateBlockSize(capacity);

        BtreeFlyweight node = root();
        while (requestedBlockSize != node.blockSize())
        {
            if (node.isEmpty())
            {
                node.split();
            }

            node = node.walkLeftChild();
            if(node.isFull())
            {
                node = node.walkParent().walkRightChild();
            }
            if (node.isFull())
            {
                node.walkParent().walkParent().walkRightChild();
            }
        }

        if (node.isFull())
        {
            return -1;
        }

        int index = node.index();
        node.fill();


        while (!node.isRoot() && node.isLeftFull() && node.isRightFull())
        {
            node = node.walkParent();
            assert node.isSplit();
            node.fill();
        }
        return buffer.addressOffset() + index;
    }

    public BtreeFlyweight root()
    {
        return btree.wrap(buffer, 0);
    }

    @Override
    public void release(long offset, int capacity)
    {
        // TODO Auto-generated method stub
    }

    private int calculateBlockSize(
        int size)
    {
        return findNextPositivePowerOfTwo(size);
    }

    public static int sizeOfMetaData(
            int capacity,
            int largestBlockSize,
            int smallestBlockSize)
    {
        assert capacity == largestBlockSize;
        final int bTreeLength = bTreeLength(largestBlockSize, smallestBlockSize);
        return bTreeLength + SIZE_OF_LOCK_FIELD;
    }

    private static int numOfOrders(
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

    private static int bTreeLength(
        int largestBlockSize,
        int smallestBlockSize)
    {
        int numOfOrders = numOfOrders(largestBlockSize, smallestBlockSize);
        return (int) Math.ceil(((0x01 << numOfOrders) * BITS_PER_ENTRY) / (BITS_PER_LONG * 1.0));
    }

}
