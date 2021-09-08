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

package net.openhft.access;

import org.jetbrains.annotations.NotNull;
import sun.misc.Unsafe;
import sun.nio.ch.DirectBuffer;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class UnsafeAccess extends Access<byte[]> {
    @NotNull
    private static final Unsafe UNSAFE;
    private static final long BYTE_BASE;
    @NotNull
    private static final Access<byte[]> INSTANCE;
    @NotNull
    private static final Access<byte[]> INSTANCE_REVERSE;

    static {
        try {
            Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            UNSAFE = (Unsafe) theUnsafe.get(null);

            BYTE_BASE = UNSAFE.arrayBaseOffset(byte[].class);
        } catch (final Exception e) {
            throw new AssertionError(e);
        }
        INSTANCE = new UnsafeAccess();
        INSTANCE_REVERSE = Access.reverse(INSTANCE);
    }

    /**
     * Get {@code this} or the reversed access object for reading the input as fixed
     * byte order of {@code byteOrder}.
     *
     * @return a {@code Access} object which will read the {@code input} with the
     * byte order of {@code byteOrder}.
     */
    public static Access<byte[]> instance() {
        return ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN ? INSTANCE : INSTANCE_REVERSE;
    }

    public static long baseOffset() {
        return BYTE_BASE;
    }

    public static long baseOffset(@NotNull final ByteBuffer buffer) {
        return BYTE_BASE + buffer.arrayOffset();
    }

    public static long baseOffset(@NotNull final DirectBuffer buffer) {
        return buffer.address();
    }

    private UnsafeAccess() {}

    @Override
    public long getLong(byte[] input, long offset) {
        return UNSAFE.getLong(input, offset);
    }

    @Override
    public int getInt(byte[] input, long offset) {
        return UNSAFE.getInt(input, offset);
    }

    @Override
    public int getByte(byte[] input, long offset) {
        return UNSAFE.getByte(input, offset);
    }
}