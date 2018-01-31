package org.reaktivity.reaktor.internal.memory;

import static java.lang.Long.numberOfLeadingZeros;
import static org.agrona.BitUtil.SIZE_OF_LONG;

import org.agrona.concurrent.UnsafeBuffer;
import org.reaktivity.reaktor.internal.memory.DefaultMemoryManager.NodeType;

class BtreeFlyweight
{
    private final DefaultMemoryManager BtreeNodeFlyweight;

    /**
     * @param defaultMemoryManager
     */
    BtreeFlyweight(DefaultMemoryManager defaultMemoryManager)
    {
        BtreeNodeFlyweight = defaultMemoryManager;
    }

    private int entryIndex;
    private UnsafeBuffer buffer;
    private byte value;

    BtreeFlyweight wrap(UnsafeBuffer buffer, int entryIndex)
    {
        this.entryIndex = entryIndex;
        this.buffer = buffer;
        final int arrayIndex = DefaultMemoryManager.arrayIndex(entryIndex);
        final int bitIndex = DefaultMemoryManager.bitIndex(entryIndex);
        // TODO got to byte array index
        
        // leave as long, move to value method
        this.value = (byte) ((buffer.getLong(arrayIndex * SIZE_OF_LONG) >> bitIndex) & 0x3L);
        return this;
    }

    NodeType type()
    {
        return NodeType.valueOf(value);
    }

    public BtreeFlyweight walkParent()
    {
        this.wrap(buffer, DefaultMemoryManager.parent(entryIndex));
        return this;
    }

    public BtreeFlyweight walkLeftChild()
    {
        this.wrap(buffer, DefaultMemoryManager.leftChild(entryIndex));
        return this;
    }

    public BtreeFlyweight walkRightChild()
    {
        this.wrap(buffer, DefaultMemoryManager.leftChild(entryIndex));
        return this;
    }

    public int order()
    {
        return DefaultMemoryManager.BITS_PER_LONG - numberOfLeadingZeros(entryIndex) - 1;
    }

    public boolean isFull()
    {
        return type() == NodeType.ALLOCATED || type() == NodeType.SPLIT_AND_FULL;
    }

    public boolean isEmpty()
    {
        return type() == NodeType.EMPTY;
    }

    public void split()
    {
        // DPW TODO
    }

    public void allocate()
    {
        // TODO Auto-generated method stub
    }

    public void getAddress()
    {
        // TODO Auto-generated method stub
    }
}