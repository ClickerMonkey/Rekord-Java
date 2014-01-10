package org.magnos.rekord.field;

import org.magnos.rekord.Field;
import org.magnos.rekord.FieldLoad;
import org.magnos.rekord.LoadProfile;
import org.magnos.rekord.Table;
import org.magnos.rekord.query.InsertAction;


public abstract class JoinField<T> extends AbstractField<T>
{

    protected Table joinTable;
    protected ForeignColumn<?>[] joinColumns;
    protected LoadProfile joinLoad;

    public JoinField( String name, int flags )
    {
        super( name, flags );
    }
    
    
    public void setJoin( Table joinTable, LoadProfile joinLoad, ForeignColumn<?> ... joinColumns )
    {
        this.joinTable = joinTable;
        this.joinLoad = joinLoad;
        this.joinColumns = joinColumns;
    }
    
    @Override
    public String getSelectExpression(FieldLoad fieldLoad)
    {
        return null;
    }
    
    @Override
    public InsertAction getInsertAction()
    {
        return InsertAction.NONE;
    }

    @Override
    public boolean isUpdatable()
    {
        return false;
    }

    @Override
    public String getSaveExpression()
    {
        return null;
    }
    
    @Override
    public Field<?> getField()
    {
        return this;
    }
    
    public Table getJoinTable()
    {
        return joinTable;
    }
    
    public ForeignColumn<?>[] getJoinColumns()
    {
        return joinColumns;
    }
    
    public LoadProfile getJoinLoad()
    {
        return joinLoad;
    }
    
    @Override
    public String endToString(StringBuilder sb)
    {
        sb.append( ", join=" ).append( joinTable.getName() );
        sb.append( "[" ).append( joinLoad.getName() ).append( "]" );
        sb.append( ", join-key={" );
        for (int i = 0; i < joinColumns.length; i++) {
            if (i > 0) sb.append( ", " );
            ForeignColumn<?> fc = joinColumns[i];
            sb.append( fc.getName() ).append( "->" ).append( joinTable.getName() ).append( "." ).append( fc.getForeignColumn().getName() );
        }
        sb.append( "}" );
        
        return super.endToString( sb );
    }
    
}
