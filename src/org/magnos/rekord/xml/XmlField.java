
package org.magnos.rekord.xml;

import java.util.Arrays;
import java.util.List;

import org.magnos.dependency.DependencyNode;
import org.magnos.rekord.Field;

abstract class XmlField implements XmlLoadable
{
	
	// set from XML
    XmlTable table;
    String name;
    int flags;
    int index;
    
    // set from validation 
    XmlTable relatedTable;    

    // set from Runnable
    Field<?> field;
    
    // nodes
    DependencyNode<Runnable> stateInstantiate = new DependencyNode<Runnable>();
    DependencyNode<Runnable> stateRelateFields = new DependencyNode<Runnable>();
    
    public void addNodes(List<DependencyNode<Runnable>> nodes)
    {
    	stateRelateFields.addDependency( stateInstantiate );
    	
    	nodes.addAll( Arrays.asList( stateInstantiate, stateRelateFields ) );
    }
    
}
