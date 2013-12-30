package org.magnos.rekord.condition;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.magnos.rekord.Column;
import org.magnos.rekord.util.SqlUtil;

public class BetweenCondition<T> implements Condition
{
	
	public Column<T> column;
	public T min;
	public T max;

	public BetweenCondition(Column<T> column, T min, T max)
	{
		this.column = column;
		this.min = min;
		this.max = max;
	}
	
	@Override
	public void toQuery( StringBuilder query )
	{
		query.append( SqlUtil.namify( column.getName() ) );
		query.append( " BETWEEN ? AND ?" );
	}

	@Override
	public int toPreparedstatement( PreparedStatement stmt, int paramIndex ) throws SQLException
	{
		stmt.setObject( paramIndex++, min );
		stmt.setObject( paramIndex++, max );
		
		return paramIndex;
	}

}
