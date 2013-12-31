
package org.magnos.rekord.type;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.magnos.rekord.Type;


public class TypeTime implements Type<Time>
{
    
    public static final TypeTime INSTANCE = new TypeTime();

    public static final String DATE_FORMAT = "HH:mm:ss.SSSSS";

    public static final ThreadLocal<SimpleDateFormat> DATE_FORMATTER_LOCAL = new ThreadLocal<SimpleDateFormat>()
    {
        protected SimpleDateFormat initialValue()
        {
            return new SimpleDateFormat( DATE_FORMAT );
        }
    };

    @Override
    public String getPartialExpression( String in, int limit )
    {
        return in;
    }

    @Override
    public Time fromResultSet( ResultSet resultSet, String column, boolean nullable ) throws SQLException
    {
        return resultSet.getTime( column );
    }

    @Override
    public Time fromResultSet( ResultSet resultSet, int column, boolean nullable ) throws SQLException
    {
        return resultSet.getTime( column );
    }

    @Override
    public boolean isPartial( Time value, int limit )
    {
        return false;
    }

    @Override
    public void toPreparedStatement( PreparedStatement preparedStatement, Time value, int paramIndex ) throws SQLException
    {
        preparedStatement.setTime( paramIndex, value );
    }

    @Override
    public String toString( Time value )
    {
        return (value == null ? null : DATE_FORMATTER_LOCAL.get().format( value ));
    }

    @Override
    public Time fromString( String x )
    {
        if (x == null)
        {
            return null;
        }

        try
        {
            return new Time( DATE_FORMATTER_LOCAL.get().parse( x ).getTime() );
        }
        catch (ParseException e)
        {
            throw new RuntimeException( e );
        }
    }

}
