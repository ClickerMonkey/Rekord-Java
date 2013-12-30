package org.magnos.rekord;

import java.sql.SQLException;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.magnos.rekord.query.SelectQuery;

public class LazyList<T extends Model> extends AbstractList<T>
{

	protected final Factory<SelectQuery<T>> queryFactory;
	protected final int fetchSize;
	protected int size = -1;
	protected BitSet pages;
	
	protected Set<T> removed;
	protected Set<T> added;
	protected Set<T> set;
	protected ArrayList<T> list;
	
	public LazyList(Factory<SelectQuery<T>> queryFactory, int fetchSize)
	{
		this.queryFactory = queryFactory;
		this.fetchSize = fetchSize;
		this.pages = new BitSet();
		this.list = new ArrayList<T>();
		this.removed = new HashSet<T>();
		this.added = new LinkedHashSet<T>();
		this.set = new HashSet<T>();
	}
	
	public int getPage(int i)
	{
		return i / fetchSize;
	}
	
	public void loadPageForIndex(int i)
	{
		loadPage( i / fetchSize );
	}
	
	public void ensureSize()
	{
		if (size == -1)
		{
			SelectQuery<T> query = queryFactory.create();
			
			if (query != null)
			{
				try
				{
					size = query.count();
					
					list.ensureCapacity( size );
					
					while (list.size() < size) 
					{
						list.add( null );
					}
					
					list.addAll( added );
					set.addAll( added );
					
					size = list.size();
				}
				catch (SQLException e)
				{
					throw new RuntimeException( "Error calculating list size", e );
				}
			}
			else
			{
				size = 0;
			}
		}
	}
	
	public void loadPage(int page)
	{
		if (pages.get( page ))
		{
			return;
		}
		
		SelectQuery<T> query = queryFactory.create();
		
		if (query != null)
		{
			final int offset = page * fetchSize;
			
			try
			{
				query.offset( (long)offset );
				query.limit( (long)fetchSize );
				ArrayList<T> pageModels = query.list( false );
				
				for (int i = 0; i < pageModels.size(); i++)
				{
					T model = pageModels.get( i );
					
					if (!removed.contains( model ))
					{
						list.set( i + offset, model );
						set.add( model );	
					}
				}	
				
				pages.set( page );
				
				query.postSelect( pageModels );
			}
			catch (SQLException e)
			{
				throw new RuntimeException( "Error fetching page " + page + " at offset " + offset + " with limit " + fetchSize, e );
			}
		}
	}
	
	public void loadAllPages()
	{
		ensureSize();
		
		int pageMax = size / fetchSize;
		
		for (int i = 0; i <= pageMax; i++)
		{
			loadPage( i );	
		}
	}
	
	@Override
	public int size()
	{
		ensureSize();
		
		return size;
	}
	
	public boolean hasSet()
	{
		return size != -1;
	}
	
	public boolean hasValue()
	{
		return size != -1 || added.size() > 1 || removed.size() > 1;
	}
	
	@Override
	public boolean isEmpty()
	{
		boolean empty = (size == 0);
		
		if (size == -1)
		{
			SelectQuery<T> query = queryFactory.create();
			
			if (query != null)
			{
				try
				{
					empty = !query.any();
				}
				catch (SQLException e)
				{
					throw new RuntimeException( "Error occurred determing emptiness of the list", e );
				}	
			}
		}
		
		return empty;
	}
	
	@Override
	public boolean contains(Object o)
	{
		boolean contained = set.contains( o );
		
		if (contained)
		{
			return true;
		}
		
		for (T element : this)
		{
			if (element == o || (element != null && o != null && element.equals(o)))
			{
				return true;
			}
		}
		
		return false;
	}

	@Override
	public boolean add( T e )
	{
		if (size == -1)
		{
			boolean success = added.add( e );
			
			if (success)
			{
				removed.remove( e );
			}
			
			return success;
		}
		
		boolean success = set.add( e );
		
		if (success)
		{
			list.add( e );
			added.add( e );
			removed.remove( e );
			size = list.size();
		}
		
		return success;
	}

