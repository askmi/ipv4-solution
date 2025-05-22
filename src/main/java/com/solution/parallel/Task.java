package com.solution.parallel;

import com.solution.Ipv4Bitmap;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

record Task(Path path, long start, long end, int bufferSize, Ipv4Bitmap bitmap) implements Runnable {

    @Override
    public void run() {
        try (InputStream in = new BufferedInputStream(new FileInputStream(path.toFile()), bufferSize)) {
            in.skipNBytes(start);
            long remaining = this.end - this.start;
            byte[] buffer = new byte[bufferSize];
            int bytesRead;
            int ipAsInt = 0, octet = 0, shift = 24;
            boolean buildingIp = false;
            while (remaining > 0 && (bytesRead = in.read(buffer)) != -1) {
                for (int i = 0; remaining > 0 && i < bytesRead; i++) {
                    byte b = buffer[i];
                    remaining--;
                    if (b >= '0' && b <= '9') {
                        octet = octet * 10 + (b - '0');
                        buildingIp = true;
                    } else if (b == '.') {
                        ipAsInt |= (octet << shift);
                        shift -= 8;
                        octet = 0;
                    } else if (b == '\n') {
                        ipAsInt |= octet;
                        bitmap.setAtomic(ipAsInt); // set bit
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
                bitmap.setAtomic(ipAsInt); // set bit
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
