package org.magnos.rekord;

import java.io.FileInputStream;
import java.sql.Timestamp;
import java.util.List;

import org.junit.Test;
import org.magnos.rekord.query.NativeQuery;
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
		
//		User u = User.byId( User.Views.SHORT_NAME, 1L );
//		System.out.println( u.getName() );
//		u.delete();
//		u.save();
		
//		User u = User.byId( User.Views.ALL, 1L );
//		System.out.println( u.getState() );
//		u.getCommentsBy().clear();
//		u.save();

//		User u1 = User.byId( User.Views.ALL, 14L );
//		System.out.println( u1 );
		
//		User u = new User();
//		u.setName( "lowercase" );
//		u.save();
		
//		User u = User.byId( User.Views.ID, 1L );
//		System.out.println( u );
//		System.out.println( u.getCommentsBy() );
//		System.out.println( u );
		
/* NativeQuery */
		NativeQuery<User> nq = User.Query.CREATED_BEFORE.create();
	        
        System.out.println( nq.getQuery().getQuery() );
        
        nq.set( "date", new Timestamp( System.currentTimeMillis() ) );
        
        List<User> users = nq.executeQuery();

        User u = users.get( 0 );
        
        trans.start();
        
        NativeQuery<User> us = User.Query.UPDATE_STATE.create();
        us.bind( u );
        us.set( "new_state", "R" );
        us.executeUpdate();
/**/
		
		trans.end( false );
		trans.close();
	}
	
}
