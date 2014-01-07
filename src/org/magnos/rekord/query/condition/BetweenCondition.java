package org.magnos.rekord.query.condition;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.magnos.rekord.Converter;
import org.magnos.rekord.Rekord;
import org.magnos.rekord.Type;
import org.magnos.rekord.convert.NoConverter;
import org.magnos.rekord.field.Column;

public class BetweenCondition<T> implements Condition
{
	
	public String expression;
	public T min;
	public T max;
	public Type<Object> type;
	public Converter<Object, T> converter;

	public BetweenCondition(Column<T> column, T min, T max)
	{
		this( column.getQuotedName(), min, max, column.getType(), column.getConverter() );
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
	public void toQuery( StringBuilder query )
	{
		query.append( expression );
		query.append( " BETWEEN ? AND ?" );
	}

	@Override
	public int toPreparedstatement( PreparedStatement stmt, int paramIndex ) throws SQLException
	{
		type.toPreparedStatement( stmt, converter.toDatabase( min ), paramIndex++ );
		type.toPreparedStatement( stmt, converter.toDatabase( min ), paramIndex++ );
		
		return paramIndex;
	}

}
