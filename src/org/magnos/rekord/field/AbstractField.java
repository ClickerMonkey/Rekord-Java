package org.magnos.rekord.field;

import org.magnos.rekord.Field;
import org.magnos.rekord.Table;

public abstract class AbstractField<T> implements Field<T>
{
	
	protected String name;
	protected int flags;
	protected int index;
	protected Table table;
	
	public AbstractField(String name, int flags)
	{
		this.name = name;
		this.flags = flags;
	}
	
	@Override
	public String getName()
	{
		return name;
	}
	
	@Override
	public int getIndex()
	{
		return index;
	}

	@Override
	public void setIndex( int valueIndex )
	{
		this.index = valueIndex;
	}

	@Override
	public void setTable( Table entity )
	{
		this.table = entity;
	}

	@Override
	public Table getTable()
	{
		return table;
	}

	@Override
	public int getFlags()
	{
		return flags;
	}
	
	@Override
	public boolean is(int flag)
	{
		return (flags & flag) == flag;
	}
	 
}
