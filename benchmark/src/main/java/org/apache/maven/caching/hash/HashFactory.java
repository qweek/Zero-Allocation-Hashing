package org.apache.maven.caching.hash;

import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public enum HashFactory
{
    SHA1( new SHA( "SHA-1" ) ),
    SHA256( new SHA( "SHA-256" ) ),
    SHA384( new SHA( "SHA-384" ) ),
    SHA512( new SHA( "SHA-512" ) ),
    XX( new XX() ),
    XXH3( new XXH3() ),
    XXH128L( new XXH128L() ),
    XXH128( new XXH128() ),
    XXMM( new XXMM() ),
    XX2( new XX2() ),
    XX3( new XX3() );

    private static final Map<String, HashFactory> LOOKUP = new HashMap<>();

    static
    {
        for ( HashFactory factory : HashFactory.values() )
        {
            LOOKUP.put( factory.getAlgorithm(), factory );
        }
    }

    public static HashFactory of( String algorithm ) throws NoSuchAlgorithmException
    {
        final HashFactory factory = LOOKUP.get( algorithm );
        if ( factory == null )
        {
            throw new NoSuchAlgorithmException( algorithm );
        }
        return factory;
    }

    private final Hash.Factory factory;

    HashFactory( Hash.Factory factory )
    {
        this.factory = factory;
    }

    public String getAlgorithm()
    {
        return factory.getAlgorithm();
    }

    public HashAlgorithm createAlgorithm()
    {
        return new HashAlgorithm( factory.algorithm() );
    }

    public HashChecksum createChecksum( int count )
    {
        return new HashChecksum( factory.algorithm(), factory.checksum( count ) );
    }
}
