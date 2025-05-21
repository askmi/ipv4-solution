package com.solution;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

public class Ipv4Bitmap {
    private static final int LONG_ARRAY_SIZE = 1 << 26; // 64MB = 2^26 longs
    private final long[] array = new long[LONG_ARRAY_SIZE];
    private static final VarHandle AA = MethodHandles.arrayElementVarHandle(long[].class);

    public void set(int ip) {
        int index = ip >>> 6;         // ip / 64
        int bit = ip & 63;            // mod 64
        long mask = 1L << bit;
        array[index] |= mask;
    }

    public void setAtomic(int ip) {
        int index = ip >>> 6;         // ip / 64
        int bit = ip & 63;            // mod 64
        long mask = 1L << bit;
        while (true) {
            long expectedValue = array[index];
            long newValue = expectedValue | mask;
            if (AA.compareAndSet(array, index, expectedValue, newValue)) {
                break;
            }
        }
//        on my hardware this version is working a little bit better
//        while (true) {
//            long expectedValue = (long) AA.getAcquire(array, index);
//            long newValue = expectedValue | mask;
//            if (expectedValue == (long) AA.compareAndExchangeRelease(array, index, expectedValue, newValue)) {
//                break;
//            }
//        }
    }

    // Counts the total number of set bits (i.e., unique IPs)
    public long cardinality() {
        long count = 0;
        for (long word : array) {
            count += Long.bitCount(word);
        }
        return count;
    }
}
