
package org.magnos.rekord.query.model;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.magnos.rekord.ListenerEvent;
import org.magnos.rekord.Logging;
import org.magnos.rekord.Model;
import org.magnos.rekord.Rekord;
import org.magnos.rekord.Table;
import org.magnos.rekord.Transaction;
import org.magnos.rekord.Value;
import org.magnos.rekord.field.Column;
import org.magnos.rekord.query.InsertQuery;
import org.magnos.rekord.query.Query;
import org.magnos.rekord.query.QueryTemplate;
import org.magnos.rekord.query.Queryable;


public class ModelInsertQuery implements ModelQuery
{

	protected final Table table;
	protected final boolean dynamic;
	protected QueryTemplate<Model> queryTemplate;
	
	public ModelInsertQuery( Table table, boolean dynamic )
	{
		this.table = table;
		this.dynamic = dynamic;
		
		if (!dynamic)
		{
		    buildQuery( table.getGivenFields() );
		}
	}
	
	protected void buildQuery(Queryable[] queryables)
	{
	    queryTemplate = InsertQuery.forFields( table, queryables );
	}
	
	public boolean execute( Model model ) throws SQLException
	{
		Table parentTable = table.getParentTable();
		Value<?>[] values = model.getValues( table.getGivenFields() );
		
		if (parentTable != null)
		{
			model.set( (Column<Object>) parentTable.getDiscriminatorColumn(), table.getDiscriminatorValue() );
			
			parentTable.getInsert().execute( model );
		}
	    
	    if (dynamic)
	    {
	        buildQuery( values );
	    }
	    
	    Query<Model> query = queryTemplate.create();
	    
	    Rekord.log( Logging.UPDATES, "pre-insert: %s -> %s", queryTemplate.getQuery(), model );
	    
		model.getTable().notifyListeners( model, ListenerEvent.PRE_INSERT );
		
		for (Value<?> v : values)
		{
			v.preSave( model );
		}

        boolean recordsInserted = false;
        
		query.bind( model );
		
		if (!query.hasSelectFields())
		{
			recordsInserted = query.executeUpdate() > 0;
		}
		else
		{
			ResultSet results = query.getResults();
			
			try
			{
				if (results.next())
				{
		            query.populate( results, model );
		            
					recordsInserted = true;
				}
			}
			finally
			{
				results.close();
			}
		}

		if (recordsInserted)
		{
		    Transaction trans = Rekord.getTransaction();
			trans.cache( table, model );
		}
		
		for (Value<?> v : values)
		{
			v.postSave( model );
			v.clearChanges();
		}
		
		model.getTable().notifyListeners( model, ListenerEvent.POST_INSERT );

        Rekord.log( Logging.UPDATES, "post-insert: %s -> %s", queryTemplate.getQuery(), model );
		
		return recordsInserted;
	}

}
