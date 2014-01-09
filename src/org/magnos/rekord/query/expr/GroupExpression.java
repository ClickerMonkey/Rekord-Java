
package org.magnos.rekord.query.expr;

import java.util.ArrayList;
import java.util.List;

import org.magnos.rekord.Key;
import org.magnos.rekord.Model;
import org.magnos.rekord.Table;
import org.magnos.rekord.field.Column;
import org.magnos.rekord.field.ForeignColumn;
import org.magnos.rekord.field.ManyToOne;
import org.magnos.rekord.field.OneToOne;
import org.magnos.rekord.query.Operator;
import org.magnos.rekord.query.QueryBuilder;
import org.magnos.rekord.query.SelectQuery;
import org.magnos.rekord.query.condition.Condition;
import org.magnos.rekord.query.condition.CustomCondition;
import org.magnos.rekord.query.condition.GroupCondition;
import org.magnos.rekord.query.condition.OperatorCondition;
import org.magnos.rekord.query.condition.PrependedCondition;

@SuppressWarnings ({ "rawtypes" } )
public class GroupExpression<R> extends PrependedCondition
{

    public static final String AND = " AND ";
    public static final String AND_NOT = " AND NOT ";
    public static final String OR = " OR ";
    public static final String OR_NOT = " OR NOT ";

    public R returning;
    public final GroupExpression<R> parent;
    public final List<PrependedCondition> conditions;

    public GroupExpression()
    {
        this( null, null, AND );
    }

