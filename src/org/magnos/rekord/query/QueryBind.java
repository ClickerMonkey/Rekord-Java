
package org.magnos.rekord.query;

import org.magnos.rekord.field.Column;

public class QueryBind
{
	
	public final String name;
	public final int index;
	public final Column<?> column;
	public final Object defaultValue;
	public final int start, end;

	public QueryBind( String name, int index, Column<?> column, Object defaultValue, int start, int end )
	{
		this.name = name;
		this.index = index;
		this.column = column;
		this.defaultValue = defaultValue;
		this.start = start;
		this.end = end;
	}
}
