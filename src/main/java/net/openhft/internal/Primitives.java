/*
 * Copyright 2014 Higher Frequency Trading http://www.higherfrequencytrading.com
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

package net.openhft.internal;

public final class Primitives {
    private Primitives() {}

    public static long unsignedInt(int i) {
        return i & 0xFFFFFFFFL;
    }

    public static int unsignedByte(int b) {
        return b & 0xFF;
    }

    /**
     * Returns a big-endian representation of {@code value} in an 8-element byte array;
     * equivalent to {@code ByteBuffer.allocate(8).putLong(value).array()}.
     * For example, the input value {@code 0x1213141516171819L} would yield the byte array
     * {@code {0x12, 0x13, 0x14, 0x15, 0x16, 0x17, 0x18, 0x19}}.
     *
     * {@see https://github.com/google/guava/blob/master/guava/src/com/google/common/primitives/Longs.java#L276}
     */
    static byte[] toByteArray(long value) {
        final byte[] result = new byte[8];
        for (int i = 7; i >= 0; i--) {
            result[i] = (byte) (value & 0xFFL);
            value >>= 8;
        }
        return result;
    }
}
