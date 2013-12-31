
package org.magnos.rekord.type;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.magnos.rekord.Type;


public class TypeDate implements Type<Date>
{
    
    public static final TypeDate INSTANCE = new TypeDate();

    @Override
    public String getPartialExpression( String in, int limit )
    {
        return in;
    }

    @Override
    public Date fromResultSet( ResultSet resultSet, String column, boolean nullable ) throws SQLException
    {
        return resultSet.getDate( column );
    }

    @Override
    public Date fromResultSet( ResultSet resultSet, int column, boolean nullable ) throws SQLException
    {
        return resultSet.getDate( column );
    }

    @Override
    public boolean isPartial( Date value, int limit )
    {
        return false;
    }

    @Override
    public void toPreparedStatement( PreparedStatement preparedStatement, Date value, int paramIndex ) throws SQLException
    {
        preparedStatement.setDate( paramIndex, value );
    }

}
