
package org.magnos.rekord.type;

import java.io.StringReader;
import java.io.StringWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Types;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.magnos.rekord.Type;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;


public class TypeXml implements Type<Document>
{
    
    public static final TypeXml INSTANCE = new TypeXml();
    
    public static Document newDocument()
    {
        return INSTANCE.documentBuilder.newDocument();
    }

    private DocumentBuilderFactory documentBuilderFactory;
    private DocumentBuilder documentBuilder;
    private TransformerFactory transformerFactory;
    private Transformer transformer;
    
    public TypeXml()
    {
        try
        {
            documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
            
            transformerFactory = TransformerFactory.newInstance();
            transformer = transformerFactory.newTransformer();
        }
        catch (ParserConfigurationException e)
        {
            throw new RuntimeException( e );
        }
        catch (TransformerConfigurationException e)
        {
            throw new RuntimeException( e );
        }
    }
    
    @Override
    public String getPartialExpression( String in, int limit )
    {
        return in;
    }

    @Override
    public Document fromResultSet( ResultSet resultSet, String column, boolean nullable ) throws SQLException
    {
        return fromXML( resultSet.getSQLXML( column ) );
    }

    @Override
    public Document fromResultSet( ResultSet resultSet, int column, boolean nullable ) throws SQLException
    {
        return fromXML( resultSet.getSQLXML( column ) );
    }
    
    private Document fromXML( SQLXML xml ) throws SQLException
    {
        Document doc = null;
        
        if (xml != null)
        {
            try
            {
                doc = documentBuilder.parse( xml.getBinaryStream() );
            }
            catch (Exception e)
            {
                throw new SQLException( "Error parsing XML column", e );
            }
        }
        
        return doc;
    }

    @Override
    public boolean isPartial( Document value, int limit )
    {
        return false;
    }

    @Override
    public void toPreparedStatement( PreparedStatement preparedStatement, Document value, int paramIndex ) throws SQLException
    {
        if (value != null)
        {
            SQLXML xml = preparedStatement.getConnection().createSQLXML();
            
            try
            {
                transformer.transform( new DOMSource( value ), new StreamResult( xml.setBinaryStream() ) );
            }
            catch (Exception e)
            {
                throw new SQLException( "Error writing XML column", e );
            }
            
            preparedStatement.setSQLXML( paramIndex, xml );
        }
        else
        {
            preparedStatement.setNull( paramIndex, Types.SQLXML );
        }
    }
    
    @Override
    public String toString( Document value )
    {
        if (value == null) 
        { 
            return null;
        }
        
        try
        {
            StringWriter writer = new StringWriter();
            
            transformer.transform( new DOMSource( value ), new StreamResult( writer ) );
            
            return writer.toString();
        }
        catch (Exception e)
        {
            throw new RuntimeException( "Error converting document to string", e );
        }
    }

    @Override
    public Document fromString( String x )
    {
        if (x == null)
        {
            return null;
        }
        
        if (x.trim().length() == 0)
        {
            return newDocument();
        }
        
        try
        {
            return documentBuilder.parse( new InputSource( new StringReader( x ) ) );
        }
        catch (Exception e)
        {
            throw new RuntimeException( "Error parsing XML column", e );
        }
    }

}