	@Override
	public boolean remove( Object o )
	{
		if (size == -1)
		{
			boolean success = removed.add( (T)o );
			
			if (success)
			{
				added.remove( o );	
			}
			
			return success;
		}
		
		boolean success = set.remove( o );
		
		if (success)
		{
			list.remove( o );
			added.remove( o );
			removed.add( (T)o );
			size = list.size();
		}
		
		return success;
	}

	@Override
	public boolean addAll( Collection<? extends T> c )
	{
		if (size == -1)
		{
			boolean success = added.addAll( c );
			
			if (success)
			{
				removed.removeAll( c );
			}
			
			return success;
		}
		
		boolean success = set.addAll( c );
		
		if (success)
		{
			list.addAll( c );
			added.addAll( c );
			removed.removeAll( c );
			size = list.size();
		}
		
		return success;
	}

	@Override
	public boolean addAll( int index, Collection<? extends T> c )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void add( int index, T element )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeAll( Collection<?> c )
	{
		if (size == -1)
		{
			boolean success = removed.addAll( (Collection<T>)c );
			
			if (success)
			{
				added.removeAll( c );
			}
			
			return success;
		}
		
		boolean success = set.removeAll( c );
		
		if (success)
		{
			list.removeAll( c );
			added.removeAll( c );
			removed.addAll( (Collection<T>)c );
			size = list.size();
		}
		
		return success;
	}

	public void set(Collection<? extends T> c)
	{
		clear();
		addAll( c );
	}
	
	@Override
	public boolean retainAll( Collection<?> c )
	{
		loadAllPages();
		
		boolean changed = false;
		
		for (Object value : this)
		{
			if (!c.contains( value ))
			{
				removed.add( (T)value );
				added.remove( value );
				set.remove( value );
				list.remove( value );
			}
		}

		size = list.size();
		
		return changed;
	}

	@Override
	public void clear()
	{
		loadAllPages();
		
		removed.addAll( list );
		list.clear();
		set.clear();
		added.clear();
		size = 0;
	}

	@Override
	public T get( int index )
	{
		ensureSize();
		loadPageForIndex( index );
		
		return list.get( index );
	}

	@Override
	public T set( int index, T element )
	{
		ensureSize();
		
		if (index >= size)
		{
			throw new IndexOutOfBoundsException();
		}
		
		loadPageForIndex( index );
		
		T existing = list.get( index );

		if (existing != element && existing != null)
		{
			added.remove( existing );
			removed.add( existing );
			set.remove( existing );
			
			added.add( element );
			removed.remove( element );
			set.add( element );
			list.set( index, element );
		}
		
		return existing;
	}

	@Override
	public T remove( int index )
	{
		ensureSize();
		
		T existing = list.get( index );
		
		if (existing == null)
		{
			loadPageForIndex( index );
			existing = list.remove( index );
		}
		
		if (existing != null)
		{
			added.remove( existing );
			set.remove( existing );
			removed.add( existing );
			size = list.size();
		}
		
		return existing;
	}

	@Override
	public int indexOf( Object o )
	{
		if (set.contains( o ))
		{
			return list.indexOf( o );
		}
		
		ensureSize();
		
		int pageMax = size / fetchSize;
		
		for (int i = 0; i <= pageMax; i++)
		{
			loadPage( i );
			
			if (set.contains( o ))
			{
				return list.indexOf( o );
			}
		}
		
		return -1;
	}

	@Override
	public int lastIndexOf( Object o )
	{
		if (set.contains( o ))
		{
			return list.lastIndexOf( o );
		}
		
		ensureSize();
		
		int pageMax = size / fetchSize;
		
		for (int i = pageMax; i >= 0; i--)
		{
			loadPage( i );
			
			if (set.contains( o ))
			{
				return list.lastIndexOf( o );
			}
		}
		
		return -1;
	}

	public Set<T> getRemoved()
	{
		return removed;
	}

	public Set<T> getAdded()
	{
		return added;
	}

	public Set<T> getSet()
	{
		return set;
	}
	
}
