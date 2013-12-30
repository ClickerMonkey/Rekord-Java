package org.magnos.rekord.condition;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.magnos.rekord.field.Column;
import org.magnos.rekord.util.SqlUtil;

public class LiteralCondition implements Condition
{

	public String literal;
	
	public LiteralCondition(String literal)
	{
		this.literal = literal;
	}
	
	@Override
	public void toQuery( StringBuilder query )
	{
		query.append( literal );
	}

	@Override
	public int toPreparedstatement( PreparedStatement stmt, int paramIndex ) throws SQLException
	{
		return paramIndex;
	}
	
	public static LiteralCondition forNullColumn(Column<?> column)
	{
		return new LiteralCondition( SqlUtil.namify( column.getName() ) + " IS NULL" );
	}

}
