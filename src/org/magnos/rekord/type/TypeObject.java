
package org.magnos.rekord.type;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.magnos.rekord.Type;


public class TypeObject implements Type<Object>
{
    
    public static final TypeObject INSTANCE = new TypeObject();

    @Override
    public String getPartialExpression( String in, int limit, String alias )
    {
        return in;
    }

    @Override
    public Object fromResultSet( ResultSet resultSet, String column, boolean nullable ) throws SQLException
    {
        return resultSet.getObject( column );
    }

    @Override
    public Object fromResultSet( ResultSet resultSet, int column, boolean nullable ) throws SQLException
    {
        return resultSet.getObject( column );
    }

    @Override
    public boolean isPartial( Object value, int limit )
    {
        return false;
    }

    @Override
    public void toPreparedStatement( PreparedStatement preparedStatement, Object value, int paramIndex ) throws SQLException
    {
        preparedStatement.setObject( paramIndex, value );
    }

    @Override
    public String toString( Object value )
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object fromString( String x )
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toQueryString( Object value )
    {
        return null;
    }

}
