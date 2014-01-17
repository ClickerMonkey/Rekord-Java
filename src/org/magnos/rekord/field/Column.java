
package org.magnos.rekord.field;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.magnos.rekord.Converter;
import org.magnos.rekord.Field;
import org.magnos.rekord.FieldLoad;
import org.magnos.rekord.Model;
import org.magnos.rekord.Type;
import org.magnos.rekord.Value;
import org.magnos.rekord.query.InsertAction;
import org.magnos.rekord.query.Query;
import org.magnos.rekord.query.SelectQuery;
import org.magnos.rekord.query.expr.ColumnResolver;
import org.magnos.rekord.util.SqlUtil;


public class Column<T> extends AbstractField<T>
{

    protected final int sqlType;
    protected final Type<Object> type;
    protected final String in;
    protected final String out;
    protected final T defaultValue;
    protected final Converter<Object, T> converter;

    public Column( String column, int sqlType, Type<Object> type, int flags, String in, String out, T defaultValue, Converter<Object, T> converter )
    {
        super( column, flags );

        this.sqlType = sqlType;
        this.type = type;
        this.in = in;
        this.out = out;
        this.defaultValue = defaultValue;
        this.converter = converter;
    }

    @Override
    public boolean isSelectable()
    {
        return !is( LAZY );
    }

    @Override
    public String getSelectExpression( ColumnResolver resolver, FieldLoad fieldLoad )
    {
        int limit = fieldLoad.getLimit();

        if (limit == -1)
        {
            return resolver.resolve( this );
        }

        return type.getPartialExpression( resolver.resolve( this ), limit, quotedName );
    }

    @Override
    public InsertAction getInsertAction()
    {
        return is( HAS_DEFAULT ) ? InsertAction.RETURN : InsertAction.VALUE;
    }

    @Override
    public boolean isUpdatable()
    {
        return !is(READ_ONLY) || is(ALWAYS_UPDATE);
    }

    @Override
    public String getSaveExpression()
    {
        return out;
    }

    @Override
    public Value<T> newValue( Model model )
    {
        return new ColumnValue<T>( this );
    }
    
    public String getIn()
    {
        return in;
    }

    public String getOut()
    {
        return out;
    }

    public String getSelectionExpression()
    {
        return in.replaceAll( "\\?", name );
    }

    public int getSqlType()
    {
        return sqlType;
    }

    public Type<Object> getType()
    {
        return type;
    }

    public T getDefaultValue()
    {
        return defaultValue;
    }

    public Converter<Object, T> getConverter()
    {
        return converter;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = beginToString();
        sb.append( ", sql-type=" ).append( sqlType );
        sb.append( ", type=" ).append( type.getClass().getSimpleName() );
        sb.append( ", in=" ).append( in );
        sb.append( ", out=" ).append( out );
        sb.append( ", default-value=" ).append( type.toString( converter.toDatabase( defaultValue ) ) );
        sb.append( ", converter=" ).append( converter.getClass().getSimpleName() );
        return endToString( sb );
    }

    private static class ColumnValue<T> implements Value<T>
    {

        private final Column<T> field;
        private boolean changed = false;
        private boolean partial = false;
        private boolean defaultValue = false;
        private T value;

        public ColumnValue( Column<T> field )
        {
            this.field = field;
        }

        @Override
        public T get( Model model )
        {
            if (field.is( LAZY ) && value == null && model.hasKey())
            {
                try
                {
                    Query<Model> query = new SelectQuery<Model>( model ).create();
                    query.bind( model );

                    value = query.first( field );
                    partial = false;
                    defaultValue = false;
                }
                catch (SQLException e)
                {
                    throw new RuntimeException( e );
                }
            }
            else if (value == null)
            {
                value = field.getDefaultValue();
                defaultValue = true;
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
            if (!SqlUtil.equals( this.value, value ))
            {
                this.value = value;
                this.changed = true;
                this.partial = false;
                this.defaultValue = false;
            }
        }

        @Override
        public boolean hasChanged()
        {
            return changed;
        }

        @Override
        public void clearChanges()
        {
            changed = false;
        }

        @Override
        public InsertAction getInsertAction()
        {
            return field.is( HAS_DEFAULT ) && defaultValue ? InsertAction.RETURN : InsertAction.VALUE;
        }

        @Override
        public boolean isUpdatable()
        {
            return field.is(ALWAYS_UPDATE) || (!field.is( READ_ONLY ) && !partial && changed);
        }

        @Override
        public String getSaveExpression()
        {
            return field.getSaveExpression();
        }
        
        @Override
        public void fromResultSet( ResultSet results ) throws SQLException
        {
            final Type<Object> type = field.getType();
            final Converter<Object, T> converter = field.getConverter();

            Object databaseValue = type.fromResultSet( results, field.getName(), !field.is( NON_NULL ) );
            value = converter.fromDatabase( databaseValue );
            defaultValue = false;
            changed = false;
        }

        @Override
        public int toPreparedStatement( PreparedStatement preparedStatement, int paramIndex ) throws SQLException
        {
            final Type<Object> type = field.getType();
            final Converter<Object, T> converter = field.getConverter();

            type.toPreparedStatement( preparedStatement, converter.toDatabase( value ), paramIndex );

            return paramIndex + 1;
        }

        @Override
        public void load( FieldLoad fieldLoad ) throws SQLException
        {

        }

        @Override
        public void fromSelect( ResultSet results, FieldLoad fieldLoad ) throws SQLException
        {
            fromResultSet( results );

            int limit = fieldLoad.getLimit();

            if (limit != -1)
            {
                partial = field.getType().isPartial( value, limit );
            }
        }

        @Override
        public void postSelect( Model model, FieldLoad fieldLoad ) throws SQLException
        {

        }

        @Override
        public void preSave( Model model ) throws SQLException
        {
            if (field.is( NON_NULL ) && !field.is( GENERATED ) && value == null)
            {
                throw new RuntimeException( "field " + field.getName() + " on type " + model.getTable().getName() + " was null and it cannot be: " + model );
            }
        }

        @Override
        public void postSave( Model model ) throws SQLException
        {

        }

        @Override
        public void preDelete( Model model ) throws SQLException
        {

        }

        @Override
        public void postDelete( Model model ) throws SQLException
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

        @Override
        public String toString()
        {
            return field.getName() + "=" + value;
        }
    }

}
