
package org.magnos.rekord.field;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.magnos.rekord.Converter;
import org.magnos.rekord.Field;
import org.magnos.rekord.FieldView;
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
		return !is(LAZY);
	}
	
	@Override
	public String getSelectionExpression(FieldView fieldView)
	{
		int limit = fieldView.getLimit();

		if (limit == -1)
		{
			return getSelectionExpression();
		}
		
		return type.getPartialExpression( getSelectionExpression(), limit, quotedName );	
	}

	@Override
	public void prepareInsert( InsertQuery query )
	{
		if (is( HAS_DEFAULT ))
		{
			query.addReturning( name );
		}
		else
		{
			query.addColumn( name, out );
		}
	}

	@Override
	public void prepareUpdate( UpdateQuery query )
	{
		if (!is( READ_ONLY ))
		{
			query.addSet( this );
		}
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
		return in.replaceAll( "\\?", quotedName );
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
		sb.append( ", default-value=" ).append( type.toString( converter.convertTo( defaultValue ) ) );
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
			this.value = field.getDefaultValue();
		}

		@Override
		public T get( Model model )
		{
			if (field.is( LAZY ) && value == null && model.hasKey())
			{
				try
				{
					SelectQuery<Model> query = new SelectQuery<Model>( model );

					value = query.grab( field );
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
		public void fromResultSet( ResultSet results ) throws SQLException
		{
			final Type<Object> type = field.getType();
			final Converter<Object, T> converter = field.getConverter();

			Object databaseValue = type.fromResultSet( results, field.getName(), !field.is( NON_NULL ) );
			value = converter.convertFrom( databaseValue );
			defaultValue = false;
		}

		@Override
		public int toPreparedStatement( PreparedStatement preparedStatement, int paramIndex ) throws SQLException
		{
			final Type<Object> type = field.getType();
			final Converter<Object, T> converter = field.getConverter();

			type.toPreparedStatement( preparedStatement, converter.convertTo( value ), paramIndex );

			return paramIndex + 1;
		}

		@Override
		public void load( FieldView fieldView ) throws SQLException
		{

		}

		@Override
		public void prepareDynamicInsert( InsertQuery query )
		{
			if (field.is( HAS_DEFAULT ) && defaultValue)
			{
				query.addReturning( field.getName() );
			}
			else
			{
				query.addColumn( field.getQuotedName(), field.getOut() );
			}
		}

		@Override
		public int toInsert( PreparedStatement preparedStatement, int paramIndex ) throws SQLException
		{
			if (!field.is( HAS_DEFAULT ) || !defaultValue)
			{
				paramIndex = toPreparedStatement( preparedStatement, paramIndex );
			}

			return paramIndex;
		}

		@Override
		public void fromInsertReturning( ResultSet results ) throws SQLException
		{
			if (field.is( HAS_DEFAULT ) && defaultValue)
			{
				fromResultSet( results );
			}
		}

		@Override
		public void prepareDynamicUpdate( UpdateQuery query )
		{
			if (!field.is( READ_ONLY ) && !partial)
			{
				query.addSet( field );
			}
		}

		@Override
		public int toUpdate( PreparedStatement preparedStatement, int paramIndex ) throws SQLException
		{
			if (!field.is( READ_ONLY ) && !partial)
			{
				paramIndex = toPreparedStatement( preparedStatement, paramIndex );
			}

			return paramIndex;
		}

		@Override
		public void fromSelect( ResultSet results, FieldView fieldView ) throws SQLException
		{
			fromResultSet( results );

			int limit = fieldView.getLimit();

			if (limit != -1)
			{
				partial = field.getType().isPartial( value, limit );
			}
		}

		@Override
		public void postSelect( Model model, FieldView fieldView ) throws SQLException
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
