
package org.magnos.rekord.type;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.magnos.rekord.Type;


public class TypeLong implements Type<Long>
{
    
    public static final TypeLong INSTANCE = new TypeLong();

    @Override
    public String getPartialExpression( String in, int limit )
    {
        return in;
    }

    @Override
    public Long fromResultSet( ResultSet resultSet, String column, boolean nullable ) throws SQLException
    {
        long value = resultSet.getLong( column );

        return (nullable && resultSet.wasNull() ? null : value);
    }

    @Override
    public Long fromResultSet( ResultSet resultSet, int column, boolean nullable ) throws SQLException
    {
        long value = resultSet.getLong( column );

        return (nullable && resultSet.wasNull() ? null : value);
    }

    @Override
    public boolean isPartial( Long value, int limit )
    {
        return false;
    }

    @Override
    public void toPreparedStatement( PreparedStatement preparedStatement, Long value, int paramIndex ) throws SQLException
    {
        if (value == null)
        {
            preparedStatement.setNull( paramIndex, Types.BIGINT );
        }
        else
        {
            preparedStatement.setLong( paramIndex, value );
        }
    }

}
