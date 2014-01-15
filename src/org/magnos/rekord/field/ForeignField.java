package org.magnos.rekord.field;

import org.magnos.rekord.Converter;
import org.magnos.rekord.Table;
import org.magnos.rekord.Type;


public class ForeignField<T> extends Column<T>
{
	
	protected Column<T> foreignColumn;
	protected Table foreignTable;

	public ForeignField( String column, int sqlType, Type<Object> type, String in, String out, T defaultValue, Converter<Object, T> converter )
	{
		super( column, sqlType, type, NONE, in, out, defaultValue, converter );
	}

	public Column<T> getForeignColumn()
	{
		return foreignColumn;
	}
	
	public void setForeignColumn( Column<T> foreignColumn )
	{
		this.foreignColumn = foreignColumn;
	}
	
    public Table getForeignTable()
    {
        return foreignTable;
    }

    public void setForeignTable( Table foreignTable )
    {
        this.foreignTable = foreignTable;
    }

    @Override
	public String toString()
	{
	    StringBuilder sb = beginToString();
        sb.append( ", sql-type=" ).append( sqlType );
        sb.append( ", type=" ).append( type.getClass().getSimpleName() );
        sb.append( ", in=" ).append( in );
        sb.append( ", out=" ).append( out );
        sb.append( ", default-value=" ).append( type.toString( defaultValue ) );
        sb.append( ", converter=" ).append( converter.getClass().getSimpleName() );
        sb.append( ", references=" ).append( foreignTable.getName() ).append( "." ).append( foreignColumn.getName() );
        return endToString( sb );
	}

}
