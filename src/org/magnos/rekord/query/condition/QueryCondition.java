package org.magnos.rekord.query.condition;

import org.magnos.rekord.query.QueryBuilder;
import org.magnos.rekord.query.SelectQuery;


public class QueryCondition implements Condition
{

    public SelectQuery<?> subquery;
    public String expression;
    public Object[] values;
    
    public QueryCondition(SelectQuery<?> subquery, String expression, Object ... values)
    {
        this.subquery = subquery;
        this.expression = expression;
        this.values = values;
    }
    
    @Override
    public void toQuery( QueryBuilder query )
    {
        query.append( "(" );
        subquery.toQueryBuilder( query );
        query.append( ")" );
        
        query.appendValuable( expression, values );
    }

}
