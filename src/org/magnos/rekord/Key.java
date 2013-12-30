package org.magnos.rekord;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.magnos.rekord.condition.Condition;
import org.magnos.rekord.field.Column;

public interface Key
{
	public void fromResultSet(ResultSet results) throws SQLException;
	public int toPreparedStatement(PreparedStatement preparedStatement, int paramIndex) throws SQLException;
	public int size();
	public Object valueAt(int index);
	public Column<?> fieldAt(int index);
	public Condition condition();
	public boolean exists();
	public int hashCode();
	public boolean equals(Object o);
}
