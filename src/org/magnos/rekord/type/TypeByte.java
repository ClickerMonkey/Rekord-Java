
package org.magnos.rekord.type;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.magnos.rekord.Type;


public class TypeByte implements Type<Byte>
{

    public static final TypeByte INSTANCE = new TypeByte();
    
    @Override
    public String getPartialExpression( String in, int limit )
    {
        return in;
    }

    @Override
    public Byte fromResultSet( ResultSet resultSet, String column, boolean nullable ) throws SQLException
    {
        byte value = resultSet.getByte( column );

        return (nullable && resultSet.wasNull() ? null : value);
    }

    @Override
    public Byte fromResultSet( ResultSet resultSet, int column, boolean nullable ) throws SQLException
    {
        byte value = resultSet.getByte( column );

        return (nullable && resultSet.wasNull() ? null : value);
    }

    @Override
    public boolean isPartial( Byte value, int limit )
    {
        return false;
    }

    @Override
    public void toPreparedStatement( PreparedStatement preparedStatement, Byte value, int paramIndex ) throws SQLException
    {
        if (value == null)
        {
            preparedStatement.setNull( paramIndex, Types.TINYINT );
        }
        else
        {
            preparedStatement.setByte( paramIndex, value );
        }
    }

}
