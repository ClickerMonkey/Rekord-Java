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
		
		Transaction trans = Rekord.getTransaction();
		trans.start();
		
		User u = User.byId( User.Views.ALL, 1L );
		Comment c = u.getCommentsBy().get( 0 );
		u.getCommentsBy().clear();
		u.getCommentsBy().add( c );
		u.save();

//		User u = User.byId( User.Views.ID, 1L );
//		
//		System.out.println( u );
//		System.out.println( u.getCommentsBy() );
//		System.out.println( u );
		
		trans.rollback();
		trans.close();
	}
	
}
