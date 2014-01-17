package org.magnos.rekord.query.condition;

import org.magnos.rekord.Converter;
import org.magnos.rekord.Rekord;
import org.magnos.rekord.Type;
import org.magnos.rekord.convert.NoConverter;
import org.magnos.rekord.field.Column;
import org.magnos.rekord.query.QueryBuilder;
import org.magnos.rekord.query.expr.ColumnResolver;

public class InCondition<T> implements Condition
{

	public boolean not;
	public String expression;
	public T[] values;
	public Type<Object> type;
	public Converter<Object, T> converter;

    public InCondition(Column<T> column, boolean not, T ... values)
    {
        this( column.getSelectionExpression(), not, column.getType(), column.getConverter(), values );
    }
    
    public InCondition(ColumnResolver resolver, Column<T> column, boolean not, T ... values)
    {
        this( resolver.resolve( column ), not, column.getType(), column.getConverter(), values );
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
	public void toQuery( QueryBuilder query )
	{
		query.appendValuable( expression );
		
		if (not)
		{
			query.append( " NOT" );
		}
		
		query.append( " IN (" );
		
		for (int i = 0; i < values.length; i++)
		{
			query.append( "?", null, converter.toDatabase( values[i] ), type );
		}
		
		query.append( ")" );
	}
	
	public static String generateParameters(int count)
	{
	    StringBuilder sb = new StringBuilder();

	    for (int i = 0; i < count; i++)
	    {
            sb.append( ", ?" );
	    }
	    
	    return sb.substring( 2 );
	}

}
