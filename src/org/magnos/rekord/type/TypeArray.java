
package org.magnos.rekord.type;

import java.sql.Array;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.magnos.rekord.Type;


public class TypeArray implements Type<Array>
{
    
    public static final TypeArray INSTANCE = new TypeArray();

    @Override
    public String getPartialExpression( String in, int limit, String alias )
    {
        return in;
    }

    @Override
    public Array fromResultSet( ResultSet resultSet, String column, boolean nullable ) throws SQLException
    {
        return resultSet.getArray( column );
    }

    @Override
    public Array fromResultSet( ResultSet resultSet, int column, boolean nullable ) throws SQLException
    {
        return resultSet.getArray( column );
    }

    @Override
    public boolean isPartial( Array value, int limit )
    {
        return false;
    }

    @Override
    public void toPreparedStatement( PreparedStatement preparedStatement, Array value, int paramIndex ) throws SQLException
    {
        preparedStatement.setArray( paramIndex, value );
    }

    @Override
    public String toString( Array value )
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Array fromString( String x )
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toQueryString( Array value )
    {
        return null;
    }

}
