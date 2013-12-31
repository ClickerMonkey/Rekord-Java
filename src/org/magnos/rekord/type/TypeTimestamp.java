
package org.magnos.rekord.type;

import java.sql.Timestamp;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.magnos.rekord.Type;


public class TypeTimestamp implements Type<Timestamp>
{
    
    public static final TypeTimestamp INSTANCE = new TypeTimestamp();

    @Override
    public String getPartialExpression( String in, int limit )
    {
        return in;
    }

    @Override
    public Timestamp fromResultSet( ResultSet resultSet, String column, boolean nullable ) throws SQLException
    {
        return resultSet.getTimestamp( column );
    }

    @Override
    public Timestamp fromResultSet( ResultSet resultSet, int column, boolean nullable ) throws SQLException
    {
        return resultSet.getTimestamp( column );
    }

    @Override
    public boolean isPartial( Timestamp value, int limit )
    {
        return false;
    }

    @Override
    public void toPreparedStatement( PreparedStatement preparedStatement, Timestamp value, int paramIndex ) throws SQLException
    {
        preparedStatement.setTimestamp( paramIndex, value );
    }

}
