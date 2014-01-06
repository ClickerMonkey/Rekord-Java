
package org.magnos.rekord;

public class FieldView
{
	public static final FieldView DEFAULT = new FieldView();

	private View view;
	private int limit;

	public FieldView()
	{
		this( null, -1 );
	}

	public FieldView( View view, int limit )
	{
		this.view = view;
		this.limit = limit;
	}

	public View getView()
	{
		return view;
	}

	public void setView( View view )
	{
		this.view = view;
	}

	public View getView( View defaultView )
	{
		return (view != null ? view : defaultView);
	}

	public int getLimit()
	{
		return limit;
	}

	public void setLimit( int limit )
	{
		this.limit = limit;
	}

}
