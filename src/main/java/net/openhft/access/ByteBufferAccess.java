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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class ByteBufferAccess extends Access<ByteBuffer> {
    @NotNull
    private static final Access<ByteBuffer> INSTANCE_LE = new ByteBufferAccess();
    @NotNull
    private static final Access<ByteBuffer> INSTANCE_BE = Access.reverse(INSTANCE_LE);

    /**
     * Get {@code this} or the reversed access object for reading the input as fixed
     * byte order of {@code byteOrder}.
     *
     * @param input the accessed object
     * @return a {@code Access} object which will read the {@code input} with the
     * byte order of {@code byteOrder}.
     */
    public static Access<ByteBuffer> instance(final ByteBuffer input) {
        return input.order() == ByteOrder.LITTLE_ENDIAN ? INSTANCE_LE : INSTANCE_BE;
    }

    private ByteBufferAccess() {}

    @Override
    public long getLong(ByteBuffer input, long offset) {
        return input.getLong((int) offset);
    }

    @Override
    public int getInt(ByteBuffer input, long offset) {
        return input.getInt((int) offset);
    }

    @Override
    public int getByte(ByteBuffer input, long offset) {
        return input.get((int) offset);
    }
}
