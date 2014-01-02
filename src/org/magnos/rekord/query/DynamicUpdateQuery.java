package org.magnos.rekord.query;

import java.sql.SQLException;

import org.magnos.rekord.Model;
import org.magnos.rekord.Table;

public class DynamicUpdateQuery extends UpdateQuery
{
	
	public DynamicUpdateQuery(Table table)
	{
		super( table );
	}
	
	public boolean execute( Model model ) throws SQLException
	{
		preSave( model );
		
		prepareDynamic( model );
		
		return updateModel( model );
	}
	
}
