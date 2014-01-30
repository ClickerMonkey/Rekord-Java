package org.magnos.rekord;

import org.magnos.rekord.query.QueryTemplate;
import org.magnos.rekord.query.model.ModelQuery;

public interface Dialect
{
	public ModelQuery newInsertQuery(Table table);
	public ModelQuery newUpdateQuery(Table table);
	public ModelQuery newDeleteQuery(Table table);
	public QueryTemplate<Model> newSelectQuery();
}
