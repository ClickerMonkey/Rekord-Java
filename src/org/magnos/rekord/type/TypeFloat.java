
package org.magnos.rekord.type;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.magnos.rekord.Type;


public class TypeFloat implements Type<Float>
{

    public static final TypeFloat INSTANCE = new TypeFloat();
    
    @Override
    public String getPartialExpression( String in, int limit, String alias )
    {
        return in;
    }

    @Override
    public Float fromResultSet( ResultSet resultSet, String column, boolean nullable ) throws SQLException
    {
        float value = resultSet.getFloat( column );

        return (nullable && resultSet.wasNull() ? null : value);
    }

    @Override
    public Float fromResultSet( ResultSet resultSet, int column, boolean nullable ) throws SQLException
    {
        float value = resultSet.getFloat( column );

        return (nullable && resultSet.wasNull() ? null : value);
    }

    @Override
    public boolean isPartial( Float value, int limit )
    {
        return false;
    }

    @Override
    public void toPreparedStatement( PreparedStatement preparedStatement, Float value, int paramIndex ) throws SQLException
    {
        if (value == null)
        {
            preparedStatement.setNull( paramIndex, Types.FLOAT );
        }
        else
        {
            preparedStatement.setFloat( paramIndex, value );
        }
    }

    @Override
    public String toString( Float value )
    {
        return (value == null ? null : value.toString());
    }

    @Override
    public Float fromString( String x )
    {
        return (x == null ? null : Float.valueOf( x ));
    }

    @Override
    public String toQueryString( Float value )
    {
        return toString( value );
    }

}
