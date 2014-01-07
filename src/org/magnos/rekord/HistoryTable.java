
package org.magnos.rekord;

import org.magnos.rekord.field.Column;
import org.magnos.rekord.query.NativeQuery;
import org.magnos.rekord.query.QueryTemplate;
import org.magnos.rekord.query.Selection;
import org.magnos.rekord.query.condition.Condition;
import org.magnos.rekord.query.expr.GroupExpression;
import org.magnos.rekord.util.SqlUtil;

public class HistoryTable
{

	protected String historyTable;
	protected String historyKey;
	protected String historyTimestamp;
	protected Column<?>[] historyColumns;
	protected QueryTemplate<Model> query;

	public HistoryTable( Table table, String historyTable, String historyKey, String historyTimestamp, Column<?>... historyColumns )
	{
		this.historyTable = historyTable;
		this.historyKey = historyKey;
		this.historyTimestamp = historyTimestamp;
		this.historyColumns = historyColumns;
		this.query = buildQuery( table, this );
	}

	public String getHistoryTable()
	{
		return historyTable;
	}

	public void setHistoryTable( String historyTable )
	{
		this.historyTable = historyTable;
	}

	public String getHistoryKey()
	{
		return historyKey;
	}

	public void setHistoryKey( String historyKey )
	{
		this.historyKey = historyKey;
	}

	public String getHistoryTimestamp()
	{
		return historyTimestamp;
	}

	public void setHistoryTimestamp( String historyTimestamp )
	{
		this.historyTimestamp = historyTimestamp;
	}

	public Column<?>[] getHistoryColumns()
	{
		return historyColumns;
	}

	public void setHistoryColumns( Column<?>[] historyColumns )
	{
		this.historyColumns = historyColumns;
	}
	
	public QueryTemplate<Model> getQuery()
	{
		return query;
	}

	public void setQuery( QueryTemplate<Model> query )
	{
		this.query = query;
	}

	public static QueryTemplate<Model> buildQuery(Table table, HistoryTable history)
	{
		final String historyTimestamp = history.getHistoryTimestamp();
		final String historyKey = history.getHistoryKey();
		final Selection selection = Selection.fromFields( history.getHistoryColumns() );
		
		Condition where = new GroupExpression().whereKeyBind( table );
		
		StringBuilder queryBuilder = new StringBuilder();
		queryBuilder.append( "SELECT " );
		queryBuilder.append( selection.getExpression() );
		
		if (historyTimestamp != null)
		{
			queryBuilder.append( ", " ).append( SqlUtil.namify( historyTimestamp ) );
		}
		if (historyKey != null)
		{
			queryBuilder.append( ", " ).append( SqlUtil.namify( historyKey ) );
		}
		
		queryBuilder.append( " FROM " );
		queryBuilder.append( SqlUtil.namify( history.getHistoryTable() ) );
		queryBuilder.append( " WHERE " );
		where.toQuery( queryBuilder );
		
		if (historyTimestamp != null)
		{
			queryBuilder.append( " ORDER BY " ).append( SqlUtil.namify( historyTimestamp ) );
		}
		else if (historyKey != null)
		{
			queryBuilder.append( " ORDER BY " ).append( SqlUtil.namify( historyKey ) );
		}
		
		String query = queryBuilder.toString();
		
		return NativeQuery.parse( table, query, null );
	}

}
