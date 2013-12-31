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
		
//		User u = User.byId( User.Views.ALL, 2L );
//		u.getCommentsBy().clear();
//		u.save();

		User u = new User();
		System.out.println( u.getCommentable().getCount() );
//		u.setName( "lowercase" );
//		u.save();
		
//		User u = User.byId( User.Views.ID, 2L );
//		System.out.println( u );
//		System.out.println( u.getCommentsBy() );
//		System.out.println( u );
		
		trans.end( false );
		trans.close();
	}
	
}
