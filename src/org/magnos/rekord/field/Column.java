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
import org.magnos.rekord.Type;
import org.magnos.rekord.Value;
import org.magnos.rekord.query.InsertQuery;
import org.magnos.rekord.query.SelectQuery;
import org.magnos.rekord.query.UpdateQuery;
import org.magnos.rekord.util.SqlUtil;


public class Column<T> extends AbstractField<T>
{
	
	protected final int sqlType;
	protected final Type<T> type;
	protected final String in;
	protected final String out;
	protected final T defaultValue;

	public Column( String column, int sqlType, Type<T> type, int flags, String in, String out, T defaultValue )
	{
		super( column, flags );
		
		this.sqlType = sqlType;
		this.type = type;
		this.in = in;
		this.out = out;
		this.defaultValue = defaultValue;
	}

	@Override
	public void prepareInsert(InsertQuery query)
	{
		if (is(Flags.GENERATED))
		{
			query.addReturning( name );
		}
		else
		{
			query.addColumn( name, out );
		}
	}

	@Override
	public void prepareSelect(SelectQuery<?> query)
	{
		if (!is(Flags.LAZY))
		{
			query.select( this, getSelectionExpression() );
		}
	}
	
	@Override
	public Value<T> newValue(Model model)
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
        return in.replaceAll( "\\?", SqlUtil.namify( name ) );
    }
    
    public int getSqlType()
    {
        return sqlType;
    }
    
    public Type<T> getType()
    {
        return type;
    }
    
    public T getDefaultValue()
    {
        return defaultValue;
    }

    private static class ColumnValue<T> implements Value<T>
	{
		private final Column<T> field;
		private boolean changed = false;
		private T value;
		
		public ColumnValue(Column<T> field)
		{
			this.field = field;
			this.value = field.getDefaultValue();
		}
		
		@Override
		public T get(Model model)
		{
			if (field.is( Flags.LAZY ) && value == null && model.hasKey())
			{
				try
				{
				    SelectQuery<Model> query = new SelectQuery<Model>( model );
				    
				    value = query.grab( field );
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
			if (!SqlUtil.equals( this.value, value ))
			{
				this.value = value;
				this.changed = true;	
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
		public void fromResultSet( ResultSet results ) throws SQLException
		{
		    value = field.getType().fromResultSet( results, field.getName(), !field.is( Flags.NON_NULL ) );
		}
		
		@Override
		public int toPreparedStatement(PreparedStatement preparedStatement, int paramIndex) throws SQLException
		{
		    field.getType().toPreparedStatement( preparedStatement, value, paramIndex );
			
			return paramIndex + 1;
		}
		
		@Override
		public void fromInsertReturning(ResultSet results) throws SQLException
		{
			if (field.is( Flags.GENERATED ))
			{
				fromResultSet( results );
			}
		}
		
		@Override
		public int toInsert(PreparedStatement preparedStatement, int paramIndex) throws SQLException
		{
			if (!field.is( Flags.GENERATED ))
			{
				paramIndex = toPreparedStatement( preparedStatement, paramIndex );
			}
			
			return paramIndex;
		}

		@Override
		public void prepareUpdate( UpdateQuery query )
		{
			if (!field.is( Flags.READ_ONLY ))
			{
				query.addSet( field.getName(), field.getOut() );
			}
		}

		@Override
		public int toUpdate( PreparedStatement preparedStatement, int paramIndex ) throws SQLException
		{
			if (!field.is( Flags.READ_ONLY ))
			{
				paramIndex = toPreparedStatement( preparedStatement, paramIndex );
			}
			
			return paramIndex;
		}

		@Override
		public void fromSelect( ResultSet results ) throws SQLException
		{
			fromResultSet( results );
		}

		@Override
		public void postSelect(Model model, SelectQuery<?> query) throws SQLException
		{
			
		}
		
		@Override
		public void preSave(Model model) throws SQLException
		{
			if (field.is( Flags.NON_NULL ) && !field.is( Flags.GENERATED ) && value == null)
			{
				throw new RuntimeException( "field " + field.getName() + " on type " + model.getTable().getName() + " was null and it cannot be: " + model );
			}
		}
		
		@Override
		public void postSave(Model model) throws SQLException
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
		public void serialize(ObjectOutputStream out) throws IOException
		{
			out.writeObject( value );
		}

		@Override
		public void deserialize(ObjectInputStream in) throws IOException, ClassNotFoundException
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
