package org.magnos.rekord.util;

import java.lang.reflect.Constructor;

import org.magnos.rekord.Factory;

public class ReflectionFactory<T> implements Factory<T>
{
	
	private static final Class<?>[] C_PARAMS = {};
	private static final Object[] C_ARGS = {};
	
	private Constructor<T> constructor; 
	
	public ReflectionFactory(Class<T> clazz) throws SecurityException, NoSuchMethodException
	{
		constructor = clazz.getConstructor( C_PARAMS );
	}

	@Override
	public T create()
	{
		try
		{
			return constructor.newInstance( C_ARGS );
		}
		catch (RuntimeException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			throw new RuntimeException( e );
		}
	}

}
