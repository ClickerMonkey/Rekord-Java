
package org.magnos.rekord.type;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.magnos.rekord.Type;


public class TypeBoolean implements Type<Boolean>
{

    public static final TypeBoolean INSTANCE = new TypeBoolean();

    private static final Set<String> TRUES = new HashSet<String>( Arrays.asList( "1", "t", "true", "y", "ya", "yes", "yessums" ) );
    private static final Set<String> FALSES = new HashSet<String>( Arrays.asList( "0", "f", "false", "n", "nah", "no", "nope" ) );
    
    public static boolean parse(String value, String valueName)
    {
        if (value != null)
        {
            value = value.toLowerCase();
            
            if (TRUES.contains( value ))
            {
                return true;
            }
            
            if (FALSES.contains( value ))
            {
                return false;
            }
        }
        
        throw new RuntimeException(valueName + " was not a valid boolean value (" + value + "), acceptable trues: " + TRUES + ", acceptable falses: " + FALSES);
    }
    
    @Override
    public String getPartialExpression( String in, int limit, String alias )
    {
        return in;
    }

    @Override
    public Boolean fromResultSet( ResultSet resultSet, String column, boolean nullable ) throws SQLException
    {
        boolean value = resultSet.getBoolean( column );

        return (nullable && resultSet.wasNull() ? null : value);
    }

    @Override
    public Boolean fromResultSet( ResultSet resultSet, int column, boolean nullable ) throws SQLException
    {
        boolean value = resultSet.getBoolean( column );

        return (nullable && resultSet.wasNull() ? null : value);
    }

    @Override
    public boolean isPartial( Boolean value, int limit )
    {
        return false;
    }

    @Override
    public void toPreparedStatement( PreparedStatement preparedStatement, Boolean value, int paramIndex ) throws SQLException
    {
        if (value == null)
        {
            preparedStatement.setNull( paramIndex, Types.BIT );
        }
        else
        {
            preparedStatement.setBoolean( paramIndex, value );
        }
    }

    @Override
    public String toString( Boolean value )
    {
        return (value == null ? null : value.toString());
    }

    @Override
    public Boolean fromString( String x )
    {
        return parse( x, "the given string" );
    }

    @Override
    public String toQueryString( Boolean value )
    {
        return toString( value );
    }

}
