package org.magnos.rekord;

import java.io.FileInputStream;
import java.sql.Timestamp;
import java.util.List;

import org.junit.Test;
import org.magnos.rekord.query.Query;
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
		
		long uid = new SelectQuery<Model>( User.TABLE ).create().withLimit( 1 ).first( User.ID );
		
//		User u = User.byId( User.Load.SHORT_NAME, uid );
//		System.out.println( u.getName() );
//		u.delete();
		
//		User u = User.byId( User.Load.ALL, uid );
//		System.out.println( u.getState() );
//		u.getCommentsBy().clear();
//		u.save();

//		User u1 = User.byId( User.Load.ALL, uid );
//		System.out.println( u1 );
		
//		User u = new User();
//		u.setName( "lowercase" );
//		u.save();
		
		User u = User.byId( User.Load.ID, uid );
		System.out.println( u );
		for (Comment c : u.getCommentsBy()) {
		    System.out.println( c );
		}
		System.out.println( u );
		
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
		
//		SelectQuery<Comment> numberOfComments = new SelectQuery<Comment>( Comment.TABLE );
//		numberOfComments.count().where( Comment.USER_ID ).eq( User.ID );
//		
//		SelectQuery<User> query = new SelectQuery<User>( User.TABLE );
//		query.select( User.Load.ALL );
//		query.where( User.COMMENTABLE ).eqExp( "?cid" )
//			  .and( User.NAME ).eq( "LOWERCASE" )
//			  .and( User.CREATED_TIMESTAMP ).between( new Timestamp( System.currentTimeMillis() - 10000000000L ), new Timestamp( System.currentTimeMillis() ) )
//		      .and( numberOfComments ).gt( 0 );
//		
//		System.out.println( query.create().bind( "cid", 5L ).list() );

		trans.end( false );
		trans.close();
	}
	
}
