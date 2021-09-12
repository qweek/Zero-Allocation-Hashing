package org.apache.maven.caching.hash;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Longs;
import net.openhft.hashing.LongHashFunction;
import net.openhft.hashing.LongTupleHashFunction;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.google.common.primitives.Longs.toByteArray;

/**
 * XX
 */
public class XXH128 implements Hash.Factory
{
    static final LongTupleHashFunction INSTANCE = LongTupleHashFunction.xx128();

    @Override
    public String getAlgorithm()
    {
        return "XXH128";
    }

    @Override
    public Hash.Algorithm algorithm()
    {
        return new XXH128.Algorithm();
    }

    @Override
    public Hash.Checksum checksum( int count )
    {
        return new XXH128.Checksum( ByteBuffer.allocate( capacity( count ) ) );
    }

    static int capacity( int count )
    {
        // Java 8: Long.BYTES
        return count * Long.SIZE / Byte.SIZE * 2;
    }

    static class Algorithm implements Hash.Algorithm
    {
        @Override
        public byte[] hash( byte[] array )
        {
            return toByteArray( INSTANCE.hashBytes( array ) );
        }

        @Override
        public byte[] hash( Path path ) throws IOException
        {
            return hash( Files.readAllBytes( path ) );
        }
    }

    static class Checksum implements Hash.Checksum
    {
        private final ByteBuffer buffer;

        Checksum( ByteBuffer buffer )
        {
            this.buffer = buffer;
        }

        @Override
        public void update( byte[] hash )
        {
            buffer.put( hash );
        }

        @Override
        public byte[] digest()
        {
            return toByteArray( INSTANCE.hashBytes( buffer, 0, buffer.position() ) );
        }
    }

    private static byte[] toByteArray(long[] longs) {
        return Bytes.concat(Longs.toByteArray(longs[0]), Longs.toByteArray(longs[1]));
    }
}
