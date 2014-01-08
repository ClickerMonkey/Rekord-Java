package org.magnos.rekord.query;

import org.magnos.rekord.Field;
import org.magnos.rekord.Model;
import org.magnos.rekord.Table;
import org.magnos.rekord.field.Column;
import org.magnos.rekord.query.condition.Condition;
import org.magnos.rekord.query.expr.GroupExpression;

public class UpdateQuery
{

	public static QueryTemplate<Model> forFields( Table table, Field<?> ... fields)
	{
		Condition where = new GroupExpression().whereKeyBind( table );
		
		StringBuilder queryBuilder = new StringBuilder();
		queryBuilder.append( "UPDATE " );
		queryBuilder.append( table.getQuotedName() );
		queryBuilder.append( " SET " );
		
		int columnsSet = 0;
		
		for (int i = 0; i < fields.length; i++)
		{
			Field<?> f = fields[i];
			
			if (f instanceof Column)
			{
				if (columnsSet++ > 0)
				{
					queryBuilder.append( ", " );
				}
				
				Column<?> c = (Column<?>) f;
				
				queryBuilder.append( c.getQuotedName() );
				queryBuilder.append( " = ?" );
				queryBuilder.append( c.getName() );
			}
		}
		
		queryBuilder.append( " WHERE " );
		where.toQuery( queryBuilder );
		
		return NativeQuery.parse( table, queryBuilder.toString() );
	}
	
}
