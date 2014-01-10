package org.magnos.rekord.field;

import org.magnos.rekord.Model;
import org.magnos.rekord.Value;

public class InheritColumn<T> extends Column<T>
{

    public Column<T> inherited;
    
    public InheritColumn( String column, Column<T> inherited )
    {
        super( column, inherited.getSqlType(), inherited.getType(), inherited.getFlags(), inherited.getIn(), inherited.getOut(), inherited.getDefaultValue(), inherited.getConverter() );
    }

    @Override
    public Value<T> newValue( Model model )
    {
        return model.valueOf( inherited );
    }
    
}
