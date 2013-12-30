package org.magnos.rekord.key;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.magnos.rekord.Key;
import org.magnos.rekord.Table;
import org.magnos.rekord.condition.Condition;
import org.magnos.rekord.field.Column;

public class SingleValueKey implements Key
{

	public Table<?> table;
	public Object value;
	
	public SingleValueKey(Table<?> table)
	{
		this.table = table;
	}
	
	@Override
	public void fromResultSet( ResultSet results ) throws SQLException
	{
		final Column<?> column = table.getKeyColumns()[0];
		
		value = results.getObject( column.getName() );
	}

	@Override
	public int toPreparedStatement( PreparedStatement preparedStatement, int paramIndex ) throws SQLException
	{
		preparedStatement.setObject( paramIndex++, value );
		
		return paramIndex;
	}

	@Override
	public int size()
	{
		return 1;
	}

	@Override
	public Object valueAt( int index )
	{
		return value;
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
		return (value != null);
	}
	
	@Override
	public int hashCode()
	{
		return Keys.hashCode( value );
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
