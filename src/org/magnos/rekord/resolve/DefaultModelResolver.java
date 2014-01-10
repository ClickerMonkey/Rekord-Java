package org.magnos.rekord.resolve;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.magnos.rekord.Field;
import org.magnos.rekord.FieldLoad;
import org.magnos.rekord.Key;
import org.magnos.rekord.ListenerEvent;
import org.magnos.rekord.LoadProfile;
import org.magnos.rekord.Model;
import org.magnos.rekord.ModelResolver;
import org.magnos.rekord.Table;
import org.magnos.rekord.Transaction;
import org.magnos.rekord.Value;



public class DefaultModelResolver implements ModelResolver
{

    @Override
    public Model resolve( Transaction trans, ResultSet results, Table table, LoadProfile load, Field<?>[] selectFields ) throws SQLException
    {
        final Key key = table.keyFromResults( results );

        Model model = trans.getCached( table, key );

        if (model == null)
        {
            model = table.newModel();

            populate( model, results, true, load, selectFields );

            trans.cache( table, model );
        }
        else
        {
            populate( model, results, false, load, selectFields );
        }

        table.notifyListeners( model, ListenerEvent.POST_SELECT );

        return model;
    }

    @Override
    public void populate( Model model, ResultSet results, boolean overwrite, LoadProfile load, Field<?>[] selectFields ) throws SQLException
    {
        if (load != null)
        {
            for (Field<?> f : selectFields)
            {
                Value<?> value = model.valueOf( f );

                if (overwrite || !value.hasValue())
                {
                    value.fromSelect( results, load.getFieldLoad( f ) );
                }
            }
        }
        else
        {
            for (Field<?> f : selectFields)
            {
                Value<?> value = model.valueOf( f );

                if (overwrite || !value.hasValue())
                {
                    value.fromSelect( results, FieldLoad.DEFAULT );
                }
            }
        }
    }

}
