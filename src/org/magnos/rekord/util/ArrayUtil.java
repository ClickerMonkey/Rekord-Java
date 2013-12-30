package org.magnos.rekord.util;

import java.lang.reflect.Array;

public class ArrayUtil
{

	public static <T> T[] asArray(T ... elements)
	{
		return elements;
	}
	
	public static <T> T[] join(Class<T> itemType, T[] ... arrays)
	{
		int length = 0;
		
		for (int i = 0; i < arrays.length; i++) {
			T[] a = arrays[i];
			if (a != null) {
				length += a.length;
			}
		}
		
		T[] joined = (T[])Array.newInstance( itemType, length );
		int offset = 0;
		
		for (int i = 0; i < arrays.length; i++) {
			T[] a = arrays[i];
			if (a != null) {
				System.arraycopy( a, 0, joined, offset, a.length );
				offset += a.length;
			}
		}
		
		return joined;
	}
	
}
