package org.magnos.rekord;

import org.magnos.rekord.field.Column;
import org.magnos.rekord.field.InheritColumn;

public class Group extends RolePlayer
{
	private static final long serialVersionUID = 1L;

	public static final Factory<Group> FACTORY					= new Factory<Group>() {
		public Group create() {
			return new Group();
		}
	};
	
	public static final Table           	TABLE 				= Rekord.getTable( "group", FACTORY );
	public static final InheritColumn<Long>	ROLE_PLAYER_ID		= TABLE.getField( "role_player_id" );
	public static final Column<String> 		PASSWORD			= TABLE.getField( "password" );
	public static final Column<String> 		SALT				= TABLE.getField( "salt" );

	public Group()
	{
		super( TABLE );
	}
	
	public Group( Table table )
	{
		super( table );
	}
	
	public String getPassword() 				{ return get( PASSWORD ); }
	public void setPassword(String x)			{ set( PASSWORD, x ); }
	public String getSalt() 					{ return get( SALT ); }
	public void setSalt(String x)				{ set( SALT, x ); }
	
}
