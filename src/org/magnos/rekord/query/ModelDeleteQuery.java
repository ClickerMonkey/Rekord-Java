
package org.magnos.rekord.query;

import java.sql.SQLException;

import org.magnos.rekord.ListenerEvent;
import org.magnos.rekord.Logging;
import org.magnos.rekord.Model;
import org.magnos.rekord.Rekord;
import org.magnos.rekord.Table;
import org.magnos.rekord.Value;
import org.magnos.rekord.field.Column;
import org.magnos.rekord.query.condition.Conditions;


public class ModelDeleteQuery
{

	public static final String QUERY_FORMAT = "DELETE FROM %s WHERE %s";
	
	protected Table table;
	protected QueryTemplate<Model> query;

	public ModelDeleteQuery( Table table )
	{
		Column<?>[] keys = table.getKeyColumns();

		String queryString = String.format( QUERY_FORMAT, table.getQuotedName(), Conditions.whereString( keys ) );

		QueryBind[] binds = new QueryBind[keys.length];

		for (int i = 0; i < keys.length; i++)
		{
			Column<?> c = keys[i];

			binds[i] = new QueryBind( c.getName(), i, c, -1, -1 );
		}

		this.query = new QueryTemplate<Model>( table, queryString, null, binds, null );
	}

	public boolean execute( Model model ) throws SQLException
	{
		Rekord.log( Logging.DELETES, "%s -> %s", query.getQuery(), model.getKey() );

		model.getTable().notifyListeners( model, ListenerEvent.PRE_DELETE );
		
		final Value<?>[] values = model.getValues();

		for (Value<?> v : values)
		{
			v.preDelete( model );
		}

		boolean deleted = query.create().bind( model ).executeUpdate() > 0;

		if (deleted)
		{
			for (Value<?> v : values)
			{
				v.postDelete( model );
			}
			
			model.getTable().notifyListeners( model, ListenerEvent.POST_DELETE );
		}

		return deleted;
	}

}
