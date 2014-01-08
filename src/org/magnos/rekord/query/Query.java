
package org.magnos.rekord.query;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.magnos.rekord.Field;
import org.magnos.rekord.FieldLoad;
import org.magnos.rekord.Key;
import org.magnos.rekord.ListenerEvent;
import org.magnos.rekord.Logging;
import org.magnos.rekord.Model;
import org.magnos.rekord.Rekord;
import org.magnos.rekord.Table;
import org.magnos.rekord.Transaction;
import org.magnos.rekord.Type;
import org.magnos.rekord.Value;
import org.magnos.rekord.LoadProfile;
import org.magnos.rekord.field.Column;


public class Query<M extends Model>
{

    public static final String SELECTION_COUNT = "COUNT(*)";
    public static final String SELECTION_EXISTENTIAL = "1";

    protected final QueryTemplate<M> template;
    protected final Object[] values;
    protected final Type<Object>[] types;

    public Query( QueryTemplate<M> template )
    {
        this.template = template;
        this.values = new Object[template.getBindCount()];
        this.types = new Type[template.getBindCount()];
    }

    public QueryTemplate<M> getTemplate()
    {
        return template;
    }

    public Object[] getValues()
    {
        return values;
    }

    public String getReadableQuery()
    {
        StringBuilder readable = new StringBuilder();
        String query = template.getQuery();

        int paramIndex = 0;

        for (int i = 0; i < values.length; i++)
        {
            Object value = values[i];
            boolean quotable = (value instanceof String || value instanceof Date || value instanceof byte[]);
            int next = query.indexOf( '?', paramIndex );

            readable.append( query, paramIndex, next );

            if (quotable)
            {
                readable.append( "'" );
            }

            readable.append( types[i].toString( values[i] ) );

            if (quotable)
            {
                readable.append( "'" );
            }

            paramIndex = next + 1;
        }

        readable.append( query, paramIndex, query.length() );

        return readable.toString();
    }

    public Type<Object>[] getTypes()
    {
        return types;
    }

    protected PreparedStatement prepare( Transaction trans, String query ) throws SQLException
    {
        PreparedStatement stmt = trans.prepare( query );

        for (int i = 0; i < values.length; i++)
        {
            types[i].toPreparedStatement( stmt, values[i], i + 1 );
        }

        return stmt;
    }

    private ResultSet getResultsFromQuery( String query ) throws SQLException
    {
        Transaction trans = Rekord.getTransaction();
        PreparedStatement stmt = prepare( trans, query );

        return stmt.executeQuery();
    }

    public ResultSet getResults() throws SQLException
    {
        return getResultsFromQuery( template.getQuery() );
    }

    public ResultSet getResults( String alternativeSelection ) throws SQLException
    {
        return getResultsFromQuery( template.getQuery( alternativeSelection ) );
    }

    public Query<M> clear()
    {
        for (int i = 0; i < values.length; i++)
        {
            types[i] = null;
            values[i] = null;
        }

        return this;
    }

    public Query<M> bind( String variableName, Object value )
    {
        int i = template.indexOf( variableName );
        values[i] = value;
        types[i] = Rekord.getTypeForObject( value );

        return this;
    }

    public <T> Query<M> bind( Column<T> column, M model )
    {
        int i = template.indexOf( column.getName() );
        values[i] = column.getConverter().toDatabase( model.get( column ) );
        types[i] = column.getType();

        return this;
    }

    public <T> Query<M> bind( Column<T> column, T value )
    {
        int i = template.indexOf( column.getName() );
        values[i] = column.getConverter().toDatabase( value );
        types[i] = column.getType();

        return this;
    }

    public Query<M> bind( Model model )
    {
        for (int i = 0; i < values.length; i++)
        {
            QueryBind bind = template.getBind( i );
            Column<Object> c = (Column<Object>)bind.column;

            if (c != null)
            {
                values[i] = c.getConverter().toDatabase( model.get( c ) );
                types[i] = c.getType();
            }
        }

        return this;
    }

    public Query<M> bind( Key key )
    {
        for (int i = 0; i < key.size(); i++)
        {
            Column<Object> c = (Column<Object>)key.fieldAt( i );

            bind( c, key.valueAt( i ) );
        }

        return this;
    }

    public int executeUpdate() throws SQLException
    {
        Transaction trans = Rekord.getTransaction();

        PreparedStatement stmt = prepare( trans, template.getQuery() );

        return stmt.executeUpdate();
    }

    public void addBatch() throws SQLException
    {
        Transaction trans = Rekord.getTransaction();

        PreparedStatement stmt = prepare( trans, template.getQuery() );

        stmt.addBatch();
    }

