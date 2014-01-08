
package org.magnos.rekord.type;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.magnos.rekord.Type;


public class TypeTimestamp implements Type<Timestamp>
{
    
    public static final TypeTimestamp INSTANCE = new TypeTimestamp();

    public static final String DATE_FORMAT = "MM/dd/yyyy HH:mm:ss.SSSSS";

    public static final ThreadLocal<SimpleDateFormat> DATE_FORMATTER_LOCAL = new ThreadLocal<SimpleDateFormat>()
    {
        protected SimpleDateFormat initialValue()
        {
            return new SimpleDateFormat( DATE_FORMAT );
        }
    };

    @Override
    public String getPartialExpression( String in, int limit, String alias )
    {
        return in;
    }

    @Override
    public Timestamp fromResultSet( ResultSet resultSet, String column, boolean nullable ) throws SQLException
    {
        return resultSet.getTimestamp( column );
    }

    @Override
    public Timestamp fromResultSet( ResultSet resultSet, int column, boolean nullable ) throws SQLException
    {
        return resultSet.getTimestamp( column );
    }

    @Override
    public boolean isPartial( Timestamp value, int limit )
    {
        return false;
    }

    @Override
    public void toPreparedStatement( PreparedStatement preparedStatement, Timestamp value, int paramIndex ) throws SQLException
    {
        preparedStatement.setTimestamp( paramIndex, value );
    }

    @Override
    public String toString( Timestamp value )
    {
        return (value == null ? null : DATE_FORMATTER_LOCAL.get().format( value ));
    }

    @Override
    public Timestamp fromString( String x )
    {
        if (x == null)
        {
            return null;
        }

        try
        {
            return new Timestamp( DATE_FORMATTER_LOCAL.get().parse( x ).getTime() );
        }
        catch (ParseException e)
        {
            throw new RuntimeException( e );
        }
    }

    @Override
    public String toQueryString( Timestamp value )
    {
        return "'" + toString( value ) + "'";
    }

}
