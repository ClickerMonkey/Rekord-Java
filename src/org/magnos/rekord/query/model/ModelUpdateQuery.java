package org.magnos.rekord.query.model;

import java.sql.SQLException;

import org.magnos.rekord.HistoryTable;
import org.magnos.rekord.ListenerEvent;
import org.magnos.rekord.Logging;
import org.magnos.rekord.Model;
import org.magnos.rekord.Rekord;
import org.magnos.rekord.Table;
import org.magnos.rekord.Transaction;
import org.magnos.rekord.Value;
import org.magnos.rekord.query.InsertQuery;
import org.magnos.rekord.query.Query;
import org.magnos.rekord.query.QueryTemplate;
import org.magnos.rekord.query.Queryable;
import org.magnos.rekord.query.SelectQuery;
import org.magnos.rekord.query.UpdateQuery;

public class ModelUpdateQuery
{
	
	protected final Table table;
	protected final QueryTemplate<Model> queryHistory;
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
        
        boolean recordsUpdated = false;
        
        if (model.hasChanged())
        {
            if (dynamic)
            {
                buildQuery( values );
            }
            
            Rekord.log( Logging.UPDATES, "update: %s -> %s", queryTemplate.getQuery(), model );
            
            saveHistory( model );
            
            Query<Model> query = queryTemplate.create();
            query.bind( model );
            
            recordsUpdated = query.executeUpdate() > 0;
            
            if (recordsUpdated)
            {
                Transaction trans = Rekord.getTransaction();
                trans.cache( model );
            }
            
            for (Value<?> v : values)
            {
                v.clearChanges();
            }
        }
	    
		for (Value<?> v : values)
		{
			v.postSave( model );
		}
		
		model.getTable().notifyListeners( model, ListenerEvent.POST_UPDATE );
		
		return recordsUpdated;
	}

    protected void saveHistory(Model model) throws SQLException
    {
        if (queryHistory != null)
        {
            Query<Model> query = queryHistory.create();
            
            query.bind( model );
            query.executeUpdate();
            
            Rekord.log( Logging.HISTORY, "history saved: %s -> %s", queryHistory, model );
        }
    }
	
	public static QueryTemplate<Model> buildHistoryInsert(Table table)
	{
		if (!table.hasHistory()) 
		{
			return null;
		}
		
		HistoryTable history = table.getHistory();
		
		SelectQuery<Model> select = new SelectQuery<Model>( table );
		select.select( history.getHistoryColumns() );
		select.whereKeyBind( table );
		
		InsertQuery<Model> insert = new InsertQuery<Model>( table );
		insert.into( history.getHistoryTable() );
		
		return insert.newTemplateFromSelect( select );
	}
	
}
