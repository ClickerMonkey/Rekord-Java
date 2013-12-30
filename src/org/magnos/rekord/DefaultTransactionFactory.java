package org.magnos.rekord;

import java.beans.PropertyVetoException;
import java.sql.SQLException;

import com.mchange.v2.c3p0.ComboPooledDataSource;

public class DefaultTransactionFactory implements Factory<Transaction>
{

	private ComboPooledDataSource dataSource;
	
	public DefaultTransactionFactory(String driverClass, String jdbcUrl, String user, String password) throws PropertyVetoException
	{
		dataSource = new ComboPooledDataSource();
		dataSource.setDriverClass( driverClass );
		dataSource.setJdbcUrl( jdbcUrl );
		dataSource.setUser( user );
		dataSource.setPassword( password );
	}
	
	@Override
	public Transaction create()
	{
		try
		{
			return new AbstractTransaction( dataSource.getConnection() );
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			
			return null;
		}
	}

}
