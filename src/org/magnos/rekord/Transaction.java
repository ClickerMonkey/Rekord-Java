package org.magnos.rekord;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

public interface Transaction
{
	public Connection getConnection();
	public PreparedStatement prepare(String query) throws SQLException;
	public void start();
	public boolean isStarted();
	public void end(boolean commit);
	public void close();
	public boolean isClosed();
	
	public <T extends Model> Map<Key, T> getCache(Table table);
	public <T extends Model> T getCached(Table table, Key key);
	public boolean cache(Table table, Model model);
	public void purge(Table table, Model model);
}
