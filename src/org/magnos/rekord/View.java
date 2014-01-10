package org.magnos.rekord;

import org.magnos.rekord.field.Column;
import org.magnos.rekord.query.SelectQuery;


public class View
{

    protected Table table;
    protected String[] columnSelections;
    protected boolean[] columnAlias;
    protected String from;
    
    public Table getTable()
    {
        return table;
    }
    
    public void setTable( Table table )
    {
        this.table = table;
    }
    
    public String[] getColumnSelections()
    {
        return columnSelections;
    }
    
    public void setColumnSelections( String[] columnSelections )
    {
        this.columnSelections = columnSelections;
    }
    
    public boolean[] getColumnAlias()
    {
        return columnAlias;
    }
    
    public void setColumnAlias( boolean[] columnAlias )
    {
        this.columnAlias = columnAlias;
    }
    
    public String getFrom()
    {
        return from;
    }
    
    public void setFrom( String from )
    {
        this.from = from;
    }
    
    public <M extends Model> SelectQuery<M> newSelect( LoadProfile load )
    {
        SelectQuery<M> select = new SelectQuery<M>( table );
        
        for (Field<?> f : load.getSelection().getFields())
        {
            int i = f.getIndex();
         
            if (f instanceof Column)
            {
                if (columnAlias[i])
                {
                    select.select( f, columnSelections[i], f.getQuotedName() );
                }
                else
                {
                    select.select( f, columnSelections[i] );    
                }    
            }
        }
        
        select.from( from );
        
        select.setLoadProfile( load );
        
        return select;
    }
    
}
