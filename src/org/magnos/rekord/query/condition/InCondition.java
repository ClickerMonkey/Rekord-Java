package org.magnos.rekord.query.condition;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.magnos.rekord.Converter;
import org.magnos.rekord.Rekord;
import org.magnos.rekord.Type;
import org.magnos.rekord.convert.NoConverter;
import org.magnos.rekord.field.Column;

public class InCondition<T> implements Condition
{

	public boolean not;
	public String expression;
	public T[] values;
	public Type<Object> type;
	public Converter<Object, T> converter;
	
	public InCondition(Column<T> column, boolean not, T ... values)
	{
		this( column.getQuotedName(), not, column.getType(), column.getConverter(), values );
	}
	
	public InCondition(String expression, boolean not, T ... values)
	{
		this( expression, not, Rekord.getTypeForObject( (Object)values[0] ), (Converter<Object, T>) NoConverter.INSTANCE, values );
	}
	
	public InCondition(String expression, boolean not, Type<Object> type, Converter<Object, T> converter, T ... values)
	{
		this.expression = expression;
		this.not = not;
		this.type = type;
		this.converter = converter;
		this.values = values;
	}
	
	@Override
	public void toQuery( StringBuilder query )
	{
		query.append( expression );
		
		if (not)
		{
			query.append( " NOT" );
		}
		
		query.append( " IN (" );
		
		for (int i = 0; i < values.length; i++)
		{
			if (i > 0) query.append( ',' );
			query.append( '?' );
		}
		
		query.append( ")" );
	}

	@Override
	public int toPreparedstatement( PreparedStatement stmt, int paramIndex ) throws SQLException
	{
		for (int i = 0; i < values.length; i++)
		{
			type.toPreparedStatement( stmt, converter.convertTo( values[i] ), paramIndex++ );
		}
		
		return paramIndex;
	}

}
