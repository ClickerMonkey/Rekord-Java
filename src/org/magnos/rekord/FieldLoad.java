
package org.magnos.rekord;

public class FieldLoad
{
	public static final FieldLoad DEFAULT = new FieldLoad();

	private LoadProfile loadProfile;
	private int limit;

	public FieldLoad()
	{
		this( null, -1 );
	}

	public FieldLoad( LoadProfile loadProfile, int limit )
	{
		this.loadProfile = loadProfile;
		this.limit = limit;
	}

	public LoadProfile getLoadProfile()
	{
		return loadProfile;
	}

	public void setLoadProfile( LoadProfile loadProfile )
	{
		this.loadProfile = loadProfile;
	}

	public LoadProfile getLoadProfile( LoadProfile defaultLoadProfile )
	{
		return (loadProfile != null ? loadProfile : defaultLoadProfile);
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
