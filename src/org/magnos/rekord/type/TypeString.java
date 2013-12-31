
package org.magnos.rekord.type;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.magnos.rekord.Type;


public class TypeString implements Type<String>
{
    
    public static final TypeString INSTANCE = new TypeString();

    @Override
    public String getPartialExpression( String in, int limit )
    {
        return "substring(" + in + " for " + limit + ")";
    }

    @Override
    public String fromResultSet( ResultSet resultSet, String column, boolean nullable ) throws SQLException
    {
        return resultSet.getString( column );
    }

    @Override
    public String fromResultSet( ResultSet resultSet, int column, boolean nullable ) throws SQLException
    {
        return resultSet.getString( column );
    }

    @Override
    public boolean isPartial( String value, int limit )
    {
        return value.length() < limit;
    }

    @Override
    public void toPreparedStatement( PreparedStatement preparedStatement, String value, int paramIndex ) throws SQLException
    {
        preparedStatement.setString( paramIndex, value );
    }

}
