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

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

import static java.nio.ByteOrder.LITTLE_ENDIAN;

public class UnsafeAccess extends Access<byte[]> {
    private static final VarHandle LONG_HANDLE = MethodHandles.byteArrayViewVarHandle(long[].class, LITTLE_ENDIAN);
    private static final VarHandle INT_HANDLE = MethodHandles.byteArrayViewVarHandle(int[].class, LITTLE_ENDIAN);
    @NotNull
    private static final Access<byte[]> INSTANCE = new UnsafeAccess();

    /**
     * Get {@code this} or the reversed access object for reading the input as fixed
     * byte order of {@code byteOrder}.
     *
     * @return a {@code Access} object which will read the {@code input} with the
     * byte order of {@code byteOrder}.
     */
    public static Access<byte[]> instance() {
        return INSTANCE;
    }

    private UnsafeAccess() {}

    @Override
    public long getLong(byte[] input, long offset) {
        return (long) LONG_HANDLE.get(input, (int)offset);
    }

    @Override
    public int getInt(byte[] input, long offset) {
        return (int) INT_HANDLE.get(input, (int)offset);
    }

    @Override
    public int getByte(byte[] input, long offset) {
        return input[(int)offset];
    }
}