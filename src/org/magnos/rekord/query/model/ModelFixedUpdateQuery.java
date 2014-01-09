package org.magnos.rekord.query.model;

import java.sql.SQLException;

import org.magnos.rekord.Model;
import org.magnos.rekord.Table;

public class ModelFixedUpdateQuery extends ModelUpdateQuery
{
	
	public ModelFixedUpdateQuery(Table table)
	{
		super( table );
		
		prepare( table.getFields() );
	}
	
	public boolean execute( Model model ) throws SQLException
	{
		preSave( model );
		
		return updateModel( model );
	}
	
}
