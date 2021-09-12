/*
 * Copyright 2015 Higher Frequency Trading http://www.higherfrequencytrading.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.hash;

import net.openhft.access.Access;
import net.openhft.access.UnsafeAccess;
import net.openhft.internal.Primitives;

import static net.openhft.internal.Maths.unsignedLongMulXorFold;

/**
 * Adapted version of XXH3 implementation from https://github.com/Cyan4973/xxHash.
 * This implementation provides endian-independent hash values, but it's slower on big-endian platforms.
 */
class XxHash3 extends HashFunction {
    static final XxHash3 INSTANCE = new XxHash3();

    private static class Secret {
        private static final Access<byte[]> unsafe = UnsafeAccess.instance();

        /*! Pseudorandom secret taken directly from FARSH. */
        private static final byte[] secret = {
                (byte)0xb8, (byte)0xfe, (byte)0x6c, (byte)0x39, (byte)0x23, (byte)0xa4, (byte)0x4b, (byte)0xbe, (byte)0x7c, (byte)0x01, (byte)0x81, (byte)0x2c, (byte)0xf7, (byte)0x21, (byte)0xad, (byte)0x1c,
                (byte)0xde, (byte)0xd4, (byte)0x6d, (byte)0xe9, (byte)0x83, (byte)0x90, (byte)0x97, (byte)0xdb, (byte)0x72, (byte)0x40, (byte)0xa4, (byte)0xa4, (byte)0xb7, (byte)0xb3, (byte)0x67, (byte)0x1f,
                (byte)0xcb, (byte)0x79, (byte)0xe6, (byte)0x4e, (byte)0xcc, (byte)0xc0, (byte)0xe5, (byte)0x78, (byte)0x82, (byte)0x5a, (byte)0xd0, (byte)0x7d, (byte)0xcc, (byte)0xff, (byte)0x72, (byte)0x21,
                (byte)0xb8, (byte)0x08, (byte)0x46, (byte)0x74, (byte)0xf7, (byte)0x43, (byte)0x24, (byte)0x8e, (byte)0xe0, (byte)0x35, (byte)0x90, (byte)0xe6, (byte)0x81, (byte)0x3a, (byte)0x26, (byte)0x4c,
                (byte)0x3c, (byte)0x28, (byte)0x52, (byte)0xbb, (byte)0x91, (byte)0xc3, (byte)0x00, (byte)0xcb, (byte)0x88, (byte)0xd0, (byte)0x65, (byte)0x8b, (byte)0x1b, (byte)0x53, (byte)0x2e, (byte)0xa3,
                (byte)0x71, (byte)0x64, (byte)0x48, (byte)0x97, (byte)0xa2, (byte)0x0d, (byte)0xf9, (byte)0x4e, (byte)0x38, (byte)0x19, (byte)0xef, (byte)0x46, (byte)0xa9, (byte)0xde, (byte)0xac, (byte)0xd8,
                (byte)0xa8, (byte)0xfa, (byte)0x76, (byte)0x3f, (byte)0xe3, (byte)0x9c, (byte)0x34, (byte)0x3f, (byte)0xf9, (byte)0xdc, (byte)0xbb, (byte)0xc7, (byte)0xc7, (byte)0x0b, (byte)0x4f, (byte)0x1d,
                (byte)0x8a, (byte)0x51, (byte)0xe0, (byte)0x4b, (byte)0xcd, (byte)0xb4, (byte)0x59, (byte)0x31, (byte)0xc8, (byte)0x9f, (byte)0x7e, (byte)0xc9, (byte)0xd9, (byte)0x78, (byte)0x73, (byte)0x64,
                (byte)0xea, (byte)0xc5, (byte)0xac, (byte)0x83, (byte)0x34, (byte)0xd3, (byte)0xeb, (byte)0xc3, (byte)0xc5, (byte)0x81, (byte)0xa0, (byte)0xff, (byte)0xfa, (byte)0x13, (byte)0x63, (byte)0xeb,
                (byte)0x17, (byte)0x0d, (byte)0xdd, (byte)0x51, (byte)0xb7, (byte)0xf0, (byte)0xda, (byte)0x49, (byte)0xd3, (byte)0x16, (byte)0x55, (byte)0x26, (byte)0x29, (byte)0xd4, (byte)0x68, (byte)0x9e,
                (byte)0x2b, (byte)0x16, (byte)0xbe, (byte)0x58, (byte)0x7d, (byte)0x47, (byte)0xa1, (byte)0xfc, (byte)0x8f, (byte)0xf8, (byte)0xb8, (byte)0xd1, (byte)0x7a, (byte)0xd0, (byte)0x31, (byte)0xce,
                (byte)0x45, (byte)0xcb, (byte)0x3a, (byte)0x8f, (byte)0x95, (byte)0x16, (byte)0x04, (byte)0x28, (byte)0xaf, (byte)0xd7, (byte)0xfb, (byte)0xca, (byte)0xbb, (byte)0x4b, (byte)0x40, (byte)0x7e,
        };

