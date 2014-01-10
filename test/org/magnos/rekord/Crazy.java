package org.magnos.rekord;

import java.sql.Timestamp;

import org.magnos.rekord.field.Column;

public class Crazy extends Model
{
	private static final long serialVersionUID = 1L;
	
	public static final Factory<Crazy> FACTORY			 = new Factory<Crazy>() {
		public Crazy create() {
			return new Crazy();
		}
	};

	public static final Table           	TABLE 				= Rekord.getTable( "crazy", FACTORY );
	public static final Column<Long> 		ID0					= TABLE.getField( "id_0" );
	public static final Column<Long>        ID1                 = TABLE.getField( "id_1" );
	public static final Column<Float> 	    DIAMETER 			= TABLE.getField( "diameter" );
	public static final Column<Integer>     UPDATES             = TABLE.getField( "updates" );
	public static final Column<Timestamp> 	LAST_MODIFIED_TMS 	= TABLE.getField( "last_modified_tms" );
	
	public static class Load
	{
	    public static final LoadProfile All = TABLE.getLoadProfile( "all" );
	}
	
	public Crazy()
	{
		super( TABLE );
	}

    public Long getId0()                                { return get( ID0 ); }
    public void setId0(Long x)                          { set( ID0, x ); }
    public Long getId1()                                { return get( ID1 ); }
    public void setId1(Long x)                          { set( ID1, x ); }
    public Float getDiameter()                          { return get( DIAMETER ); }
    public void setDiameter(Float x)                    { set( DIAMETER, x ); }
	public Integer getUpdates()				            { return get( UPDATES ); }
	public Timestamp getLastModifiedTimestamp()		    { return get( LAST_MODIFIED_TMS ); }
	public void setLastModifiedTimestamp(Timestamp x)	{ set( LAST_MODIFIED_TMS, x ); }
	
}
