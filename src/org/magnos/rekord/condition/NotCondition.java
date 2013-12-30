package org.magnos.rekord.condition;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class NotCondition implements Condition
{

	public Condition condition;
	
	public NotCondition(Condition condition)
	{
		this.condition = condition;
	}
	
	@Override
	public void toQuery(StringBuilder query)
	{
		query.append( " NOT " );
		condition.toQuery( query );
	}

	@Override
	public int toPreparedstatement( PreparedStatement stmt, int paramIndex ) throws SQLException
	{
		return condition.toPreparedstatement( stmt, paramIndex );
	}

}