        static long i64(final long offset) { return unsafe.i64(secret, offset); }
        static  int i32(final long offset) { return unsafe.i32(secret, offset); }
    }

    // Primes
    private static final long XXH_PRIME32_1 = 0x9E3779B1L;   /*!< 0b10011110001101110111100110110001 */
    private static final long XXH_PRIME32_2 = 0x85EBCA77L;   /*!< 0b10000101111010111100101001110111 */
    private static final long XXH_PRIME32_3 = 0xC2B2AE3DL;   /*!< 0b11000010101100101010111000111101 */

    private static final long XXH_PRIME64_1 = 0x9E3779B185EBCA87L;   /*!< 0b1001111000110111011110011011000110000101111010111100101010000111 */
    private static final long XXH_PRIME64_2 = 0xC2B2AE3D27D4EB4FL;   /*!< 0b1100001010110010101011100011110100100111110101001110101101001111 */
    private static final long XXH_PRIME64_3 = 0x165667B19E3779F9L;   /*!< 0b0001011001010110011001111011000110011110001101110111100111111001 */
    private static final long XXH_PRIME64_4 = 0x85EBCA77C2B2AE63L;   /*!< 0b1000010111101011110010100111011111000010101100101010111001100011 */
    private static final long XXH_PRIME64_5 = 0x27D4EB2F165667C5L;   /*!< 0b0010011111010100111010110010111100010110010101100110011111000101 */

    // only support fixed size secret
    private static final long nbStripesPerBlock = (192 - 64) / 8;
    private static final long block_len = 64 * nbStripesPerBlock;

