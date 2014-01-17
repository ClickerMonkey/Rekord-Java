package org.magnos.rekord.query;

import org.magnos.rekord.Table;
import org.magnos.rekord.query.expr.ExpressionChain;

public class Join extends ExpressionChain<Join>
{
    public static final String INNER = " INNER JOIN ";
    public static final String LEFT = " LEFT OUTER JOIN ";
    public static final String RIGHT = " RIGHT OUTER JOIN ";
    public static final String FULL = " FULL OUTER JOIN ";
	
    private String joinType;
    private Table table;
    private String tableExpression;
    
    public Join(String joinType, Table table)
    {
        this( joinType, table, table.getQuotedName() );
    }
    
    public Join(String joinType, TableAlias aliased )
    {
        this( joinType, aliased.getTable(), aliased.getSelectionExpression() );
        
        this.columnResolver = aliased;
    }
    
    public Join(String joinType, Table table, String tableExpression)
    {
        this.joinType = joinType;
        this.table = table;
        this.tableExpression = tableExpression;
    }
    
    public void toQuery( QueryBuilder query )
    {
        query.append( joinType );
        query.append( tableExpression );
        query.append( " ON " );
        
        super.toQuery( query );
    }

    public String getJoinType()
    {
        return joinType;
    }

    public Table getTable()
    {
        return table;
    }
    
    public String getTableExpression()
    {
        return tableExpression;
    }
    
}
