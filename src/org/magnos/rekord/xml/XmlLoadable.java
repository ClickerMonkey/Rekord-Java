package org.magnos.rekord.xml;

import java.util.List;
import java.util.Map;

import org.magnos.dependency.DependencyNode;
import org.magnos.rekord.Converter;


interface XmlLoadable
{
	
    void validate (
    		XmlTable table, 
    		Map<String, XmlTable> tableMap,
    		Map<String, Converter<?, ?>> converters
    );
    
    void addNodes(List<DependencyNode<Runnable>> nodes);
    
}