    @Override
    public <T> long hash(final T input, final Access<T> access, final long off, final long length) {
        if (length <= 16) {
            // len_0to16_64b
            if (length > 8) {
                // len_9to16_64b
                final long bitflip1 = Secret.i64(24) ^ Secret.i64(32);
                final long bitflip2 = Secret.i64(40) ^ Secret.i64(48);
                final long input_lo = access.i64(input, off) ^ bitflip1;
                final long input_hi = access.i64(input, off + length - 8) ^ bitflip2;
                final long acc = length + Long.reverseBytes(input_lo) + input_hi + unsignedLongMulXorFold(input_lo, input_hi);
                return avalanche(acc);
            }
            if (length >= 4) {
                // len_4to8_64b
                long s = Long.reverseBytes(0L);
                final long input1 = access.i32(input, off); // high int will be shifted
                final long input2 = access.u32(input, off + length - 4);
                final long bitflip = (Secret.i64(8) ^ Secret.i64(16)) - s;
                final long keyed = (input2 + (input1 << 32)) ^ bitflip;
                return rrmxmx(keyed, length);
            }
            if (length != 0) {
                // len_1to3_64b
                final int c1 = access.u8(input, off);
                final int c2 = access.i8(input, off + (length >> 1)); // high 3 bytes will be shifted
                final int c3 = access.u8(input, off + length - 1);
                final long combined = Primitives.unsignedInt((c1 << 16) | (c2  << 24) | c3 | ((int)length << 8));
                final long bitflip = Primitives.unsignedInt(Secret.i32(0) ^ Secret.i32(4));
                return XXH64_avalanche(combined ^ bitflip);
            }
            return XXH64_avalanche(Secret.i64(56) ^ Secret.i64(64));
        }
        if (length <= 128) {
            // len_17to128_64b
            long acc = length * XXH_PRIME64_1;

            if (length > 32) {
                if (length > 64) {
                    if (length > 96) {
                        acc += mix16B(input, access, off + 48, 96);
                        acc += mix16B(input, access, off + length - 64, 112);
                    }
                    acc += mix16B(input, access, off + 32, 64);
                    acc += mix16B(input, access, off + length - 48, 80);
                }
                acc += mix16B(input, access, off + 16, 32);
                acc += mix16B(input, access, off + length - 32, 48);
            }
            acc += mix16B(input, access, off, 0);
            acc += mix16B(input, access, off + length - 16, 16);

            return avalanche(acc);
        }
        if (length <= 240) {
            // len_129to240_64b
            long acc = length * XXH_PRIME64_1;
            final int nbRounds = (int)length / 16;
            int i = 0;
            for (; i < 8; ++i) {
                acc += mix16B(input, access, off + 16L*i, 16L*i);
            }
            acc = avalanche(acc);

            for (; i < nbRounds; ++i) {
                acc += mix16B(input, access, off + 16L*i, 16L*(i-8) + 3);
            }

            /* last bytes */
            acc += mix16B(input, access, off + length - 16, 136 - 17);
            return avalanche(acc);
        }

        // hashLong_64b_internal
        long acc_0 = XXH_PRIME32_3;
        long acc_1 = XXH_PRIME64_1;
        long acc_2 = XXH_PRIME64_2;
        long acc_3 = XXH_PRIME64_3;
        long acc_4 = XXH_PRIME64_4;
        long acc_5 = XXH_PRIME32_2;
        long acc_6 = XXH_PRIME64_5;
        long acc_7 = XXH_PRIME32_1;

        // hashLong_internal_loop
        final long nb_blocks = (length - 1) / block_len;
        for (long n = 0; n < nb_blocks; n++) {
            // accumulate
            final long offBlock = off + n * block_len;
            for (long s = 0; s < nbStripesPerBlock; s++ ) {
                // accumulate_512
                final long offStripe = offBlock + s * 64;
                final long offSec = s * 8;
                {
                    final long data_val_0 = access.i64(input, offStripe);
                    final long data_val_1 = access.i64(input, offStripe + 8);
                    final long data_key_0 = data_val_0 ^ Secret.i64(offSec);
                    final long data_key_1 = data_val_1 ^ Secret.i64(offSec + 8);
                    /* swap adjacent lanes */
                    acc_0 += data_val_1 + (0xFFFFFFFFL & data_key_0) * (data_key_0 >>> 32);
                    acc_1 += data_val_0 + (0xFFFFFFFFL & data_key_1) * (data_key_1 >>> 32);
                }
                {
                    final long data_val_0 = access.i64(input, offStripe + 8*2);
                    final long data_val_1 = access.i64(input, offStripe + 8*3);
                    final long data_key_0 = data_val_0 ^ Secret.i64(offSec + 8*2);
                    final long data_key_1 = data_val_1 ^ Secret.i64(offSec + 8*3);
                    /* swap adjacent lanes */
                    acc_2 += data_val_1 + (0xFFFFFFFFL & data_key_0) * (data_key_0 >>> 32);
                    acc_3 += data_val_0 + (0xFFFFFFFFL & data_key_1) * (data_key_1 >>> 32);
                }
                {
                    final long data_val_0 = access.i64(input, offStripe + 8*4);
                    final long data_val_1 = access.i64(input, offStripe + 8*5);
                    final long data_key_0 = data_val_0 ^ Secret.i64(offSec + 8*4);
                    final long data_key_1 = data_val_1 ^ Secret.i64(offSec + 8*5);
                    /* swap adjacent lanes */
                    acc_4 += data_val_1 + (0xFFFFFFFFL & data_key_0) * (data_key_0 >>> 32);
                    acc_5 += data_val_0 + (0xFFFFFFFFL & data_key_1) * (data_key_1 >>> 32);
                }
                {
                    final long data_val_0 = access.i64(input, offStripe + 8*6);
                    final long data_val_1 = access.i64(input, offStripe + 8*7);
                    final long data_key_0 = data_val_0 ^ Secret.i64(offSec + 8*6);
                    final long data_key_1 = data_val_1 ^ Secret.i64(offSec + 8*7);
                    /* swap adjacent lanes */
                    acc_6 += data_val_1 + (0xFFFFFFFFL & data_key_0) * (data_key_0 >>> 32);
                    acc_7 += data_val_0 + (0xFFFFFFFFL & data_key_1) * (data_key_1 >>> 32);
                }
            }

            // scrambleAcc_scalar
            final long offSec = 192 - 64;
            acc_0 = (acc_0 ^ (acc_0 >>> 47) ^ Secret.i64(offSec)) * XXH_PRIME32_1;
            acc_1 = (acc_1 ^ (acc_1 >>> 47) ^ Secret.i64(offSec + 8)) * XXH_PRIME32_1;
            acc_2 = (acc_2 ^ (acc_2 >>> 47) ^ Secret.i64(offSec + 8*2)) * XXH_PRIME32_1;
            acc_3 = (acc_3 ^ (acc_3 >>> 47) ^ Secret.i64(offSec + 8*3)) * XXH_PRIME32_1;
            acc_4 = (acc_4 ^ (acc_4 >>> 47) ^ Secret.i64(offSec + 8*4)) * XXH_PRIME32_1;
            acc_5 = (acc_5 ^ (acc_5 >>> 47) ^ Secret.i64(offSec + 8*5)) * XXH_PRIME32_1;
            acc_6 = (acc_6 ^ (acc_6 >>> 47) ^ Secret.i64(offSec + 8*6)) * XXH_PRIME32_1;
            acc_7 = (acc_7 ^ (acc_7 >>> 47) ^ Secret.i64(offSec + 8*7)) * XXH_PRIME32_1;
        }

        /* last partial block */
        final long nbStripes = ((length - 1) - (block_len * nb_blocks)) / 64;
        final long offBlock = off + block_len * nb_blocks;
        for (long s = 0; s < nbStripes; s++) {
            // accumulate_512
            final long offStripe = offBlock + s * 64;
            final long offSec = s * 8;
            {
                final long data_val_0 = access.i64(input, offStripe);
                final long data_val_1 = access.i64(input, offStripe + 8);
                final long data_key_0 = data_val_0 ^ Secret.i64(offSec);
                final long data_key_1 = data_val_1 ^ Secret.i64(offSec + 8);
                /* swap adjacent lanes */
                acc_0 += data_val_1 + (0xFFFFFFFFL & data_key_0) * (data_key_0 >>> 32);
                acc_1 += data_val_0 + (0xFFFFFFFFL & data_key_1) * (data_key_1 >>> 32);
            }
            {
                final long data_val_0 = access.i64(input, offStripe + 8*2);
                final long data_val_1 = access.i64(input, offStripe + 8*3);
                final long data_key_0 = data_val_0 ^ Secret.i64(offSec + 8*2);
                final long data_key_1 = data_val_1 ^ Secret.i64(offSec + 8*3);
                /* swap adjacent lanes */
                acc_2 += data_val_1 + (0xFFFFFFFFL & data_key_0) * (data_key_0 >>> 32);
                acc_3 += data_val_0 + (0xFFFFFFFFL & data_key_1) * (data_key_1 >>> 32);
            }
            {
                final long data_val_0 = access.i64(input, offStripe + 8*4);
                final long data_val_1 = access.i64(input, offStripe + 8*5);
                final long data_key_0 = data_val_0 ^ Secret.i64(offSec + 8*4);
                final long data_key_1 = data_val_1 ^ Secret.i64(offSec + 8*5);
                /* swap adjacent lanes */
                acc_4 += data_val_1 + (0xFFFFFFFFL & data_key_0) * (data_key_0 >>> 32);
                acc_5 += data_val_0 + (0xFFFFFFFFL & data_key_1) * (data_key_1 >>> 32);
            }
            {
                final long data_val_0 = access.i64(input, offStripe + 8*6);
                final long data_val_1 = access.i64(input, offStripe + 8*7);
                final long data_key_0 = data_val_0 ^ Secret.i64(offSec + 8*6);
                final long data_key_1 = data_val_1 ^ Secret.i64(offSec + 8*7);
                /* swap adjacent lanes */
                acc_6 += data_val_1 + (0xFFFFFFFFL & data_key_0) * (data_key_0 >>> 32);
                acc_7 += data_val_0 + (0xFFFFFFFFL & data_key_1) * (data_key_1 >>> 32);
            }
        }

        /* last stripe */
        // accumulate_512
        final long offStripe = off + length - 64;
        final long offSec = 192 - 64 - 7;
        {
            final long data_val_0 = access.i64(input, offStripe);
            final long data_val_1 = access.i64(input, offStripe + 8);
            final long data_key_0 = data_val_0 ^ Secret.i64(offSec);
            final long data_key_1 = data_val_1 ^ Secret.i64(offSec + 8);
            /* swap adjacent lanes */
            acc_0 += data_val_1 + (0xFFFFFFFFL & data_key_0) * (data_key_0 >>> 32);
            acc_1 += data_val_0 + (0xFFFFFFFFL & data_key_1) * (data_key_1 >>> 32);
        }
        {
            final long data_val_0 = access.i64(input, offStripe + 8*2);
            final long data_val_1 = access.i64(input, offStripe + 8*3);
            final long data_key_0 = data_val_0 ^ Secret.i64(offSec + 8*2);
            final long data_key_1 = data_val_1 ^ Secret.i64(offSec + 8*3);
            /* swap adjacent lanes */
            acc_2 += data_val_1 + (0xFFFFFFFFL & data_key_0) * (data_key_0 >>> 32);
            acc_3 += data_val_0 + (0xFFFFFFFFL & data_key_1) * (data_key_1 >>> 32);
        }
        {
            final long data_val_0 = access.i64(input, offStripe + 8*4);
            final long data_val_1 = access.i64(input, offStripe + 8*5);
            final long data_key_0 = data_val_0 ^ Secret.i64(offSec + 8*4);
            final long data_key_1 = data_val_1 ^ Secret.i64(offSec + 8*5);
            /* swap adjacent lanes */
            acc_4 += data_val_1 + (0xFFFFFFFFL & data_key_0) * (data_key_0 >>> 32);
            acc_5 += data_val_0 + (0xFFFFFFFFL & data_key_1) * (data_key_1 >>> 32);
        }
        {
            final long data_val_0 = access.i64(input, offStripe + 8*6);
            final long data_val_1 = access.i64(input, offStripe + 8*7);
            final long data_key_0 = data_val_0 ^ Secret.i64(offSec + 8*6);
            final long data_key_1 = data_val_1 ^ Secret.i64(offSec + 8*7);
            /* swap adjacent lanes */
            acc_6 += data_val_1 + (0xFFFFFFFFL & data_key_0) * (data_key_0 >>> 32);
            acc_7 += data_val_0 + (0xFFFFFFFFL & data_key_1) * (data_key_1 >>> 32);
        }

        // mergeAccs
        final long result64 = length * XXH_PRIME64_1
                + mix2Accs(acc_0, acc_1, 11)
                + mix2Accs(acc_2, acc_3, 11 + 16)
                + mix2Accs(acc_4, acc_5, 11 + 16 * 2)
                + mix2Accs(acc_6, acc_7, 11 + 16 * 3);

        return avalanche(result64);
    }

