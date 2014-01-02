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
import org.magnos.rekord.Table;
import org.magnos.rekord.Type;
import org.magnos.rekord.Value;
import org.magnos.rekord.query.InsertQuery;
import org.magnos.rekord.query.SelectQuery;
import org.magnos.rekord.query.UpdateQuery;
import org.magnos.rekord.util.SqlUtil;


public class ForeignColumn<T> extends Column<T>
{
	
	protected Column<?> foreignColumn;
	protected Table foreignTable;

	public ForeignColumn( String column, int sqlType, Type<Object> type, String in, String out, T defaultValue, Converter<Object, T> converter )
	{
		super( column, sqlType, type, NONE, in, out, defaultValue, converter );
	}

	@Override
	public void prepareSelect(SelectQuery<?> query)
	{
		query.select( this, getSelectionExpression() );
	}

	@Override
	public void prepareInsert(InsertQuery query)
	{
		query.addColumn( quotedName, out );
	}
	
	@Override
	public Value<T> newValue(Model model)
	{
		return new ForeignValue<T>( this );
	}
	
	public Column<?> getForeignColumn()
	{
		return foreignColumn;
	}
	
	public void setForeignColumn( Column<?> foreignColumn )
	{
		this.foreignColumn = foreignColumn;
	}
	
    public Table getForeignTable()
    {
        return foreignTable;
    }

    public void setForeignTable( Table foreignTable )
    {
        this.foreignTable = foreignTable;
    }

    @Override
	public String toString()
	{
	    StringBuilder sb = beginToString();
        sb.append( ", sql-type=" ).append( sqlType );
        sb.append( ", type=" ).append( type.getClass().getSimpleName() );
        sb.append( ", in=" ).append( in );
        sb.append( ", out=" ).append( out );
        sb.append( ", default-value=" ).append( type.toString( defaultValue ) );
        sb.append( ", converter=" ).append( converter.getClass().getSimpleName() );
        sb.append( ", references=" ).append( foreignTable.getName() ).append( "." ).append( foreignColumn.getName() );
        return endToString( sb );
	}

	private static class ForeignValue<T> implements Value<T>
	{
		private final ForeignColumn<T> field;
		private boolean changed = false;
		private T value;
		
		public ForeignValue(ForeignColumn<T> field)
		{
			this.field = field;
			this.value = field.getDefaultValue();
		}
		
		@Override
		public T get(Model model)
		{
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
			final Type<Object> type = field.getType();
			final Converter<Object, T> converter = field.getConverter();
			
			value = converter.convertFrom( type.fromResultSet( results, field.getName(), !field.is( NON_NULL ) ) );
		}
		
		@Override
		public int toPreparedStatement(PreparedStatement preparedStatement, int paramIndex) throws SQLException
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
		public void prepareDynamicInsert(InsertQuery query)
		{
			query.addColumn( field.getQuotedName(), field.getOut() );
		}
		
		@Override
		public int toInsert(PreparedStatement preparedStatement, int paramIndex) throws SQLException
		{
			return toPreparedStatement( preparedStatement, paramIndex );
		}
		
		@Override
		public void fromInsertReturning(ResultSet results) throws SQLException
		{
			
		}

		@Override
		public void prepareDynamicUpdate( UpdateQuery query )
		{
			query.addSet( field, field.getOut() );
		}

		@Override
		public int toUpdate( PreparedStatement preparedStatement, int paramIndex ) throws SQLException
		{
			return toPreparedStatement( preparedStatement, paramIndex );
		}

		@Override
		public void fromSelect( ResultSet results, FieldView fieldView ) throws SQLException
		{
			fromResultSet( results );
		}

		@Override
		public void postSelect(Model model, FieldView fieldView) throws SQLException
		{
			
		}
		
		@Override
		public void preSave(Model model) throws SQLException
		{
			if (field.is( NON_NULL ) && value == null)
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
