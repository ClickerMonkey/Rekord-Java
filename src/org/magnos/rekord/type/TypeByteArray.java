
package org.magnos.rekord.type;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.magnos.rekord.Type;


public class TypeByteArray implements Type<byte[]>
{

    public static final TypeByteArray INSTANCE = new TypeByteArray();

    public static final char[] HEX = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

    @Override
    public String getPartialExpression( String in, int limit, String alias )
    {
        return "substring(" + in + " for " + limit + ") as " + alias;
    }

    @Override
    public byte[] fromResultSet( ResultSet resultSet, String column, boolean nullable ) throws SQLException
    {
        return resultSet.getBytes( column );
    }

    @Override
    public byte[] fromResultSet( ResultSet resultSet, int column, boolean nullable ) throws SQLException
    {
        return resultSet.getBytes( column );
    }

    @Override
    public boolean isPartial( byte[] value, int limit )
    {
        return value.length < limit;
    }

    @Override
    public void toPreparedStatement( PreparedStatement preparedStatement, byte[] value, int paramIndex ) throws SQLException
    {
        preparedStatement.setBytes( paramIndex, value );
    }

    @Override
    public String toString( byte[] value )
    {
        if (value == null)
        {
            return null;
        }

        StringBuilder sb = new StringBuilder( value.length << 1 );

        for (byte b : value)
        {
            int c0 = (b & 0xF0) >> 4;
            int c1 = (b & 0x0F) >> 0;

            sb.append( HEX[c0] );
            sb.append( HEX[c1] );
        }

        return sb.toString();
    }

    @Override
    public byte[] fromString( String x )
    {
        if (x == null)
        {
            return null;
        }

        if ((x.length() & 1) == 1)
        {
            throw new RuntimeException( "A byte[] string representation cannot have an odd number of characters: " + x );
        }

        byte[] bytes = new byte[x.length() >> 1];

        for (int i = 0; i < x.length(); i += 2)
        {
            int c0 = hexFromChar( x.charAt( i + 0 ) );
            int c1 = hexFromChar( x.charAt( i + 1 ) );
            
            bytes[i >> 1] = (byte)((c0 << 4) | c1);
        }

        return bytes;
    }

    private int hexFromChar( char c )
    {
        if (c >= '0' && c <= '9')
        {
            return c - '0';
        }
        if (c >= 'A' && c <= 'F')
        {
            return c - 'A' + 10;
        }
        if (c >= 'a' && c <= 'f')
        {
            return c - 'a' + 10;
        }
        throw new RuntimeException( c + " is not a valid hex character!" );
    }

    @Override
    public String toQueryString( byte[] value )
    {
        return "decode('" + toString( value ) + "')";
    }

}
