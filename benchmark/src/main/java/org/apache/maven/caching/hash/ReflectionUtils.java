package org.apache.maven.caching.hash;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * ReflectionUtils
 */
class ReflectionUtils
{
    static Method getMethod( String className, String methodName, Class<?>... parameterTypes )
    {
        try
        {
            final Method method = Class.forName( className ).getMethod( methodName, parameterTypes );
            method.setAccessible( true );
            return method;
        }
        catch ( Exception ignore )
        {
            return null;
        }
    }

    static Object getField( String className, String fieldName )
    {
        try
        {
            final Field field = Class.forName( className ).getDeclaredField( fieldName );
            field.setAccessible( true );
            return field.get( null );
        }
        catch ( Exception ignore )
        {
            return null;
        }
    }

    private ReflectionUtils()
    {
    }
}
