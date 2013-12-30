package org.magnos.rekord;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

public interface Transaction
{
	public Connection getConnection();
	public PreparedStatement prepare(String query) throws SQLException;
	public boolean isStarted();
	public void start();
	public void end(boolean commit);
	public void close();
	
	public <T extends Model> Map<Key, T> getCache(Table<T> table);
	public <T extends Model> T getCached(Table<T> table, Key key);
	public <T extends Model> void cache(T model);
}
