
package org.magnos.rekord.query.model;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.magnos.rekord.ListenerEvent;
import org.magnos.rekord.Model;
import org.magnos.rekord.Rekord;
import org.magnos.rekord.Table;
import org.magnos.rekord.Transaction;
import org.magnos.rekord.Value;
import org.magnos.rekord.query.InsertAction;
import org.magnos.rekord.query.NativeQuery;
import org.magnos.rekord.query.Query;
import org.magnos.rekord.query.QueryTemplate;
import org.magnos.rekord.query.Queryable;


public abstract class ModelInsertQuery
{

	protected Table table;
	protected QueryTemplate<Model> queryTemplate;
	protected String query;
	protected StringBuilder columns = new StringBuilder();
	protected StringBuilder values = new StringBuilder();
	protected StringBuilder returnings = new StringBuilder();

	public ModelInsertQuery( Table table )
	{
		this.table = table;
	}
	
	protected void prepare(Queryable[] queryables)
	{
	    for (Queryable q : queryables)
	    {
	        InsertAction insertAction = q.getInsertAction();
	        
	        switch (insertAction) 
	        {
	        case NONE:
	            break;
	        case RETURN:
	            append( returnings, ",", "#" + q.getName() );
	            break;
	        case VALUE:
	            append( columns, ",", q.getQuotedName() );
	            append( values, ",", q.getSaveExpression() );
	            break;
	        }
	    }
	}
	
	protected void prepareFixed()
	{
	    prepare( table.getFields() );
	}
	
	protected void prepareDynamic(Model model)
	{
	    prepare( model.getValues() );
	}

	protected void buildQuery()
	{
		StringBuilder queryBuilder = new StringBuilder();
		queryBuilder.append( "INSERT INTO " );
		queryBuilder.append( table.getQuotedName() );
		queryBuilder.append( " " );

		if (columns.length() > 0)
		{
			queryBuilder.append( "(" ).append( columns ).append( ")" );
			queryBuilder.append( " VALUES " );
			queryBuilder.append( "(" ).append( values ).append( ")" );
		}
		else
		{
		    queryBuilder.append( "DEFAULT VALUES" );
		}
		
		if (returnings.length() > 0)
		{
			queryBuilder.append( " RETURNING " ).append( returnings );
		}

		query = queryBuilder.toString();
		queryTemplate = NativeQuery.parse( table, query );
	}

	private void append( StringBuilder out, String delimiter, String text )
	{
		if (out.length() > 0)
		{
			out.append( delimiter );
		}

		out.append( text );
	}
	
	protected boolean executeInsert( Model model ) throws SQLException
	{
		model.getTable().notifyListeners( model, ListenerEvent.PRE_INSERT );
		
		final Value<?>[] values = model.getValues();

		for (Value<?> v : values)
		{
			v.preSave( model );
		}

        boolean recordsInserted = false;
        
		Query<Model> query = queryTemplate.create();
		query.bind( model );
		
		if (returnings.length() == 0)
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
			trans.cache( model );
		}
		
		for (Value<?> v : values)
		{
			v.postSave( model );
			v.clearChanges();
		}
		
		model.getTable().notifyListeners( model, ListenerEvent.POST_INSERT );

		return recordsInserted;
	}

	public abstract boolean execute( Model model ) throws SQLException;

}
