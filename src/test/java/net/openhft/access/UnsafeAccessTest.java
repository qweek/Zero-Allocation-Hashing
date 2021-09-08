package net.openhft.access;

import net.openhft.internal.Primitives;
import org.junit.Assert;
import org.junit.Test;

import static java.nio.ByteOrder.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeTrue;

public class UnsafeAccessTest {
    public Access<byte[]> unsafe = UnsafeAccess.instance();

    @Test
    public void testUnsafeAccess() {
        {
            final byte[] b = new byte[]{(byte)0xF4, 0x5D};
            assertEquals((int) b[0], unsafe.getByte(b, UnsafeAccess.baseOffset()));
            assertEquals((int) b[1], unsafe.getByte(b, UnsafeAccess.baseOffset() + 1));
            Assert.assertEquals(Primitives.unsignedByte(b[0]), unsafe.getUnsignedByte(b, UnsafeAccess.baseOffset()));
            assertEquals(Primitives.unsignedByte(b[1]), unsafe.getUnsignedByte(b, UnsafeAccess.baseOffset() + 1));
        }
    }

    @Test
    public void testUnsafeAccessUnalignLE() {
        assumeTrue(nativeOrder() == LITTLE_ENDIAN);

        {
            final byte[] b = new byte[]{(byte)0xF4, 0x5D};
            assertEquals(0x5D, unsafe.getByte(b, UnsafeAccess.baseOffset() + 1));
            assertEquals(Primitives.unsignedByte(0x5D), unsafe.getUnsignedByte(b, UnsafeAccess.baseOffset() + 1));
        }
    }

    @Test
    public void testUnsafeAccessUnalignBE() {
        assumeTrue(nativeOrder() == BIG_ENDIAN);

        {
            final byte[] b = new byte[]{(byte)0xF4, 0x5D};
            assertEquals(0x5D, unsafe.getByte(b, UnsafeAccess.baseOffset() + 1));
            assertEquals(Primitives.unsignedByte(0x5D), unsafe.getUnsignedByte(b, UnsafeAccess.baseOffset() + 1));
        }
    }
}
