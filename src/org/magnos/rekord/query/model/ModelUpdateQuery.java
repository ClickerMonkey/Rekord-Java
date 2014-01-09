package org.magnos.rekord.query.model;

import java.sql.SQLException;

import org.magnos.rekord.Field;
import org.magnos.rekord.HistoryTable;
import org.magnos.rekord.ListenerEvent;
import org.magnos.rekord.Logging;
import org.magnos.rekord.Model;
import org.magnos.rekord.Rekord;
import org.magnos.rekord.Table;
import org.magnos.rekord.Transaction;
import org.magnos.rekord.Value;
import org.magnos.rekord.query.Query;
import org.magnos.rekord.query.QueryBind;
import org.magnos.rekord.query.QueryBuilder;
import org.magnos.rekord.query.QueryTemplate;
import org.magnos.rekord.query.Queryable;
import org.magnos.rekord.query.UpdateQuery;
import org.magnos.rekord.query.condition.Condition;
import org.magnos.rekord.query.expr.GroupExpression;
import org.magnos.rekord.util.SqlUtil;

public class ModelUpdateQuery
{
	
	protected final Table table;
	protected final Query<Model> queryHistory;
	protected final boolean dynamic;
	protected QueryTemplate<Model> queryTemplate;
	
	public ModelUpdateQuery(Table table, boolean dynamic)
	{
		this.table = table;
		this.dynamic = dynamic;
		this.queryHistory = buildHistoryInsert( table );
		
		if (!dynamic)
		{
		    buildQuery( table.getFields() );
		}
	}
	
	protected void buildQuery( Queryable[] querables )
	{
	    queryTemplate = UpdateQuery.forFields( table, querables );
	}
	
	public boolean execute( Model model ) throws SQLException
	{
	    final Value<?>[] values = model.getValues();
	    
	    model.getTable().notifyListeners( model, ListenerEvent.PRE_UPDATE );
        
        for (Value<?> v : values)
        {
            v.preSave( model );
        }
        
        if (dynamic)
        {
            buildQuery( values );
        }
	    
	    Query<Model> query = queryTemplate.create();
		
		Rekord.log( Logging.UPDATES, "pre-update: %s -> %s", queryTemplate.getQuery(), model );
		
		saveHistory( model );

		query.bind( model );
		
		boolean recordsUpdated = query.executeUpdate() > 0;
		
		if (recordsUpdated)
		{
		    Transaction trans = Rekord.getTransaction();
			trans.cache( model );
		}
		
		for (Value<?> v : values)
		{
            v.clearChanges();
			v.postSave( model );
		}
		
		model.getTable().notifyListeners( model, ListenerEvent.POST_UPDATE );
		
		Rekord.log( Logging.UPDATES, "post-update: %s -> %s", queryTemplate.getQuery(), model );
		
		return recordsUpdated;
	}

    protected void saveHistory(Model model) throws SQLException
    {
        if (queryHistory != null)
        {
            queryHistory.bind( model );
            queryHistory.executeUpdate();
            
            Rekord.log( Logging.HISTORY, "history saved: %s -> %s", queryHistory, model );
        }
    }
	
	private static Query<Model> buildHistoryInsert(Table table)
	{
		if (!table.hasHistory()) 
		{
			return null;
		}
		
		Condition condition = GroupExpression.detached().whereKeyBind( table );
		
		HistoryTable history = table.getHistory();
		
		String columns = SqlUtil.join( ",", history.getHistoryColumns() );
		
		QueryBuilder qb = new QueryBuilder();
		qb.append( "INSERT INTO ", SqlUtil.namify( history.getHistoryTable() ) );
		qb.append( "(", columns, ")" );
		qb.append( "SELECT ", columns );
		qb.append( " FROM ", table.getQuotedName(), " WHERE " );
		condition.toQuery( qb );
		
		String query = qb.getQueryString();
		QueryBind[] binds = qb.getBindsArray();
		Field<?>[] select = new Field[0];
		
		return new QueryTemplate<Model>( table, query, null, binds, select ).create();
	}
	
}