    public int[] executeBatch() throws SQLException
    {
        Transaction trans = Rekord.getTransaction();

        PreparedStatement stmt = prepare( trans, template.getQuery() );

        return stmt.executeBatch();
    }

    public int count() throws SQLException
    {
        Transaction trans = Rekord.getTransaction();

        PreparedStatement stmt = prepare( trans, template.getQuery( SELECTION_COUNT ) );
        stmt.setFetchSize( 1 );

        ResultSet results = stmt.executeQuery();

        try
        {
            return results.next() ? results.getInt( 1 ) : 0;
        }
        finally
        {
            results.close();
        }
    }

    public boolean any() throws SQLException
    {
        Transaction trans = Rekord.getTransaction();

        PreparedStatement stmt = prepare( trans, template.getQuery( SELECTION_EXISTENTIAL ) );
        stmt.setFetchSize( 1 );

        ResultSet results = stmt.executeQuery();

        try
        {
            return results.next();
        }
        finally
        {
            results.close();
        }
    }

    public <T> T first( String expression, Type<T> type ) throws SQLException
    {
        T first = null;

        Transaction trans = Rekord.getTransaction();

        PreparedStatement stmt = prepare( trans, template.getQuery( expression ) );
        stmt.setFetchSize( 1 );

        ResultSet results = stmt.executeQuery();

        try
        {
            if (results.next())
            {
                first = type.fromResultSet( results, 1, true );
            }
        }
        finally
        {
            results.close();
        }

        return first;
    }

    public <T> T first( Column<T> column ) throws SQLException
    {
        return first( column.getQuotedName(), (Type<T>)column.getType() );
    }

    public <T, C extends Collection<T>> C select( String expression, Type<T> type, C out ) throws SQLException
    {
        Transaction trans = Rekord.getTransaction();

        PreparedStatement stmt = prepare( trans, template.getQuery( expression ) );

        ResultSet results = stmt.executeQuery();

        try
        {
            while (results.next())
            {
                out.add( type.fromResultSet( results, 1, true ) );
            }
        }
        finally
        {
            results.close();
        }

        return out;
    }

    public <T> List<T> list( String expression, Type<T> type ) throws SQLException
    {
        return select( expression, type, new ArrayList<T>() );
    }

    public <T> Set<T> set( String expression, Type<T> type ) throws SQLException
    {
        return select( expression, type, new HashSet<T>() );
    }

    public <T, C extends Collection<T>> C select( Column<T> column, C out ) throws SQLException
    {
        return select( column.getQuotedName(), (Type<T>)column.getType(), out );
    }

    public <T> List<T> list( Column<T> column ) throws SQLException
    {
        return select( column.getQuotedName(), (Type<T>)column.getType(), new ArrayList<T>() );
    }

    public <T> Set<T> set( Column<T> column ) throws SQLException
    {
        return select( column.getQuotedName(), (Type<T>)column.getType(), new HashSet<T>() );
    }

    public M first() throws SQLException
    {
        return first( template.getQuery(), template.getSelection(), template.getLoadProfile(), true );
    }
    
    public M first( LoadProfile loadProfile ) throws SQLException
    {
        Selection s = loadProfile.getSelection();
        
        return first( template.getQuery( s.getExpression() ), s.getFields(), loadProfile, true );
    }
    
    public M first(String queryString, Field<?>[] fields, LoadProfile loadProfile, boolean postSelect) throws SQLException
    {
        M first = null;

        Transaction trans = Rekord.getTransaction();

        PreparedStatement stmt = prepare( trans, queryString );
        stmt.setFetchSize( 1 );

        ResultSet results = stmt.executeQuery();

        try
        {
            if (results.next())
            {
                first = fromResultSet( trans, results, fields, loadProfile );
            }
        }
        finally
        {
            results.close();
        }
        
        if (postSelect && first != null)
        {
            postSelect( first, fields, loadProfile );
        }

        return first;
    }

    protected <C extends Collection<M>> C select( String queryString, Field<?>[] fields, LoadProfile loadProfile, boolean postSelect, C out ) throws SQLException
    {
        Transaction trans = Rekord.getTransaction();
        PreparedStatement stmt = prepare( trans, queryString );

        ResultSet results = stmt.executeQuery();

        try
        {
            while (results.next())
            {
                M model = fromResultSet( trans, results, fields, loadProfile );

                out.add( model );
            }
        }
        finally
        {
            results.close();
        }

        if (postSelect)
        {
            postSelect( out, fields, loadProfile );
        }

        return out;
    }

    public <C extends Collection<M>> C select( C out ) throws SQLException
    {
        return select( template.getQuery(), template.getSelection(), template.getLoadProfile(), true, out );
    }

