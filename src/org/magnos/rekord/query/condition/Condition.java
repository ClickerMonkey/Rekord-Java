package org.magnos.rekord.query.condition;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface Condition
{
	
	public void toQuery(StringBuilder query);
	
	public int toPreparedstatement(PreparedStatement stmt, int paramIndex) throws SQLException;
	
}
