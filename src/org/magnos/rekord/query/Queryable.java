package org.magnos.rekord.query;

import org.magnos.rekord.FieldLoad;


public interface Queryable
{
    public String getName();
    public String getQuotedName();
    public boolean isSelectable();
    public String getSelectExpression(FieldLoad fieldLoad);
    
    public InsertAction getInsertAction();
    public boolean isUpdatable();
    public String getSaveExpression();
}
