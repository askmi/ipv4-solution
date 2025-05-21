package com.solution.parallel;

import com.solution.Counter;
import com.solution.Ipv4Bitmap;

import java.nio.file.Path;

import static java.lang.System.out;

public record ParallelCounter(Path path, int cpus, Ipv4Bitmap bitmap) implements Counter {

    private static final int CPUs = Runtime.getRuntime().availableProcessors();

    public ParallelCounter {
        if (cpus < 1) {
            throw new IllegalArgumentException("cpus must be >= 1");
        }
    }

    public ParallelCounter(Path path, Ipv4Bitmap bitmap) {
        this(path, CPUs, bitmap);
    }

    public long count(int bufferSize) throws Exception {
        if (bufferSize < 1) {
            throw new IllegalArgumentException("bufferSize must be > 0");
        }
        out.println("CPUs: " + cpus);
        return count(bufferSize, new Partitioner(path, cpus));
    }

    // for tests
    long count(int bufferSize, Partitioner partitioner) throws Exception {
        long[] partitions = partitioner.partitions();
        Thread[] threads = new Thread[cpus];
        for (int i = 0; i < cpus; i++) {
            threads[i] = Thread.ofPlatform()
                .name("IpCounter#" + i)
                .start(new Task(path, partitions[i], partitions[i + 1], bufferSize, bitmap));
        }
        for (Thread thread : threads) {
            thread.join();
        }
        return bitmap.cardinality();
    }
}
