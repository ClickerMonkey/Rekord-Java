
package org.magnos.rekord.query;

import java.sql.SQLException;

import org.magnos.rekord.Logging;
import org.magnos.rekord.Model;
import org.magnos.rekord.Rekord;
import org.magnos.rekord.Table;


public class DynamicInsertQuery extends InsertQuery
{

	public DynamicInsertQuery( Table table )
	{
		super( table );
	}

	public boolean execute( Model model ) throws SQLException
	{
		Rekord.log( Logging.UPDATES, "pre-insert: %s -> %s", query, model );
		
		prepareDynamic( model );
		buildQuery();
		
		boolean recordsInserted = executeInsert( model );

		Rekord.log( Logging.UPDATES, "post-insert: %s -> %s", query, model );
		
		return recordsInserted;
	}

}
