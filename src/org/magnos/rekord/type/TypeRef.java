
package org.magnos.rekord.type;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Ref;

import org.magnos.rekord.Type;


public class TypeRef implements Type<Ref>
{
    
    public static final TypeRef INSTANCE = new TypeRef();

    @Override
    public String getPartialExpression( String in, int limit )
    {
        return in;
    }

    @Override
    public Ref fromResultSet( ResultSet resultSet, String column, boolean nullable ) throws SQLException
    {
        return resultSet.getRef( column );
    }

    @Override
    public Ref fromResultSet( ResultSet resultSet, int column, boolean nullable ) throws SQLException
    {
        return resultSet.getRef( column );
    }

    @Override
    public boolean isPartial( Ref value, int limit )
    {
        return false;
    }

    @Override
    public void toPreparedStatement( PreparedStatement preparedStatement, Ref value, int paramIndex ) throws SQLException
    {
        preparedStatement.setRef( paramIndex, value );
    }

}
