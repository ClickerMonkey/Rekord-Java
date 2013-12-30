
package org.magnos.rekord;

import java.util.Date;


public class HistoryKey<T> implements Comparable<HistoryKey<T>>
{

	private long historyId = -1;
	private Date historyTimestamp;
	private T model;

	public long getHistoryId()
	{
		return historyId;
	}

	public void setHistoryId( long historyId )
	{
		this.historyId = historyId;
	}

	public Date getHistoryTimestamp()
	{
		return historyTimestamp;
	}

	public void setHistoryTimestamp( Date historyTimestamp )
	{
		this.historyTimestamp = historyTimestamp;
	}

	public T getModel()
	{
		return model;
	}

	public void setModel( T model )
	{
		this.model = model;
	}

	@Override
	public int hashCode()
	{
		return (int)(historyId ^ (historyId >>> 32));
	}

	@Override
	public boolean equals( Object obj )
	{
		if (obj == null || obj.getClass() != HistoryKey.class)
		{
			return false;
		}
		
		return historyId == ((HistoryKey<?>)obj).historyId;
	}

	@Override
	public int compareTo( HistoryKey<T> o )
	{
		return historyTimestamp == null || o.historyTimestamp == null ? 0 : historyTimestamp.compareTo( o.historyTimestamp );
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append( "{" );

		if (historyId != -1) {
			sb.append( "historyId=" ).append( historyId ).append( ", " );
		}
		
		if (historyTimestamp != null) {
			sb.append( "historyTimestamp=" ).append( historyTimestamp ).append( ", " );
		}

		sb.append( "model=" ).append( model );
		
		sb.append( "}" );
		return sb.toString();
	}
	
}
