
package org.magnos.rekord.query.expr;

import java.util.ArrayList;
import java.util.List;

import org.magnos.rekord.Key;
import org.magnos.rekord.Model;
import org.magnos.rekord.Operator;
import org.magnos.rekord.Table;
import org.magnos.rekord.field.Column;
import org.magnos.rekord.field.ForeignColumn;
import org.magnos.rekord.field.ManyToOne;
import org.magnos.rekord.field.OneToOne;
import org.magnos.rekord.query.QueryBuilder;
import org.magnos.rekord.query.condition.Condition;
import org.magnos.rekord.query.condition.CustomCondition;
import org.magnos.rekord.query.condition.GroupCondition;
import org.magnos.rekord.query.condition.OperatorCondition;
import org.magnos.rekord.query.condition.PrependedCondition;

@SuppressWarnings ({ "rawtypes" } )
public class GroupExpression extends PrependedCondition
{

    public static final String AND = " AND ";
    public static final String AND_NOT = " AND NOT ";
    public static final String OR = " OR ";
    public static final String OR_NOT = " OR NOT ";

    public final GroupExpression parent;
    public final List<PrependedCondition> conditions;

    public GroupExpression()
    {
        this( null, AND );
    }

    public GroupExpression( GroupExpression parent, String prepend )
    {
        super( prepend, null );
        
        this.parent = parent;
        this.conditions = new ArrayList<PrependedCondition>();
    }
    
    public boolean hasConditions()
    {
        return conditions.size() > 0;
    }

    public void toQuery( QueryBuilder query )
    {
        if ( conditions.isEmpty() )
        {
            return;
        }
        
        if ( conditions.size() == 1 )
        {
            conditions.get( 0 ).toQuery( query );
        }
        else
        {
            query.appendValuable( "(" );
            
            for (int i = 0; i < conditions.size(); i++)
            {
                PrependedCondition pc = conditions.get( i );
                
                if (i > 0)
                {
                    query.appendValuable( pc.prepend );
                }
                
                pc.toQuery( query );
            }
            
            query.appendValuable( ")" );
        }
    }

    protected GroupExpression add( String prepend, Condition condition )
    {
        conditions.add( new PrependedCondition( prepend, condition ) );

        return this;
    }

    protected GroupExpression newChild( String prepend )
    {
        GroupExpression child = new GroupExpression( this, prepend );

        conditions.add( child );

        return child;
    }

    protected Expression<Object> newStringExpression( String prepend, String expression )
    {
        return new StringExpression( this, prepend, expression );
    }

    protected GroupExpression newStringExpressionCustom( String prepend, String expression, Object[] values )
    {
        return add( prepend, new CustomCondition( expression, values ) );
    }

    protected GroupExpression newKeyCondition( String prepend, Key key )
    {
        final int keySize = key.size();
        Condition[] conditions = new Condition[keySize];

        for (int i = 0; i < keySize; i++)
        {
            conditions[i] = new OperatorCondition( key.fieldAt( i ), Operator.EQ, key.valueAt( i ) );
        }

        return add( prepend, new GroupCondition( AND, conditions ) );
    }

    protected GroupExpression newForeignKeyCondition( String prepend, Key key )
    {
        final int keySize = key.size();
        Condition[] conditions = new Condition[keySize];

        for (int i = 0; i < keySize; i++)
        {
            ForeignColumn<?> foreign = (ForeignColumn<?>)key.fieldAt( i );

            conditions[i] = new OperatorCondition( foreign.getForeignColumn(), Operator.EQ, key.valueAt( i ) );
        }

        return add( prepend, new GroupCondition( AND, conditions ) );
    }

    protected GroupExpression newColumnsBindExpression( String prepend, Column<?>... columns )
    {
        Condition[] conditions = new Condition[columns.length];

        for (int i = 0; i < columns.length; i++)
        {
            conditions[i] = getColumnBindCondition( columns[i] );
        }

        return add( prepend, new GroupCondition( AND, conditions ) );
    }

    protected GroupExpression newColumnsForeignBindExpression( String prepend, ForeignColumn<?>... columns )
    {
        Condition[] conditions = new Condition[columns.length];

        for (int i = 0; i < columns.length; i++)
        {
            conditions[i] = getColumnForeignBindCondition( columns[i] );
        }

        return add( prepend, new GroupCondition( AND, conditions ) );
    }

    protected GroupExpression newColumnBindExpression( String prepend, Column<?> column )
    {
        return add( prepend, getColumnBindCondition( column ) );
    }

