
package org.magnos.rekord.query;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.magnos.rekord.Field;
import org.magnos.rekord.FieldLoad;
import org.magnos.rekord.Key;
import org.magnos.rekord.ListenerEvent;
import org.magnos.rekord.LoadProfile;
import org.magnos.rekord.Logging;
import org.magnos.rekord.Model;
import org.magnos.rekord.Rekord;
import org.magnos.rekord.Table;
import org.magnos.rekord.Transaction;
import org.magnos.rekord.Type;
import org.magnos.rekord.Value;
import org.magnos.rekord.field.Column;


public class Query<M extends Model>
{

    public static final String SELECTION_COUNT = "COUNT(*)";
    public static final String SELECTION_EXISTENTIAL = "1";

    protected final QueryTemplate<M> template;
    protected final Object[] values;
    protected final Type<Object>[] types;
    protected LoadProfile loadProfile;
    protected Field<?>[] selectFields;
    protected String selectExpression;
    protected StringBuilder orderBy;
    protected Integer offset;
    protected Integer limit;
    protected LockMode lock;
    protected boolean postSelect;

    public Query( QueryTemplate<M> template )
    {
        this.template = template;
        this.loadProfile = template.getLoadProfile();
        this.selectFields = template.getSelectFields();
        this.selectExpression = template.getSelectExpression();
        this.orderBy = new StringBuilder();
        this.offset = null;
        this.limit = null;
        this.lock = LockMode.NONE;
        
        final int bindCount = template.getBindCount();
        
        this.values = new Object[ bindCount ];
        this.types = new Type[ bindCount ];
        
        for (int i = 0; i < bindCount; i++)
        {
        	QueryBind qb = template.getBind( i );
        	
        	this.values[i] = qb.defaultValue;
        	this.types[i] = qb.defaultType;
        }
    }

    public QueryTemplate<M> getTemplate()
    {
        return template;
    }

    public Object[] getValues()
    {
        return values;
    }

    public Type<Object>[] getTypes()
    {
        return types;
    }
    
    public Field<?>[] getSelectFields()
    {
        return selectFields;
    }
    
    protected void ensureSelect()
    {
        if (!template.isSelect())
        {
            throw new RuntimeException( "This action can only be done on SELECT statements" );
        }
    }
    
    public Query<M> withSelectFields( Field<?> ... selection )
    {
        ensureSelect();
        
        this.selectFields = selection;
        
        return this;
    }

    public String getSelectExpression()
    {
        return selectExpression;
    }
    
    public Query<M> withSelectExpression( String selectExpression )
    {
        ensureSelect();
        
        this.selectExpression = selectExpression;
        
        return this;
    }
    
    public StringBuilder getOrderBy()
    {
        return orderBy;
    }
    
    public Query<M> withOrderBy( StringBuilder orderBy )
    {
        ensureSelect();
        
        this.orderBy = orderBy;
        
        return this;
    }
    
    public Query<M> orderBy( Column<?> column )
    {
        return orderBy( column.getQuotedName() );
    }
    
    public Query<M> orderBy( Column<?> column, Order order )
    {
        return orderBy( column.getQuotedName() + " " + order.name() );
    }
    
    public Query<M> orderBy( String expression )
    {
        ensureSelect();
        
        if ( orderBy.length() > 0 )
        {
            orderBy.append( ", " );
        }
        
        orderBy.append( expression );
        
        return this;
    }

    public Integer getOffset()
    {
        return offset;
    }
    
    public Query<M> withOffset( Integer offset )
    {
        ensureSelect();
        
        this.offset = offset;
        
        return this;
    }

    public Integer getLimit()
    {
        return limit;
    }
    
    public Query<M> withLimit( Integer limit )
    {
        ensureSelect();
        
        this.limit = limit;
        
        return this;
    }

    public LockMode getLock()
    {
        return lock;
    }

    public Query<M> withLock( LockMode lock )
    {
        ensureSelect();
        
        this.lock = lock;
        
        return this;
    }
    
