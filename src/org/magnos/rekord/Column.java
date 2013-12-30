package org.magnos.rekord;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.magnos.rekord.query.InsertQuery;
import org.magnos.rekord.query.SelectQuery;
import org.magnos.rekord.query.UpdateQuery;
import org.magnos.rekord.util.SqlUtil;


public class Column<T> extends AbstractField<T>
{
	
	protected int type;

	public Column( String column, int type, int flags )
	{
		super( column, flags );
		
		this.type = type;
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
			query.addColumn( name, "?" );
		}
	}

	@Override
	public void prepareSelect(SelectQuery<?> query)
	{
		if (!is(Flags.LAZY))
		{
			query.select( this, SqlUtil.namify( name ) );
		}
	}
	
	@Override
	public Value<T> newValue(Model model)
	{
		return new ColumnValue<T>( this );
	}

	public int getType()
	{
		return type;
	}

	private static class ColumnValue<T> implements Value<T>
	{
		private final Column<T> field;
		private boolean changed = false;
		private T value;
		
		public ColumnValue(Column<T> field)
		{
			this.field = field;
		}
		
		@SuppressWarnings ("rawtypes" )
		@Override
		public T get(Model model)
		{
			if (field.is( Flags.LAZY ) && value == null && model.hasKey())
			{
				try
				{
					value = (T) new SelectQuery( model ).grab( field );	
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
			value = (T) results.getObject( field.getName() );
		}
		
		@Override
		public int toPreparedStatement(PreparedStatement preparedStatement, int paramIndex) throws SQLException
		{
			preparedStatement.setObject( paramIndex, value, field.getType() );
			
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
				query.addSet( field.getName(), "?" );
			}
		}

		@Override
		public int toUpdate( PreparedStatement preparedStatement, int paramIndex ) throws SQLException
		{
			if (!field.is(Flags.READ_ONLY))
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
			
		}
		
		@Override
		public void postSave(Model model) throws SQLException
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