    protected Condition getColumnBindCondition( Column<?> c )
    {
    	return new OperatorCondition( c.getQuotedName(), c, c.getName(), "?", Operator.EQ, null, c.getType(), c.getConverter() ); 
    }

    protected Condition getColumnForeignBindCondition( ForeignColumn<?> c )
    {
    	return new OperatorCondition( c.getQuotedName(), c.getForeignColumn(), c.getForeignColumn().getName(), "?", Operator.EQ, null, c.getType(), c.getConverter() );
    }


    public GroupExpression end()
    {
        return parent;
    }
    

    public Expression<Object> where( String expression )
    {
        return newStringExpression( AND, expression );
    }

    public <T> ColumnExpression<T> where( Column<T> column )
    {
    	return new ColumnExpression<T>( this, AND, column );
    }
    
    public <M extends Model> ModelExpression<M> where( OneToOne<M> oneToOne )
    {
    	return new ModelExpression<M>( this, AND, oneToOne, oneToOne.getJoinColumns() );
    }
    
    public <M extends Model> ModelExpression<M> where( ManyToOne<M> manyToOne )
    {
    	return new ModelExpression<M>( this, AND, manyToOne, manyToOne.getJoinColumns() );
    }

    public GroupExpression where( String expression, Object... values )
    {
        return newStringExpressionCustom( AND, expression, values );
    }

    public GroupExpression whereKey( Key key )
    {
        return newKeyCondition( AND, key );
    }

    public GroupExpression whereForeignKey( Key key )
    {
        return newForeignKeyCondition( AND, key );
    }

    public GroupExpression whereKeyBind( Table table )
    {
        return newColumnsBindExpression( AND, table.getKeyColumns() );
    }
    
    public GroupExpression whereForeignKeyBind( ForeignColumn<?> ... columns )
    {
        return newColumnsForeignBindExpression( AND, columns );
    }

    public GroupExpression whereColumnBind( Column<?>... columns )
    {
        return newColumnsBindExpression( AND, columns );
    }
    
    public GroupExpression whereBind( Column<?> column )
    {
        return newColumnBindExpression( AND, column );
    }
    
    

    public GroupExpression and()
    {
        return newChild( AND );
    }

    public Expression<Object> and( String expression )
    {
        return newStringExpression( AND, expression );
    }

    public <T> ColumnExpression<T> and( Column<T> column )
    {
    	return new ColumnExpression<T>( this, AND, column );
    }
    
    public <M extends Model> ModelExpression<M> and( OneToOne<M> oneToOne )
    {
    	return new ModelExpression<M>( this, AND, oneToOne, oneToOne.getJoinColumns() );
    }
    
    public <M extends Model> ModelExpression<M> and( ManyToOne<M> manyToOne )
    {
    	return new ModelExpression<M>( this, AND, manyToOne, manyToOne.getJoinColumns() );
    }

    public GroupExpression and( String expression, Object... values )
    {
        return newStringExpressionCustom( AND, expression, values );
    }

    public GroupExpression andKey( Key key )
    {
        return newKeyCondition( AND, key );
    }

    public GroupExpression andForeignKey( Key key )
    {
        return newForeignKeyCondition( AND, key );
    }

    public GroupExpression andKeyBind( Table table )
    {
        return newColumnsBindExpression( AND, table.getKeyColumns() );
    }
    
    public GroupExpression andForeignKeyBind( ForeignColumn<?> ... columns )
    {
        return newColumnsForeignBindExpression( AND, columns );
    }

    public GroupExpression andColumnBind( Column<?>... columns )
    {
        return newColumnsBindExpression( AND, columns );
    }
    
    public GroupExpression andBind( Column<?> column )
    {
        return newColumnBindExpression( AND, column );
    }
    
    

    public GroupExpression andNot()
    {
        return newChild( AND_NOT );
    }

    public Expression<Object> andNot( String expression )
    {
        return newStringExpression( AND_NOT, expression );
    }

    public <T> ColumnExpression<T> andNot( Column<T> column )
    {
    	return new ColumnExpression<T>( this, AND_NOT, column );
    }
    
    public <M extends Model> ModelExpression<M> andNot( OneToOne<M> oneToOne )
    {
    	return new ModelExpression<M>( this, AND_NOT, oneToOne, oneToOne.getJoinColumns() );
    }
    
    public <M extends Model> ModelExpression<M> andNot( ManyToOne<M> manyToOne )
    {
    	return new ModelExpression<M>( this, AND_NOT, manyToOne, manyToOne.getJoinColumns() );
    }

