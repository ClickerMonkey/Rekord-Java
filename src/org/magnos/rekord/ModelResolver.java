package org.magnos.rekord;

import java.sql.ResultSet;
import java.sql.SQLException;


public interface ModelResolver
{

    public Model resolve( Transaction trans, ResultSet results, Table table, LoadProfile load, Field<?>[] selectFields ) throws SQLException;
    
    public void populate( Model model, ResultSet results, boolean overwrite, LoadProfile load, Field<?>[] selectFields ) throws SQLException;

}