    public Query<M> withLoad( LoadProfile loadProfile )
    {
        ensureSelect();
        
        Selection s = loadProfile.getSelection();
        
        this.selectExpression = s.getExpression();
        this.selectFields = s.getFields();
        this.loadProfile = loadProfile;
        
        return this;
    }
    
    public boolean isPostSelect()
    {
        return postSelect;
    }
    
    public Query<M> withPostSelect( boolean postSelect )
    {
        ensureSelect();
        
        this.postSelect = postSelect;
        
        return this;
    }
    
    protected String getSelectQuery( boolean withRestrictions )
    {
        StringBuilder select = new StringBuilder();
        
        select.append( template.getQuery( selectExpression ) );
        
        if (withRestrictions)
        {
            if (orderBy.length() > 0)
            {
                select.append( " ORDER BY " );
                select.append( orderBy );
            }
            
            if (limit != null)
            {
                select.append( " LIMIT " );
                select.append( limit );
            }
            
            if (offset != null)
            {
                select.append( " OFFSET " );
                select.append( offset );
            }
        }
        
        switch (lock)
        {
        case NONE:
            break;
        case EXCLUSIVE:
            select.append( " FOR UPDATE" );
            break;
        case SHARE:
            select.append( " FOR KEY SHARE" );
            break;
        }
        
        return select.toString();
    }
    
    public String getFinalQuery( boolean withRestrictions )
    {
        if (template.isSelect())
        {
            return getSelectQuery( withRestrictions );
        }
        
        return template.getQuery();
    }

    protected PreparedStatement prepare( Transaction trans, String query ) throws SQLException
    {
        if (Rekord.isLogging( Logging.HUMAN_READABLE_QUERY ))
        {
            Rekord.log( getReadableQuery( query ) );
        }
        
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
        return getResultsFromQuery( getFinalQuery( true ) );
    }

    public ResultSet getResults( boolean withRestrictions ) throws SQLException
    {
        return getResultsFromQuery( getFinalQuery( withRestrictions ) );
    }
    
    public Query<M> reset()
    {
        return resetSelection().resetRestrictions().resetPostSelect().resetBinds();
    }
    
    public Query<M> resetSelection()
    {
        loadProfile = template.getLoadProfile();
        selectExpression = template.getSelectExpression();
        selectFields = template.getSelectFields();
        
        return this;
    }
    
    public Query<M> resetRestrictions()
    {
        orderBy.setLength( 0 );
        offset = null;
        limit = null;
        lock = LockMode.NONE;
        
        return this;
    }
    
    public Query<M> resetPostSelect()
    {
        postSelect = true;
        
        return this;
    }
    
    public Query<M> resetBinds()
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

