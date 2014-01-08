package org.magnos.rekord.field;

import org.magnos.rekord.FieldLoad;
import org.magnos.rekord.Model;
import org.magnos.rekord.Table;
import org.magnos.rekord.Value;
import org.magnos.rekord.query.model.ModelInsertQuery;
import org.magnos.rekord.query.model.ModelUpdateQuery;

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
	public boolean isSelectable()
	{
		return false;
	}
	
	@Override
	public String getSelectionExpression(FieldLoad fieldLoad)
	{
		return null;
	}
	
	@Override
	public void prepareInsert( ModelInsertQuery query )
	{

	}

	@Override
	public void prepareUpdate( ModelUpdateQuery query )
	{

	}

	@Override
	public Value<T> newValue( Model model )
	{
		return null;
	}

}