    public GroupExpression andNot( String expression, Object... values )
    {
        return newStringExpressionCustom( AND_NOT, expression, values );
    }

    public GroupExpression andNotKey( Key key )
    {
        return newKeyCondition( AND_NOT, key );
    }

    public GroupExpression andNotForeignKey( Key key )
    {
        return newForeignKeyCondition( AND_NOT, key );
    }

    public GroupExpression andNotKeyBind( Table table )
    {
        return newColumnsBindExpression( AND_NOT, table.getKeyColumns() );
    }
    
    public GroupExpression andNotForeignKeyBind( ForeignColumn<?> ... columns )
    {
        return newColumnsForeignBindExpression( AND_NOT, columns );
    }

    public GroupExpression andNotColumnBind( Column<?>... columns )
    {
        return newColumnsBindExpression( AND_NOT, columns );
    }
    
    public GroupExpression andNotBind( Column<?> column )
    {
        return newColumnBindExpression( AND_NOT, column );
    }
    
    

    public GroupExpression or()
    {
        return newChild( OR );
    }

    public Expression<Object> or( String expression )
    {
        return newStringExpression( OR, expression );
    }

    public <T> ColumnExpression<T> or( Column<T> column )
    {
    	return new ColumnExpression<T>( this, OR, column );
    }
    
    public <M extends Model> ModelExpression<M> or( OneToOne<M> oneToOne )
    {
    	return new ModelExpression<M>( this, OR, oneToOne, oneToOne.getJoinColumns() );
    }
    
    public <M extends Model> ModelExpression<M> or( ManyToOne<M> manyToOne )
    {
    	return new ModelExpression<M>( this, OR, manyToOne, manyToOne.getJoinColumns() );
    }

    public GroupExpression or( String expression, Object... values )
    {
        return newStringExpressionCustom( OR, expression, values );
    }

    public GroupExpression orKey( Key key )
    {
        return newKeyCondition( OR, key );
    }

    public GroupExpression orForeignKey( Key key )
    {
        return newForeignKeyCondition( OR, key );
    }

    public GroupExpression orKeyBind( Table table )
    {
        return newColumnsBindExpression( OR, table.getKeyColumns() );
    }
    
    public GroupExpression orForeignKeyBind( ForeignColumn<?> ... columns )
    {
        return newColumnsForeignBindExpression( OR, columns );
    }

    public GroupExpression orColumnBind( Column<?>... columns )
    {
        return newColumnsBindExpression( OR, columns );
    }
    
    public GroupExpression orBind( Column<?> column )
    {
        return newColumnBindExpression( OR, column );
    }
    

    public GroupExpression orNot()
    {
        return newChild( OR_NOT );
    }

    public Expression<Object> orNot( String expression )
    {
        return newStringExpression( OR_NOT, expression );
    }

    public <T> ColumnExpression<T> orNot( Column<T> column )
    {
    	return new ColumnExpression<T>( this, OR_NOT, column );
    }
    
    public <M extends Model> ModelExpression<M> orNot( OneToOne<M> oneToOne )
    {
    	return new ModelExpression<M>( this, OR_NOT, oneToOne, oneToOne.getJoinColumns() );
    }
    
    public <M extends Model> ModelExpression<M> orNot( ManyToOne<M> manyToOne )
    {
    	return new ModelExpression<M>( this, OR_NOT, manyToOne, manyToOne.getJoinColumns() );
    }

    public GroupExpression orNot( String expression, Object... values )
    {
        return newStringExpressionCustom( OR_NOT, expression, values );
    }

    public GroupExpression orNotKey( Key key )
    {
        return newKeyCondition( OR_NOT, key );
    }

    public GroupExpression orNotForeignKey( Key key )
    {
        return newForeignKeyCondition( OR_NOT, key );
    }

    public GroupExpression orNotKeyBind( Table table )
    {
        return newColumnsBindExpression( OR_NOT, table.getKeyColumns() );
    }
    
    public GroupExpression orNotForeignKeyBind( ForeignColumn<?> ... columns )
    {
        return newColumnsForeignBindExpression( OR_NOT, columns );
    }

    public GroupExpression orNotColumnBind( Column<?>... columns )
    {
        return newColumnsBindExpression( OR_NOT, columns );
    }

    public GroupExpression orNotBind( Column<?> column )
    {
        return newColumnBindExpression( OR_NOT, column );
    }

}
