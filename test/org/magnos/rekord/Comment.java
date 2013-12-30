package org.magnos.rekord;

import org.magnos.rekord.field.Column;
import org.magnos.rekord.field.ForeignColumn;
import org.magnos.rekord.field.ManyToOne;


public class Comment extends Model
{
	private static final long serialVersionUID = 1L;

	public static final Factory<Comment> 		FACTORY			 = new Factory<Comment>() {
		public Comment create() {
			return new Comment();
		}
	};
	
	public static final Table<Comment> 			TABLE 			= Rekord.getTable( "comment", FACTORY );
	public static final Column<Long> 			ID 				= TABLE.getField( "id" );
	public static final Column<String> 			TEXT 			= TABLE.getField( "text" );
	public static final ForeignColumn<Long> 	COMMENTABLE_ID 	= TABLE.getField( "commentable_id" );
	public static final ForeignColumn<Long> 	USER_ID 		= TABLE.getField( "user_id" );
	public static final ManyToOne<User>			USER			= TABLE.getField( "user" );
	public static final ManyToOne<Commentable>	COMMENTABLE 	= TABLE.getField( "commentable" );
	
	public static class Views
	{
		public static final View 				ALL 			= TABLE.getView( "all" );
		public static final View				ID				= TABLE.getView( "id" );
	}

	public Comment()
	{
		super( TABLE );
	}
	
	public Long getId()							{ return get( ID ); }
	public void setId(Long x)					{ set( ID, x ); }
	public String getText()						{ return get( TEXT ); }
	public void setText(String x)				{ set( TEXT, x ); }
	public Long getCommentableId()				{ return get( COMMENTABLE_ID ); }
	public Commentable getCommentable()			{ return get( COMMENTABLE ); }
	public void setCommentable(Commentable x) 	{ set( COMMENTABLE, x ); }
	public Long getUserId()						{ return get( USER_ID ); }
	public User getUser()						{ return get( USER ); }
	public void setUser(User x)					{ set( USER, x ); }
	
}