    private static long XXH64_avalanche(long h64) {
        h64 ^= h64 >>> 33;
        h64 *= XXH_PRIME64_2;
        h64 ^= h64 >>> 29;
        h64 *= XXH_PRIME64_3;
        return h64 ^ (h64 >>> 32);
    }

    private static long avalanche(long h64) {
        h64 ^= h64 >>> 37;
        h64 *= 0x165667919E3779F9L;
        return h64 ^ (h64 >>> 32);
    }

    private static long rrmxmx(long h64, final long length) {
        h64 ^= Long.rotateLeft(h64, 49) ^ Long.rotateLeft(h64, 24);
        h64 *= 0x9FB21C651E98DF25L;
        h64 ^= (h64 >>> 35) + length;
        h64 *= 0x9FB21C651E98DF25L;
        return h64 ^ (h64 >>> 28);
    }

    private static <T> long mix16B(final T input, final Access<T> access, final long offIn, final long offSec) {
        final long input_lo = access.i64(input, offIn);
        final long input_hi = access.i64(input, offIn + 8);
        return unsignedLongMulXorFold(
                input_lo ^ Secret.i64(offSec),
                input_hi ^ Secret.i64(offSec+8)
        );
    }

    private static long mix2Accs(final long acc_lh, final long acc_rh, final long offSec) {
        return unsignedLongMulXorFold(
                acc_lh ^ Secret.i64(offSec),
                acc_rh ^ Secret.i64(offSec+8)
        );
    }
}
