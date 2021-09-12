package org.apache.maven.caching.hash;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

/**
 * ThreadLocalDigest
 */
public class ThreadLocalDigest
{
    public static MessageDigest get( ThreadLocal<MessageDigest> local, String algorithm )
    {
        final MessageDigest digest = local.get();
        if ( digest == null )
        {
            return create( local, algorithm );
        }

        if ( Objects.equals( digest.getAlgorithm(), algorithm ) )
        {
            return reset( digest );
        }

        reset( digest );
        return create( local, algorithm );
    }

    private static MessageDigest create( ThreadLocal<MessageDigest> local, String algorithm )
    {
        try
        {
            final MessageDigest digest = MessageDigest.getInstance( algorithm );
            local.set( digest );
            return digest;
        }
        catch ( NoSuchAlgorithmException e )
        {
            throw new RuntimeException( "Cannot create message digest with algorithm: " + algorithm, e );
        }
    }

    private static MessageDigest reset( MessageDigest digest )
    {
        digest.reset();
        return digest;
    }

    private ThreadLocalDigest()
    {
    }
}
