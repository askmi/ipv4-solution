package com.solution;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.BDDAssertions.then;

class SimpleCounterTest {

    @Test
    void testCount() throws IOException {
        // file located in root project folder
        var counter = new SimpleCounter(Path.of(".").resolve("ip_address.txt"), new Ipv4Bitmap());

        long actual = counter.count(1024);

        then(actual).isEqualTo(6);
    }
}