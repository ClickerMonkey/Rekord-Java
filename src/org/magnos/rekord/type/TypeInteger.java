
package org.magnos.rekord.type;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.magnos.rekord.Type;


public class TypeInteger implements Type<Integer>
{
    
    public static final TypeInteger INSTANCE = new TypeInteger();

    @Override
    public String getPartialExpression( String in, int limit )
    {
        return in;
    }

    @Override
    public Integer fromResultSet( ResultSet resultSet, String column, boolean nullable ) throws SQLException
    {
        int value = resultSet.getInt( column );

        return (nullable && resultSet.wasNull() ? null : value);
    }

    @Override
    public Integer fromResultSet( ResultSet resultSet, int column, boolean nullable ) throws SQLException
    {
        int value = resultSet.getInt( column );

        return (nullable && resultSet.wasNull() ? null : value);
    }

    @Override
    public boolean isPartial( Integer value, int limit )
    {
        return false;
    }

    @Override
    public void toPreparedStatement( PreparedStatement preparedStatement, Integer value, int paramIndex ) throws SQLException
    {
        if (value == null)
        {
            preparedStatement.setNull( paramIndex, Types.INTEGER );
        }
        else
        {
            preparedStatement.setInt( paramIndex, value );
        }
    }

    @Override
    public String toString( Integer value )
    {
        return (value == null ? null : value.toString());
    }

    @Override
    public Integer fromString( String x )
    {
        return (x == null ? null : Integer.valueOf( x ));
    }

}
