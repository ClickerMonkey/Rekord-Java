package org.magnos.rekord.condition;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.magnos.rekord.Operator;
import org.magnos.rekord.field.Column;

public class OperatorCondition implements Condition
{

	public String column;
	public Operator operator;
	public Object value;
	
	public OperatorCondition(Column<?> column, Operator operator, Object value)
	{
		this( column.getQuotedName(), operator, value );
	}
	
	public OperatorCondition(String column, Operator operator, Object value)
	{
		this.column = column;
		this.operator = operator;
		this.value = value;
	}
	
	@Override
	public void toQuery( StringBuilder query )
	{
		query.append( column );
		query.append( operator.getSymbol() );
		query.append( "?" );
	}

	@Override
	public int toPreparedstatement( PreparedStatement stmt, int paramIndex ) throws SQLException
	{
		stmt.setObject( paramIndex++, value );
		
		return paramIndex;
	}

}
