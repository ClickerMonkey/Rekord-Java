
package org.magnos.rekord.type;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.magnos.rekord.Type;


public class TypeByteArray implements Type<byte[]>
{
    
    public static final TypeByteArray INSTANCE = new TypeByteArray();

    @Override
    public String getPartialExpression( String in, int limit )
    {
        return "substring(" + in + " for " + limit + ")";
    }

    @Override
    public byte[] fromResultSet( ResultSet resultSet, String column, boolean nullable ) throws SQLException
    {
        return resultSet.getBytes( column );
    }

    @Override
    public byte[] fromResultSet( ResultSet resultSet, int column, boolean nullable ) throws SQLException
    {
        return resultSet.getBytes( column );
    }

    @Override
    public boolean isPartial( byte[] value, int limit )
    {
        return value.length < limit;
    }

    @Override
    public void toPreparedStatement( PreparedStatement preparedStatement, byte[] value, int paramIndex ) throws SQLException
    {
        preparedStatement.setBytes( paramIndex, value );
    }

}