    public <T> Query<M> bind( String variableName, Column<T> forColumn, T value )
    {
        int i = template.indexOf( variableName );
        values[i] = forColumn.getConverter().toDatabase( value );
        types[i] = forColumn.getType();

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

        String currentExpression = selectExpression;
        selectExpression = expression;
        
        PreparedStatement stmt = prepare( trans, getSelectQuery( true ) );
        stmt.setFetchSize( 1 );
        
        selectExpression = currentExpression;

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

        String currentExpression = selectExpression;
        selectExpression = expression;
        
        PreparedStatement stmt = prepare( trans, getSelectQuery( true ) );

        selectExpression = currentExpression;
        
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

    public <T, C extends Collection<T>> C select( Column<T> column, C out ) throws SQLException
    {
        return select( column.getQuotedName(), (Type<T>)column.getType(), out );
    }

    public <T> List<T> list( String expression, Type<T> type ) throws SQLException
    {
        return select( expression, type, new ArrayList<T>() );
    }

    public <T> List<T> list( Column<T> column ) throws SQLException
    {
        return select( column.getQuotedName(), (Type<T>)column.getType(), new ArrayList<T>() );
    }

    public <T> Set<T> set( String expression, Type<T> type ) throws SQLException
    {
        return select( expression, type, new HashSet<T>() );
    }

    public <T> Set<T> set( Column<T> column ) throws SQLException
    {
        return select( column.getQuotedName(), (Type<T>)column.getType(), new HashSet<T>() );
    }
    
    public M first() throws SQLException
    {
        M first = null;

        Transaction trans = Rekord.getTransaction();

        PreparedStatement stmt = prepare( trans, getSelectQuery( true ) );
        stmt.setFetchSize( 1 );
        
        ResultSet results = stmt.executeQuery();

        try
        {
            if (results.next())
            {
                first = fromResultSet( trans, results );
            }
        }
        finally
        {
            results.close();
        }
        
        if (postSelect && first != null)
        {
            postSelect( first );
        }

        return first;
    }

    protected <C extends Collection<M>> C select( C out ) throws SQLException
    {
        Transaction trans = Rekord.getTransaction();
        PreparedStatement stmt = prepare( trans, getSelectQuery( true ) );

        ResultSet results = stmt.executeQuery();

        try
        {
            while (results.next())
            {
                M model = fromResultSet( trans, results );

                out.add( model );
            }
        }
        finally
        {
            results.close();
        }

        if (postSelect)
        {
            postSelect( out );
        }

        return out;
    }

    public List<M> list() throws SQLException
    {
        return select( new ArrayList<M>() );
    }

    public Set<M> set() throws SQLException
    {
        return select( new HashSet<M>() );
    }
    
    public M fromResultSet( Transaction trans, ResultSet results ) throws SQLException
    {
        final Table table = template.getTable();
        final Key key = table.keyFromResults( results );

        M model = trans.getCached( table, key );

        if (model == null)
        {
            model = table.newModel();

            populate( results, model );

            trans.cache( model );
        }
        else
        {
            merge( results, model );
        }

        table.notifyListeners( model, ListenerEvent.POST_SELECT );

        return model;
    }

    public void populate( ResultSet results, Model model ) throws SQLException
    {
        if (loadProfile != null)
        {
            for (Field<?> f : selectFields)
            {
                model.valueOf( f ).fromSelect( results, loadProfile.getFieldLoad( f ) );
            }
        }
        else
        {
            for (Field<?> f : selectFields)
            {
                model.valueOf( f ).fromSelect( results, FieldLoad.DEFAULT );
            }
        }
    }

    public void merge( ResultSet results, Model model ) throws SQLException
    {
        if (loadProfile != null)
        {
            for (Field<?> f : selectFields)
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
            for (Field<?> f : selectFields)
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
        if (loadProfile != null)
        {
            for (M model : collection)
            {
                postSelectLoadful( model );
            }
        }
        else
        {
            for (M model : collection)
            {
                postSelectLoadless( model );
            }
        }
    }

    public void postSelect( M model ) throws SQLException
    {
        if (loadProfile != null)
        {
            postSelectLoadful( model );
        }
        else
        {
            postSelectLoadless( model );
        }
    }

    private void postSelectLoadless( M model ) throws SQLException
    {
        for (Field<?> f : selectFields)
        {
            model.valueOf( f ).postSelect( model, FieldLoad.DEFAULT );
        }
    }

    private void postSelectLoadful( M model ) throws SQLException
    {
        for (Field<?> f : selectFields)
        {
            model.valueOf( f ).postSelect( model, loadProfile.getFieldLoad( f ) );
        }
    }
    
    public String getReadableQuery()
    {
        return getReadableQuery( getFinalQuery( true ) );
    }
    
    public String getReadableQuery( String query )
    {
        StringBuilder readable = new StringBuilder();
        
        int start = 0;
        int end = query.indexOf( '?' );
        int paramIndex = 0;
        
        while (end != -1)
        {
            readable.append( query.substring( start, end ) );
            
            Object value = values[paramIndex];
            String valueString = value == null ? "NULL" : types[paramIndex].toQueryString( value );
            
            readable.append( valueString );
            
            paramIndex++;
            start = end + 1;
            end = query.indexOf( '?', start );
        }
        
        readable.append( query.substring( start ) );
        
        return readable.toString();
    }
    
    @Override
    public String toString()
    {
        return getReadableQuery();
    }
    
}
