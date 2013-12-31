
package org.magnos.rekord.type;

import java.sql.Time;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.magnos.rekord.Type;


public class TypeTime implements Type<Time>
{
    
    public static final TypeTime INSTANCE = new TypeTime();

    @Override
    public String getPartialExpression( String in, int limit )
    {
        return in;
    }

    @Override
    public Time fromResultSet( ResultSet resultSet, String column, boolean nullable ) throws SQLException
    {
        return resultSet.getTime( column );
    }

    @Override
    public Time fromResultSet( ResultSet resultSet, int column, boolean nullable ) throws SQLException
    {
        return resultSet.getTime( column );
    }

    @Override
    public boolean isPartial( Time value, int limit )
    {
        return false;
    }

    @Override
    public void toPreparedStatement( PreparedStatement preparedStatement, Time value, int paramIndex ) throws SQLException
    {
        preparedStatement.setTime( paramIndex, value );
    }

}
