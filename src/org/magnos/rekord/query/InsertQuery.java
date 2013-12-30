
package org.magnos.rekord.query;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.magnos.rekord.Field;
import org.magnos.rekord.Logging;
import org.magnos.rekord.Model;
import org.magnos.rekord.Rekord;
import org.magnos.rekord.Table;
import org.magnos.rekord.Transaction;
import org.magnos.rekord.Value;
import org.magnos.rekord.util.SqlUtil;


public class InsertQuery
{

	protected Table<?> table;
	protected String query;
	protected StringBuilder columns = new StringBuilder();
	protected StringBuilder values = new StringBuilder();
	protected StringBuilder returnings = new StringBuilder();

	public InsertQuery( Table<?> table )
	{
		this.table = table;

		for (Field<?> f : table.getFields())
		{
			f.prepareInsert( this );
		}

		StringBuilder queryBuilder = new StringBuilder();
		queryBuilder.append( "INSERT INTO " );
		queryBuilder.append( SqlUtil.namify( table.getName() ) );
		queryBuilder.append( " " );

		if (columns.length() > 0)
		{
			queryBuilder.append( "(" ).append( columns ).append( ")" );
			queryBuilder.append( " VALUES " );
			queryBuilder.append( "(" ).append( values ).append( ")" );
		}
		if (returnings.length() > 0)
		{
			queryBuilder.append( " RETURNING " ).append( returnings );
		}

		this.query = queryBuilder.toString();
	}

	public void addColumn( String column, String value )
	{
		append( columns, ",", SqlUtil.namify( column ) );
		append( values, ",", value );
	}

	public void addReturning( String returning )
	{
		append( returnings, ",", SqlUtil.namify( returning ) );
	}

	private void append( StringBuilder out, String delimiter, String text )
	{
		if (out.length() > 0)
		{
			out.append( delimiter );
		}

		out.append( text );
	}

	public boolean execute( Model model ) throws SQLException
	{
		Rekord.log( Logging.UPDATES, "pre-insert: %s -> %s", query, model );
		
		final Value<?>[] values = model.getValues();
		Transaction trans = Rekord.getTransaction();

		for (Value<?> v : values)
		{
			v.preSave( model );
		}
		
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
					
					trans.cache( model );
				}
			}
			finally
			{
				results.close();
			}
		}
		
		for (Value<?> v : values)
		{
			v.postSave( model );
			v.clearChanges();
		}

		Rekord.log( Logging.UPDATES, "post-insert: %s -> %s", query, model );
		
		return recordsInserted;
	}

}
