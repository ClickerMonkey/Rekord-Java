
package org.magnos.rekord;

public class HistoryTable
{

	private String historyTable;
	private String historyKey;
	private String historyTimestamp;
	private Column<?>[] historyColumns;

	public HistoryTable( String historyTable, String historyKey, String historyTimestamp, Column<?>... historyColumns )
	{
		this.historyTable = historyTable;
		this.historyKey = historyKey;
		this.historyTimestamp = historyTimestamp;
		this.historyColumns = historyColumns;
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

}
