package org.magnos.rekord.xml;

import org.magnos.rekord.Listener;
import org.magnos.rekord.ListenerEvent;


class XmlListenerClass
{
    String elementName;
    String listenerClassName;
    Class<?> listenerClass;
    String[] listenerEventNames;
    
    ListenerEvent[] listenerEvents;
    
    public Listener<?> newInstance()
    {
        try
        {
            return (Listener<?>)listenerClass.newInstance();
        }
        catch (Exception e)
        {
            throw new RuntimeException( "Listener implementation " + listenerClassName + " requires a public no-arg constructor", e );
        }
    }
    
}
