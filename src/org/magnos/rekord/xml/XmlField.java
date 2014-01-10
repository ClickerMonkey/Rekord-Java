
package org.magnos.rekord.xml;

import org.magnos.rekord.Field;

abstract class XmlField extends XmlLoadable
{
    XmlTable table;
    XmlTable relatedTable;
    String name;
    int flags;
    int index;

    Field<?> field;
}
