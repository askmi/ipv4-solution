package com.solution;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

/**
 * Simple single threaded counter.
 */
public record SimpleCounter(Path path, Ipv4Bitmap bitmap) implements Counter {

    public long count(int bufferSize) throws IOException {
        if (bufferSize < 1) {
            throw new IllegalArgumentException("bufferSize must be > 0");
        }
        try (InputStream input = new BufferedInputStream(new FileInputStream(path.toFile()), bufferSize)) {
            byte[] buffer = new byte[bufferSize];
            int bytesRead;
            int ipAsInt = 0, octet = 0, shift = 24;
            boolean buildingIp = false;
            while ((bytesRead = input.read(buffer)) != -1) {
                for (int i = 0; i < bytesRead; i++) {
                    byte b = buffer[i];
                    if (b >= '0' && b <= '9') {
                        octet = octet * 10 + (b - '0');
                        buildingIp = true;
                    } else if (b == '.') {
                        ipAsInt |= (octet << shift);
                        shift -= 8;
                        octet = 0;
                    } else if (b == '\n') {
                        ipAsInt |= octet;
                        bitmap.set(ipAsInt); // set bit
                        // Reset state
                        ipAsInt = 0;
                        octet = 0;
                        shift = 24;
                        buildingIp = false;
                    } else if (b == '\r') {
                        // ignore, for Windows line endings
                    } else {
                        // invalid character - reset
                        ipAsInt = 0;
                        octet = 0;
                        shift = 24;
                        buildingIp = false;
                        // optionally: log error or throw exception
                    }
                }
            }
            // Handle possible last line if no newline at EOF
            if (buildingIp) {
                ipAsInt |= octet;
                bitmap.set(ipAsInt);
            }
        }
        return bitmap.cardinality();
    }
}
