package org.magnos.rekord.key;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.magnos.rekord.Column;
import org.magnos.rekord.Field;
import org.magnos.rekord.Key;
import org.magnos.rekord.Model;
import org.magnos.rekord.Value;
import org.magnos.rekord.condition.Condition;

public class MultiModelKey implements Key
{

	public Model model;
	public Value<?>[] values;
	
	public MultiModelKey(Model model)
	{
		this( model, model.getTable().getKeyColumns() );
	}
	
	public MultiModelKey(Model model, Field<?> ... fields)
	{
		this.model = model;
		this.values = new Value[ fields.length ];
		
		for (int i = 0; i < fields.length; i++)
		{
			this.values[i] = model.valueOf( fields[i] );
		}
	}
	
	@Override
	public void fromResultSet( ResultSet results ) throws SQLException
	{
		for (int i = 0; i < values.length; i++)
		{
			values[i].fromResultSet( results );
		}
	}

	@Override
	public int toPreparedStatement( PreparedStatement preparedStatement, int paramIndex ) throws SQLException
	{
		for (int i = 0; i < values.length; i++)
		{
			paramIndex = values[i].toPreparedStatement( preparedStatement, paramIndex );
		}
		
		return paramIndex;
	}

	@Override
	public int size()
	{
		return values.length;
	}

	@Override
	public Object valueAt( int index )
	{
		return values[ index ].get( model );
	}
	
	@Override
	public Column<?> fieldAt( int index )
	{
		return (Column<?>)values[ index ].getField();
	}

	@Override
	public Condition condition()
	{
		return model.getTable().getKeyCondition();
	}

	@Override
	public boolean exists()
	{
		for (int i = 0; i < values.length; i++)
		{
			if (!values[i].hasValue())
			{
				return false;
			}
		}
		
		return true;
	}
	
	@Override
	public int hashCode()
	{
		return Keys.hashCode( this );
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
