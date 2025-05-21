package com.solution.parallel;

import java.io.IOException;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import static java.lang.System.out;

/**
 * Split file into equal parts.
 */
record Partitioner(Path path, int nPartitions, long minFileSizeBytes, char separator) {

    private static final long MIN_FILE_SIZE_BYTES = 1 * 1024 * 1024 * 1024; // 1GB
    private static final sun.misc.Unsafe UNSAFE = initUnsafe();

    Partitioner {
        if (nPartitions <= 0) {
            throw new IllegalArgumentException("nPartitions must be > 0");
        }
        if (minFileSizeBytes <= 8) {
            throw new IllegalArgumentException("minFileSizeBytes must be >= 8");
        }
    }

    Partitioner(Path path, int nPartitions) {
        this(path, nPartitions, MIN_FILE_SIZE_BYTES, '\n');
    }

    // return offsets
    long[] partitions() throws IOException {
        out.println("Partitioning: " + path);
        long[] offsets = new long[nPartitions + 1];
        try (var fileChannel = FileChannel.open(path, StandardOpenOption.READ)) {
            long fileSize = fileChannel.size();
            MemorySegment memory = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileSize, Arena.global());
            long address = memory.address();
            out.println("addresses: " + address + ".." + (address + fileSize)
                + ", " + fileSize / MIN_FILE_SIZE_BYTES + "GB"); // roughly

            if (fileSize < minFileSizeBytes) {
                throw new IllegalArgumentException("small fileSize " + fileSize / 1024
                    + "kb, use parallel counter for files larger then " + minFileSizeBytes / 1024 + "kb");
            }
            long partitionSize = fileSize / nPartitions;
            if (partitionSize < 8) {
                throw new IllegalArgumentException("too many partitions, try n < " + nPartitions);
            }
            long start = address;
            for (int i = 0; i < nPartitions; i++) {
                boolean isNotLast = i + 1 < nPartitions;
                long end = isNotLast ? start + partitionSize : address + fileSize;
                while (start > address && start < address + fileSize && UNSAFE.getByte(start - 1) != separator) start++;
                while (end < address + fileSize && UNSAFE.getByte(end - 1) != separator) end++;
                if (start > address + fileSize || end > address + fileSize) {
                    throw new IllegalArgumentException("too many partitions, try n < " + nPartitions);
                }
                out.println("partition#" + i + ": " + start + ".." + end
                    + ", " + (end - start) / (long) 1E6 + "Mb"); // roughly

                offsets[i] = start - address;
                start = end;
            }
            offsets[nPartitions] = start + fileSize;
        }
        return offsets;
    }

    private static sun.misc.Unsafe initUnsafe() {
        try {
            java.lang.reflect.Field theUnsafe = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            return (sun.misc.Unsafe) theUnsafe.get(sun.misc.Unsafe.class);
        } catch (Exception e) {
            return null;
        }
    }
}
