
package org.magnos.rekord.query;

import org.magnos.rekord.Table;
import org.magnos.rekord.field.Column;
import org.magnos.rekord.query.expr.ColumnResolver;


public class TableAlias implements ColumnResolver
{

    private final Table table;
    private final String alias;

    public TableAlias( Table table, String alias )
    {
        this.table = table;
        this.alias = alias;
    }

    public <T> ColumnAlias<T> alias( Column<T> column )
    {
        return new ColumnAlias<T>( this, column );
    }

    public Table getTable()
    {
        return table;
    }

    public String getAlias()
    {
        return alias;
    }
    
    public String getSelectionExpression()
    {
        return table.getQuotedName() + " AS " + alias;
    }
    
    @Override
    public String resolve( Column<?> column )
    {
        return alias + "." + column.getSelectionExpression();
    }

}
