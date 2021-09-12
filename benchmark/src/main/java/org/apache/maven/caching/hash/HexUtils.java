package org.apache.maven.caching.hash;

import com.google.common.base.CharMatcher;
import com.google.common.io.BaseEncoding;

/**
 * HexUtils
 */
public class HexUtils
{
    private static final BaseEncoding ENCODING = BaseEncoding.base16().lowerCase();
    private static final CharMatcher MATCHER = CharMatcher.is( '0' );

    public static String encode( byte[] hash )
    {
        return trimLeadingZero( ENCODING.encode( hash ) );
    }

    public static byte[] decode( String hex )
    {
        return ENCODING.decode( padLeadingZero( hex ) );
    }

    private static String trimLeadingZero( String hex )
    {
        String value = MATCHER.trimLeadingFrom( hex );
        return value.isEmpty() ? "0" : value;
    }

    private static String padLeadingZero( String hex )
    {
        String value = hex.toLowerCase();
        return value.length() % 2 == 0 ? value : "0" + value;
    }
}
