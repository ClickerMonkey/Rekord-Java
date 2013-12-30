package org.magnos.rekord.query;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.magnos.rekord.HistoryTable;
import org.magnos.rekord.Logging;
import org.magnos.rekord.Model;
import org.magnos.rekord.Rekord;
import org.magnos.rekord.Table;
import org.magnos.rekord.Transaction;
import org.magnos.rekord.Value;
import org.magnos.rekord.condition.Condition;
import org.magnos.rekord.condition.Conditions;
import org.magnos.rekord.field.Column;
import org.magnos.rekord.util.SqlUtil;

public class UpdateQuery
{
	
	protected Table<?> table;
	protected String queryFormat;
	protected Condition condition;
	protected String queryHistory;
	protected StringBuilder query;
	
	public UpdateQuery(Table<?> table)
	{
		this.table = table;
		this.query = new StringBuilder();
		this.condition = Conditions.where( table.getKeyColumns() );
		this.queryFormat = buildUpdate( table, condition );
		this.queryHistory = buildHistoryInsert( table, condition );
	}
	
	public void addSet(Column<?> column, String value)
	{
		addSet( column.getName(), value );
	}
	
	public void addSet(String column, String value)
	{
		if (query.length() > 0)
		{
			query.append( "," );
		}
		
		query.append( SqlUtil.namify( column ) );
		query.append( " = " );
		query.append( value );
	}
	
	public boolean execute( Model model ) throws SQLException
	{
		final Value<?>[] values = model.getValues();
		
		for (Value<?> v : values)
		{
			v.preSave( model );
		}
		
		boolean recordsUpdated = false;
		
		query.setLength( 0 );
		
		for (Value<?> v : values)
		{
			if (v.hasChanged())
			{
				v.prepareUpdate( this );
			}
		}
		
		if (query.length() > 0)
		{
			Transaction trans = Rekord.getTransaction();
			String queryString = String.format( queryFormat, query );
			
			Rekord.log( Logging.UPDATES, "%s -> %s", queryString, model );
			
			if (queryHistory != null)
			{
				PreparedStatement stmt = trans.prepare( queryHistory );
				Conditions.whereBind( condition, table.getKeyColumns(), values );
				condition.toPreparedstatement( stmt, 1 );
				stmt.executeUpdate();
				
				Rekord.log( Logging.HISTORY, "history saved: %s -> %s", queryHistory, model );
			}
			
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
		
		return recordsUpdated;
	}
	
	private static String buildUpdate(Table<?> table, Condition condition) 
	{
		StringBuilder queryFormatBuilder = new StringBuilder();
		queryFormatBuilder.append( "UPDATE " );
		queryFormatBuilder.append( SqlUtil.namify( table.getName() ) );
		queryFormatBuilder.append( " SET %s WHERE " );
		condition.toQuery( queryFormatBuilder );
		
		return queryFormatBuilder.toString();
	}
	
	private static String buildHistoryInsert(Table<?> table, Condition condition)
	{
		if (!table.hasHistory()) 
		{
			return null;
		}
		
		HistoryTable history = table.getHistory();
		
		String columns = SqlUtil.joinAndNamify( history.getHistoryColumns() );
		
		StringBuilder queryHistoryBuilder = new StringBuilder();
		queryHistoryBuilder.append( "INSERT INTO " );
		queryHistoryBuilder.append( SqlUtil.namify( history.getHistoryTable() ) );
		queryHistoryBuilder.append( "(" );
		queryHistoryBuilder.append( columns );
		queryHistoryBuilder.append( ") " );
		queryHistoryBuilder.append( "SELECT " );
		queryHistoryBuilder.append( columns );
		queryHistoryBuilder.append( " FROM " );
		queryHistoryBuilder.append( SqlUtil.namify( table.getName() ) );
		queryHistoryBuilder.append( " WHERE " );
		condition.toQuery( queryHistoryBuilder );
		
		return queryHistoryBuilder.toString();
	}
	
}
