package org.magnos.rekord;

import org.magnos.rekord.field.Column;

public class RolePlayer extends Model
{
	private static final long serialVersionUID = 1L;
	
	public static final Factory<RolePlayer> FACTORY			 = new Factory<RolePlayer>() {
		public RolePlayer create() {
			return new RolePlayer();
		}
	};

	public static final Table           	TABLE 				= Rekord.getTable( "role_player", FACTORY );
	public static final Column<Long> 		ID					= TABLE.getField( "id" );
	public static final Column<String> 		NAME				= TABLE.getField( "name" );

	public RolePlayer()
	{
		super( TABLE );
	}
	
	public Long getId()						{ return get( ID ); }
	public void setId(Long x)				{ set( ID, x ); }
	public String getName()					{ return get( NAME ); }
	public void setName(String x)			{ set( NAME, x ); }
	
}
