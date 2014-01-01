
package org.magnos.rekord;



public class View
{

	private final String name;
	private final Field<?>[] fields;
	private final FieldView[] fieldViews;

	public View( String name, Field<?>[] fields, FieldView[] fieldViews )
	{
		this.name = name;
		this.fields = fields;
		this.fieldViews = fieldViews;
	}

	public View getFieldView( Field<?> f, View defaultView )
	{
		FieldView fc = fieldViews[ f.getIndex() ];
		
		return (fc == null || fc.getView() == null ? defaultView : fc.getView());
	}

	public String getName()
	{
		return name;
	}

	public Field<?>[] getFields()
	{
		return fields;
	}

	public FieldView[] getFieldViews()
	{
		return fieldViews;
	}
	
	@Override
	public String toString()
	{
	    StringBuilder sb = new StringBuilder();
	    sb.append( name );
	    sb.append( ": [" );

	    for (int i = 0; i < fields.length; i++) {
	        if (i > 0) sb.append( ", " );
	        sb.append( fields[i].getName() );
	        if (fieldViews[i] != null) {
	        	if (fieldViews[i].getLimit() != -1) {
	        		sb.append( "(" ).append( fieldViews[i].getLimit() ).append( ")" );
	        	}
	        	if (fieldViews[i].getView() != null) {
	        		sb.append( "[" ).append( fieldViews[i].getView().getName() ).append( "]" );	
	        	}
	        }
	    }
	    
	    sb.append( "]" );
	    
	    return sb.toString();
	}

}
