
package org.magnos.rekord;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public interface Type<T>
{
    public String getPartialExpression( String in, int limit, String alias );

    public T fromResultSet( ResultSet resultSet, String column, boolean nullable ) throws SQLException;

    public T fromResultSet( ResultSet resultSet, int column, boolean nullable ) throws SQLException;

    public boolean isPartial( T value, int limit );

    public void toPreparedStatement( PreparedStatement preparedStatement, T value, int paramIndex ) throws SQLException;

    public String toString( T value );

    public T fromString( String x );
}
