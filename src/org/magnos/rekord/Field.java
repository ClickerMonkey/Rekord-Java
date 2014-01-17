package org.magnos.rekord;

import org.magnos.rekord.query.Queryable;
import org.magnos.rekord.query.expr.ColumnResolver;


public interface Field<T> extends Queryable
{
    public static final int NONE            = 0;
    public static final int READ_ONLY       = 1 << 0;
    public static final int GENERATED       = 1 << 1;
    public static final int HAS_DEFAULT     = 1 << 2;
    public static final int LAZY            = 1 << 3;
    public static final int NON_NULL        = 1 << 4;
    public static final int MODEL           = 1 << 5;
    public static final int MODEL_LIST      = 1 << 6;
    public static final int RETURN_ON_SAVE  = 1 << 7;
    public static final int ALWAYS_UPDATE     = 1 << 8;
    
	public String getName();
    public String getQuotedName();

	public int getIndex();
	public void setIndex(int index);
	
	public void setTable(Table table);
	public Table getTable();
	
	public int getFlags();
	public boolean is(int flag);

	public boolean isSelectable();
    public String getSelectExpression(ColumnResolver resolver, FieldLoad fieldLoad);
	
	public Value<T> newValue(Model model);
}