    public <C extends Collection<M>> C select( LoadProfile loadProfile, C out ) throws SQLException
    {
        Selection s = loadProfile.getSelection();

        return select( template.getQuery( s.getExpression() ), s.getFields(), loadProfile, true, out );
    }

    public List<M> list() throws SQLException
    {
        return select( new ArrayList<M>() );
    }

    public List<M> list( LoadProfile loadProfile ) throws SQLException
    {
        return select( loadProfile, new ArrayList<M>() );
    }

    public List<M> list( int offset, int limit, boolean postSelect ) throws SQLException
    {
        return select( template.getQueryPage( offset, limit ), template.getSelection(), template.getLoadProfile(), postSelect, new ArrayList<M>() );
    }

    public Set<M> set() throws SQLException
    {
        return select( new HashSet<M>() );
    }
    
    public Set<M> set( LoadProfile loadProfile ) throws SQLException
    {
        return select( loadProfile, new HashSet<M>() );
    }

    public M fromResultSet( Transaction trans, ResultSet results ) throws SQLException
    {
        return fromResultSet( trans, results, template.getSelection(), template.getLoadProfile() );
    }

    public M fromResultSet( Transaction trans, ResultSet results, Field<?>[] fields, LoadProfile loadProfile ) throws SQLException
    {
        final Table table = template.getTable();
        final Key key = table.keyFromResults( results );

        M model = trans.getCached( table, key );

        if (model == null)
        {
            model = table.newModel();

            populate( results, model, fields, loadProfile );

            if (trans.cache( model ))
            {
                Rekord.log( Logging.CACHING, "to-cache: %s", model );
            }
        }
        else
        {
            Rekord.log( Logging.CACHING, "from-cache: %s", model );

            merge( results, model, fields, loadProfile );
        }

        table.notifyListeners( model, ListenerEvent.POST_SELECT );

        return model;
    }

    public void populate( ResultSet results, Model model ) throws SQLException
    {
        populate( results, model, template.getSelection(), template.getLoadProfile() );
    }

    public void populate( ResultSet results, Model model, Field<?>[] fields, LoadProfile loadProfile ) throws SQLException
    {
        if (loadProfile != null)
        {
            for (Field<?> f : fields)
            {
                model.valueOf( f ).fromSelect( results, loadProfile.getFieldLoad( f ) );
            }
        }
        else
        {
            for (Field<?> f : fields)
            {
                model.valueOf( f ).fromSelect( results, FieldLoad.DEFAULT );
            }
        }
    }

    public void merge( ResultSet results, Model model ) throws SQLException
    {
        merge( results, model, template.getSelection(), template.getLoadProfile() );
    }

    public void merge( ResultSet results, Model model, Field<?>[] fields, LoadProfile loadProfile ) throws SQLException
    {
        if (loadProfile != null)
        {
            for (Field<?> f : fields)
            {
                Value<?> value = model.valueOf( f );

                if (!value.hasValue())
                {
                    value.fromSelect( results, loadProfile.getFieldLoad( f ) );
                }
            }
        }
        else
        {
            for (Field<?> f : fields)
            {
                Value<?> value = model.valueOf( f );

                if (!value.hasValue())
                {
                    value.fromSelect( results, FieldLoad.DEFAULT );
                }
            }
        }
    }

    public void postSelect( Collection<M> collection ) throws SQLException
    {
        postSelect( collection, template.getSelection(), template.getLoadProfile() );
    }

    public void postSelect( Collection<M> collection, Field<?>[] fields, LoadProfile load ) throws SQLException
    {
        if (load != null)
        {
            for (M model : collection)
            {
                postSelectLoadful( model, fields, load );
            }
        }
        else
        {
            for (M model : collection)
            {
                postSelectLoadless( model, fields );
            }
        }
    }

    public void postSelect( M model ) throws SQLException
    {
        postSelect( model, template.getSelection(), template.getLoadProfile() );
    }

    public void postSelect( M model, Field<?>[] fields, LoadProfile loadProfile ) throws SQLException
    {
        if (loadProfile != null)
        {
            postSelectLoadful( model, fields, loadProfile );
        }
        else
        {
            postSelectLoadless( model, fields );
        }
    }

    private void postSelectLoadless( M model, Field<?>[] fields ) throws SQLException
    {
        for (Field<?> f : template.getSelection())
        {
            model.valueOf( f ).postSelect( model, FieldLoad.DEFAULT );
        }
    }

    private void postSelectLoadful( M model, Field<?>[] fields, LoadProfile loadProfile ) throws SQLException
    {
        for (Field<?> f : fields)
        {
            model.valueOf( f ).postSelect( model, loadProfile.getFieldLoad( f ) );
        }
    }

}
