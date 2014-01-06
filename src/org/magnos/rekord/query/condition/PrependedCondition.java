package org.magnos.rekord.query.condition;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PrependedCondition implements Condition
{
	
	public final String prepend;
	public final Condition condition;
	
	public PrependedCondition(String prepend, Condition condition)
	{
		this.prepend = prepend;
		this.condition = condition;
	}

	@Override
	public void toQuery( StringBuilder query )
	{
		condition.toQuery( query );
	}

	@Override
	public int toPreparedstatement( PreparedStatement stmt, int paramIndex ) throws SQLException
	{
		return condition.toPreparedstatement( stmt, paramIndex );
	}

}
