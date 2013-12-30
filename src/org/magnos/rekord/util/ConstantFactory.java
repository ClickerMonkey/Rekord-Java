package org.magnos.rekord.util;

import org.magnos.rekord.Factory;

public class ConstantFactory<T> implements Factory<T>
{
	
    private final T constant;
	
	public ConstantFactory(T constant)
	{
	    this.constant = constant;
	}

	@Override
	public T create()
	{
	    return constant;
	}

}
