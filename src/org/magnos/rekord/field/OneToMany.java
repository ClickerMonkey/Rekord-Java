package org.magnos.rekord.field;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import org.magnos.rekord.Factory;
import org.magnos.rekord.Field;
import org.magnos.rekord.LazyList;
import org.magnos.rekord.Model;
import org.magnos.rekord.Operator;
import org.magnos.rekord.Table;
import org.magnos.rekord.Value;
import org.magnos.rekord.View;
import org.magnos.rekord.condition.AndCondition;
import org.magnos.rekord.condition.OperatorCondition;
import org.magnos.rekord.query.InsertQuery;
import org.magnos.rekord.query.SelectQuery;
import org.magnos.rekord.query.UpdateQuery;

public class OneToMany<T extends Model> extends AbstractField<List<T>>
{

	protected final int fetchSize;
	protected final boolean cascadeDelete;
	protected Table joinTable;
	protected ForeignColumn<?>[] joinColumns;
	protected View joinView;
	
	public OneToMany( String name, int flags, int fetchSize, boolean cascadeDelete )
	{
		super( name, flags );
		
		this.fetchSize = fetchSize;
		this.cascadeDelete = cascadeDelete;
	}
	
	public void setJoin( Table joinTable, View joinView, ForeignColumn<?> ... joinColumns )
	{
		this.joinTable = joinTable;
		this.joinView = joinView;
		this.joinColumns = joinColumns;
	}
	
	@Override
	public void prepareSelect( SelectQuery<?> query )
	{

	}

	@Override
	public void prepareInsert( InsertQuery query )
	{
		
	}
	
	@Override
	public Value<List<T>> newValue(Model model)
	{
		return new OneToManyValue<T>( this, model );
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
	
	public int getFetchSize()
	{
		return fetchSize;
	}
	
    public boolean isCascadeDelete()
    {
        return cascadeDelete;
    }

    private static class OneToManyValue<T extends Model> implements Value<List<T>>, Factory<SelectQuery<T>>
	{
		private final OneToMany<T> field;
		private final Model model;
		private final LazyList<T> value;
		private boolean changed = false;
		private SelectQuery<T> query;
		private OperatorCondition[] keyConditions;
		private AndCondition whereCondition;
		
		public OneToManyValue(OneToMany<T> field, Model model)
		{
			this.field = field;
			this.model = model;
			this.query = new SelectQuery<T>( field.getJoinTable() );
			this.buildWhere();
			this.updateSelect( null );
			this.value = new LazyList<T>( this, field.getFetchSize() );
		}

		@Override
		public SelectQuery<T> create()
		{
			updateSelectCondition();
			
			return query;
		}
		
		@Override
		public List<T> get( Model model )
		{
			return value;
		}

		@Override
		public boolean hasValue()
		{
			return value.hasValue();
		}

		@Override
		public void set( Model model, List<T> value )
		{
			this.value.set( value );
			this.changed = true;
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
			
		}

		@Override
		public void postSave(Model m) throws SQLException
		{
			for (T model : value.getRemoved())
			{
				model.delete();
			}
			
			if (value.hasSet())
			{
				for (T model : value.getSet())
				{
					setModelForeignKey( m, model );
					model.save();
				}
			}
			else
			{
				for (T model : value.getAdded())
				{
					setModelForeignKey( m, model );
					model.save();
				}
			}
		}

        @Override
        public void preDelete(Model model) throws SQLException
        {
            if (field.isCascadeDelete() && value != null)
            {
                value.clear();
            }
        }

        @Override
        public void postDelete(Model model) throws SQLException
        {
            
        }

		@Override
		public void serialize(ObjectOutputStream out) throws IOException
		{
			out.writeObject( value.getSet() );
		}

		@Override
		public void deserialize(ObjectInputStream in) throws IOException, ClassNotFoundException
		{
			value.set( (Set<T>)in.readObject() );
		}
		
		@Override
		public Field<List<T>> getField()
		{
			return field;
		}
		
		private void setModelForeignKey( Model one, Model many )
		{
			ForeignColumn<?>[] columns = field.getJoinColumns();
			
			for (int i = 0; i < columns.length; i++)
			{
				many.set( (Field<Object>)columns[i], one.get( columns[i].getForeignColumn() ) );
			}
		}

		private void buildWhere()
		{
			final ForeignColumn<?>[] columns = field.getJoinColumns();
			
			keyConditions = new OperatorCondition[ columns.length ];
			
			for (int i = 0; i < columns.length; i++)
			{
				keyConditions[i] = new OperatorCondition( columns[i], Operator.EQ, null );
			}
			
			whereCondition = new AndCondition( keyConditions );
		}
		
		private void updateSelect( View parentView )
		{
			View fieldView = query.getView();
			
			if (fieldView == null)
			{
				fieldView = field.getJoinView();
			}
			if (parentView != null)
			{
				fieldView = parentView.getFieldView( field, fieldView );
			}
			
			query.clear();
			query.select( fieldView );
			query.where( whereCondition );
		}
		
		private void updateSelectCondition()
		{
			final ForeignColumn<?>[] columns = field.getJoinColumns();
			
			for (int i = 0; i < columns.length; i++)
			{
				keyConditions[i].value = model.get( columns[i].getForeignColumn() );
			}
		}
		
		@Override
		public String toString()
		{
			return field.getName() + "=" + (value.hasValue() ? value : "'not loaded'");
		}
	}

}
