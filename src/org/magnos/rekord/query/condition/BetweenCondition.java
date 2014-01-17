package org.magnos.rekord.query.condition;

import org.magnos.rekord.Converter;
import org.magnos.rekord.Rekord;
import org.magnos.rekord.Type;
import org.magnos.rekord.convert.NoConverter;
import org.magnos.rekord.field.Column;
import org.magnos.rekord.query.QueryBuilder;
import org.magnos.rekord.query.expr.ColumnResolver;

public class BetweenCondition<T> implements Condition
{
	
	public String expression;
	public T min;
	public T max;
	public Type<Object> type;
	public Converter<Object, T> converter;

    public BetweenCondition(Column<T> column, T min, T max)
    {
        this( column.getSelectionExpression(), min, max, column.getType(), column.getConverter() );
    }

    public BetweenCondition(ColumnResolver resolver, Column<T> column, T min, T max)
    {
        this( resolver.resolve( column ), min, max, column.getType(), column.getConverter() );
    }

	public BetweenCondition(String expression, T min, T max)
	{
		this( expression, min, max, Rekord.getTypeForObject( (Object)min ), (Converter<Object, T>) NoConverter.INSTANCE );
	}
	
	public BetweenCondition(String expression, T min, T max, Type<Object> type, Converter<Object, T> converter)
	{
		this.expression = expression;
		this.min = min;
		this.max = max;
		this.type = type;
		this.converter = converter;
	}
	
	@Override
	public void toQuery( QueryBuilder query )
	{
		query.appendValuable( expression );
		query.append( " BETWEEN " );
		query.append( "?", null, converter.toDatabase( min ), type );
		query.append( " AND " );
		query.append( "?", null, converter.toDatabase( max ), type );
	}

}
