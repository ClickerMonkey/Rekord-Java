
package org.magnos.rekord.field;

import org.magnos.rekord.Field;
import org.magnos.rekord.Table;
import org.magnos.rekord.util.SqlUtil;


public abstract class AbstractField<T> implements Field<T>
{

    protected String name;
    protected String quotedName;
    protected int flags;
    protected int index;
    protected Table table;

    public AbstractField( String name, int flags )
    {
        this.name = name;
        this.quotedName = SqlUtil.namify( name );
        this.flags = flags;
    }

    @Override
    public String getName()
    {
        return name;
    }
    
    public String getQuotedName()
    {
    	return name;
    }

    @Override
    public int getIndex()
    {
        return index;
    }

    @Override
    public void setIndex( int valueIndex )
    {
        this.index = valueIndex;
    }

    @Override
    public void setTable( Table entity )
    {
        this.table = entity;
    }

    @Override
    public Table getTable()
    {
        return table;
    }

    @Override
    public int getFlags()
    {
        return flags;
    }

    @Override
    public boolean is( int flag )
    {
        return (flags & flag) == flag;
    }

    protected StringBuilder beginToString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append( name );
        sb.append( ": {" );

        sb.append( "index=" ).append( index );
        
        if (is( READ_ONLY ))
        {
            sb.append( ", read-only" );
        }
        if (is( GENERATED ))
        {
            sb.append( ", generated" );
        }
        if (is( LAZY ))
        {
            sb.append( ", lazy" );
        }
        if (is( NON_NULL ))
        {
            sb.append( ", non-null" );
        }

        return sb;
    }

    protected String endToString( StringBuilder sb )
    {
        sb.append( "}" );
        
        return sb.toString();
    }

}
