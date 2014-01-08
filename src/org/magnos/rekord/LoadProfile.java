
package org.magnos.rekord;

import org.magnos.rekord.query.Selection;



public class LoadProfile
{

	private final String name;
	private final Field<?>[] fields;
	private final FieldLoad[] fieldLoads;
	private final Selection selection;

	public LoadProfile( String name, Field<?>[] fields, FieldLoad[] fieldLoads )
	{
		this.name = name;
		this.fields = fields;
		this.fieldLoads = fieldLoads;
		this.selection = Selection.fromLoadProfile( this );
	}

	public FieldLoad getFieldLoad( Field<?> f )
	{
		return fieldLoads[ f.getIndex() ];
	}
	
	public LoadProfile getFieldLoad( Field<?> f, LoadProfile defaultLoad )
	{
		FieldLoad fc = fieldLoads[ f.getIndex() ];
		
		return (fc == null || fc.getLoadProfile() == null ? defaultLoad : fc.getLoadProfile());
	}

	public String getName()
	{
		return name;
	}

	public Field<?>[] getFields()
	{
		return fields;
	}

	public FieldLoad[] getFieldLoads()
	{
		return fieldLoads;
	}
	
	public Selection getSelection()
	{
		return selection;
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
	        if (fieldLoads[i] != null) {
	        	if (fieldLoads[i].getLimit() != -1) {
	        		sb.append( "(" ).append( fieldLoads[i].getLimit() ).append( ")" );
	        	}
	        	if (fieldLoads[i].getLoadProfile() != null) {
	        		sb.append( "[" ).append( fieldLoads[i].getLoadProfile().getName() ).append( "]" );	
	        	}
	        }
	    }
	    
	    sb.append( "]" );
	    
	    return sb.toString();
	}
	
	public static LoadProfile coalesce(LoadProfile parentLoad, LoadProfile childLoad, LoadProfile defaultChildLoad, Field<?> field)
	{
		if (childLoad == null)
		{
			childLoad = defaultChildLoad;
		}
		
		if (parentLoad != null)
		{
			childLoad = parentLoad.getFieldLoad( field, childLoad );
		}
		
		return childLoad;
	}

}
