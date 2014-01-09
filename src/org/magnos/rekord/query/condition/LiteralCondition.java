package org.magnos.rekord.query.condition;

import org.magnos.rekord.Field;
import org.magnos.rekord.query.QueryBuilder;

public class LiteralCondition implements Condition
{

	public String literal;
	
	public LiteralCondition(String literal)
	{
		this.literal = literal;
	}
	
	@Override
	public void toQuery( QueryBuilder query )
	{
		query.appendValuable( literal );
	}

	public static LiteralCondition forNull(Field<?> field)
	{
		return new LiteralCondition( field.getQuotedName() + " IS NULL" );
	}
	
	public static LiteralCondition forNotNull(Field<?> field)
	{
		return new LiteralCondition( field.getQuotedName() + " IS NOT NULL" );
	}

}
