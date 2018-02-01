package org.reaktivity.reaktor.internal.memory;

import static java.lang.Long.numberOfLeadingZeros;
import static org.reaktivity.reaktor.internal.memory.DefaultMemoryManager.BITS_PER_ENTRY;
import static org.reaktivity.reaktor.internal.memory.DefaultMemoryManager.BITS_PER_LONG;

import org.agrona.concurrent.UnsafeBuffer;

class BtreeFlyweight
{
    static final long EMPTY = 0x00L;
    static final long FULL = 0x01L;
    static final long SPLIT = 0x02L;
    static final long UNSPLIT_MASH = FULL;

    private final int largestBlock;

    private int entryIndex;
    private UnsafeBuffer buffer;
    private int offset;

     BtreeFlyweight(
        int largestBlock,
        int offset) // TODO move offset to wrap?
    {
        this.largestBlock = largestBlock;
        this.offset = offset;
    }

    public BtreeFlyweight wrap(
        UnsafeBuffer buffer,
        int entryIndex)
    {
        this.entryIndex = entryIndex;
        this.buffer = buffer;
        return this;
    }

    private long value()
    {
        final int arrayIndex = arrayIndex();
        final int bitIndex = bitOffset();
        return ((buffer.getLong(arrayIndex) >> bitIndex) & 0x3L);
    }

    public BtreeFlyweight walkParent()
    {
        this.wrap(buffer, (entryIndex - 1) >> 1);
        return this;
    }

    public BtreeFlyweight walkLeftChild()
    {
        this.wrap(buffer, 2 * entryIndex + 1);
        return this;
    }

    public BtreeFlyweight walkRightChild()
    {
        this.wrap(buffer, 2 * entryIndex + 2);
        return this;
    }

    public int order()
    {
        return BITS_PER_LONG - numberOfLeadingZeros(entryIndex) - 1;
    }

    public boolean isFull()
    {
        return (value() & FULL) > 0;
    }

    public boolean isFree()
    {
        return isEmpty() && !isSplit();
    }

    public boolean isEmpty()
    {
        return (value() & FULL) == 0;
    }

    public boolean isSplit()
    {
        return (value() & SPLIT) > 0;
    }

    public void split()
    {
        long newValue = SPLIT << (BITS_PER_LONG - bitOffset());
        buffer.putLong(arrayIndex(), buffer.getLong(arrayIndex()) | newValue);
    }

    public void combine() //rename unsplit ?
    {
        final long newValueMask = ~(~UNSPLIT_MASH << (BITS_PER_LONG - bitOffset()));
        long newValue = buffer.getLong(arrayIndex()) & newValueMask;
        buffer.putLong(arrayIndex(), newValue);
    }

    public void free() // TODO
    {
        long newValue = ~(SPLIT << (EMPTY - bitOffset()));
        buffer.putLong(arrayIndex(), buffer.getLong(arrayIndex()) & newValue);
    }

    // WARNING, this unsplits it AND releases it, not sure if you want it.
    public void empty()
    {
        final long newValueMask = ~(~EMPTY << (BITS_PER_LONG - bitOffset()));
        long newValue = buffer.getLong(arrayIndex()) & newValueMask;
        buffer.putLong(arrayIndex(), newValue);
    }

    public void splitAndFill()
    {
        long newValue = 0xffffffffffffffffL ^ ((SPLIT | FULL) << (BITS_PER_LONG - bitOffset()));
        buffer.putLong(arrayIndex(), buffer.getLong(arrayIndex()) | newValue);
    }

    public void fill()
    {
        final long newValueMask = FULL << (BITS_PER_LONG - bitOffset());
        final long newValue = buffer.getLong(arrayIndex()) | newValueMask;
        buffer.putLong(arrayIndex(), newValue);
    }

    public boolean isLeftFull()
    {
        this.walkLeftChild();
        boolean result = isFull();
        this.walkParent();
        return result;
    }

    public boolean isRightFull()
    {
        this.walkRightChild();
        boolean result = isFull();
        this.walkParent();
        return result;
    }

    public boolean isLeftFullOrSplit()
    {
        this.walkLeftChild();
        boolean result = isFull() || isSplit();
        this.walkParent();
        return result;
    }

    public boolean isRightFullOrSplit()
    {
        this.walkRightChild();
        boolean result = isFull() || isSplit();
        this.walkParent();
        return result;
    }

    public int index()
    {
        return entryIndex;
    }

    private int arrayIndex()
    {
        return offset + (int) entryIndex / (BITS_PER_LONG >> (BITS_PER_ENTRY - 1));
    }

    private int bitOffset()
    {
        return (entryIndex % (BITS_PER_LONG >> (BITS_PER_ENTRY - 1))) * BITS_PER_ENTRY;
    }

    public int blockSize()
    {
        // TODO optimize
        int index = entryIndex;
        int size = largestBlock;
        while(index != 0)
        {
            size = size >> index;
            index--;
        }
        return size;
    }

    public boolean isRoot()
    {
        return entryIndex == 0;
    }

    public boolean isLeftChild()
    {
        return !isRoot() && entryIndex % 2 == 0;
    }

    public boolean isRightChild()
    {
        return !isRoot() && entryIndex % 2 == 1;
    }
}