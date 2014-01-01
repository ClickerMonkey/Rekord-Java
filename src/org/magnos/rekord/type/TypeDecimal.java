
package org.magnos.rekord.type;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.magnos.rekord.Type;


public class TypeDecimal implements Type<BigDecimal>
{
    
    public static final TypeDecimal INSTANCE = new TypeDecimal();

    @Override
    public String getPartialExpression( String in, int limit, String alias )
    {
        return in;
    }

    @Override
    public BigDecimal fromResultSet( ResultSet resultSet, String column, boolean nullable ) throws SQLException
    {
        return resultSet.getBigDecimal( column );
    }

    @Override
    public BigDecimal fromResultSet( ResultSet resultSet, int column, boolean nullable ) throws SQLException
    {
        return resultSet.getBigDecimal( column );
    }

    @Override
    public boolean isPartial( BigDecimal value, int limit )
    {
        return false;
    }

    @Override
    public void toPreparedStatement( PreparedStatement preparedStatement, BigDecimal value, int paramIndex ) throws SQLException
    {
        preparedStatement.setBigDecimal( paramIndex, value );
    }

    @Override
    public String toString( BigDecimal value )
    {
        return null;
    }

    @Override
    public BigDecimal fromString( String x )
    {
        return null;
    }

}
