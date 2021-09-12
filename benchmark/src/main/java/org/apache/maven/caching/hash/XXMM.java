package org.apache.maven.caching.hash;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Path;

import static com.google.common.primitives.Longs.toByteArray;
import static java.nio.channels.FileChannel.MapMode.READ_ONLY;
import static java.nio.file.StandardOpenOption.READ;

/**
 * XXMM
 */
public class XXMM implements Hash.Factory
{
    private static final ThreadLocal<CloseableBuffer> BUFFER = new ThreadLocal<>();

    @Override
    public String getAlgorithm()
    {
        return "XXMM";
    }

    @Override
    public Hash.Algorithm algorithm()
    {
        return new Algorithm();
    }

    @Override
    public Hash.Checksum checksum( int count )
    {
        return new XX.Checksum( ThreadLocalBuffer.get( BUFFER, XX.capacity( count ) ) );
    }

    private static class Algorithm extends XX.Algorithm
    {
        @Override
        public byte[] hash( Path path ) throws IOException
        {
            try ( FileChannel channel = FileChannel.open( path,
                    READ ); CloseableBuffer buffer = CloseableBuffer.mappedBuffer( channel, READ_ONLY ) )
            {
                return toByteArray( XX.INSTANCE.hashBytes( buffer.getBuffer() ) );
            }
        }
    }
}
