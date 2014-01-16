package org.magnos.rekord;

import java.io.FileInputStream;

import org.junit.Test;
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
		      .and( numberOfComments ).gt( 0 )
		      .and()
		          .where( User.ID ).gt( 0L )
		          .or( User.ID ).lt( 0L )
		      .end();
		
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
