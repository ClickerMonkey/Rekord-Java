
package org.magnos.rekord.key;

import org.magnos.rekord.Key;


public class Keys
{

	public static int hashCode( Key key )
	{
		final int prime = 31;
		int result = 1;

		for (int i = 0; i < key.size(); i++)
		{
			result = prime * result + hashCode( key.valueAt( i ) );
		}

		return result;
	}

	public static int hashCode( Object obj )
	{
		return (obj == null ? 0 : obj.hashCode());
	}

	public static boolean equals( Key a, Object other )
	{
		if (other == null || !(other instanceof Key))
		{
			return false;
		}

		Key b = (Key)other;

		if (a.size() != b.size())
		{
			return false;
		}

		for (int i = 0; i < a.size(); i++)
		{
			if (!equals( a.valueAt( i ), b.valueAt( i ) ))
			{
				return false;
			}
		}

		return true;
	}

	public static boolean equals( Object a, Object b )
	{
		return (a == b || (a != null && b != null && a.equals( b )));
	}
	
	public static String toString( Key k )
	{
		StringBuilder sb = new StringBuilder();
		sb.append( "{" );

		for (int i = 0; i < k.size(); i++)
		{
			if (i > 0)
			{
				sb.append( ", " );
			}
			
			sb.append( k.fieldAt( i ).getName() );
			sb.append( "=" );
			sb.append( k.valueAt( i ) );
		}
		
		sb.append( "}" );
		return sb.toString();
	}

}
