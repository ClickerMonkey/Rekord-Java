package org.magnos.rekord.query.condition;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.magnos.rekord.Rekord;
import org.magnos.rekord.Type;

public class CustomCondition implements Condition
{

	public String expression;
	public Object[] values;
	
	public CustomCondition(String expression, Object ... values)
	{
		this.expression = expression;
		this.values = values;
	}
	
	@Override
	public void toQuery( StringBuilder query )
	{
		query.append( expression );
	}

	@Override
	public int toPreparedstatement( PreparedStatement stmt, int paramIndex ) throws SQLException
	{
		for (int i = 0; i < values.length; i++)
		{
			Object val = values[i];
			Type<Object> type = Rekord.getTypeForObject( val );
			
			type.toPreparedStatement( stmt, val, paramIndex++ );
		}
		
		return paramIndex;
	}

}
