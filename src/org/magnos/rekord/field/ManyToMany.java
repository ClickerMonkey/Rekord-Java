package org.magnos.rekord.field;

import org.magnos.rekord.Model;
import org.magnos.rekord.Table;
import org.magnos.rekord.Value;
import org.magnos.rekord.query.InsertQuery;
import org.magnos.rekord.query.SelectQuery;
import org.magnos.rekord.query.UpdateQuery;

public class ManyToMany<T extends Model> extends AbstractField<T>
{

	protected boolean manageRelationship;
	protected Table relationshipTable;
	protected Table relatedTable;
	
	public ManyToMany( String name, int flags )
	{
		super( name, flags );
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
	public void prepareUpdate( UpdateQuery query )
	{

	}

	@Override
	public Value<T> newValue( Model model )
	{
		return null;
	}

}
