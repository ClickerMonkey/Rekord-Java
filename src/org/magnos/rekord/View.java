
package org.magnos.rekord;

import java.util.HashMap;
import java.util.Map;


public class View
{

	private final String name;
	private final Field<?>[] fields;
	private final Map<Field<?>, View> fieldViews;

	public View( String name, Field<?>... fields )
	{
		this.name = name;
		this.fields = fields;
		this.fieldViews = new HashMap<Field<?>, View>();
	}

	public View add( Field<?> f, View v )
	{
		fieldViews.put( f, v );

		return this;
	}

	public View getFieldView( Field<?> f, View defaultView )
	{
		View v = fieldViews.get( f );

		return (v == null ? defaultView : v);
	}

	public String getName()
	{
		return name;
	}

	public Field<?>[] getFields()
	{
		return fields;
	}

	public Map<Field<?>, View> getFieldViews()
	{
		return fieldViews;
	}

}
