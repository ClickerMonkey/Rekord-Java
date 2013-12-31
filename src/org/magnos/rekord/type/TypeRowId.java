
package org.magnos.rekord.type;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.RowId;
import java.sql.SQLException;

import org.magnos.rekord.Type;


public class TypeRowId implements Type<RowId>
{
    
    public static final TypeRowId INSTANCE = new TypeRowId();

    @Override
    public String getPartialExpression( String in, int limit )
    {
        return in;
    }

    @Override
    public RowId fromResultSet( ResultSet resultSet, String column, boolean nullable ) throws SQLException
    {
        return resultSet.getRowId( column );
    }

    @Override
    public RowId fromResultSet( ResultSet resultSet, int column, boolean nullable ) throws SQLException
    {
        return resultSet.getRowId( column );
    }

    @Override
    public boolean isPartial( RowId value, int limit )
    {
        return false;
    }

    @Override
    public void toPreparedStatement( PreparedStatement preparedStatement, RowId value, int paramIndex ) throws SQLException
    {
        preparedStatement.setRowId( paramIndex, value );
    }

}
