package org.magnos.rekord.query;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.magnos.rekord.Factory;
import org.magnos.rekord.Field;
import org.magnos.rekord.Model;
import org.magnos.rekord.Table;
import org.magnos.rekord.field.Column;
import org.magnos.rekord.query.expr.GroupExpression;

public class InsertQuery<M extends Model> extends GroupExpression<InsertQuery<M>> implements Factory<Query<M>>
{

    public final Table table;
    public QueryBuilder columns;
    public QueryBuilder values;
    public QueryBuilder returning;
    public List<Field<?>> returningFields;
    
    public InsertQuery( Table table )
    {
        this.table = table;
        this.columns = new QueryBuilder();
        this.values = new QueryBuilder();
        this.returning = new QueryBuilder();
        this.returningFields = new ArrayList<Field<?>>();
    }
    
    public InsertQuery<M> reset()
    {
        columns.clear();
        values.clear();
        returning.clear();
        returningFields.clear();
        
        return this;
    }
    
    public <T> InsertQuery<M> insert( Column<T> column )
    {
        columns.pad( ", " );
        columns.append( column.getQuotedName() );
        
        values.pad( ", " );
        values.append( column.getName(), column.getIn(), column, null, column.getType() );
        
        return this;
    }
    
    public <T> InsertQuery<M> insert( Column<T> column, T value )
    {
        columns.pad( ", " );
        columns.append( column.getQuotedName() );
        
        values.pad( ", " );
        values.append( column.getName(), column.getIn(), column, column.getConverter().toDatabase( value ), column.getType() );
        
        return this;
    }
    
    public <T> InsertQuery<M> returning( Field<T> field )
    {
        if (field instanceof Column)
        {
            returning.pad( ", " );
            returning.append( field.getQuotedName() );
        }
        
        returningFields.add( field );
        
        return this;
    }
    
    public QueryTemplate<M> newTemplate()
    {
        QueryBuilder qb = new QueryBuilder();
        
        qb.append( "INSERT INTO " );
        qb.append( table.getQuotedName() );
        qb.append( " " );
        
        if (columns.hasQuery())
        {
            qb.append( "(" );
            qb.append( columns );
            qb.append( ") VALUES (" );
            qb.append( values );
            qb.append( ")" );
        }
        else
        {
            qb.append( "DEFAULT VALUES" );
        }
        
        if (returning.hasQuery())
        {
            qb.append( " RETURNING " );
            qb.append( returning );
        }
        
        return qb.create( table, null, returningFields );
    }
    
    @Override
    public Query<M> create()
    {
        return newTemplate().create();
    }
    
	public static QueryTemplate<Model> forFields( Table table, Field<?> ... fields)
	{
	    InsertQuery<Model> insert = new InsertQuery<Model>( table );
	    
	    Set<Field<?>> fieldSet = new HashSet<Field<?>>();

	    for (Field<?> f : fields)
	    {
	        fieldSet.add( f );
	    }
	    
	    for (Field<?> f : table.getFields())
	    {
	        if (f.is( Field.HAS_DEFAULT ) && !fieldSet.contains( f ))
	        {
	            insert.returning( f );
	        }
	    }
	    
	    for (Field<?> f : fields)
	    {
	        if (f instanceof Column)
	        {
	            insert.insert( (Column<?>)f );
	        }
	        else
	        {
	            insert.returning( f );
	        }
	    }
        
		return insert.newTemplate();
	}
	
}
