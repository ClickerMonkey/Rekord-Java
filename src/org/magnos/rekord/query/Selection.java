package org.magnos.rekord.query;

import java.util.ArrayList;
import java.util.List;

import org.magnos.rekord.Field;
import org.magnos.rekord.FieldView;
import org.magnos.rekord.View;

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
				
				String se = f.getSelectionExpression( FieldView.DEFAULT );
				
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
	
	public static Selection fromView(View view)
	{
		StringBuilder expressionBuilder = new StringBuilder();
		List<Field<?>> selectedList = new ArrayList<Field<?>>();
		
		for (Field<?> f : view.getFields())
		{
			if (f.isSelectable())
			{
				selectedList.add( f );
				
				String se = f.getSelectionExpression( view.getFieldView( f ) );
				
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
