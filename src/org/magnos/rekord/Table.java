
package org.magnos.rekord;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.magnos.rekord.condition.Condition;
import org.magnos.rekord.condition.Conditions;
import org.magnos.rekord.key.MultiModelKey;
import org.magnos.rekord.key.MultiValueKey;
import org.magnos.rekord.key.SingleModelKey;
import org.magnos.rekord.key.SingleValueKey;
import org.magnos.rekord.query.DeleteQuery;
import org.magnos.rekord.query.InsertQuery;
import org.magnos.rekord.query.UpdateQuery;
import org.magnos.rekord.util.ArrayUtil;

public class Table<M extends Model>
{
	
	private static final Field<?>[] NO_FIELDS = {};

	protected final int id;
	protected final String table;
	protected Factory<M> factory;
	protected Column<?>[] keyColumns = {};
	protected Field<?>[] fields = {};
	protected InsertQuery insert;
	protected UpdateQuery update;
	protected DeleteQuery delete;
	protected Condition keyCondition;
	protected Map<String, Field<?>> fieldMap;
	protected View[] views;
	protected Map<String, View> viewMap;
	protected HistoryTable history;
	protected View viewAll;
	protected View viewId;
	
	public Table( String table, Column<?> ... keyColumns )
	{
		this( table, keyColumns, NO_FIELDS );
	}
	
	public Table( String table, Table<? super M> extension)
	{
		this( table, extension.keyColumns, extension.fields );
	}
	
	private Table( String table, Column<?>[] id, Field<?>[] existingFields)
	{
		this.table = table;
		this.id = Rekord.newTable( this );
		this.fieldMap = new HashMap<String, Field<?>>();
		this.viewMap = new HashMap<String, View>();
		this.keyColumns = id;
		this.fields = existingFields;
		this.keyCondition = Conditions.where( id );
		this.mapFields( existingFields );
	}
	
	public void setFields(Field<?> ... newFields)
	{
		int fieldCount = fields.length;
		
		fields = ArrayUtil.join( Field.class, fields, newFields );
		registerFields( fieldCount );
		mapFields( newFields );
		
		insert = new InsertQuery( this );
		update = new UpdateQuery( this );
		delete = new DeleteQuery( this );
	}
	
	public void setViews(View ... newViews)
	{
		views = newViews;
		
		for (View v : views)
		{
			viewMap.put( v.getName(), v );
		}
		
		viewAll = viewMap.get( "all" );
		viewId = viewMap.get( "id" );
	}
	
	private void registerFields(int start)
	{
		while (start < fields.length)
		{
			Field<?> f = fields[start];
			f.setIndex( start );
			f.setTable( this );
			start++;
		}
	}
	
	private void mapFields(Field<?>[] fields)
	{
		if (fields != null)
		{
			for (Field<?> f : fields)
			{
				fieldMap.put( f.getName(), f );
			}	
		}
	}

	public Value<?>[] newValues(Model model)
	{
		final int valueCount = fields.length;
		Value<?>[] values = new Value[ valueCount ];
		
		for (int i = 0; i < valueCount; i++)
		{
			values[i] = fields[i].newValue(model);
		}
		
		return values;
	}
	
	public Key keyForModel(Model model)
	{
		return getKeySize() == 1 ? new SingleModelKey( model ) : new MultiModelKey( model );
	}
	
	public Key keyForCaching()
	{
		return getKeySize() == 1 ? new SingleValueKey( this ) : new MultiValueKey( this );
	}
	
	public Key keyFromResults(ResultSet results) throws SQLException
	{
		Key key = keyForCaching();
		key.fromResultSet( results );
		return key;
	}
	
	public Key keyForFields(Model model, Field<?> ... fields)
	{
		return fields.length == 1 ? new SingleModelKey( model, fields[0] ) : new MultiModelKey( model, fields );
	}

	public int id()
	{
		return id;
	}
	
	public String getName()
	{
		return table;
	}

	public Field<?>[] getFields()
	{
		return fields;
	}

	public Column<?>[] getKeyColumns()
	{
		return keyColumns;
	}
	
	public int getKeySize()
	{
		return keyColumns.length;
	}

	public Condition getKeyCondition()
	{
		return keyCondition;
	}
	
	public InsertQuery getInsert()
	{
		return insert;
	}

	public UpdateQuery getUpdate()
	{
		return update;
	}

	public DeleteQuery getDelete()
	{
		return delete;
	}

	public Factory<M> getFactory()
	{
		return factory;
	}
	
	public void setFactory( Factory<M> factory )
	{
		this.factory = factory;
	}

	public M newModel()
	{
		return factory.create();
	}
	
	public <F extends Field<?>> F getField(String name)
	{
		return (F) fieldMap.get( name );
	}
	
	public View getView(String name)
	{
		return viewMap.get( name );
	}
	
	public HistoryTable getHistory()
	{
		return history;
	}

	public void setHistory( HistoryTable history )
	{
		this.history = history;
	}
	
	public boolean hasHistory()
	{
		return (history != null);
	}
	
	public View getViewAll()
	{
		return viewAll;
	}

	public View getViewId()
	{
		return viewId;
	}
	
}
