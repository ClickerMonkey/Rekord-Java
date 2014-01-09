package org.magnos.rekord.query.condition;

import org.magnos.rekord.Converter;
import org.magnos.rekord.Operator;
import org.magnos.rekord.Rekord;
import org.magnos.rekord.Type;
import org.magnos.rekord.convert.NoConverter;
import org.magnos.rekord.field.Column;
import org.magnos.rekord.query.QueryBuilder;

public class OperatorCondition<T> implements Condition
{

	public String columnName;
	public Column<?> column;
	public String name;
	public String in;
	public Operator operator;
	public T value;
	public Type<Object> type;
	public Converter<Object, T> converter;
	
	public OperatorCondition(Column<T> column, Operator operator, T value)
	{
		this( column.getQuotedName(), null, column.getName(), column.getIn(), operator, value, column.getType(), column.getConverter() );
	}
	
	public OperatorCondition(String expression, Operator operator, T value)
	{
		this( expression, null, null, "?", operator, value, Rekord.getTypeForObject( (Object)value ), (Converter<Object, T>) NoConverter.INSTANCE );
	}

	public OperatorCondition(String columnName, Column<?> column, String name, String in, Operator operator, T value, Type<Object> type, Converter<Object, T> converter)
	{
		this.columnName = columnName;
		this.column = column;
		this.name = name;
		this.in = in;
		this.operator = operator;
		this.value = value;
		this.type = type;
		this.converter = converter;
	}
	
	@Override
	public void toQuery( QueryBuilder query )
	{
		query.appendValuable( columnName );
		query.append( operator.getSymbol() );
		query.append( name, in, column, converter.toDatabase( value ), type );
	}

}
