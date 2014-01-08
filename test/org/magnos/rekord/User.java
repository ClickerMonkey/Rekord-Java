package org.magnos.rekord;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import org.magnos.rekord.field.Column;
import org.magnos.rekord.field.ForeignColumn;
import org.magnos.rekord.field.OneToMany;
import org.magnos.rekord.field.OneToOne;
import org.magnos.rekord.query.Query;
import org.magnos.rekord.query.QueryTemplate;
import org.magnos.rekord.query.SelectQuery;

public class User extends Model
{
	private static final long serialVersionUID = 1L;
	
	public static final Factory<User> 			FACTORY	 			= new Factory<User>() {
		public User create() {
			return new User();
		}
	};

	public static final Table 			        TABLE 				= Rekord.getTable( "user", FACTORY ); 
	public static final Column<Long> 			ID 					= TABLE.getField( "id" );
	public static final Column<String> 			NAME 				= TABLE.getField( "name" );
	public static final Column<UserState> 		STATE				= TABLE.getField( "state" );
	public static final Column<Date> 			CREATED_TIMESTAMP 	= TABLE.getField( "created_timestamp" );
	public static final ForeignColumn<Long> 	COMMENTABLE_ID 		= TABLE.getField( "commentable_id" );
	public static final OneToOne<Commentable> 	COMMENTABLE 		= TABLE.getField( "commentable" );
	public static final OneToMany<Comment> 		COMMENTS_BY 		= TABLE.getField( "comments_by" );
	
	public static class Load
	{
		public static final LoadProfile 		ALL 				= TABLE.getLoadProfile( "all" );
		public static final LoadProfile			ID					= TABLE.getLoadProfile( "id" );
		public static final LoadProfile 		WITHOUT_COMMENTS 	= TABLE.getLoadProfile( "without-comments" );
		public static final LoadProfile 		FOR_LINK		 	= TABLE.getLoadProfile( "for-link" );
		public static final LoadProfile 		SHORT_NAME		 	= TABLE.getLoadProfile( "short-name" );
	}
	
	public static class Queries
	{
	    public static final QueryTemplate<User>   CREATED_BEFORE      = TABLE.getQuery( "created-before" );
	    public static final QueryTemplate<User>   UPDATE_STATE        = TABLE.getQuery( "update-state" );
	    public static final QueryTemplate<User>   BY_ID               = TABLE.getQuery( "by-id" );
	    public static final QueryTemplate<User>   BY_NAME             = TABLE.getQuery( "by-name" );
	}
	
	public User()
	{
		super( TABLE );
	}
	
	public Long getId() 						{ return get( ID ); }
	public void setId(Long id)					{ set( ID, id ); }
	public String getName()						{ return get( NAME ); }
	public void setName(String x)				{ set( NAME, x ); }
	public UserState getState()					{ return get( STATE ); }
	public void setState(UserState x)			{ set( STATE, x ); }
	public Date getCreatedTimestamp()			{ return get( CREATED_TIMESTAMP ); }
	public void setCreatedTimestamp(Date x) 	{ set( CREATED_TIMESTAMP, x ); }
	public Long getCommentableId()				{ return get( COMMENTABLE_ID ); } 
	public Commentable getCommentable()			{ return get( COMMENTABLE ); }
	public void setCommentable(Commentable x) 	{ set( COMMENTABLE, x ); }
	public List<Comment> getComments()			{ return getCommentable().getComments(); }
	public void setComments(List<Comment> x)	{ getCommentable().setComments( x ); }
	public List<Comment> getCommentsBy()		{ return get( COMMENTS_BY ); }
	
	public static List<User> byName(LoadProfile load, String name) throws SQLException
	{
	    return Queries.BY_NAME.create().bind( "name", name ).list( load );
	}
	
	public static User byId(LoadProfile load, long id) throws SQLException
	{
	    return Queries.BY_ID.create().bind( "id", id ).first( load );
	}
	
	public static List<User> all(LoadProfile load) throws SQLException
	{
	    SelectQuery<User> select = new SelectQuery<User>( TABLE );
        select.select( load );
        
        Query<User> query = select.newQuery();
        
		return query.list();
	}
	
}
