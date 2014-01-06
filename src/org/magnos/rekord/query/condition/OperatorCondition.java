package org.magnos.rekord.query.condition;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.magnos.rekord.Converter;
import org.magnos.rekord.Operator;
import org.magnos.rekord.Rekord;
import org.magnos.rekord.Type;
import org.magnos.rekord.convert.NoConverter;
import org.magnos.rekord.field.Column;

public class OperatorCondition<T> implements Condition
{

	public String column;
	public String in;
	public Operator operator;
	public T value;
	public Type<Object> type;
	public Converter<Object, T> converter;
	
	public OperatorCondition(Column<T> column, Operator operator, T value)
	{
		this( column.getQuotedName(), column.getIn(), operator, value, column.getType(), column.getConverter() );
	}
	
	public OperatorCondition(String expression, Operator operator, T value)
	{
		this( expression, "?", operator, value, Rekord.getTypeForObject( (Object)value ), (Converter<Object, T>) NoConverter.INSTANCE );
	}

	public OperatorCondition(String column, String in, Operator operator, T value, Type<Object> type, Converter<Object, T> converter)
	{
		this.column = column;
		this.in = in;
		this.operator = operator;
		this.value = value;
		this.type = type;
		this.converter = converter;
	}
	
	@Override
	public void toQuery( StringBuilder query )
	{
		query.append( column );
		query.append( operator.getSymbol() );
		query.append( in );
	}

	@Override
	public int toPreparedstatement( PreparedStatement stmt, int paramIndex ) throws SQLException
	{
		type.toPreparedStatement( stmt, converter.convertTo( value ), paramIndex++ );
		
		return paramIndex;
	}

}
