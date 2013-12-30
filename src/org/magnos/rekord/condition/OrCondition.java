package org.magnos.rekord.condition;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class OrCondition implements Condition
{

	public Condition[] conditions;
	
	public OrCondition(Condition ... conditions)
	{
		this.conditions = conditions;
	}
	
	@Override
	public void toQuery(StringBuilder query)
	{
		query.append( "(" );
		
		for (int i = 0; i < conditions.length; i++)
		{
			if (i > 0)
			{
				query.append( " OR " );
			}
			
			conditions[i].toQuery( query );
		}
		
		query.append( ")" );
	}

	@Override
	public int toPreparedstatement( PreparedStatement stmt, int paramIndex ) throws SQLException
	{
		for (int i = 0; i < conditions.length; i++)
		{
			paramIndex = conditions[i].toPreparedstatement( stmt, paramIndex );
		}
		
		return paramIndex;
	}

}
