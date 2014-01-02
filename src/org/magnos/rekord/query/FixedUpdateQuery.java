package org.magnos.rekord.query;

import java.sql.SQLException;

import org.magnos.rekord.Model;
import org.magnos.rekord.Table;

public class FixedUpdateQuery extends UpdateQuery
{
	
	public FixedUpdateQuery(Table table)
	{
		super( table );
		
		prepareFixed();
	}
	
	public boolean execute( Model model ) throws SQLException
	{
		preSave( model );
		
		return updateModel( model );
	}
	
}
