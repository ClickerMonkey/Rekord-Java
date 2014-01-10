package org.magnos.rekord.query.condition;

import org.magnos.rekord.Converter;
import org.magnos.rekord.Rekord;
import org.magnos.rekord.Type;
import org.magnos.rekord.convert.NoConverter;
import org.magnos.rekord.field.Column;
import org.magnos.rekord.field.ForeignColumn;
import org.magnos.rekord.query.Operator;
import org.magnos.rekord.query.QueryBuilder;

public class OperatorCondition<T> implements Condition
{

	public String columnName;
	public Column<?> column;
	public String name;
	public String in;
	public String symbol;
	public T value;
	public Type<Object> type;
	public Converter<Object, T> converter;
    
    public OperatorCondition(Column<T> column, Operator operator, T value)
    {
        this( column.getQuotedName(), null, column.getName(), column.getSelectionExpression(), operator.getSymbol(), value, column.getType(), column.getConverter() );
    }
    
    public OperatorCondition(Column<T> column, String symbol, T value)
    {
        this( column.getQuotedName(), null, column.getName(), column.getSelectionExpression(), symbol, value, column.getType(), column.getConverter() );
    }
    
    public OperatorCondition(String expression, Operator operator, T value)
    {
        this( expression, null, null, "?", operator.getSymbol(), value, Rekord.getTypeForObject( (Object)value ), (Converter<Object, T>) NoConverter.INSTANCE );
    }
    
    public OperatorCondition(String expression, String symbol, T value)
    {
        this( expression, null, null, "?", symbol, value, Rekord.getTypeForObject( (Object)value ), (Converter<Object, T>) NoConverter.INSTANCE );
    }

	public OperatorCondition(String columnName, Column<?> column, String name, String in, String symbol, T value, Type<Object> type, Converter<Object, T> converter)
	{
		this.columnName = columnName;
		this.column = column;
		this.name = name;
		this.in = in;
		this.symbol = symbol;
		this.value = value;
		this.type = type;
		this.converter = converter;
	}
	
	@Override
	public void toQuery( QueryBuilder query )
	{
		query.appendValuable( columnName );
		query.append( symbol );
		query.append( name, in, column, converter.toDatabase( value ), type );
	}

    public static <T> OperatorCondition<T> forColumnBind(Column<T> c, Operator op)
    {
        return new OperatorCondition<T>( c.getQuotedName(), c, c.getName(), c.getSelectionExpression(), op.getSymbol(), null, c.getType(), c.getConverter() );
    }

    public static <T> OperatorCondition<T> forForeignColumnBind(ForeignColumn<T> c, Operator op)
    {
        return new OperatorCondition<T>( c.getQuotedName(), c.getForeignColumn(), c.getForeignColumn().getName(), c.getSelectionExpression(), op.getSymbol(), null, c.getType(), c.getConverter() );
    }
	
}
