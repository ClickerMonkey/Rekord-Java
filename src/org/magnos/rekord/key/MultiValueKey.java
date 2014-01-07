package org.magnos.rekord.key;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.magnos.rekord.Converter;
import org.magnos.rekord.Key;
import org.magnos.rekord.Table;
import org.magnos.rekord.Type;
import org.magnos.rekord.field.Column;

public class MultiValueKey implements Key
{

	public Table table;
	public Object[] values;
	
	public MultiValueKey(Table table)
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
		    final Column<?> c = columns[i];
	        final Converter<Object, ?> converter = c.getConverter();
	        final Type<Object> type = c.getType();
	        
			values[i] = converter.fromDatabase( type.fromResultSet( results, c.getName(), true ) );
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
