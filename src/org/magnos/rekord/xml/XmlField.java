
package org.magnos.rekord.xml;

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
    DependencyNode<Runnable> nodeValidate = new DependencyNode<Runnable>();
    DependencyNode<Runnable> nodeInstantiate = new DependencyNode<Runnable>();
    DependencyNode<Runnable> nodeRelateFields = new DependencyNode<Runnable>();
    
    public void addNodes(List<DependencyNode<Runnable>> nodes)
    {
    	nodeInstantiate.addDependency( nodeValidate );
    	nodeRelateFields.addDependency( nodeInstantiate );
    	
    	nodes.add( nodeValidate );
    	nodes.add( nodeInstantiate );
    	nodes.add( nodeRelateFields );
    }
    
}
