
package org.magnos.rekord.type;

import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.magnos.rekord.Type;


public class TypeBlob implements Type<Blob>
{

    public static final TypeBlob INSTANCE = new TypeBlob();
    
    @Override
    public String getPartialExpression( String in, int limit )
    {
        return in;
    }

    @Override
    public Blob fromResultSet( ResultSet resultSet, String column, boolean nullable ) throws SQLException
    {
        return resultSet.getBlob( column );
    }

    @Override
    public Blob fromResultSet( ResultSet resultSet, int column, boolean nullable ) throws SQLException
    {
        return resultSet.getBlob( column );
    }

    @Override
    public boolean isPartial( Blob value, int limit )
    {
        return false;
    }

    @Override
    public void toPreparedStatement( PreparedStatement preparedStatement, Blob value, int paramIndex ) throws SQLException
    {
        preparedStatement.setBlob( paramIndex, value );
    }

}
