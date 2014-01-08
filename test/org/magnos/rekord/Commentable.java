package org.magnos.rekord;

import java.util.Date;
import java.util.List;

import org.magnos.rekord.field.Column;
import org.magnos.rekord.field.OneToMany;

public class Commentable extends Model
{
	private static final long serialVersionUID = 1L;
	
	public static final Factory<Commentable> FACTORY			 = new Factory<Commentable>() {
		public Commentable create() {
			return new Commentable();
		}
	};

	public static final Table           	TABLE 				= Rekord.getTable( "commentable", FACTORY );
	public static final Column<Long> 		ID 					= TABLE.getField( "id" );
	public static final Column<Integer> 	COUNT 				= TABLE.getField( "count" );
	public static final Column<Date> 		CREATED_TIMESTAMP 	= TABLE.getField( "created_timestamp" );
	public static final OneToMany<Comment> 	COMMENTS 			= TABLE.getField( "comments" );
	
	public static class Views
	{
		public static final LoadProfile 			ALL 				= TABLE.getLoadProfile( "all" );
		public static final LoadProfile			ID					= TABLE.getLoadProfile( "id" );
	}
	
	public Commentable()
	{
		super( TABLE );
	}
	
	public Long getId()						{ return get( ID ); }
	public void setId(Long x)				{ set( ID, x ); }
	public Integer getCount()				{ return get( COUNT ); }
	public void setCount(Integer x)			{ set( COUNT, x ); }
	public Date getCreatedTimestamp()		{ return get( CREATED_TIMESTAMP ); }
	public void setCreatedTimestamp(Date x)	{ set( CREATED_TIMESTAMP, x ); }
	public List<Comment> getComments()		{ return get( COMMENTS ); }
	public void setComments(List<Comment> x){ set( COMMENTS, x ); }
	
}
