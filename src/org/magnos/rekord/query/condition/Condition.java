package org.magnos.rekord.query.condition;

import org.magnos.rekord.query.QueryBuilder;

public interface Condition
{
	
	public void toQuery(QueryBuilder query);
	
}
