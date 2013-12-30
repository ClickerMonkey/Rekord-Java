package org.magnos.rekord.xml;

import java.util.Map;


abstract class XmlLoadable
{
    abstract void validate(XmlTable table, Map<String, XmlTable> tableMap);
    void instantiateFieldImplementation() {}
    void instantiateTableImplementation() {}
    void instantiateViewImplementation() {}
    void initializeTable() {}
    void relateFieldReferences() {}
    void finishTable() {}
}