    public GroupExpression( R returning, GroupExpression<R> parent, String prepend )
    {
        super( prepend, null );
        
        this.returning = returning;
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
            if (parent != null)
            {
                query.append( "(" );
            }
            
            for (int i = 0; i < conditions.size(); i++)
            {
                PrependedCondition pc = conditions.get( i );
                
                if (i > 0)
                {
                    query.append( pc.prepend );
                }
                
                pc.toQuery( query );
            }
            
            if (parent != null)
            {
                query.append( ")" );
            }
        }
    }

    protected R add( String prepend, Condition condition )
    {
        conditions.add( new PrependedCondition( prepend, condition ) );

        return returning;
    }

    protected GroupExpression<R> newChild( String prepend )
    {
        GroupExpression<R> child = new GroupExpression<R>( returning, this, prepend );

        conditions.add( child );

        return child;
    }

    protected Expression<R, Object> newStringExpression( String prepend, String expression )
    {
        return new GivenExpression<R>( returning, this, prepend, expression );
    }

    protected R newStringExpressionCustom( String prepend, String expression, Object[] values )
    {
        return add( prepend, new CustomCondition( expression, values ) );
    }

    protected R newKeyCondition( String prepend, Key key )
    {
        final int keySize = key.size();
        Condition[] conditions = new Condition[keySize];

        for (int i = 0; i < keySize; i++)
        {
            conditions[i] = new OperatorCondition( key.fieldAt( i ), Operator.EQ, key.valueAt( i ) );
        }

        return add( prepend, new GroupCondition( AND, conditions ) );
    }

    protected R newForeignKeyCondition( String prepend, Key key )
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

    protected R newColumnsBindExpression( String prepend, Column<?>... columns )
    {
        Condition[] conditions = new Condition[columns.length];

        for (int i = 0; i < columns.length; i++)
        {
            conditions[i] = OperatorCondition.forColumnBind( columns[i], Operator.EQ );
        }

        return add( prepend, new GroupCondition( AND, conditions ) );
    }

    protected R newColumnsForeignBindExpression( String prepend, ForeignColumn<?>... columns )
    {
        Condition[] conditions = new Condition[columns.length];

        for (int i = 0; i < columns.length; i++)
        {
            conditions[i] = OperatorCondition.forForeignColumnBind( columns[i], Operator.EQ );
        }

        return add( prepend, new GroupCondition( AND, conditions ) );
    }

    protected R newColumnBindExpression( String prepend, Column<?> column )
    {
        return add( prepend, OperatorCondition.forColumnBind( column, Operator.EQ ) );
    }


    public R end()
    {
        return returning;
    }
    
    
    public R where( Condition condition )
    {
        return add( AND, condition );
    }

    public Expression<R, Object> where( String expression )
    {
        return newStringExpression( AND, expression );
    }
    
    public <T> QueryExpression<R, T> where( SelectQuery<?> subquery )
    {
        return new QueryExpression<R, T>( returning, this, AND, subquery );
    }

    public <T> ColumnExpression<R, T> where( Column<T> column )
    {
    	return new ColumnExpression<R, T>( returning, this, AND, column );
    }
    
    public StringColumnExpression<R> whereString( Column<String> column )
    {
        return new StringColumnExpression<R>( returning, this, AND, column );
    }
    
    public <M extends Model> ModelExpression<R, M> where( OneToOne<M> oneToOne )
    {
    	return new ModelExpression<R, M>( returning, this, AND, oneToOne, oneToOne.getJoinColumns() );
    }
    
    public <M extends Model> ModelExpression<R, M> where( ManyToOne<M> manyToOne )
    {
    	return new ModelExpression<R, M>( returning, this, AND, manyToOne, manyToOne.getJoinColumns() );
    }

    public R where( String expression, Object... values )
    {
        return newStringExpressionCustom( AND, expression, values );
    }

    public R whereKey( Key key )
    {
        return newKeyCondition( AND, key );
    }

    public R whereForeignKey( Key key )
    {
        return newForeignKeyCondition( AND, key );
    }

    public R whereKeyBind( Table table )
    {
        return newColumnsBindExpression( AND, table.getKeyColumns() );
    }
    
    public R whereForeignKeyBind( ForeignColumn<?> ... columns )
    {
        return newColumnsForeignBindExpression( AND, columns );
    }

    public R whereColumnBind( Column<?>... columns )
    {
        return newColumnsBindExpression( AND, columns );
    }
    
    public R whereBind( Column<?> column )
    {
        return newColumnBindExpression( AND, column );
    }
    
    

    public GroupExpression<R> and()
    {
        return newChild( AND );
    }
    
    public R and( Condition condition )
    {
        return add( AND, condition );
    }

    public Expression<R, Object> and( String expression )
    {
        return newStringExpression( AND, expression );
    }
    
    public <T> QueryExpression<R, T> and( SelectQuery<?> subquery )
    {
        return new QueryExpression<R, T>( returning, this, AND, subquery );
    }

    public <T> ColumnExpression<R, T> and( Column<T> column )
    {
    	return new ColumnExpression<R, T>( returning, this, AND, column );
    }
    
    public StringColumnExpression<R> andString( Column<String> column )
    {
        return new StringColumnExpression<R>( returning, this, AND, column );
    }
    
    public <M extends Model> ModelExpression<R, M> and( OneToOne<M> oneToOne )
    {
    	return new ModelExpression<R, M>( returning, this, AND, oneToOne, oneToOne.getJoinColumns() );
    }
    
    public <M extends Model> ModelExpression<R, M> and( ManyToOne<M> manyToOne )
    {
    	return new ModelExpression<R, M>( returning, this, AND, manyToOne, manyToOne.getJoinColumns() );
    }

    public R and( String expression, Object... values )
    {
        return newStringExpressionCustom( AND, expression, values );
    }

    public R andKey( Key key )
    {
        return newKeyCondition( AND, key );
    }

    public R andForeignKey( Key key )
    {
        return newForeignKeyCondition( AND, key );
    }

    public R andKeyBind( Table table )
    {
        return newColumnsBindExpression( AND, table.getKeyColumns() );
    }
    
    public R andForeignKeyBind( ForeignColumn<?> ... columns )
    {
        return newColumnsForeignBindExpression( AND, columns );
    }

    public R andColumnBind( Column<?>... columns )
    {
        return newColumnsBindExpression( AND, columns );
    }
    
    public R andBind( Column<?> column )
    {
        return newColumnBindExpression( AND, column );
    }
    
    

    public GroupExpression<R> andNot()
    {
        return newChild( AND_NOT );
    }
    
    public R andNot( Condition condition )
    {
        return add( AND_NOT, condition );
    }

    public Expression<R, Object> andNot( String expression )
    {
        return newStringExpression( AND_NOT, expression );
    }
    
    public <T> QueryExpression<R, T> andNot( SelectQuery<?> subquery )
    {
        return new QueryExpression<R, T>( returning, this, AND_NOT, subquery );
    }

    public <T> ColumnExpression<R, T> andNot( Column<T> column )
    {
    	return new ColumnExpression<R, T>( returning, this, AND_NOT, column );
    }
    
    public StringColumnExpression<R> andNotString( Column<String> column )
    {
        return new StringColumnExpression<R>( returning, this, AND_NOT, column );
    }
    
    public <M extends Model> ModelExpression<R, M> andNot( OneToOne<M> oneToOne )
    {
    	return new ModelExpression<R, M>( returning, this, AND_NOT, oneToOne, oneToOne.getJoinColumns() );
    }
    
    public <M extends Model> ModelExpression<R, M> andNot( ManyToOne<M> manyToOne )
    {
    	return new ModelExpression<R, M>( returning, this, AND_NOT, manyToOne, manyToOne.getJoinColumns() );
    }

    public R andNot( String expression, Object... values )
    {
        return newStringExpressionCustom( AND_NOT, expression, values );
    }

    public R andNotKey( Key key )
    {
        return newKeyCondition( AND_NOT, key );
    }

    public R andNotForeignKey( Key key )
    {
        return newForeignKeyCondition( AND_NOT, key );
    }

    public R andNotKeyBind( Table table )
    {
        return newColumnsBindExpression( AND_NOT, table.getKeyColumns() );
    }
    
    public R andNotForeignKeyBind( ForeignColumn<?> ... columns )
    {
        return newColumnsForeignBindExpression( AND_NOT, columns );
    }

    public R andNotColumnBind( Column<?>... columns )
    {
        return newColumnsBindExpression( AND_NOT, columns );
    }
    
    public R andNotBind( Column<?> column )
    {
        return newColumnBindExpression( AND_NOT, column );
    }
    
    

    public GroupExpression<R> or()
    {
        return newChild( OR );
    }
    
    public R or( Condition condition )
    {
        return add( OR, condition );
    }

    public Expression<R, Object> or( String expression )
    {
        return newStringExpression( OR, expression );
    }
    
    public <T> QueryExpression<R, T> or( SelectQuery<?> subquery )
    {
        return new QueryExpression<R, T>( returning, this, OR, subquery );
    }

    public <T> ColumnExpression<R, T> or( Column<T> column )
    {
    	return new ColumnExpression<R, T>( returning, this, OR, column );
    }
    
    public StringColumnExpression<R> orString( Column<String> column )
    {
        return new StringColumnExpression<R>( returning, this, OR, column );
    }
    
    public <M extends Model> ModelExpression<R, M> or( OneToOne<M> oneToOne )
    {
    	return new ModelExpression<R, M>( returning, this, OR, oneToOne, oneToOne.getJoinColumns() );
    }
    
    public <M extends Model> ModelExpression<R, M> or( ManyToOne<M> manyToOne )
    {
    	return new ModelExpression<R, M>( returning, this, OR, manyToOne, manyToOne.getJoinColumns() );
    }

    public R or( String expression, Object... values )
    {
        return newStringExpressionCustom( OR, expression, values );
    }

    public R orKey( Key key )
    {
        return newKeyCondition( OR, key );
    }

    public R orForeignKey( Key key )
    {
        return newForeignKeyCondition( OR, key );
    }

    public R orKeyBind( Table table )
    {
        return newColumnsBindExpression( OR, table.getKeyColumns() );
    }
    
    public R orForeignKeyBind( ForeignColumn<?> ... columns )
    {
        return newColumnsForeignBindExpression( OR, columns );
    }

    public R orColumnBind( Column<?>... columns )
    {
        return newColumnsBindExpression( OR, columns );
    }
    
    public R orBind( Column<?> column )
    {
        return newColumnBindExpression( OR, column );
    }
    

    public GroupExpression<R> orNot()
    {
        return newChild( OR_NOT );
    }
    
    public R orNot( Condition condition )
    {
        return add( OR_NOT, condition );
    }

    public Expression<R, Object> orNot( String expression )
    {
        return newStringExpression( OR_NOT, expression );
    }
    
    public <T> QueryExpression<R, T> orNot( SelectQuery<?> subquery )
    {
        return new QueryExpression<R, T>( returning, this, OR_NOT, subquery );
    }

    public <T> ColumnExpression<R, T> orNot( Column<T> column )
    {
    	return new ColumnExpression<R, T>( returning, this, OR_NOT, column );
    }
    
    public StringColumnExpression<R> orNotString( Column<String> column )
    {
        return new StringColumnExpression<R>( returning, this, OR_NOT, column );
    }
    
    public <M extends Model> ModelExpression<R, M> orNot( OneToOne<M> oneToOne )
    {
    	return new ModelExpression<R, M>( returning, this, OR_NOT, oneToOne, oneToOne.getJoinColumns() );
    }
    
    public <M extends Model> ModelExpression<R, M> orNot( ManyToOne<M> manyToOne )
    {
    	return new ModelExpression<R, M>( returning, this, OR_NOT, manyToOne, manyToOne.getJoinColumns() );
    }

    public R orNot( String expression, Object... values )
    {
        return newStringExpressionCustom( OR_NOT, expression, values );
    }

    public R orNotKey( Key key )
    {
        return newKeyCondition( OR_NOT, key );
    }

    public R orNotForeignKey( Key key )
    {
        return newForeignKeyCondition( OR_NOT, key );
    }

    public R orNotKeyBind( Table table )
    {
        return newColumnsBindExpression( OR_NOT, table.getKeyColumns() );
    }
    
    public R orNotForeignKeyBind( ForeignColumn<?> ... columns )
    {
        return newColumnsForeignBindExpression( OR_NOT, columns );
    }

    public R orNotColumnBind( Column<?>... columns )
    {
        return newColumnsBindExpression( OR_NOT, columns );
    }

    public R orNotBind( Column<?> column )
    {
        return newColumnBindExpression( OR_NOT, column );
    }
    
    public static <P extends GroupExpression<P>> GroupExpression<P> detached()
    {
        GroupExpression<P> ge = new GroupExpression<P>( null, null, AND );
        ge.returning = (P)ge;
        return ge;
    }

}
