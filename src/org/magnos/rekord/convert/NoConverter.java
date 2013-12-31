package org.magnos.rekord.convert;

public class NoConverter<T> extends AbstractConverter<T, T>
{

	@SuppressWarnings ("rawtypes" )
	public static final NoConverter INSTANCE = new NoConverter();
	
	@Override
	public T convertFrom( T in )
	{
		return in;
	}

	@Override
	public T convertTo( T out )
	{
		return out;
	}

}
