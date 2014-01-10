package org.magnos.rekord.query.model;

import java.sql.SQLException;

import org.magnos.rekord.Model;

public interface ModelQuery
{
	public boolean execute( Model model ) throws SQLException;
}
