
package org.magnos.rekord;

import org.magnos.rekord.field.Column;
import org.magnos.rekord.query.QueryBind;
import org.magnos.rekord.query.QueryBuilder;
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
		
		QueryBuilder qb = new QueryBuilder();
		qb.append( "SELECT " );
		qb.append( selection.getExpression() );
		
		if (historyTimestamp != null)
		{
			qb.append( ", ", SqlUtil.namify( historyTimestamp ) );
		}
		if (historyKey != null)
		{
			qb.append( ", ", SqlUtil.namify( historyKey ) );
		}
		
		qb.append( " FROM " );
		qb.append( SqlUtil.namify( history.getHistoryTable() ) );
		qb.append( " WHERE " );
		where.toQuery( qb );
		
		if (historyTimestamp != null)
		{
			qb.append( " ORDER BY ", SqlUtil.namify( historyTimestamp ) );
		}
		else if (historyKey != null)
		{
			qb.append( " ORDER BY ", SqlUtil.namify( historyKey ) );
		}
		
		String query = qb.getQueryString();
		QueryBind[] binds = qb.getBindsArray();
		Field<?>[] select = selection.getFields();
		
		return new QueryTemplate<Model>( table, query, null, binds, select );
	}

}
