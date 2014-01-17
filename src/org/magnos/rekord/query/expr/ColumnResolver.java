
package org.magnos.rekord.query.expr;

import org.magnos.rekord.field.Column;


public interface ColumnResolver
{

    public String resolve( Column<?> column );

    public static final ColumnResolver DEFAULT = new ColumnResolver() {

        public String resolve( Column<?> column )
        {
            return column.getSelectionExpression();
        }
    };
}