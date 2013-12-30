package org.magnos.rekord.xml;

import java.util.Iterator;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XmlIterator<T extends Node> implements Iterator<T>, Iterable<T>
{

	private final NodeList nodes;
	private int index = -1;
	private int next = 0;
	
	public XmlIterator( Element e )
	{
		this( e.getChildNodes() );
	}
	
	public XmlIterator( NodeList nodes) 
	{
		this.nodes = nodes;
		this.next = getNextNode( index );
	}

	@Override
	public Iterator<T> iterator()
	{
		return this;
	}
	
	@Override
	public boolean hasNext()
	{
		return next != -1;
	}

	@Override
	public T next()
	{
		index = next;
		next = getNextNode( index );
		
		return (T)nodes.item( index );
	}

	@Override
	public void remove()
	{
		throw new UnsupportedOperationException();
	}
	
	private int getNextNode(int i)
	{
		while (++i < nodes.getLength()) 
		{
			if (nodes.item(i).getNodeType() == Node.ELEMENT_NODE) 
			{
				return i;
			}
		}
		
		return -1;
	}

}