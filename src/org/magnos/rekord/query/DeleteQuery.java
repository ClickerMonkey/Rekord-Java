package org.magnos.rekord.query;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.magnos.rekord.Logging;
import org.magnos.rekord.Model;
import org.magnos.rekord.Rekord;
import org.magnos.rekord.Table;
import org.magnos.rekord.Transaction;
import org.magnos.rekord.condition.Condition;
import org.magnos.rekord.condition.Conditions;
import org.magnos.rekord.util.SqlUtil;

public class DeleteQuery
{
	
	protected Table<?> table;
	protected Condition condition;
	protected String query;
	
	public DeleteQuery(Table<?> table)
	{
		this.table = table;
		this.condition = Conditions.where( table.getKeyColumns() );
		
		StringBuilder queryBuilder = new StringBuilder();
		queryBuilder.append( "DELETE FROM " );
		queryBuilder.append( SqlUtil.namify( table.getName() ) );
		queryBuilder.append( " WHERE " );
		condition.toQuery( queryBuilder );
		
		this.query = queryBuilder.toString();
	}
	
	public boolean execute( Model model ) throws SQLException
	{
		Rekord.log( Logging.DELETES, "%s -> %s", query, model.getKey() );
		
		Transaction trans = Rekord.getTransaction();
		PreparedStatement stmt = trans.prepare( query );
		
		Conditions.whereBind( condition, table.getKeyColumns(), model.getValues() );
		condition.toPreparedstatement( stmt, 1 );
		
		return stmt.executeUpdate() > 0;
	}
	
}
