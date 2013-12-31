
package org.magnos.rekord.type;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.magnos.rekord.Type;


public class TypeBoolean implements Type<Boolean>
{

    public static final TypeBoolean INSTANCE = new TypeBoolean();
    
    @Override
    public String getPartialExpression( String in, int limit )
    {
        return in;
    }

    @Override
    public Boolean fromResultSet( ResultSet resultSet, String column, boolean nullable ) throws SQLException
    {
        boolean value = resultSet.getBoolean( column );

        return (nullable && resultSet.wasNull() ? null : value);
    }

    @Override
    public Boolean fromResultSet( ResultSet resultSet, int column, boolean nullable ) throws SQLException
    {
        boolean value = resultSet.getBoolean( column );

        return (nullable && resultSet.wasNull() ? null : value);
    }

    @Override
    public boolean isPartial( Boolean value, int limit )
    {
        return false;
    }

    @Override
    public void toPreparedStatement( PreparedStatement preparedStatement, Boolean value, int paramIndex ) throws SQLException
    {
        if (value == null)
        {
            preparedStatement.setNull( paramIndex, Types.BIT );
        }
        else
        {
            preparedStatement.setBoolean( paramIndex, value );
        }
    }

}
