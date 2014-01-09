
package org.magnos.rekord.query.expr;

import org.magnos.rekord.field.Column;
import org.magnos.rekord.query.condition.ConditionResolver;
import org.magnos.rekord.query.condition.OperatorCondition;

public class StringColumnExpression<R> extends ColumnExpression<R, String>
{

    public StringColumnExpression( ConditionResolver<R> resolver, Column<String> column )
    {
        super( resolver, column );
    }
    
    protected R newOperationCondition(String symbol, String value)
    {
        return resolver.resolve( new OperatorCondition<String>( column, symbol, value ) );
    }
    
    public R like( String value )
    {
        return newOperationCondition( " LIKE ", value );
    }
    
    public R ilike( String value )
    {
        return newOperationCondition( " ILIKE ", value );
    }
    
    public R ieq( String value )
    {
        String columnName = "UPPER(" + column.getQuotedName() + ")";
        
        return resolver.resolve( new OperatorCondition<String>( columnName, null, column.getName(), "UPPER(?)", " = ", value, column.getType(), column.getConverter() ) );
    }
    
}
