
package org.magnos.rekord;

import org.magnos.rekord.query.Query;
import org.magnos.rekord.query.QueryTemplate;


public class SaveProfile
{
	private final String name;
	private final Field<?>[] fields;
	private final QueryTemplate<Model> insertTemplate;
	private final QueryTemplate<Model> updateTemplate;

	public SaveProfile(String name, Field<?>[] fields, QueryTemplate<Model> insertTemplate, QueryTemplate<Model> updateTemplate)
	{
		this.name = name;
		this.fields = fields;
		this.insertTemplate = insertTemplate;
		this.updateTemplate = updateTemplate;
	}
	
	public String getName()
	{
		return name;
	}

	public Field<?>[] getFields()
	{
		return fields;
	}

	public QueryTemplate<Model> getInsertTemplate()
	{
		return insertTemplate;
	}

	public QueryTemplate<Model> getUpdateTemplate()
	{
		return updateTemplate;
	}
	
	public Query<Model> newQuery( boolean hasKey )
	{
		return hasKey ? updateTemplate.create() : insertTemplate.create();
	}

}
