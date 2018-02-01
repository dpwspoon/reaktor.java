package org.reaktivity.reaktor.internal.memory;

import java.nio.ByteBuffer;

import org.agrona.concurrent.UnsafeBuffer;
import org.reaktivity.reaktor.internal.layouts.Layout;

public class MemoryLayout extends Layout{

    @Override
    public void close()
    {
        // NOOP (not currently mapped?)
    }

    public static final class Builder extends Layout.Builder<MemoryLayout>
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
        public MemoryLayout build()
        {
//            final int numOfOrders = numOfOrders(memoryCapacity, smallestBlockSize);
//            final int capacity = memoryCapacity + SIZE_OF_LOCK + bTreeLength(numOfOrders);
//            final ByteBuffer mappedMemory = ByteBuffer.allocateDirect(capacity);
//            final UnsafeBuffer mutableBuffer = new UnsafeBuffer(mappedMemory);
//            final int memoryOffset = 0;
//            final int lockOffset = memoryCapacity;
//            final int bTreeOffset = memoryOffset + lockOffset;
//
//            return new DefaultMemoryManager(
//                mutableBuffer,
//                memoryOffset,
//                lockOffset,
//                bTreeOffset,
//                numOfOrders,
//                smallestBlockSize,
//                memoryCapacity);
        }
    }
    
//    private static int bTreeLength(int largetOrder)
//    {
//        return (int) ((orderLength(largetOrder << 2) * BITS_PER_ENTRY) / BITS_PER_LONG);
//    }
}
