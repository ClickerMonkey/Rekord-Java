package org.magnos.rekord;

public class Flags
{
	
	public static final int NONE 		= 0;
	public static final int READ_ONLY 	= 1 << 0;
	public static final int GENERATED 	= 1 << 1;
	public static final int LAZY 		= 1 << 2; 
	public static final int NON_NULL    = 1 << 3;
	
	public static boolean is(int flagSet, int flag)
	{
		return (flagSet & flag) != 0;
	}
	
	public static boolean isNot(int flagSet, int flag)
	{
		return (flagSet & flag) == 0;
	}
	
}
