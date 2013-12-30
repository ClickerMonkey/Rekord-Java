package org.magnos.rekord;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import org.magnos.rekord.query.SelectQuery;

public class User extends Model
{
	private static final long serialVersionUID = 1L;
	
	public static final Factory<User> 			FACTORY	 			= new Factory<User>() {
		public User create() {
			return new User();
		}
	};

	public static final Table<User> 			TABLE 				= Rekord.getTable( "user", FACTORY ); 
	public static final Column<Long> 			ID 					= TABLE.getField( "id" );
	public static final Column<String> 			NAME 				= TABLE.getField( "name" );
	public static final Column<Date> 			CREATED_TIMESTAMP 	= TABLE.getField( "created_timestamp" );
	public static final ForeignColumn<Long> 	COMMENTABLE_ID 		= TABLE.getField( "commentable_id" );
	public static final OneToOne<Commentable> 	COMMENTABLE 		= TABLE.getField( "commentable" );
	public static final OneToMany<Comment> 		COMMENTS_BY 		= TABLE.getField( "comments_by" );
	
	public static class Views
	{
		public static final View 				ALL 				= TABLE.getView( "all" );
		public static final View				ID					= TABLE.getView( "id" );
		public static final View 				WITHOUT_COMMENTS 	= TABLE.getView( "without-comments" );
		public static final View 				FOR_LINK		 	= TABLE.getView( "for-link" );
	}
	
	public User()
	{
		super( TABLE );
	}
	
	public Long getId() 						{ return get( ID ); }
	public void setId(Long id)					{ set( ID, id ); }
	public String getName()						{ return get( NAME ); }
	public void setName(String name)			{ set( NAME, name ); }
	public Date getCreatedTimestamp()			{ return get( CREATED_TIMESTAMP ); }
	public void setCreatedTimestamp(Date x) 	{ set( CREATED_TIMESTAMP, x ); }
	public Long getCommentableId()				{ return get( COMMENTABLE_ID ); } 
	public Commentable getCommentable()			{ return get( COMMENTABLE ); }
	public void setCommentable(Commentable x) 	{ set( COMMENTABLE, x ); }
	public List<Comment> getComments()			{ return getCommentable().getComments(); }
	public void setComments(List<Comment> x)	{ getCommentable().setComments( x ); }
	public List<Comment> getCommentsBy()		{ return get( COMMENTS_BY ); }
	
	public static List<User> byName(View view, String name) throws SQLException
	{
		return new SelectQuery<User>( TABLE ).select( view ).by( NAME, name ).list();
	}
	
	public static User byId(View view, long id) throws SQLException
	{
		return new SelectQuery<User>( TABLE ).select( view ).by( ID, id ).first();
	}
	
	public static List<User> all(View view) throws SQLException
	{
		return new SelectQuery<User>( TABLE ).select( view ).list();
	}
	
}
