
package org.magnos.rekord.query;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.magnos.rekord.Model;
import org.magnos.rekord.Rekord;
import org.magnos.rekord.Table;
import org.magnos.rekord.Transaction;
import org.magnos.rekord.Type;
import org.magnos.rekord.field.Column;
import org.magnos.rekord.type.TypeObject;


public class NativeQuery<M extends Model>
{

    public static final Pattern TOKEN_PATTERN = Pattern.compile( "(#[\\w_][\\w_\\d\\$]*|\\?[\\w_][\\w_\\d\\$]*)" );

    protected final NativeQueryTemplate<M> query;
    protected final Object[] values;
    protected final Type<Object>[] types;

    public NativeQuery( NativeQueryTemplate<M> query )
    {
        this.query = query;
        this.values = new Object[query.sets()];
        this.types = new Type[query.sets()];
    }

    protected PreparedStatement prepare() throws SQLException
    {
        Transaction trans = Rekord.getTransaction();
        PreparedStatement stmt = trans.prepare( query.getQuery() );

        for (int i = 0; i < values.length; i++)
        {
            types[i].toPreparedStatement( stmt, values[i], i + 1 );
        }

        return stmt;
    }

    public NativeQuery<M> clear()
    {
        for (int i = 0; i < values.length; i++)
        {
            types[i] = null;
            values[i] = null;
        }

        return this;
    }

    public NativeQuery<M> bind( M model )
    {
        final Table table = model.getTable();

        for (int i = 0; i < values.length; i++)
        {
            Column<?> c = table.getField( query.setAt( i ).name );

            if (c != null)
            {
                types[i] = c.getType();
                values[i] = model.get( c );
            }
        }

        return this;
    }

    public NativeQuery<M> set( String variableName, Object value )
    {
        int i = query.setIndex( variableName );
        values[i] = value;
        types[i] = value != null ? (Type<Object>)Rekord.getType( value.getClass() ) : TypeObject.INSTANCE;

        return this;
    }

    public int executeUpdate() throws SQLException
    {
        PreparedStatement stmt = prepare();

        return stmt.executeUpdate();
    }

    public List<M> executeQuery() throws SQLException
    {
        PreparedStatement stmt = prepare();

        ResultSet results = stmt.executeQuery();

        try
        {
            return SelectQuery.collect( results, query.getTable(), query.getView(), query.getSelectFields(), new ArrayList<M>(), true );
        }
        finally
        {
            results.close();
        }
    }

    public NativeQueryTemplate<M> getQuery()
    {
        return query;
    }

    public Object[] getValues()
    {
        return values;
    }

    public Type<Object>[] getTypes()
    {
        return types;
    }

}
