package org.magnos.rekord.field;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.magnos.rekord.Field;
import org.magnos.rekord.Key;
import org.magnos.rekord.Logging;
import org.magnos.rekord.Model;
import org.magnos.rekord.Rekord;
import org.magnos.rekord.Table;
import org.magnos.rekord.Transaction;
import org.magnos.rekord.Value;
import org.magnos.rekord.View;
import org.magnos.rekord.query.InsertQuery;
import org.magnos.rekord.query.SelectQuery;
import org.magnos.rekord.query.UpdateQuery;

public class OneToOne<T extends Model> extends AbstractField<T>
{

	protected Table joinTable;
	protected ForeignColumn<?>[] joinColumns;
	protected View joinView;
	
	public OneToOne( String name, int flags )
	{
		super( name, flags );
	}
	
	public void setJoin( Table joinTable, View joinView, ForeignColumn<?>[] joinColumns )
	{
		this.joinTable = joinTable;
		this.joinView = joinView;
		this.joinColumns = joinColumns;
	}

	@Override
	public void prepareSelect( SelectQuery<?> query )
	{
		if (!is(LAZY))
		{
			query.addPostSelectField( this );
		}
	}

	@Override
	public void prepareInsert( InsertQuery query )
	{
	}
	
	@Override
	public Value<T> newValue(Model model)
	{
		return new OneToOneValue<T>( this, model );
	}
	
	public Table getJoinTable()
	{
		return joinTable;
	}
	
	public View getJoinView()
	{
		return joinView;
	}

	public ForeignColumn<?>[] getJoinColumns()
	{
		return joinColumns;
	}
    
    @Override
    public String toString()
    {
        StringBuilder sb = beginToString();
        sb.append( ", join=" ).append( joinTable.getName() );
        sb.append( "[" ).append( joinView.getName() ).append( "]" );
        sb.append( ", join-key={" );
        for (int i = 0; i < joinColumns.length; i++) {
            if (i > 0) sb.append( ", " );
            ForeignColumn<?> fc = joinColumns[i];
            sb.append( fc.getName() ).append( "->" ).append( joinTable.getName() ).append( "." ).append( fc.getForeignColumn().getName() );
        }
        sb.append( "}" );
        return endToString( sb );
    }
	
	private static class OneToOneValue<T extends Model> implements Value<T>
	{
		private final OneToOne<T> field;
		private T value;
		private Key key;
		private Model model;
		private boolean changed = false;
		
		public OneToOneValue(OneToOne<T> field, Model model)
		{
			this.field = field;
			this.model = model;
		}
		
		private Key getKey()
		{
			if (key == null)
			{
				key = model.getTable().keyForFields( model, field.getJoinColumns() );
			}
			
			return key;
		}
		
		private void copyBackKey( T value )
		{
			ForeignColumn<Object>[] foreign = (ForeignColumn<Object>[])field.getJoinColumns();
			
			for (int i = 0; i < foreign.length; i++)
			{
				model.set( foreign[i], value.get( foreign[i].getForeignColumn() ) );	
			}
		}
		
		private void clearKey()
		{
			for (ForeignColumn<?> c : field.getJoinColumns())
			{
				model.set( c, null );
			}
		}
		
		@Override
		public T get( Model model )
		{
			if (field.is( LAZY ) && value == null && getKey().exists())
			{
				try
				{
					loadFromKey( null );	
				}
				catch (SQLException e)
				{
					e.printStackTrace();
				}
			}
			else if (field.is( NON_NULL ) && value == null)
			{
                value = field.getJoinTable().newModel();
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
			this.changed = true;
			
			if (value != null)
			{
				copyBackKey( value );
			}
			else
			{
				clearKey();
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
			
		}
		
		@Override
		public void postSelect(Model model, SelectQuery<?> query) throws SQLException
		{
			if (!field.is(LAZY) && getKey().exists())
			{
				loadFromKey( query.getView() );
			}
		}
		
		@Override
		public void fromResultSet( ResultSet results ) throws SQLException
		{
			
		}
		
		@Override
		public int toPreparedStatement( PreparedStatement preparedStatement, int paramIndex ) throws SQLException
		{
			return paramIndex;
		}
		
		@Override
		public void preSave(Model model) throws SQLException
		{
			if (!getKey().exists() && field.is( GENERATED ))
			{
				if (value == null)
				{
					value = field.getJoinTable().newModel();
				}
				
				value.save();
				copyBackKey( value );
			}
		}
		
		@Override
		public void postSave(Model model) throws SQLException
		{
			if (value != null && value.hasKey())
			{
				value.update();
			}
		}

        @Override
        public void preDelete(Model model) throws SQLException
        {
            
        }

        @Override
        public void postDelete(Model model) throws SQLException
        {
            
        }
		
		private void loadFromKey( View parentView ) throws SQLException
		{
			Key key = getKey();
			Transaction trans = Rekord.getTransaction();
			value = trans.getCached( field.getJoinTable(), key );
			
			View fieldView = field.getJoinView();
			
			if (parentView != null)
			{
				fieldView = parentView.getFieldView( field, fieldView );
			}
			
			if (value == null)
			{
				value = new SelectQuery<T>( field.getJoinTable() ).select( fieldView ).byForeignKey( key ).first();	
			}
			else
			{
				value.load( fieldView );
				
				Rekord.log( Logging.CACHING, "one-to-one from-cache: %s", value );
			}
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
			
			if (value != null)
			{
				copyBackKey( value );	
			}
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
