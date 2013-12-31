
package org.magnos.rekord.type;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.magnos.rekord.Type;


public class TypeDouble implements Type<Double>
{

    public static final TypeDouble INSTANCE = new TypeDouble();
    
    @Override
    public String getPartialExpression( String in, int limit )
    {
        return in;
    }

    @Override
    public Double fromResultSet( ResultSet resultSet, String column, boolean nullable ) throws SQLException
    {
        double value = resultSet.getDouble( column );

        return (nullable && resultSet.wasNull() ? null : value);
    }

    @Override
    public Double fromResultSet( ResultSet resultSet, int column, boolean nullable ) throws SQLException
    {
        double value = resultSet.getDouble( column );

        return (nullable && resultSet.wasNull() ? null : value);
    }

    @Override
    public boolean isPartial( Double value, int limit )
    {
        return false;
    }

    @Override
    public void toPreparedStatement( PreparedStatement preparedStatement, Double value, int paramIndex ) throws SQLException
    {
        if (value == null)
        {
            preparedStatement.setNull( paramIndex, Types.DOUBLE );
        }
        else
        {
            preparedStatement.setDouble( paramIndex, value );
        }
    }

}
