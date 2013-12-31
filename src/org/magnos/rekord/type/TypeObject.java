
package org.magnos.rekord.type;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.magnos.rekord.Type;


public class TypeObject implements Type<Object>
{
    
    public static final TypeObject INSTANCE = new TypeObject();

    @Override
    public String getPartialExpression( String in, int limit )
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

}
