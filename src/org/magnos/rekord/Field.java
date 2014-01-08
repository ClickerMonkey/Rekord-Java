package org.magnos.rekord;

import org.magnos.rekord.query.Queryable;


public interface Field<T> extends Queryable
{
    public static final int NONE        = 0;
    public static final int READ_ONLY   = 1 << 0;
    public static final int GENERATED   = 1 << 1;
    public static final int HAS_DEFAULT = 1 << 2;
    public static final int LAZY        = 1 << 3;
    public static final int NON_NULL    = 1 << 4;
    public static final int MODEL    	= 1 << 5;
    public static final int MODEL_LIST	= 1 << 6;
    
	public String getName();
	
	public int getIndex();
	public void setIndex(int index);
	
	public void setTable(Table table);
	public Table getTable();
	
	public int getFlags();
	public boolean is(int flag);
	
	public Value<T> newValue(Model model);
}
