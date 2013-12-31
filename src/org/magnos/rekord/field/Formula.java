package org.magnos.rekord.field;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.magnos.rekord.Field;
import org.magnos.rekord.Flags;
import org.magnos.rekord.Model;
import org.magnos.rekord.Value;
import org.magnos.rekord.query.InsertQuery;
import org.magnos.rekord.query.SelectQuery;
import org.magnos.rekord.query.UpdateQuery;
import org.magnos.rekord.util.SqlUtil;


public class Formula<T> extends AbstractField<T>
{
    
    private final String formula;
    private final String alias;

    public Formula( String name, String formula, String alias, int flags )
    {
        super( name, flags );
        
        this.formula = formula;
        this.alias = alias;
    }
    
    public String getFormula()
    {
        return formula;
    }
    
    public String getAlias()
    {
        return alias;
    }

    @Override
    public void prepareSelect( SelectQuery<?> query )
    {
        if (!is(Flags.LAZY))
        {
            query.select( this, formula + " AS " + SqlUtil.namify( alias ) );
        }
    }

    @Override
    public void prepareInsert( InsertQuery query )
    {
        
    }

    @Override
    public Value<T> newValue( Model model )
    {
        return new FormulaValue<T>( this );
    }
    
    private static class FormulaValue<T> implements Value<T>
    {
        private final Formula<T> field;
        private T value;
        
        public FormulaValue(Formula<T> field)
        {
            this.field = field;
        }

        @SuppressWarnings ("rawtypes" )
        @Override
        public T get( Model model )
        {
            if (field.is( Flags.LAZY ) && value == null && model.hasKey())
            {
                try
                {
                    value = (T) new SelectQuery( model ).grab( field.getFormula() ); 
                }
                catch (SQLException e)
                {
                    throw new RuntimeException( e );
                }
            }
            
            return value;
        }

        @Override
        public boolean hasValue()
        {
            return (value != null);
        }

        @Override
        public void set( Model model, T value )
        {
            this.value = value;
        }

        @Override
        public boolean hasChanged()
        {
            return false;
        }

        @Override
        public void clearChanges()
        {
            
        }

        @Override
        public void fromInsertReturning( ResultSet results ) throws SQLException
        {
            
        }

        @Override
        public int toInsert( PreparedStatement preparedStatement, int paramIndex ) throws SQLException
        {
            return paramIndex;
        }

        @Override
        public void prepareUpdate( UpdateQuery query )
        {
            
        }

        @Override
        public int toUpdate( PreparedStatement preparedStatement, int paramIndex ) throws SQLException
        {
            return paramIndex;
        }

        @Override
        public void fromSelect( ResultSet results ) throws SQLException
        {
            fromResultSet( results );
        }

        @Override
        public void postSelect( Model model, SelectQuery<?> query ) throws SQLException
        {
            
        }

        @Override
        public void fromResultSet( ResultSet results ) throws SQLException
        {
            value = (T) results.getObject( field.getAlias() );
        }

        @Override
        public int toPreparedStatement( PreparedStatement preparedStatement, int paramIndex ) throws SQLException
        {
            return paramIndex;
        }

        @Override
        public void preSave( Model model ) throws SQLException
        {
            
        }

        @Override
        public void postSave( Model model ) throws SQLException
        {
            
        }

        @Override
        public void preDelete(Model model) throws SQLException
        {
            
        }

        @Override
        public void postDelete(Model model) throws SQLException
        {
            
        }

        @Override
        public void serialize( ObjectOutputStream out ) throws IOException
        {
            out.writeObject( value );
        }

        @Override
        public void deserialize( ObjectInputStream in ) throws IOException, ClassNotFoundException
        {
            value = (T)in.readObject();
        }

        @Override
        public Field<T> getField()
        {
            return field;
        }
        
    }

}
