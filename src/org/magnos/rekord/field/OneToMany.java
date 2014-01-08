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
import org.magnos.rekord.FieldLoad;
import org.magnos.rekord.LoadProfile;
import org.magnos.rekord.Model;
import org.magnos.rekord.Table;
import org.magnos.rekord.Value;
import org.magnos.rekord.query.InsertAction;
import org.magnos.rekord.query.Query;
import org.magnos.rekord.query.QueryTemplate;
import org.magnos.rekord.query.SelectQuery;
import org.magnos.rekord.util.LazyList;

public class OneToMany<T extends Model> extends AbstractField<List<T>>
{

	protected final int fetchSize;
	protected final boolean cascadeDelete;
	protected final boolean cascadeSave;
	protected Table joinTable;
	protected ForeignColumn<?>[] joinColumns;
	protected LoadProfile joinLoad;
	
	public OneToMany( String name, int flags, int fetchSize, boolean cascadeDelete, boolean cascadeSave )
	{
		super( name, flags );
		
		this.fetchSize = fetchSize;
		this.cascadeDelete = cascadeDelete;
		this.cascadeSave = cascadeSave;
	}
	
	public void setJoin( Table joinTable, LoadProfile joinLoad, ForeignColumn<?> ... joinColumns )
	{
		this.joinTable = joinTable;
		this.joinLoad = joinLoad;
		this.joinColumns = joinColumns;
	}

	@Override
	public boolean isSelectable()
	{
		return false;
	}
	
	@Override
	public String getSelectExpression(FieldLoad fieldLoad)
	{
		return null;
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
	public Value<List<T>> newValue(Model model)
	{
		return new OneToManyValue<T>( this, model );
	}
	
	public Table getJoinTable()
	{
		return joinTable;
	}
	
	public LoadProfile getJoinLoad()
	{
		return joinLoad;
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
    
    public boolean isCascadeSave()
    {
        return cascadeSave;
    }
    
    @Override
    public String toString()
    {
        StringBuilder sb = beginToString();
        sb.append( ", fetch-size=" ).append( fetchSize );
        sb.append( ", cascade-delete=" ).append( cascadeDelete );
        sb.append( ", join=" ).append( joinTable.getName() );
        sb.append( "[" ).append( joinLoad.getName() ).append( "]" );
        sb.append( ", join-key={" );
        for (int i = 0; i < joinColumns.length; i++) {
            if (i > 0) sb.append( ", " );
            ForeignColumn<?> fc = joinColumns[i];
            sb.append( fc.getName() ).append( "->" ).append( joinTable.getName() ).append( "." ).append( fc.getForeignColumn().getName() );
        }
        sb.append( "}" );
        return endToString( sb );
    }

    private static class OneToManyValue<T extends Model> implements Value<List<T>>, Factory<Query<T>>
	{
		private final OneToMany<T> field;
		private final Model model;
		private final LazyList<T> value;
		private boolean changed = false;
		private SelectQuery<T> select;
		private QueryTemplate<T> queryTemplate;
		
		public OneToManyValue(OneToMany<T> field, Model model)
		{
			this.field = field;
			this.model = model;
			
			this.select = new SelectQuery<T>( field.getJoinTable() );
			this.select.whereForeignKeyBind( field.getJoinColumns() );
			
			this.updateQuery( null );
			
			this.value = new LazyList<T>( this, field.getFetchSize() );
		}

		@Override
		public Query<T> create()
		{
		    if (!model.hasKey())
		    {
		        return null;
		    }
		    
		    Query<T> query = queryTemplate.create();
		    
		    query.bind( model );
			
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
        public String getName()
        {
            return field.getName();
        }

        @Override
        public String getQuotedName()
        {
            return field.getQuotedName();
        }

        @Override
        public boolean isSelectable()
        {
            return false;
        }

        @Override
        public String getSelectExpression( FieldLoad fieldLoad )
        {
            return null;
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
		    if (field.isCascadeSave())
		    {
		        Set<T> removed = value.getRemoved();
	            Set<T> added = value.getAdded();
	            
	            for (T child : removed)
	            {
	                child.delete();
	            }

	            if (value.hasSet())
	            {
	                for (T child : value.getSet())
	                {
	                    setModelForeignKey( m, child );
	                    child.save();
	                }
	            }
	            else
	            {
	                for (T child : added)
	                {
	                    setModelForeignKey( m, child );
	                    child.save();
	                }
	            }
	            
	            removed.clear();
	            added.clear();
		    }
		}

        @Override
        public void preDelete(Model model) throws SQLException
        {
            if (field.isCascadeDelete())
            {
                select.clear();
                select.select( field.getJoinTable().getLoadProfileId() );
                
                queryTemplate = select.newTemplate();
                
                value.clear();
                
                Set<T> removed = value.getRemoved();
                
                for (T child : removed)
                {
                	child.delete();
                }
                
                removed.clear();
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
		
		@Override
		public String toString()
		{
			return field.getName() + "=" + (value.hasValue() ? value : "'not loaded'");
		}
		
        private void setModelForeignKey( Model one, Model many )
        {
            ForeignColumn<?>[] columns = field.getJoinColumns();
            
            for (int i = 0; i < columns.length; i++)
            {
                many.set( (Field<Object>)columns[i], one.get( columns[i].getForeignColumn() ) );
            }
        }

        private void updateQuery(LoadProfile parentLoad)
        {
            LoadProfile fieldLoad = null;
            
            if (parentLoad != null)
            {
                fieldLoad = parentLoad.getFieldLoad( field, select.getLoadProfile() );    
            }
            
            if (fieldLoad == null)
            {
                fieldLoad = field.getJoinLoad();
            }
            
            select.clear();
            select.select( fieldLoad );
            
            queryTemplate = select.newTemplate();
        }
	}

}
