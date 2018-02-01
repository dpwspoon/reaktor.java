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

    private final BtreeFlyweight btreeRO;

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
        this.btreeRO = new BtreeFlyweight(largestBlock, smallestBlock, metaDataOffset + SIZE_OF_LOCK_FIELD);
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
        while (!(requestedBlockSize == node.blockSize() && node.isFree()))
        {
            if (requestedBlockSize > node.blockSize() || node.isFull())
            {
                while(node.isRightChild())
                {
                    node = node.walkParent();
                }
                if(node.isLeftChild())
                {
                    node = node.walkParent();
                    node = node.walkRightChild();  // TODO optimize
                }
                else
                {
                    break; // you are root
                }
            }
            else
            {
                node = node.walkLeftChild();
            }
        }

        if (node.isFull())
        {
            return -1;
        }

        node.fill();

        int indexInOrder = (node.index() + 1) % (2 << node.order()); // TODO move to flyweight
        int memoffset = indexInOrder * node.blockSize();
        long addressOffset = buffer.addressOffset() + memoffset;

        while (!node.isRoot())
        {
            node = node.walkParent();
            // TODO optimize (can break out quick)
            if (node.isLeftFull() && node.isRightFull())
            {
                node.fill();
            }
            else
            {
                node.split();
            }
        }
        return addressOffset;
    }

    public BtreeFlyweight root()
    {
        return btreeRO.wrap(buffer, 0);
    }

    @Override
    public void release(
        long offset,
        int capacity)
    {
        offset -= buffer.addressOffset();
        int blockSize = calculateBlockSize(capacity);
        int order = calculateOrder(blockSize);
        int entryIndex = order == 0 ? 0 : (int) (offset / (order * blockSize));
        BtreeFlyweight node = btreeRO.wrap(buffer, entryIndex);
        node.empty();
        while(!node.isRoot())
        {
            node = node.walkParent();
            if(!node.isRightFullOrSplit() && !node.isLeftFullOrSplit())
            {
                node.empty();
            }
            else
            {
                node.free();
            }
        }
    }

    private int calculateOrder(int blockSize)
    {
        int order = 0;
        while (largestBlock >> order != blockSize)
        {
            order++;
        }
        return order;
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
