package org.magnos.rekord;

import java.io.FileInputStream;

import org.junit.Test;
import org.magnos.rekord.query.Select;
import org.magnos.rekord.query.SelectQuery;
import org.magnos.rekord.xml.XmlLoader;


public class TestRekord
{
	
	@Test
	public void test() throws Exception
	{
		XmlLoader.load( new FileInputStream( "test/test.xml" ) );
		
		for (int i = 0; i < Rekord.getTableCount(); i++) {
		    System.out.println( Rekord.getTable( i ) );
		}
		
		Transaction trans = Rekord.getTransaction();
		trans.start();

/* INHERITANCE 1 LEVEL * /
		Group g = new Group();
		g.setName( "name#1" );
		g.setPassword( "password#1" );
		g.save();
		
		g.setPassword( "passsword#2" );
		g.save();
		
		g.setName( "name#2" );
		g.save();
		
		g.delete();
/**/
	
/* INHERITANCE 2 LEVEL */
		CommentableGroup cg = new CommentableGroup();
		cg.setName( "name#3" );
		cg.setPassword( "password#3" );
		cg.save();

		cg.getCommentable().setCount( 4 );
		cg.setPassword( "password#4" );
		cg.save();
/**/
		
/* JOINING */
		
		SelectQuery<CommentableGroup> q = Select.build( CommentableGroup.TABLE, CommentableGroup.TABLE.getLoadProfileAll() );
		
//		SelectQuery<CommentableGroup> q = Select.from( CommentableGroup.TABLE );
//		
//		TableAlias commentableGroup = q.getTableAlias();
//		TableAlias group = q.alias( Group.TABLE );
//		TableAlias rolePlayer = q.alias( RolePlayer.TABLE );
//		
//		Join groupJoin = q.join( Join.INNER, group );
//		groupJoin.where( Group.ROLE_PLAYER_ID ).eq( commentableGroup.alias( CommentableGroup.GROUP_ID ) );
//
//		Join rolePlayerJoin = q.join( Join.INNER, rolePlayer );
//		rolePlayerJoin.where( RolePlayer.ID ).eq( group.alias( Group.ROLE_PLAYER_ID ) );
//		
//		q.select( commentableGroup.alias( CommentableGroup.COMMENTABLE_ID ) );
//		q.select( group.alias( Group.PASSWORD ) );
//		q.select( rolePlayer.alias( RolePlayer.NAME ) );
		
		System.out.println( q.create().list() );
/**/
		
//		long uid0 = new SelectQuery<Model>( User.TABLE ).create().withOffset( 0 ).first( User.ID );
//		long uid1 = new SelectQuery<Model>( User.TABLE ).create().withOffset( 1 ).first( User.ID );
		
/* CRAZY (return-on-save, always-update, out, last-modified-columns) * /
		Crazy c = new Crazy();
		c.setDiameter( 4.5f );
		c.save();
		System.out.println( c );
		
		Thread.sleep( 1000 );
		
		c.setDiameter( 3.5f );
		c.save();
		System.out.println( c );
		
		/* throws a ConcurrentModificationException * /
		c.setLastModifiedTimestamp( new Timestamp( System.currentTimeMillis() - 10000 ) );
		c.setDiameter( 2.5f );
		c.save();

/**/
		
/* UPDATING (history table) * /
		User u = User.byId( User.Load.ALL, uid0 );
		u.setName( "New Name!" );
		u.save();
/**/
		
/* DELETING * /
		User u = User.byId( User.Load.SHORT_NAME, uid0 );
		System.out.println( u.getName() );
		System.out.println( u.exists() );
		u.delete();
		System.out.println( u.exists() );
		
		User u1 = User.byId( User.Load.ALL, uid1 );
		System.out.println( u1.getState() );
		u1.getCommentsBy().clear();
		u1.save();
/**/

//		User u1 = User.byId( User.Load.ALL, uid0 );
//		System.out.println( u1 );
		
//		User u = new User();
//		u.setName( "lowercase" );
//		u.save();
		
//		User u = User.byId( User.Load.ID, uid1 );
//		System.out.println( u );
//		System.out.println( u.getCommentsBy() );
//		System.out.println( u );
		
/* NativeQuery * /
		Query<User> nq = User.Queries.CREATED_BEFORE.create();
		nq.bind( "date", User.CREATED_TIMESTAMP, new Timestamp( System.currentTimeMillis() ) );
		
		System.out.println( nq.getReadableQuery() );
		
		String name = nq.first( User.NAME );
        System.out.println( name );
        
        List<User> users = nq.list();

        System.out.println( users );
        
        User u = users.get( 0 );
        
        trans.start();
        
        Query<User> us = User.Queries.UPDATE_STATE.create();
        us.bind( u );
        us.bind( "new_state", User.STATE, UserState.REGISTERED );
        us.executeUpdate();
/**/
		
/*SelectQuery #1 * /
		SelectQuery<Comment> numberOfComments = new SelectQuery<Comment>( Comment.TABLE );
		numberOfComments.count().where( Comment.USER_ID ).eq( User.ID );
		
		SelectQuery<User> query = new SelectQuery<User>( User.TABLE );
		query.select( User.Load.ALL );
		query.where( User.COMMENTABLE ).eqExp( "?cid" )
			  .and( User.NAME ).eq( "LOWERCASE" )
			  .and( User.CREATED_TIMESTAMP ).between( new Timestamp( System.currentTimeMillis() - 10000000000L ), new Timestamp( System.currentTimeMillis() ) )
		      .and( numberOfComments ).gt( 0 );
		
		System.out.println( query.create().bind( "cid", 5L ).list() );
/**/
		  
/*SelectQuery #2* /
		Condition c_user = is( Comment.USER_ID ).eq( User.ID );
		
        SelectQuery<Comment> numberOfComments = new SelectQuery<Comment>( Comment.TABLE ).count().where( c_user );
		
		Timestamp t0 = new Timestamp( System.currentTimeMillis() - 10000000000L );
		Timestamp t1 = new Timestamp( System.currentTimeMillis() );
        
        SelectQuery<User> query = new SelectQuery<User>( User.TABLE );
        query.select( User.Load.ALL );
        query.where(
            is( User.COMMENTABLE ).eqExp( "?cid" ),
            isString( User.NAME ).ieq( "clickerMONKEY" ),
            is( User.CREATED_TIMESTAMP ).between( t0, t1 ),
            is( numberOfComments ).gt( 0 ),
            or( 
                is( User.ID ).gt( 0L ), 
                is( User.ID ).lt( 0L ) 
            )
        );
        
        System.out.println( query.create().bind( "cid", 3L ).list() );
/**/
        
//        System.out.println( Select.all( Commentable.TABLE, Commentable.ID ) );

//        System.out.println( Select.byUnique( User.TABLE, User.ID, 4L ) );
        
		trans.end( false );
		trans.close();
	}
	
}
