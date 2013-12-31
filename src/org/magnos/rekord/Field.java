package org.magnos.rekord;

import org.magnos.rekord.query.InsertQuery;
import org.magnos.rekord.query.SelectQuery;


public interface Field<T>
{
	public String getName();
	
	public int getIndex();
	public void setIndex(int index);
	
	public void setTable(Table table);
	public Table getTable();
	
	public int getFlags();
	public boolean is(int flag);
	
	public Value<T> newValue(Model model);
	
	public void prepareSelect(SelectQuery<?> query);
	public void prepareInsert(InsertQuery query);
}
