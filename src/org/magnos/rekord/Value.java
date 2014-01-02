package org.magnos.rekord;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.magnos.rekord.query.InsertQuery;
import org.magnos.rekord.query.UpdateQuery;

public interface Value<T>
{
	public T get(Model model);
	public boolean hasValue();
	public void set(Model model, T value);
	
	public boolean hasChanged();
	public void clearChanges();
	
	public void load(FieldView fieldView) throws SQLException;

	public void prepareDynamicInsert(InsertQuery query);
	public int toInsert(PreparedStatement preparedStatement, int paramIndex) throws SQLException;
	public void fromInsertReturning(ResultSet results) throws SQLException;
	
	public void prepareDynamicUpdate(UpdateQuery query);
	public int toUpdate(PreparedStatement preparedStatement, int paramIndex) throws SQLException;
	
	public void fromSelect(ResultSet results, FieldView fieldView) throws SQLException;
	public void postSelect(Model model, FieldView fieldView) throws SQLException;
	
	public void fromResultSet(ResultSet results) throws SQLException;
	public int toPreparedStatement(PreparedStatement preparedStatement, int paramIndex) throws SQLException;
	
	public void preSave(Model model) throws SQLException;
	public void postSave(Model model) throws SQLException;
	
	public void preDelete(Model model) throws SQLException;
	public void postDelete(Model model) throws SQLException;
	
	public void serialize(ObjectOutputStream out) throws IOException;
	public void deserialize(ObjectInputStream in) throws IOException, ClassNotFoundException;
	
	public Field<T> getField();
}
