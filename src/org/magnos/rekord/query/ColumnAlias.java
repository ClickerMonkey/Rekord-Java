
package org.magnos.rekord.query;

import org.magnos.rekord.field.Column;
import org.magnos.rekord.query.expr.ColumnResolver;

public class ColumnAlias<T> implements ColumnResolver
{

    private final TableAlias table;
    private final Column<T> column;

    protected ColumnAlias( TableAlias table, Column<T> column )
    {
        this.table = table;
        this.column = column;
    }

    public TableAlias getTable()
    {
        return table;
    }

    public Column<T> getColumn()
    {
        return column;
    }

    public String getSelectionExpression()
    {
        return table.resolve( column );
    }
    
    @Override
    public String resolve( Column<?> column )
    {
        return table.resolve( column );
    }

}
