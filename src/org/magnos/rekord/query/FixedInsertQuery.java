
package org.magnos.rekord.query;

import java.sql.SQLException;

import org.magnos.rekord.Logging;
import org.magnos.rekord.Model;
import org.magnos.rekord.Rekord;
import org.magnos.rekord.Table;


public class FixedInsertQuery extends InsertQuery
{

	public FixedInsertQuery( Table table )
	{
		super( table );
		prepareFixed();
		buildQuery();
	}

	public boolean execute( Model model ) throws SQLException
	{
		Rekord.log( Logging.UPDATES, "pre-insert: %s -> %s", query, model );
		
		boolean recordsInserted = executeInsert( model );

		Rekord.log( Logging.UPDATES, "post-insert: %s -> %s", query, model );
		
		return recordsInserted;
	}

}
