package org.magnos.rekord.query;

import java.util.ArrayList;
import java.util.List;

import org.magnos.rekord.Field;
import org.magnos.rekord.FieldLoad;
import org.magnos.rekord.LoadProfile;
import org.magnos.rekord.query.expr.ColumnResolver;

public class Selection
{
	protected final String expression;
	protected final Field<?>[] fields;
	
	protected Selection(String expression, Field<?>[] fields)
	{
		this.expression = expression;
		this.fields = fields;
	}

	public String getExpression()
	{
		return expression;
	}

	public Field<?>[] getFields()
	{
		return fields;
	}
	
	public static Selection fromFields(Field<?> ... fields)
	{
		StringBuilder expressionBuilder = new StringBuilder();
		List<Field<?>> selectedList = new ArrayList<Field<?>>();
		
		for (Field<?> f : fields)
		{
			if (f.isSelectable())
			{
				selectedList.add( f );
				
				String se = f.getSelectExpression( ColumnResolver.DEFAULT, FieldLoad.DEFAULT );
				
				if (se != null)
				{
					if (expressionBuilder.length() > 0)
					{
						expressionBuilder.append( ", " );
					}
					
					expressionBuilder.append( se );
				}
			}
		}
		
		String expression = expressionBuilder.toString();
		Field<?>[] selected = selectedList.toArray( new Field[ selectedList.size() ] );
		
		return new Selection( expression, selected );
	}
	
	public static Selection fromLoadProfile(LoadProfile load)
	{
		StringBuilder expressionBuilder = new StringBuilder();
		List<Field<?>> selectedList = new ArrayList<Field<?>>();
		
		for (Field<?> f : load.getFields())
		{
			if (f.isSelectable())
			{
				selectedList.add( f );
				
				String se = f.getSelectExpression( ColumnResolver.DEFAULT, load.getFieldLoad( f ) );
				
				if (se != null)
				{
					if (expressionBuilder.length() > 0)
					{
						expressionBuilder.append( ", " );
					}
					
					expressionBuilder.append( se );
				}
			}
		}
		
		String expression = expressionBuilder.toString();
		Field<?>[] selected = selectedList.toArray( new Field[ selectedList.size() ] );
		
		return new Selection( expression, selected );
	}
	
}
