package org.reaktivity.reaktor.internal.memory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.agrona.concurrent.UnsafeBuffer;
import org.junit.Test;

public class BtreeFlyweightTest
{
    private BtreeFlyweight btree = new BtreeFlyweight();

    @Test
    public void shouldWorkOnRoot()
    {
        byte[] buffer = new byte[8];
        UnsafeBuffer unsafeBuffer = new UnsafeBuffer(buffer);
        btree.wrap(unsafeBuffer, 0);
        assertEquals(0, btree.index());
        assertTrue(btree.isEmpty());
        assertFalse(btree.isSplit());
        assertFalse(btree.isFull());

        btree.fill();
        assertFalse(btree.isEmpty());
        assertFalse(btree.isSplit());
        assertTrue(btree.isFull());

        btree.empty();
        assertTrue(btree.isEmpty());
        assertFalse(btree.isSplit());
        assertFalse(btree.isFull());

        btree.split();
        assertTrue(btree.isEmpty());
        assertTrue(btree.isSplit());
        assertFalse(btree.isFull());

        btree.fill();
        assertFalse(btree.isEmpty());
        assertTrue(btree.isSplit());
        assertTrue(btree.isFull());

        BtreeFlyweight node = btree.walkLeftChild();
        assertSame(btree, node);
        assertTrue(node.isEmpty());
        assertFalse(node.isSplit());
        assertFalse(node.isFull());
        assertEquals(1, node.index());

        node.walkParent();
        assertFalse(node.isEmpty());
        assertTrue(node.isSplit());
        assertTrue(node.isFull());

        node.combine();
        assertFalse(node.isEmpty());
        assertFalse(node.isSplit());
        assertTrue(node.isFull());

        // implicit unsplit if do empty
        node.split();
        node.empty();
        assertTrue(node.isEmpty());
        assertFalse(node.isSplit());
        assertFalse(node.isFull());

        assertEquals(0, node.index());
    }

    // TODO
}
