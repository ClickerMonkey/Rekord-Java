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
import org.magnos.rekord.field.Column;
import org.magnos.rekord.query.NativeQuery;
import org.magnos.rekord.query.Query;
import org.magnos.rekord.query.QueryTemplate;
import org.magnos.rekord.query.condition.Condition;
import org.magnos.rekord.query.expr.GroupExpression;
import org.magnos.rekord.util.SqlUtil;

public abstract class ModelUpdateQuery
{
	
	protected Table table;
	protected Condition condition;
	protected Query<Model> queryHistory;
    protected String queryFormat;
	protected QueryTemplate<Model> queryTemplate;
	protected StringBuilder querySet;
	
	public ModelUpdateQuery(Table table)
	{
		this.table = table;
		this.querySet = new StringBuilder();
		this.condition = new GroupExpression().whereKeyBind( table );
		this.queryFormat = buildUpdate( table, condition );
		this.queryHistory = buildHistoryInsert( table, condition );
	}
	
	protected void prepareFixed()
	{
		querySet.setLength( 0 );
		
		for (Field<?> f : table.getFields())
		{
			f.prepareUpdate( this );
		}

        String queryString = String.format( queryFormat, querySet );
        
		queryTemplate = NativeQuery.parse( table, queryString, null );
	}
	
	protected void prepareDynamic(Model model)
	{
		querySet.setLength( 0 );
		
		for (Value<?> v : model.getValues())
		{
			if (v.hasChanged())
			{
				v.prepareDynamicUpdate( this );
			}
		}

        String queryString = String.format( queryFormat, querySet );
        
        queryTemplate = NativeQuery.parse( table, queryString, null );
	}
	
	public void set(Column<?> column)
	{
	    if (querySet.length() > 0)
        {
            querySet.append( "," );
        }
	    
	    querySet.append( column.getQuotedName() );
	    querySet.append( " = " );
	    querySet.append( column.getOutForBind() );
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
	
	protected void preSave( Model model ) throws SQLException
	{
		model.getTable().notifyListeners( model, ListenerEvent.PRE_UPDATE );
		
		for (Value<?> v : model.getValues())
		{
			v.preSave( model );
		}
	}
	
	protected boolean updateModel( Model model ) throws SQLException
	{
		Value<?>[] values = model.getValues();
		
		boolean recordsUpdated = false;
		
		if (querySet.length() > 0)
		{
		    Query<Model> query = queryTemplate.create();
		    
			Rekord.log( Logging.UPDATES, "%s -> %s", queryTemplate.getQuery(), model );
			
			saveHistory( model );

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
	
	public abstract boolean execute( Model model ) throws SQLException;
	
	private static String buildUpdate(Table table, Condition condition) 
	{
		StringBuilder queryFormatBuilder = new StringBuilder();
		queryFormatBuilder.append( "UPDATE " );
		queryFormatBuilder.append( table.getQuotedName() );
		queryFormatBuilder.append( " SET %s WHERE " );
		condition.toQuery( queryFormatBuilder );
		
		return queryFormatBuilder.toString();
	}
	
	private static Query<Model> buildHistoryInsert(Table table, Condition condition)
	{
		if (!table.hasHistory()) 
		{
			return null;
		}
		
		HistoryTable history = table.getHistory();
		
		String columns = SqlUtil.join( ",", history.getHistoryColumns() );
		
		StringBuilder queryHistoryBuilder = new StringBuilder();
		queryHistoryBuilder.append( "INSERT INTO " );
		queryHistoryBuilder.append( SqlUtil.namify( history.getHistoryTable() ) );
		queryHistoryBuilder.append( "(" );
		queryHistoryBuilder.append( columns );
		queryHistoryBuilder.append( ") " );
		queryHistoryBuilder.append( "SELECT " );
		queryHistoryBuilder.append( columns );
		queryHistoryBuilder.append( " FROM " );
		queryHistoryBuilder.append( table.getQuotedName() );
		queryHistoryBuilder.append( " WHERE " );
		condition.toQuery( queryHistoryBuilder );
		
		return NativeQuery.parse( table, queryHistoryBuilder.toString(), null ).create();
	}
	
}
