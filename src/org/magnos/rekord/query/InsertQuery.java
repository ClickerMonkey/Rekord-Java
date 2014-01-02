
package org.magnos.rekord.query;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.magnos.rekord.Field;
import org.magnos.rekord.Model;
import org.magnos.rekord.Rekord;
import org.magnos.rekord.Table;
import org.magnos.rekord.Transaction;
import org.magnos.rekord.Value;


public abstract class InsertQuery
{

	protected Table table;
	protected String query;
	protected StringBuilder columns = new StringBuilder();
	protected StringBuilder values = new StringBuilder();
	protected StringBuilder returnings = new StringBuilder();

	public InsertQuery( Table table )
	{
		this.table = table;
	}
	
	protected void prepareFixed()
	{
		for (Field<?> f : table.getFields())
		{
			f.prepareInsert( this );
		}
	}
	
	protected void prepareDynamic(Model model)
	{
		for (Value<?> v : model.getValues())
		{
			v.prepareDynamicInsert( this );
		}
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
		    queryBuilder.append( " DEFAULT VALUES " );
		}
		
		if (returnings.length() > 0)
		{
			queryBuilder.append( " RETURNING " ).append( returnings );
		}

		this.query = queryBuilder.toString();
	}
	
	public void addColumn( String column, String value )
	{
		append( columns, ",", column );
		append( values, ",", value );
	}

	public void addReturning( String returning )
	{
		append( returnings, ",", returning );
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
		final Value<?>[] values = model.getValues();

		for (Value<?> v : values)
		{
			v.preSave( model );
		}

		Transaction trans = Rekord.getTransaction();
		PreparedStatement stmt = trans.prepare( query );
		boolean recordsInserted = false;
		
		int paramIndex = 1;
		for (Value<?> v : values)
		{
			paramIndex = v.toInsert( stmt, paramIndex );
		}

		if (returnings.length() == 0)
		{
			recordsInserted = stmt.executeUpdate() > 0;
		}
		else
		{
			ResultSet results = stmt.executeQuery();
			
			try
			{
				if (results.next())
				{
					for (Value<?> v : values)
					{
						v.fromInsertReturning( results );
					}
					
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
			trans.cache( model );
		}
		
		for (Value<?> v : values)
		{
			v.postSave( model );
			v.clearChanges();
		}

		return recordsInserted;
	}

	public abstract boolean execute( Model model ) throws SQLException;

}
