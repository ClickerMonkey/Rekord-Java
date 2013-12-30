package org.magnos.rekord.key;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.magnos.rekord.Field;
import org.magnos.rekord.Key;
import org.magnos.rekord.Model;
import org.magnos.rekord.Value;
import org.magnos.rekord.condition.Condition;
import org.magnos.rekord.field.Column;

public class SingleModelKey implements Key 
{

	public Value<?> value;
	public Model model;
	
	public SingleModelKey(Model model)
	{
		this( model, model.valueOf( model.getTable().getKeyColumns()[0] ) );
	}
	
	public SingleModelKey(Model model, Field<?> field )
	{
		this( model, model.valueOf( field ) );
	}
	
	public SingleModelKey(Model model, Value<?> value)
	{
		this.model = model;
		this.value = value;
	}

	@Override
	public void fromResultSet( ResultSet results ) throws SQLException
	{
		value.fromResultSet( results );
	}

	@Override
	public int toPreparedStatement( PreparedStatement preparedStatement, int paramIndex ) throws SQLException
	{
		return value.toPreparedStatement( preparedStatement, paramIndex );
	}

	@Override
	public int size()
	{
		return 1;
	}

	@Override
	public Object valueAt( int index )
	{
		return value.get( model );
	}
	
	@Override
	public Column<?> fieldAt( int index )
	{
		return (Column<?>)value.getField();
	}

	@Override
	public Condition condition()
	{
		return model.getTable().getKeyCondition();
	}

	@Override
	public boolean exists()
	{
		return value.hasValue();
	}

	@Override
	public int hashCode()
	{
		return Keys.hashCode( value.get( model ) );
	}
	
	@Override
	public boolean equals(Object o)
	{
		return Keys.equals( this, o );
	}
	
	@Override
	public String toString()
	{
		return Keys.toString( this );
	}
	
}
