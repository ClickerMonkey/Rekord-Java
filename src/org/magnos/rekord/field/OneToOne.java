package org.magnos.rekord.field;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.magnos.rekord.Field;
import org.magnos.rekord.FieldLoad;
import org.magnos.rekord.Key;
import org.magnos.rekord.LoadProfile;
import org.magnos.rekord.Model;
import org.magnos.rekord.Rekord;
import org.magnos.rekord.Transaction;
import org.magnos.rekord.Value;
import org.magnos.rekord.query.InsertAction;
import org.magnos.rekord.query.SelectQuery;

public class OneToOne<T extends Model> extends JoinField<T>
{

	
	public OneToOne( String name, int flags )
	{
		super( name, flags );
	}
	
	@Override
	public boolean isSelectable()
	{
		return !is(LAZY);
	}
    
	@Override
	public Value<T> newValue(Model model)
	{
		return new OneToOneValue<T>( this, model );
	}
	
    @Override
    public String toString()
    {
        StringBuilder sb = beginToString();
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
			ForeignField<Object>[] foreign = (ForeignField<Object>[])field.getJoinColumns();
			
			for (int i = 0; i < foreign.length; i++)
			{
				model.set( foreign[i], value.get( foreign[i].getForeignColumn() ) );	
			}
		}
		
		private void clearKey()
		{
			for (ForeignField<?> c : field.getJoinColumns())
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
					loadFromKey( FieldLoad.DEFAULT );	
				}
				catch (SQLException e)
				{
				    throw new RuntimeException( e );
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
        public InsertAction getInsertAction()
        {
            return InsertAction.NONE;
        }

        @Override
        public boolean isUpdatable()
        {
            return false;
        }
        
        @Override
        public String getSaveExpression()
        {
            return null;
        }

		@Override
		public void load( FieldLoad fieldLoad ) throws SQLException
		{
			
		}

		@Override
		public void fromSelect( ResultSet results, FieldLoad fieldLoad ) throws SQLException
		{
			
		}
		
		@Override
		public void postSelect(Model model, FieldLoad fieldLoad) throws SQLException
		{
			if (!field.is(LAZY) && getKey().exists())
			{
				loadFromKey( fieldLoad );
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
			if (value != null && value.hasKey() && value.hasChanged())
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
        	if (value != null && value.hasKey())
            {
                value.delete();
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
        
        private void loadFromKey( FieldLoad fieldLoad ) throws SQLException
        {
            Key key = getKey();
            Transaction trans = Rekord.getTransaction();
            value = trans.getCached( field.getJoinTable(), key );
            
            LoadProfile load = fieldLoad.getLoadProfile( field.getJoinLoad() );
            
            if (value == null)
            {
                SelectQuery<T> select = new SelectQuery<T>( field.getJoinTable() );
                select.select( load );
                select.whereForeignKey( key );

                value = select.create().first();
            }
            else
            {
                value.load( load, false );
            }
        }
		
	}

}
