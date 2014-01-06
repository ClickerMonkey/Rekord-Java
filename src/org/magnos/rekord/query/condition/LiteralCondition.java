package org.magnos.rekord.query.condition;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.magnos.rekord.Field;

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
	
	public static LiteralCondition forNull(Field<?> field)
	{
		return new LiteralCondition( field.getQuotedName() + " IS NULL" );
	}
	
	public static LiteralCondition forNotNull(Field<?> field)
	{
		return new LiteralCondition( field.getQuotedName() + " IS NOT NULL" );
	}

}
