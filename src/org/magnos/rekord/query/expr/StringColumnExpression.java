
package org.magnos.rekord.query.expr;

import org.magnos.rekord.field.Column;
import org.magnos.rekord.query.condition.OperatorCondition;

public class StringColumnExpression<R> extends ColumnExpression<R, String>
{

    public StringColumnExpression( R returning, GroupExpression<R> group, String prepend, Column<String> column )
    {
        super( returning, group, prepend, column );
    }
    
    protected R newOperationCondition(String symbol, String value)
    {
        return addAndGet( new OperatorCondition<String>( column, symbol, value ) );
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
        
        return addAndGet( new OperatorCondition<String>( columnName, null, column.getName(), "UPPER(?)", " = ", value, column.getType(), column.getConverter() ) );
    }
    
}
