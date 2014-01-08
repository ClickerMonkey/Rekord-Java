package org.magnos.rekord.listener;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.magnos.rekord.Listener;
import org.magnos.rekord.ListenerEvent;
import org.magnos.rekord.Model;
import org.magnos.rekord.Rekord;
import org.magnos.rekord.Table;
import org.magnos.rekord.Transaction;
import org.magnos.rekord.field.Column;

public class Sequencer implements Listener<Model>
{
	
	public String sequenceName;
	public String columnName;
	public Column<?> column;

	@Override
	public void onEvent( Model model, ListenerEvent e ) throws SQLException
	{
		final Transaction trans = Rekord.getTransaction();
		
		PreparedStatement stmt = trans.prepare( "SELECT nextval('" + sequenceName + "') AS " + columnName );
		ResultSet results = stmt.executeQuery();
		
		try
		{
			model.valueOf( column ).fromResultSet( results );
		}
		finally
		{
			results.close();
		}
	}
	
	@Override
	public void configure( Table table, Map<String, String> attributes ) throws Exception
	{
		sequenceName = attributes.get( "sequence" );
		columnName = attributes.get( "column" );
		column = table.getField( columnName );
	}

}
