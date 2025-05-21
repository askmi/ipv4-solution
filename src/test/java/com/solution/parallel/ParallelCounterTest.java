package com.solution.parallel;

import com.solution.Ipv4Bitmap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.catchException;
import static org.assertj.core.api.BDDAssertions.then;

class ParallelCounterTest {

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4})
    void shouldCount(int cpu) throws Exception {
        // file located in root project folder
        var counter = new ParallelCounter(Path.of(".").resolve("ip_address.txt"), cpu, new Ipv4Bitmap());

        long actual = counter.count(1024, new Partitioner(counter.path(), cpu, 10, '\n'));

        then(actual).isEqualTo(6);
    }

    @Test
    void shouldThrowExWhenFileSizeTooSmall() throws Exception {
        // file located in root project folder
        var counter = new ParallelCounter(Path.of(".").resolve("ip_address.txt"), 1000, new Ipv4Bitmap());

        var actual = catchException(() -> counter.count(1024));

        then(actual).isInstanceOf(IllegalArgumentException.class)
            .hasMessage("small fileSize 0kb, use parallel counter for files larger then 1048576kb");
    }

    @Test
    void shouldThrowExWhenPartitionsNumberTooLarge() throws Exception {
        // file located in root project folder
        var counter = new ParallelCounter(Path.of(".").resolve("ip_address.txt"), 1000, new Ipv4Bitmap());

        var actual = catchException(() -> counter.count(1024, new Partitioner(counter.path(), 1000, 10, '\n')));

        then(actual).isInstanceOf(IllegalArgumentException.class)
            .hasMessage("too many partitions, try n < 1000");
    }
}