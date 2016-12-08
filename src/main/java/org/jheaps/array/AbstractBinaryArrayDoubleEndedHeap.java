package org.jheaps.array;

import java.io.Serializable;
import java.util.Comparator;
import java.util.NoSuchElementException;

import org.jheaps.DoubleEndedHeap;
import org.jheaps.annotations.VisibleForTesting;

/**
 * Abstract implementation of a binary double-ended heap using an array
 * representation.
 * 
 * @author Dimitrios Michail
 *
 * @param <K>
 *            the type of keys maintained by this heap
 */
abstract class AbstractBinaryArrayDoubleEndedHeap<K> extends AbstractArrayHeap<K>
        implements DoubleEndedHeap<K>, Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor
     * 
     * @param comparator
     *            the comparator to use
     * @param capacity
     *            the requested capacity
     */
    public AbstractBinaryArrayDoubleEndedHeap(Comparator<? super K> comparator, int capacity) {
        super(comparator, capacity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public K findMax() {
        switch (size) {
        case 0:
            throw new NoSuchElementException();
        case 1:
            return array[1];
        case 2:
            return array[2];
        default:
            if (comparator == null) {
                if (((Comparable<? super K>) array[3]).compareTo(array[2]) > 0) {
                    return array[3];
                } else {
                    return array[2];
                }
            } else {
                if (comparator.compare(array[3], array[2]) > 0) {
                    return array[3];
                } else {
                    return array[2];
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public K deleteMax() {
        K result;
        switch (size) {
        case 0:
            throw new NoSuchElementException();
        case 1:
            result = array[1];
            array[1] = null;
            size--;
            break;
        case 2:
            result = array[2];
            array[2] = null;
            size--;
            break;
        default:
            result = array[2];
            if (comparator == null) {
                if (((Comparable<? super K>) array[3]).compareTo(array[2]) > 0) {
                    result = array[3];
                    array[3] = array[size];
                    array[size] = null;
                    size--;
                    if (size >= 3) {
                        fixdownMax(3);
                    }
                } else {
                    result = array[2];
                    array[2] = array[size];
                    array[size] = null;
                    size--;
                    fixdownMax(2);
                }
            } else {
                if (comparator.compare(array[3], array[2]) > 0) {
                    result = array[3];
                    array[3] = array[size];
                    array[size] = null;
                    size--;
                    if (size >= 3) {
                        fixdownMaxWithComparator(3);
                    }
                } else {
                    result = array[2];
                    array[2] = array[size];
                    array[size] = null;
                    size--;
                    fixdownMaxWithComparator(2);
                }
            }
            break;
        }

        if (DOWNSIZING_MIN_HEAP_CAPACITY < array.length - 1 && 4 * size < array.length - 1) {
            ensureCapacity((array.length - 1) / 2 + 1);
        }

        return result;
    }

    /**
     * Upwards fix starting from a particular element
     * 
     * @param k
     *            the index of the starting element
     */
    @Override
    @SuppressWarnings("unchecked")
    protected void fixup(int k) {
        if (onMinLevel(k)) {
            int p = k / 2;
            K kValue = array[k];
            if (p > 0 && ((Comparable<? super K>) array[p]).compareTo(kValue) < 0) {
                array[k] = array[p];
                array[p] = kValue;
                fixupMax(p);
            } else {
                fixupMin(k);
            }
        } else {
            int p = k / 2;
            K kValue = array[k];
            if (p > 0 && ((Comparable<? super K>) kValue).compareTo(array[p]) < 0) {
                array[k] = array[p];
                array[p] = kValue;
                fixupMin(p);
            } else {
                fixupMax(k);
            }
        }
    }

    /**
     * Upwards fix starting from a particular element
     * 
     * @param k
     *            the index of the starting element
     */
    protected void fixupWithComparator(int k) {
        if (onMinLevel(k)) {
            int p = k / 2;
            K kValue = array[k];
            if (p > 0 && comparator.compare(array[p], kValue) < 0) {
                array[k] = array[p];
                array[p] = kValue;
                fixupMaxWithComparator(p);
            } else {
                fixupMinWithComparator(k);
            }
        } else {
            int p = k / 2;
            K kValue = array[k];
            if (p > 0 && comparator.compare(kValue, array[p]) < 0) {
                array[k] = array[p];
                array[p] = kValue;
                fixupMinWithComparator(p);
            } else {
                fixupMaxWithComparator(k);
            }
        }
    }

    /**
     * Upwards fix starting from a particular element at a minimum level
     * 
     * @param k
     *            the index of the starting element
     */
    @SuppressWarnings("unchecked")
    private void fixupMin(int k) {
        K key = array[k];
        int gp = k / 4;
        while (gp > 0 && ((Comparable<? super K>) array[gp]).compareTo(key) > 0) {
            array[k] = array[gp];
            k = gp;
            gp = k / 4;
        }
        array[k] = key;
    }

    /**
     * Upwards fix starting from a particular element at a minimum level.
     * Performs comparisons using the comparator.
     * 
     * @param k
     *            the index of the starting element
     */
    private void fixupMinWithComparator(int k) {
        K key = array[k];
        int gp = k / 4;
        while (gp > 0 && comparator.compare(array[gp], key) > 0) {
            array[k] = array[gp];
            k = gp;
            gp = k / 4;
        }
        array[k] = key;
    }

    /**
     * Upwards fix starting from a particular element at a maximum level
     * 
     * @param k
     *            the index of the starting element
     */
    @SuppressWarnings("unchecked")
    private void fixupMax(int k) {
        K key = array[k];
        int gp = k / 4;
        while (gp > 0 && ((Comparable<? super K>) array[gp]).compareTo(key) < 0) {
            array[k] = array[gp];
            k = gp;
            gp = k / 4;
        }
        array[k] = key;
    }

    /**
     * Upwards fix starting from a particular element at a maximum level.
     * Performs comparisons using the comparator.
     * 
     * @param k
     *            the index of the starting element
     */
    private void fixupMaxWithComparator(int k) {
        K key = array[k];
        int gp = k / 4;
        while (gp > 0 && comparator.compare(array[gp], key) < 0) {
            array[k] = array[gp];
            k = gp;
            gp = k / 4;
        }
        array[k] = key;
    }

    /**
     * Downwards fix starting from a particular element.
     * 
     * @param k
     *            the index of the starting element
     */
    @Override
    protected void fixdown(int k) {
        if (onMinLevel(k)) {
            fixdownMin(k);
        } else {
            fixdownMax(k);
        }
    }

    /**
     * Downwards fix starting from a particular element. Performs comparisons
     * using the comparator.
     * 
     * @param k
     *            the index of the starting element
     */
    @Override
    protected void fixdownWithComparator(int k) {
        if (onMinLevel(k)) {
            fixdownMinWithComparator(k);
        } else {
            fixdownMaxWithComparator(k);
        }
    }

    /**
     * Downwards fix starting from a particular element at a minimum level.
     * 
     * @param k
     *            the index of the starting element
     */
    @SuppressWarnings("unchecked")
    private void fixdownMin(int k) {
        int c = 2 * k;
        while (c <= size) {
            int m = minChildOrGrandchild(k);
            if (m > c + 1) { // grandchild
                if (((Comparable<? super K>) array[m]).compareTo(array[k]) >= 0) {
                    break;
                }
                K tmp = array[k];
                array[k] = array[m];
                array[m] = tmp;
                if (((Comparable<? super K>) array[m]).compareTo(array[m / 2]) > 0) {
                    tmp = array[m];
                    array[m] = array[m / 2];
                    array[m / 2] = tmp;
                }
                // go down
                k = m;
                c = 2 * k;
            } else { // child
                if (((Comparable<? super K>) array[m]).compareTo(array[k]) < 0) {
                    K tmp = array[k];
                    array[k] = array[m];
                    array[m] = tmp;
                }
                break;
            }
        }
    }

    /**
     * Downwards fix starting from a particular element at a minimum level.
     * Performs comparisons using the comparator.
     * 
     * @param k
     *            the index of the starting element
     */
    private void fixdownMinWithComparator(int k) {
        int c = 2 * k;
        while (c <= size) {
            int m = minChildOrGrandchildWithComparator(k);
            if (m > c + 1) { // grandchild
                if (comparator.compare(array[m], array[k]) >= 0) {
                    break;
                }
                K tmp = array[k];
                array[k] = array[m];
                array[m] = tmp;
                if (comparator.compare(array[m], array[m / 2]) > 0) {
                    tmp = array[m];
                    array[m] = array[m / 2];
                    array[m / 2] = tmp;
                }
                // go down
                k = m;
                c = 2 * k;
            } else { // child
                if (comparator.compare(array[m], array[k]) < 0) {
                    K tmp = array[k];
                    array[k] = array[m];
                    array[m] = tmp;
                }
                break;
            }
        }
    }

    /**
     * Downwards fix starting from a particular element at a maximum level.
     * 
     * @param k
     *            the index of the starting element
     */
    @SuppressWarnings("unchecked")
    private void fixdownMax(int k) {
        int c = 2 * k;
        while (c <= size) {
            int m = maxChildOrGrandchild(k);
            if (m > c + 1) { // grandchild
                if (((Comparable<? super K>) array[m]).compareTo(array[k]) <= 0) {
                    break;
                }
                K tmp = array[k];
                array[k] = array[m];
                array[m] = tmp;
                if (((Comparable<? super K>) array[m]).compareTo(array[m / 2]) < 0) {
                    tmp = array[m];
                    array[m] = array[m / 2];
                    array[m / 2] = tmp;
                }
                // go down
                k = m;
                c = 2 * k;
            } else { // child
                if (((Comparable<? super K>) array[m]).compareTo(array[k]) > 0) {
                    K tmp = array[k];
                    array[k] = array[m];
                    array[m] = tmp;
                }
                break;
            }
        }
    }

    /**
     * Downwards fix starting from a particular element at a maximum level.
     * Performs comparisons using the comparator.
     * 
     * @param k
     *            the index of the starting element
     */
    private void fixdownMaxWithComparator(int k) {
        int c = 2 * k;
        while (c <= size) {
            int m = maxChildOrGrandchildWithComparator(k);
            if (m > c + 1) { // grandchild
                if (comparator.compare(array[m], array[k]) <= 0) {
                    break;
                }
                K tmp = array[k];
                array[k] = array[m];
                array[m] = tmp;
                if (comparator.compare(array[m], array[m / 2]) < 0) {
                    tmp = array[m];
                    array[m] = array[m / 2];
                    array[m / 2] = tmp;
                }
                // go down
                k = m;
                c = 2 * k;
            } else { // child
                if (comparator.compare(array[m], array[k]) > 0) {
                    K tmp = array[k];
                    array[k] = array[m];
                    array[m] = tmp;
                }
                break;
            }
        }
    }

    /**
     * Return true if on a minimum level, false otherwise.
     * 
     * @param k
     *            the element
     * @return true if on a minimum level, false otherwise
     */
    @VisibleForTesting
    boolean onMinLevel(int k) {
        float kAsFloat = k;
        int exponent = Math.getExponent(kAsFloat);
        return exponent % 2 == 0;
    }

    /**
     * Given a node at a maximum level, find its child or grandchild with the
     * maximum key. This method should not be called for a node which has no
     * children.
     * 
     * @param k
     *            a node at a maximum level
     * @return the child or grandchild with a maximum key, or undefined if there
     *         are no children
     */
    @SuppressWarnings("unchecked")
    private int maxChildOrGrandchild(int k) {
        int gc = 4 * k;
        int maxgc;
        K gcValue;

        // 4 grandchilden
        if (gc + 3 <= size) {
            gcValue = array[gc];
            maxgc = gc;
            if (((Comparable<? super K>) array[++gc]).compareTo(gcValue) > 0) {
                gcValue = array[gc];
                maxgc = gc;
            }
            if (((Comparable<? super K>) array[++gc]).compareTo(gcValue) > 0) {
                gcValue = array[gc];
                maxgc = gc;
            }
            if (((Comparable<? super K>) array[++gc]).compareTo(gcValue) > 0) {
                maxgc = gc;
            }
            return maxgc;
        }

        // less or equal to 3
        switch (size - gc) {
        case 2:
            // 3 grandchildren, two children
            gcValue = array[gc];
            maxgc = gc;
            if (((Comparable<? super K>) array[++gc]).compareTo(gcValue) > 0) {
                gcValue = array[gc];
                maxgc = gc;
            }
            if (((Comparable<? super K>) array[++gc]).compareTo(gcValue) > 0) {
                maxgc = gc;
            }
            return maxgc;
        case 1:
            // 2 grandchildren, maybe two children
            gcValue = array[gc];
            maxgc = gc;
            if (((Comparable<? super K>) array[++gc]).compareTo(gcValue) > 0) {
                gcValue = array[gc];
                maxgc = gc;
            }
            if (2 * k + 1 <= size && ((Comparable<? super K>) array[2 * k + 1]).compareTo(gcValue) > 0) {
                maxgc = 2 * k + 1;
            }
            return maxgc;
        case 0:
            // 1 grandchild, maybe two children
            gcValue = array[gc];
            maxgc = gc;
            if (2 * k + 1 <= size && ((Comparable<? super K>) array[2 * k + 1]).compareTo(gcValue) > 0) {
                maxgc = 2 * k + 1;
            }
            return maxgc;
        }

        // 0 grandchildren
        maxgc = 2 * k;
        gcValue = array[maxgc];
        if (2 * k + 1 <= size && ((Comparable<? super K>) array[2 * k + 1]).compareTo(gcValue) > 0) {
            maxgc = 2 * k + 1;
        }
        return maxgc;
    }

    /**
     * Given a node at a maximum level, find its child or grandchild with the
     * maximum key. This method should not be called for a node which has no
     * children.
     * 
     * @param k
     *            a node at a maximum level
     * @return the child or grandchild with a maximum key, or undefined if there
     *         are no children
     */
    private int maxChildOrGrandchildWithComparator(int k) {
        int gc = 4 * k;
        int maxgc;
        K gcValue;

        // 4 grandchilden
        if (gc + 3 <= size) {
            gcValue = array[gc];
            maxgc = gc;
            if (comparator.compare(array[++gc], gcValue) > 0) {
                gcValue = array[gc];
                maxgc = gc;
            }
            if (comparator.compare(array[++gc], gcValue) > 0) {
                gcValue = array[gc];
                maxgc = gc;
            }
            if (comparator.compare(array[++gc], gcValue) > 0) {
                maxgc = gc;
            }
            return maxgc;
        }

        // less or equal to 3
        switch (size - gc) {
        case 2:
            // 3 grandchildren, two children
            gcValue = array[gc];
            maxgc = gc;
            if (comparator.compare(array[++gc], gcValue) > 0) {
                gcValue = array[gc];
                maxgc = gc;
            }
            if (comparator.compare(array[++gc], gcValue) > 0) {
                maxgc = gc;
            }
            return maxgc;
        case 1:
            // 2 grandchildren, maybe two children
            gcValue = array[gc];
            maxgc = gc;
            if (comparator.compare(array[++gc], gcValue) > 0) {
                gcValue = array[gc];
                maxgc = gc;
            }
            if (2 * k + 1 <= size && comparator.compare(array[2 * k + 1], gcValue) > 0) {
                maxgc = 2 * k + 1;
            }
            return maxgc;
        case 0:
            // 1 grandchild, maybe two children
            gcValue = array[gc];
            maxgc = gc;
            if (2 * k + 1 <= size && comparator.compare(array[2 * k + 1], gcValue) > 0) {
                maxgc = 2 * k + 1;
            }
            return maxgc;
        }

        // 0 grandchildren
        maxgc = 2 * k;
        gcValue = array[maxgc];
        if (2 * k + 1 <= size && comparator.compare(array[2 * k + 1], gcValue) > 0) {
            maxgc = 2 * k + 1;
        }
        return maxgc;
    }

    /**
     * Given a node at a minimum level, find its child or grandchild with the
     * minimum key. This method should not be called for a node which has no
     * children.
     * 
     * @param k
     *            a node at a minimum level
     * @return the child or grandchild with a minimum key, or undefined if there
     *         are no children
     */
    @SuppressWarnings("unchecked")
    private int minChildOrGrandchild(int k) {
        int gc = 4 * k;
        int mingc;
        K gcValue;

        // 4 grandchilden
        if (gc + 3 <= size) {
            gcValue = array[gc];
            mingc = gc;
            if (((Comparable<? super K>) array[++gc]).compareTo(gcValue) < 0) {
                gcValue = array[gc];
                mingc = gc;
            }
            if (((Comparable<? super K>) array[++gc]).compareTo(gcValue) < 0) {
                gcValue = array[gc];
                mingc = gc;
            }
            if (((Comparable<? super K>) array[++gc]).compareTo(gcValue) < 0) {
                mingc = gc;
            }
            return mingc;
        }

        // less or equal to 3
        switch (size - gc) {
        case 2:
            // 3 grandchildren, two children
            gcValue = array[gc];
            mingc = gc;
            if (((Comparable<? super K>) array[++gc]).compareTo(gcValue) < 0) {
                gcValue = array[gc];
                mingc = gc;
            }
            if (((Comparable<? super K>) array[++gc]).compareTo(gcValue) < 0) {
                mingc = gc;
            }
            return mingc;
        case 1:
            // 2 grandchildren, maybe two children
            gcValue = array[gc];
            mingc = gc;
            if (((Comparable<? super K>) array[++gc]).compareTo(gcValue) < 0) {
                gcValue = array[gc];
                mingc = gc;
            }
            if (2 * k + 1 <= size && ((Comparable<? super K>) array[2 * k + 1]).compareTo(gcValue) < 0) {
                mingc = 2 * k + 1;
            }
            return mingc;
        case 0:
            // 1 grandchild, maybe two children
            gcValue = array[gc];
            mingc = gc;
            if (2 * k + 1 <= size && ((Comparable<? super K>) array[2 * k + 1]).compareTo(gcValue) < 0) {
                mingc = 2 * k + 1;
            }
            return mingc;
        }

        // 0 grandchildren
        mingc = 2 * k;
        gcValue = array[mingc];
        if (2 * k + 1 <= size && ((Comparable<? super K>) array[2 * k + 1]).compareTo(gcValue) < 0) {
            mingc = 2 * k + 1;
        }
        return mingc;
    }

    /**
     * Given a node at a minimum level, find its child or grandchild with the
     * minimum key. This method should not be called for a node which has no
     * children.
     * 
     * @param k
     *            a node at a minimum level
     * @return the child or grandchild with a minimum key, or undefined if there
     *         are no children
     */
    private int minChildOrGrandchildWithComparator(int k) {
        int gc = 4 * k;
        int mingc;
        K gcValue;

        // 4 grandchilden
        if (gc + 3 <= size) {
            gcValue = array[gc];
            mingc = gc;
            if (comparator.compare(array[++gc], gcValue) < 0) {
                gcValue = array[gc];
                mingc = gc;
            }
            if (comparator.compare(array[++gc], gcValue) < 0) {
                gcValue = array[gc];
                mingc = gc;
            }
            if (comparator.compare(array[++gc], gcValue) < 0) {
                mingc = gc;
            }
            return mingc;
        }

        // less or equal to 3
        switch (size - gc) {
        case 2:
            // 3 grandchildren, two children
            gcValue = array[gc];
            mingc = gc;
            if (comparator.compare(array[++gc], gcValue) < 0) {
                gcValue = array[gc];
                mingc = gc;
            }
            if (comparator.compare(array[++gc], gcValue) < 0) {
                mingc = gc;
            }
            return mingc;
        case 1:
            // 2 grandchildren, maybe two children
            gcValue = array[gc];
            mingc = gc;
            if (comparator.compare(array[++gc], gcValue) < 0) {
                gcValue = array[gc];
                mingc = gc;
            }
            if (2 * k + 1 <= size && comparator.compare(array[2 * k + 1], gcValue) < 0) {
                mingc = 2 * k + 1;
            }
            return mingc;
        case 0:
            // 1 grandchild, maybe two children
            gcValue = array[gc];
            mingc = gc;
            if (2 * k + 1 <= size && comparator.compare(array[2 * k + 1], gcValue) < 0) {
                mingc = 2 * k + 1;
            }
            return mingc;
        }

        // 0 grandchildren
        mingc = 2 * k;
        gcValue = array[mingc];
        if (2 * k + 1 <= size && comparator.compare(array[2 * k + 1], gcValue) < 0) {
            mingc = 2 * k + 1;
        }
        return mingc;
    }

}
