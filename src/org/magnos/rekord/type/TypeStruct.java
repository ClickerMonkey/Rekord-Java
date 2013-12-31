
package org.magnos.rekord.type;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Struct;

import org.magnos.rekord.Type;


public class TypeStruct implements Type<Struct>
{
    
    public static final TypeStruct INSTANCE = new TypeStruct();

    @Override
    public String getPartialExpression( String in, int limit )
    {
        return in;
    }

    @Override
    public Struct fromResultSet( ResultSet resultSet, String column, boolean nullable ) throws SQLException
    {
        return (Struct) resultSet.getObject( column );
    }

    @Override
    public Struct fromResultSet( ResultSet resultSet, int column, boolean nullable ) throws SQLException
    {
        return (Struct) resultSet.getObject( column );
    }

    @Override
    public boolean isPartial( Struct value, int limit )
    {
        return false;
    }

    @Override
    public void toPreparedStatement( PreparedStatement preparedStatement, Struct value, int paramIndex ) throws SQLException
    {
        preparedStatement.setObject( paramIndex, value );
    }

    @Override
    public String toString( Struct value )
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Struct fromString( String x )
    {
        throw new UnsupportedOperationException();
    }

}
