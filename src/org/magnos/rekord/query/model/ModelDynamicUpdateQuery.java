package org.magnos.rekord.query.model;

import java.sql.SQLException;

import org.magnos.rekord.Model;
import org.magnos.rekord.Table;

public class ModelDynamicUpdateQuery extends ModelUpdateQuery
{
	
	public ModelDynamicUpdateQuery(Table table)
	{
		super( table );
	}
	
	public boolean execute( Model model ) throws SQLException
	{
		preSave( model );
		
		prepare( model.getValues() );
		
		return updateModel( model );
	}
	
}
