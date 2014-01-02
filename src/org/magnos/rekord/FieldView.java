
package org.magnos.rekord;

public class FieldView
{
    public static final FieldView DEFAULT = new FieldView(null, -1);
    
	private View view;
	private int limit = -1;

	public FieldView(View view, int limit)
	{
	    this.view = view;
	    this.limit = limit;
	}
	
	public View getView()
	{
		return view;
	}
	
	public View getView(View defaultView)
	{
	    return (view != null ? view : defaultView);
	}

	public int getLimit()
	{
		return limit;
	}

}
