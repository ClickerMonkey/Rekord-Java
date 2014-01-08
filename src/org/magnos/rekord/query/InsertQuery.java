package org.magnos.rekord.query;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.magnos.rekord.Field;
import org.magnos.rekord.Model;
import org.magnos.rekord.Table;
import org.magnos.rekord.field.Column;

public class InsertQuery
{

	public static QueryTemplate<Model> forFields( Table table, Field<?> ... fields)
	{
	    Set<Field<?>> fieldSet = new HashSet<Field<?>>();

	    for (Field<?> f : fields)
	    {
	        fieldSet.add( f );
	    }
	    
	    List<Column<?>> returningList = new ArrayList<Column<?>>();
	    
	    for (Field<?> f : table.getFields())
	    {
	        if (f.is( Field.HAS_DEFAULT ) && !fieldSet.contains( f ) && (f instanceof Column))
	        {
	            returningList.add( (Column<?>)f );
	        }
	    }
		
		StringBuilder columnBuilder = new StringBuilder();
		StringBuilder valueBuilder = new StringBuilder();
		
		int columnsSet = 0;
		
		for (int i = 0; i < fields.length; i++)
		{
			Field<?> f = fields[i];
			
			if (f instanceof Column)
			{
				if (columnsSet++ > 0)
				{
				    columnBuilder.append( ", " );
				    valueBuilder.append( ", " );
				}
				
				Column<?> c = (Column<?>) f;
				
				columnBuilder.append( c.getQuotedName() );
				valueBuilder.append( "?" ).append( c.getName() );
			}
		}
		
		StringBuilder returningBuilder = new StringBuilder();
		
		for (int i = 0; i < returningList.size(); i++)
		{
		    Column<?> c = returningList.get( i );
		    
		    if (i > 0)
		    {
		        returningBuilder.append( ", " );
		    }
		    
		    returningBuilder.append( "#" ).append( c.getName() );
		}

        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append( "INSERT INTO " );
        queryBuilder.append( table.getQuotedName() );
		queryBuilder.append( " (" );
		queryBuilder.append( columnBuilder );
		queryBuilder.append( ") VALUES (" );
		queryBuilder.append( valueBuilder );
		queryBuilder.append( ")" );
		
		if (returningBuilder.length() > 0)
		{
		    queryBuilder.append( " RETURNING " );
		    queryBuilder.append( returningBuilder );
		}
        
		return NativeQuery.parse( table, queryBuilder.toString() );
	}
	
}
