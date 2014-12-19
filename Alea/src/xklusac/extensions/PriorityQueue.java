/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package xklusac.extensions;

/**
 *
 * @author Dalibor
 */
// PriorityQueue interface
//
// ******************PUBLIC OPERATIONS*********************
// Position insert( x )   --> Insert x
// Comparable deleteMin( )--> Return and remove smallest item
// Comparable findMin( )  --> Return smallest item
// boolean isEmpty( )     --> Return true if empty; else false
// void makeEmpty( )      --> Remove all items
// int size( )            --> Return size
// void decreaseKey( p, v)--> Decrease value in p to v
// ******************ERRORS********************************
// Throws UnderflowException for findMin and deleteMin when empty

/**
 * PriorityQueue interface.
 * Some priority queues may support a decreaseKey operation,
 * but this is considered an advanced operation. If so,
 * a Position is returned by insert.
 * Note that all "matching" is based on the compareTo method.
 * @author Mark Allen Weiss
 */
public interface PriorityQueue {
    /**
     * The Position interface represents a type that can
     * be used for the decreaseKey operation.
     */
    public interface Position {
        /**
         * Returns the value stored at this position.
         * @return the value stored at this position.
         */
        Comparable getValue( );
    }

    /**
     * Insert into the priority queue, maintaining heap order.
     * Duplicates are allowed.
     * @param x the item to insert.
     * @return may return a Position useful for decreaseKey.
     */
    Position insert( Comparable x );

    /**
     * Find the smallest item in the priority queue.
     * @return the smallest item.
     * @throws UnderflowException if empty.
     */
    Comparable findMin( );

    /**
     * Remove the smallest item from the priority queue.
     * @return the smallest item.
     * @throws UnderflowException if empty.
     */
    Comparable deleteMin( );

    /**
     * Test if the priority queue is logically empty.
     * @return true if empty, false otherwise.
     */
    boolean isEmpty( );

    /**
     * Make the priority queue logically empty.
     */
    void makeEmpty( );

    /**
     * Returns the size.
     * @return current size.
     */
    int size( );

    /**
     * Change the value of the item stored in the pairing heap.
     * This is considered an advanced operation and might not
     * be supported by all priority queues. A priority queue
     * will signal its intention to not support decreaseKey by
     * having insert return null consistently.
     * @param p any non-null Position returned by insert.
     * @param newVal the new value, which must be smaller
     *    than the currently stored value.
     * @throws IllegalArgumentException if p invalid.
     * @throws UnsupportedOperationException if appropriate.
     */
    void decreaseKey( Position p, Comparable newVal );
}


