Problem

We want to store and check whether an IPv4 address has already been seen and we don’t want to use a HashSet or store actual values.

Main ideas:

- to use a minimum memory set a single bit per IP (bitmap) to store unique ipv4 addresses
- build bitmap from long array because in java no bit sized type
- represent ipv4 address as 32-bit integer and use it as index in bitmap

Memory Estimation:

All possible IPv4 addresses range is 32-bit integers: 2^32 = 4 294 967 296 addresses.

Java integer is a signed type which is not enough.

We will store those bits in a long[] (array of 64-bit words).

Array size: 2^32 / 64 (64 bits in 1 long) = 67 108 864 longs

Memory: 67 108 864 * 8 bytes = 536 870 912 bytes = 512MB

Total memory for all possible ipv4 is 512MB.

To run program specify arguments:

1. file path [required]
2. --parallel [optional]
3. number of cpus [optional]
   
java -jar .. /path/to/file 

You can run it in parallel to reduce processing time by using machine CPUs (by default number of threads the same as available cpus on your machine)

java -jar __ /path/to/file --parallel

java -jar __ /path/to/file --parallel 2

IMPORTNANT: parallel run required java 23 and file should be big enough (from 1GB by default).

Please dont blame me that Im using Unsafe:)

The can be done with even less memory by using HyperLogLog data structure but it has some probability and I did not include that solution but it works fine with that large file (1 000 000 000 unique ips)
