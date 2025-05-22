package com.solution;

import com.solution.parallel.ParallelCounter;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.locks.LockSupport;

public class Main {
    private static final int BUFFER_SIZE = 1 * 1024 * 1024; // 1MB

    static {
        System.out.println("java version: " + Runtime.getRuntime().version());
        System.out.println("RSS in kb: \nps -o rss= -p " + ProcessHandle.current().pid());
    }

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.err.println("Expected file PATH as first argument");
            System.exit(1);
        }
        Path path = Path.of(args[0]);
        if (!Files.exists(path)) {
            System.err.println("File " + path + " does not exist");
            System.exit(1);
        }
        System.out.println("Counting: " + path);

        Counter counter = null;
        Ipv4Bitmap bitmap = new Ipv4Bitmap();
        if (args.length > 1 && args[1].equals("--parallel")) {
            Integer cpus = parseCPUs(args[2]);
            counter = cpus != null
                ? new ParallelCounter(path, cpus, bitmap)
                : new ParallelCounter(path, bitmap);
        } else {
            counter = new SimpleCounter(path, bitmap);
        }

        timer();
        long cardinality = counter.count(BUFFER_SIZE);

        System.out.println("\nUnique IPs: " + cardinality);
        System.exit(0);
    }

    private static void timer() {
        Instant now = Instant.now();
        Thread.ofVirtual().start(() -> {
            while (true) {
                System.out.print("\r" + Instant.now().truncatedTo(ChronoUnit.SECONDS)
                    + ", " + Duration.between(now, Instant.now()).getSeconds() + " seconds");
                LockSupport.parkNanos(1_000_000_000L); // 1 sec
            }
        });
    }

    private static Integer parseCPUs(String cpus) {
        if (cpus == null) {
            return null;
        }
        try {
            return Integer.parseInt(cpus);
        } catch (Exception e) {
            return null;
        }
    }

}
