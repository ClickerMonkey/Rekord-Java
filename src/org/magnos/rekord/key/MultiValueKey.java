package org.magnos.rekord.key;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.magnos.rekord.Column;
import org.magnos.rekord.Key;
import org.magnos.rekord.Table;
import org.magnos.rekord.condition.Condition;

public class MultiValueKey implements Key
{

	public Table<?> table;
	public Object[] values;
	
	public MultiValueKey(Table<?> table)
	{
		this.table = table;
		this.values = new Object[ table.getKeySize() ];
	}
	
	@Override
	public void fromResultSet( ResultSet results ) throws SQLException
	{
		final Column<?>[] columns = table.getKeyColumns();
		
		for (int i = 0; i < columns.length; i++)
		{
			values[i] = results.getObject( columns[i].getName() );
		}
	}

	@Override
	public int toPreparedStatement( PreparedStatement preparedStatement, int paramIndex ) throws SQLException
	{
		for (int i = 0; i < values.length; i++)
		{
			preparedStatement.setObject( paramIndex++, values[i] );	
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
		return values[ index ];
	}
	
	@Override
	public Column<?> fieldAt( int index )
	{
		return table.getKeyColumns()[ index ];
	}

	@Override
	public Condition condition()
	{
		return table.getKeyCondition();
	}

	@Override
	public boolean exists()
	{
		for (int i = 0; i < values.length; i++)
		{
			if (values[i] == null)
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
