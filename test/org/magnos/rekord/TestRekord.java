package org.magnos.rekord;

import java.io.FileInputStream;
import java.sql.Timestamp;
import java.util.List;

import org.junit.Test;
import org.magnos.rekord.query.Query;
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
		Query<User> nq = User.Query.CREATED_BEFORE.create();
		nq.bind( "date", new Timestamp( System.currentTimeMillis() ) );
		
		System.out.println( nq.getReadableQuery() );
		
		String name = nq.first( User.NAME );
        System.out.println( name );
        
        List<User> users = nq.list();

        System.out.println( users );
        
        User u = users.get( 0 );
        
        trans.start();
        
        Query<User> us = User.Query.UPDATE_STATE.create();
        us.bind( u );
        us.bind( "new_state", "R" );
        us.executeUpdate();
/**/
        
		
		trans.end( false );
		trans.close();
	}
	
}
