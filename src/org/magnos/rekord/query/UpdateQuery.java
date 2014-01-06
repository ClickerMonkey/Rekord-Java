package org.magnos.rekord.query;

import java.sql.PreparedStatement;
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
import org.magnos.rekord.query.condition.Condition;
import org.magnos.rekord.query.condition.Conditions;
import org.magnos.rekord.util.SqlUtil;

public abstract class UpdateQuery
{
	
	protected Table table;
	protected String queryFormat;
	protected Condition condition;
	protected String queryHistory;
	protected String queryString;
	protected StringBuilder query;
	
	public UpdateQuery(Table table)
	{
		this.table = table;
		this.query = new StringBuilder();
		this.condition = Conditions.where( table.getKeyColumns() );
		this.queryFormat = buildUpdate( table, condition );
		this.queryHistory = buildHistoryInsert( table, condition );
	}
	
	protected void prepareFixed()
	{
		query.setLength( 0 );
		
		for (Field<?> f : table.getFields())
		{
			f.prepareUpdate( this );
		}

		queryString = String.format( queryFormat, query );
	}
	
	protected void prepareDynamic(Model model)
	{
		query.setLength( 0 );
		
		for (Value<?> v : model.getValues())
		{
			if (v.hasChanged())
			{
				v.prepareDynamicUpdate( this );
			}
		}
		
		queryString = String.format( queryFormat, query );
	}
	
	public void addSet(Column<?> column)
	{
		addSet( column.getQuotedName(), column.getOut() );
	}
	
	public void addSet(Column<?> column, String value)
	{
		addSet( column.getQuotedName(), value );
	}
	
	public void addSet(String column, String value)
	{
		if (query.length() > 0)
		{
			query.append( "," );
		}
		
		query.append( column );
		query.append( " = " );
		query.append( value );
	}
	
	protected void saveHistory(Model model) throws SQLException
	{
		if (queryHistory != null)
		{
			Transaction trans = Rekord.getTransaction();
			PreparedStatement stmt = trans.prepare( queryHistory );
			Conditions.whereBind( condition, table.getKeyColumns(), model.getValues() );
			condition.toPreparedstatement( stmt, 1 );
			stmt.executeUpdate();
			
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
		
		if (query.length() > 0)
		{
			Rekord.log( Logging.UPDATES, "%s -> %s", queryString, model );
			
			saveHistory( model );

			Transaction trans = Rekord.getTransaction();
			PreparedStatement stmt = trans.prepare( queryString );
			int paramIndex = 1;
		
			for (Value<?> v : values)
			{
				if (v.hasChanged())
				{
					paramIndex = v.toUpdate( stmt, paramIndex );
				}
			}
			
			Conditions.whereBind( condition, table.getKeyColumns(), values );
			paramIndex = condition.toPreparedstatement( stmt, paramIndex );
			
			recordsUpdated = stmt.executeUpdate() > 0;
			
			if (recordsUpdated)
			{
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
	
	private static String buildHistoryInsert(Table table, Condition condition)
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
		
		return queryHistoryBuilder.toString();
	}
	
}
