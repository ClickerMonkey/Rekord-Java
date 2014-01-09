
package org.magnos.rekord.query.model;

import java.sql.SQLException;

import org.magnos.rekord.ListenerEvent;
import org.magnos.rekord.Logging;
import org.magnos.rekord.Model;
import org.magnos.rekord.Rekord;
import org.magnos.rekord.Table;
import org.magnos.rekord.Transaction;
import org.magnos.rekord.Value;
import org.magnos.rekord.query.QueryBuilder;
import org.magnos.rekord.query.QueryTemplate;
import org.magnos.rekord.query.condition.Condition;
import org.magnos.rekord.query.expr.GroupExpression;


public class ModelDeleteQuery
{

	protected Table table;
	protected QueryTemplate<Model> queryTemplate;

	public ModelDeleteQuery( Table table )
	{
	    this.table = table;
	    this.queryTemplate = createQueryTemplate( table );
	}

	public boolean execute( Model model ) throws SQLException
	{
		Rekord.log( Logging.DELETES, "%s -> %s", queryTemplate.getQuery(), model.getKey() );

		table.notifyListeners( model, ListenerEvent.PRE_DELETE );
		
		final Value<?>[] values = model.getValues();

		for (Value<?> v : values)
		{
			v.preDelete( model );
		}
		
		boolean deleted = queryTemplate.create().bind( model ).executeUpdate() > 0;

		if (deleted)
		{
		    Transaction trans = Rekord.getTransaction();
		    trans.purge( model );
		    
			for (Value<?> v : values)
			{
				v.postDelete( model );
			}
			
			table.notifyListeners( model, ListenerEvent.POST_DELETE );
		}

		return deleted;
	}
	
	public static QueryTemplate<Model> createQueryTemplate( Table table )
	{
	    Condition where = GroupExpression.detached().whereKeyBind( table );
        
	    QueryBuilder qb = new QueryBuilder();
        qb.append( "DELETE FROM ", table.getQuotedName(), " WHERE " );
        where.toQuery( qb );
        
        return new QueryTemplate<Model>( table, qb.toString(), null, qb.getBindsArray(), null );
	}

}
