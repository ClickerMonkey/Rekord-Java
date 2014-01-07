package org.magnos.rekord.convert;


public class NoConverter<T> extends AbstractConverter<T, T>
{

	public static final NoConverter<?> INSTANCE = new NoConverter<Object>();
	
	@Override
	public T fromDatabase( T in )
	{
		return in;
	}

	@Override
	public T toDatabase( T out )
	{
		return out;
	}

}
