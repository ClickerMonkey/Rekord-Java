package org.magnos.rekord.xml;

import java.util.Map;

import org.magnos.rekord.Converter;


abstract class XmlLoadable
{
    abstract void validate(XmlTable table, Map<String, XmlTable> tableMap);
    void instantiateFieldImplementation(Map<String, Converter<?, ?>> converters) {}
    void instantiateTableImplementation() {}
    void instantiateViewImplementation() {}
    void initializeTable() {}
    void relateFieldReferences() {}
    void finishTable() throws Exception {}
}
