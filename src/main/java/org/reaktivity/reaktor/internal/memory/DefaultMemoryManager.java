package org.reaktivity.reaktor.internal.memory;

import org.agrona.BitUtil;
import org.agrona.concurrent.UnsafeBuffer;
import org.reaktivity.nukleus.buffer.MemoryManager;

// NOTE, order 0 is largest in terms of size
public class DefaultMemoryManager implements MemoryManager
{
    
    private final BtreeFlyweight bef = new BtreeFlyweight();

    private final int smallestBlockSize;
    private final int numOfOrders;
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
        this.largestBlock = largestBlock;
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
        while (currentOrder != node.order())
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
        if (node.order() != requestedOrder && node.isFull())
        {
            return -1;
        }

        int index = node.index();

        node = node.walkParent();
        while (node.isLeftFull() && node.isRightFull())
        {
            assert node.isSplit();
            node.splitAndFill();
            node = node.walkParent();
        }
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

        // TOOD we should be able to have a faster approach
        int order = 0;
        while(size > (smallestBlockSize << order))
        {
            order++;
        }
        return numOfOrders - 1 - order;
    }

}
