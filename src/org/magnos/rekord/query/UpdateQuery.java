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
		Condition where = GroupExpression.detached().whereKeyBind( table );
		
		QueryBuilder qb = new QueryBuilder();
		qb.append( "UPDATE ", table.getQuotedName(), " SET " );
		
		int columnsSet = 0;
		
		for (int i = 0; i < fields.length; i++)
		{
			Field<?> f = fields[i];
			
			if (f instanceof Column)
			{
				if (columnsSet++ > 0)
				{
					qb.append( ", " );
				}
				
				Column<?> c = (Column<?>) f;
				
				qb.append( c.getQuotedName() );
				qb.appendValuable( " = ?" + c.getName() );
			}
		}
		
		qb.append( " WHERE " );
		where.toQuery( qb );
		
		String query = qb.getQueryString();
		QueryBind[] binds = qb.getBindsArray();
		Field<?>[] select = new Field[ 0 ];
		
		return new QueryTemplate<Model>( table, query, null, binds, select );
	}
	
}
