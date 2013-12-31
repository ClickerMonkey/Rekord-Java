
package org.magnos.rekord.util;

import java.lang.reflect.Modifier;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

import org.magnos.rekord.field.Column;

public class SqlUtil
{
    

    private static final Map<String, Integer> SQL_TYPES = new HashMap<String, Integer>();

    static
    {
        try
        {
            int expectedModifiers = Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL;
            
            for (java.lang.reflect.Field f : Types.class.getFields())
            {
                if (f.getType() == int.class && f.getModifiers() == expectedModifiers)
                {
                    SQL_TYPES.put( f.getName(), f.getInt( null ) ); 
                }
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException( e );
        }
    }
    
    public static Integer getSqlType(String name)
    {
        return SQL_TYPES.get( name );
    }
    
	public static String namify( String name )
	{
		return "\"" + name + "\"";
	}
	
	public static String joinAndNamify( Column<?> ... columns )
	{
		StringBuilder sb = new StringBuilder();
		
		for (int i = 0; i < columns.length; i++)
		{
			if (i > 0)
			{
				sb.append( "," );
			}
			
			sb.append( namify( columns[i].getName() ) );
		}
		
		return sb.toString();
	}
	
	public static String join( String delimiter, String ... words )
	{
		StringBuilder sb = new StringBuilder();	
		
		for (int i = 0; i < words.length; i++)
		{
			if (i > 0)
			{
				sb.append( delimiter );
			}
			
			sb.append( words[i] );
		}
		
		return sb.toString();
	}
	
	public static String join( String delimiter, Column<?> ... words )
	{
		StringBuilder sb = new StringBuilder();	
		
		for (int i = 0; i < words.length; i++)
		{
			if (i > 0)
			{
				sb.append( delimiter );
			}
			
			sb.append( words[i].getName() );
		}
		
		return sb.toString();
	}

	public static String santize( String s )
	{
		s = s.replaceAll( "\\\\", "\\\\" );
		s = s.replaceAll( "\0", "\\0" );
		s = s.replaceAll( "\n", "\\n" );
		s = s.replaceAll( "\r", "\\r" );
		s = s.replaceAll( "'", "\\'" );
		s = s.replaceAll( "\"", "\\\"" );

		return s;
	}
	
	public static String createWhere( Column<?> ... columns )
	{
		StringBuilder where = new StringBuilder();
		
		for (int i = 0; i < columns.length; i++)
		{
			if (i > 0)
			{
				where.append( " AND " );
			}
			
			where.append( namify( columns[i].getName() ) );
			where.append( " = ?" );
		}
		
		return where.toString();
	}

	public static boolean equals( Object a, Object b )
	{
		return (a == b || (a != null && b != null && a.equals( b )));
	}

	
}
