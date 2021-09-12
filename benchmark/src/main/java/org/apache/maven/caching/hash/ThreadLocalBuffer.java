package org.apache.maven.caching.hash;

import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * ThreadLocalBuffer
 */
public class ThreadLocalBuffer
{
    private static final ConcurrentMap<CloseableBuffer, Boolean> LOCALS = new ConcurrentHashMap<>();

    public static ByteBuffer get( ThreadLocal<CloseableBuffer> local, int capacity )
    {
        final CloseableBuffer buffer = local.get();
        if ( buffer == null )
        {
            return create( local, capacity );
        }

        if ( capacity( buffer ) < capacity )
        {
            close( buffer );
            return create( local, capacity * 2 );
        }

        return clear( buffer );
    }

    @Override
    public void finalize()
    {
        for ( CloseableBuffer buffer : LOCALS.keySet() )
        {
            buffer.close();
        }
    }

    private static ByteBuffer create( ThreadLocal<CloseableBuffer> local, int capacity )
    {
        final CloseableBuffer buffer = CloseableBuffer.directBuffer( capacity );
        local.set( buffer );
        LOCALS.put( buffer, false );
        return buffer.getBuffer();
    }

    private static int capacity( CloseableBuffer buffer )
    {
        return buffer.getBuffer().capacity();
    }

    private static ByteBuffer clear( CloseableBuffer buffer )
    {
        return (ByteBuffer) buffer.getBuffer().clear();
    }

    private static void close( CloseableBuffer buffer )
    {
        LOCALS.remove( buffer );
        buffer.close();
    }

    private ThreadLocalBuffer()
    {
    }
}
