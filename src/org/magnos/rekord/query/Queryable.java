package org.magnos.rekord.query;

import org.magnos.rekord.Field;


public interface Queryable
{
    public Field<?> getField();
    public InsertAction getInsertAction();
    public boolean isUpdatable();
    public String getSaveExpression();
}
