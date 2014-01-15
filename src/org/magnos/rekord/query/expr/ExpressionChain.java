
package org.magnos.rekord.query.expr;

import java.util.ArrayList;
import java.util.List;

import org.magnos.rekord.Key;
import org.magnos.rekord.Model;
import org.magnos.rekord.Table;
import org.magnos.rekord.field.Column;
import org.magnos.rekord.field.ForeignField;
import org.magnos.rekord.field.ManyToOne;
import org.magnos.rekord.field.OneToOne;
import org.magnos.rekord.query.QueryBuilder;
import org.magnos.rekord.query.SelectQuery;
import org.magnos.rekord.query.condition.Condition;
import org.magnos.rekord.query.condition.ConditionResolver;
import org.magnos.rekord.query.condition.Conditions;
import org.magnos.rekord.query.condition.GroupCondition;
import org.magnos.rekord.query.condition.PrependedCondition;

public class ExpressionChain<R extends ExpressionChain<R>> extends PrependedCondition
{

    public static final String AND = " AND ";
    public static final String AND_NOT = " AND NOT ";
    public static final String OR = " OR ";
    public static final String OR_NOT = " OR NOT ";

    public R parent;
    public final List<PrependedCondition> conditions;
    public final ConditionResolver<R> resolveAnd;
    public final ConditionResolver<R> resolveAndNot;
    public final ConditionResolver<R> resolveOr;
    public final ConditionResolver<R> resolveOrNot;

    public ExpressionChain()
    {
        this( null, AND );
    }

    public ExpressionChain( R parent, String prepend )
    {
        super( prepend, null );
        
        this.parent = parent;
        this.conditions = new ArrayList<PrependedCondition>();
        this.resolveAnd = new Resolver( AND );
        this.resolveAndNot = new Resolver( AND );
        this.resolveOr = new Resolver( OR );
        this.resolveOrNot = new Resolver( OR_NOT );
    }
    
    public boolean hasConditions()
    {
        return conditions.size() > 0;
    }

    public void toQuery( QueryBuilder query )
    {
        boolean parens = parent != null && this != parent && conditions.size() > 0;
        
        if (parens)
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
        
        if (parens)
        {
            query.append( ")" );
        }
    }


    public R end()
    {
        return parent;
    }
    
    
    public R where( Condition condition )
    {
        return add( AND, condition );
    }
    
    public R where( Condition ... conditions )
    {
        return add( AND, new GroupCondition( AND, conditions ) );
    }

    public GivenExpression<R> where( String expression )
    {
        return new GivenExpression<R>( resolveAnd, expression );
    }
    
    public <T> QueryExpression<R, T> where( SelectQuery<?> subquery )
    {
        return new QueryExpression<R, T>( resolveAnd, subquery );
    }

    public <T> ColumnExpression<R, T> where( Column<T> column )
    {
        return new ColumnExpression<R, T>( resolveAnd, column );
    }
    
    public StringColumnExpression<R> whereString( Column<String> column )
    {
        return new StringColumnExpression<R>( resolveAnd, column );
    }
    
    public <M extends Model> ModelExpression<R, M> where( OneToOne<M> oneToOne )
    {
    	return new ModelExpression<R, M>( resolveAnd, oneToOne, oneToOne.getJoinColumns() );
    }
    
    public <M extends Model> ModelExpression<R, M> where( ManyToOne<M> manyToOne )
    {
    	return new ModelExpression<R, M>( resolveAnd, manyToOne, manyToOne.getJoinColumns() );
    }

    public R where( String expression, Object... values )
    {
        return Conditions.newStringExpressionCustom( resolveAnd, expression, values );
    }

    public R whereKey( Key key )
    {
        return Conditions.newKeyCondition( resolveAnd, key );
    }

    public R whereForeignKey( Key key )
    {
        return Conditions.newForeignKeyCondition( resolveAnd, key );
    }

    public R whereKeyBind( Table table )
    {
        return Conditions.newColumnsBindExpression( resolveAnd, table.getKeyColumns() );
    }
    
    public R whereForeignKeyBind( ForeignField<?> ... columns )
    {
        return Conditions.newColumnsForeignBindExpression( resolveAnd, columns );
    }

    public R whereColumnBind( Column<?>... columns )
    {
        return Conditions.newColumnsBindExpression( resolveAnd, columns );
    }
    
    public R whereBind( Column<?> column )
    {
        return Conditions.newColumnBindExpression( resolveAnd, column );
    }
    
    

    public ExpressionChain<? extends R> and()
    {
        return newChild( AND );
    }

    public R and( Condition condition )
    {
        return add( AND, condition );
    }
    
    public R and( Condition ... conditions )
    {
        return add( AND, new GroupCondition( AND, conditions ) );
    }
    
    public GivenExpression<R> and( String expression )
    {
        return new GivenExpression<R>( resolveAnd, expression );
    }
    
    public <T> QueryExpression<R, T> and( SelectQuery<?> subquery )
    {
        return new QueryExpression<R, T>( resolveAnd, subquery );
    }

    public <T> ColumnExpression<R, T> and( Column<T> column )
    {
        return new ColumnExpression<R, T>( resolveAnd, column );
    }
    
    public StringColumnExpression<R> andString( Column<String> column )
    {
        return new StringColumnExpression<R>( resolveAnd, column );
    }
    
    public <M extends Model> ModelExpression<R, M> and( OneToOne<M> oneToOne )
    {
        return new ModelExpression<R, M>( resolveAnd, oneToOne, oneToOne.getJoinColumns() );
    }
    
    public <M extends Model> ModelExpression<R, M> and( ManyToOne<M> manyToOne )
    {
        return new ModelExpression<R, M>( resolveAnd, manyToOne, manyToOne.getJoinColumns() );
    }

    public R and( String expression, Object... values )
    {
        return Conditions.newStringExpressionCustom( resolveAnd, expression, values );
    }

    public R andKey( Key key )
    {
        return Conditions.newKeyCondition( resolveAnd, key );
    }

    public R andForeignKey( Key key )
    {
        return Conditions.newForeignKeyCondition( resolveAnd, key );
    }

    public R andKeyBind( Table table )
    {
        return Conditions.newColumnsBindExpression( resolveAnd, table.getKeyColumns() );
    }
    
    public R andForeignKeyBind( ForeignField<?> ... columns )
    {
        return Conditions.newColumnsForeignBindExpression( resolveAnd, columns );
    }

    public R andColumnBind( Column<?>... columns )
    {
        return Conditions.newColumnsBindExpression( resolveAnd, columns );
    }
    
    public R andBind( Column<?> column )
    {
        return Conditions.newColumnBindExpression( resolveAnd, column );
    }
    
    

    public ExpressionChain<R> andNot()
    {
        return newChild( AND_NOT );
    }
    
    public R andNot( Condition condition )
    {
        return add( AND, condition );
    }
    
    public R andNot( Condition ... conditions )
    {
        return add( AND_NOT, new GroupCondition( AND, conditions ) );
    }

    public GivenExpression<R> andNot( String expression )
    {
        return new GivenExpression<R>( resolveAndNot, expression );
    }
    
    public <T> QueryExpression<R, T> andNot( SelectQuery<?> subquery )
    {
        return new QueryExpression<R, T>( resolveAndNot, subquery );
    }

    public <T> ColumnExpression<R, T> andNot( Column<T> column )
    {
        return new ColumnExpression<R, T>( resolveAndNot, column );
    }
    
    public StringColumnExpression<R> andNotString( Column<String> column )
    {
        return new StringColumnExpression<R>( resolveAndNot, column );
    }
    
    public <M extends Model> ModelExpression<R, M> andNot( OneToOne<M> oneToOne )
    {
        return new ModelExpression<R, M>( resolveAndNot, oneToOne, oneToOne.getJoinColumns() );
    }
    
    public <M extends Model> ModelExpression<R, M> andNot( ManyToOne<M> manyToOne )
    {
        return new ModelExpression<R, M>( resolveAndNot, manyToOne, manyToOne.getJoinColumns() );
    }

    public R andNot( String expression, Object... values )
    {
        return Conditions.newStringExpressionCustom( resolveAndNot, expression, values );
    }

    public R andNotKey( Key key )
    {
        return Conditions.newKeyCondition( resolveAndNot, key );
    }

    public R andNotForeignKey( Key key )
    {
        return Conditions.newForeignKeyCondition( resolveAndNot, key );
    }

    public R andNotKeyBind( Table table )
    {
        return Conditions.newColumnsBindExpression( resolveAndNot, table.getKeyColumns() );
    }
    
    public R andNotForeignKeyBind( ForeignField<?> ... columns )
    {
        return Conditions.newColumnsForeignBindExpression( resolveAndNot, columns );
    }

    public R andNotColumnBind( Column<?>... columns )
    {
        return Conditions.newColumnsBindExpression( resolveAndNot, columns );
    }
    
    public R andNotBind( Column<?> column )
    {
        return Conditions.newColumnBindExpression( resolveAndNot, column );
    }
    
    

    public ExpressionChain<R> or()
    {
        return newChild( OR );
    }
    
    public R or( Condition condition )
    {
        return add( AND, condition );
    }
    
    public R or( Condition ... conditions )
    {
        return add( OR, new GroupCondition( AND, conditions ) );
    }

    public GivenExpression<R> or( String expression )
    {
        return new GivenExpression<R>( resolveOr, expression );
    }
    
    public <T> QueryExpression<R, T> or( SelectQuery<?> subquery )
    {
        return new QueryExpression<R, T>( resolveOr, subquery );
    }

    public <T> ColumnExpression<R, T> or( Column<T> column )
    {
        return new ColumnExpression<R, T>( resolveOr, column );
    }
    
    public StringColumnExpression<R> orString( Column<String> column )
    {
        return new StringColumnExpression<R>( resolveOr, column );
    }
    
    public <M extends Model> ModelExpression<R, M> or( OneToOne<M> oneToOne )
    {
        return new ModelExpression<R, M>( resolveOr, oneToOne, oneToOne.getJoinColumns() );
    }
    
    public <M extends Model> ModelExpression<R, M> or( ManyToOne<M> manyToOne )
    {
        return new ModelExpression<R, M>( resolveOr, manyToOne, manyToOne.getJoinColumns() );
    }

    public R or( String expression, Object... values )
    {
        return Conditions.newStringExpressionCustom( resolveOr, expression, values );
    }

    public R orKey( Key key )
    {
        return Conditions.newKeyCondition( resolveOr, key );
    }

    public R orForeignKey( Key key )
    {
        return Conditions.newForeignKeyCondition( resolveOr, key );
    }

    public R orKeyBind( Table table )
    {
        return Conditions.newColumnsBindExpression( resolveOr, table.getKeyColumns() );
    }
    
    public R orForeignKeyBind( ForeignField<?> ... columns )
    {
        return Conditions.newColumnsForeignBindExpression( resolveOr, columns );
    }

    public R orColumnBind( Column<?>... columns )
    {
        return Conditions.newColumnsBindExpression( resolveOr, columns );
    }
    
    public R orBind( Column<?> column )
    {
        return Conditions.newColumnBindExpression( resolveOr, column );
    }
    

    public ExpressionChain<R> orNot()
    {
        return newChild( OR_NOT );
    }
    
    public R orNot( Condition condition )
    {
        return add( AND, condition );
    }
    
    public R orNot( Condition ... conditions )
    {
        return add( OR_NOT, new GroupCondition( AND, conditions ) );
    }

    public GivenExpression<R> orNot( String expression )
    {
        return new GivenExpression<R>( resolveOrNot, expression );
    }
    
    public <T> QueryExpression<R, T> orNot( SelectQuery<?> subquery )
    {
        return new QueryExpression<R, T>( resolveOrNot, subquery );
    }

    public <T> ColumnExpression<R, T> orNot( Column<T> column )
    {
        return new ColumnExpression<R, T>( resolveOrNot, column );
    }
    
    public StringColumnExpression<R> orNotString( Column<String> column )
    {
        return new StringColumnExpression<R>( resolveOrNot, column );
    }
    
    public <M extends Model> ModelExpression<R, M> orNot( OneToOne<M> oneToOne )
    {
        return new ModelExpression<R, M>( resolveOrNot, oneToOne, oneToOne.getJoinColumns() );
    }
    
    public <M extends Model> ModelExpression<R, M> orNot( ManyToOne<M> manyToOne )
    {
        return new ModelExpression<R, M>( resolveOrNot, manyToOne, manyToOne.getJoinColumns() );
    }

    public R orNot( String expression, Object... values )
    {
        return Conditions.newStringExpressionCustom( resolveOrNot, expression, values );
    }

    public R orNotKey( Key key )
    {
        return Conditions.newKeyCondition( resolveOrNot, key );
    }

    public R orNotForeignKey( Key key )
    {
        return Conditions.newForeignKeyCondition( resolveOrNot, key );
    }

    public R orNotKeyBind( Table table )
    {
        return Conditions.newColumnsBindExpression( resolveOrNot, table.getKeyColumns() );
    }
    
    public R orNotForeignKeyBind( ForeignField<?> ... columns )
    {
        return Conditions.newColumnsForeignBindExpression( resolveOrNot, columns );
    }

    public R orNotColumnBind( Column<?>... columns )
    {
        return Conditions.newColumnsBindExpression( resolveOrNot, columns );
    }
    
    public R orNotBind( Column<?> column )
    {
        return Conditions.newColumnBindExpression( resolveOrNot, column );
    }

    
    protected R add( String prepend, Condition condition )
    {
        if (condition != null)
        {
            conditions.add( new PrependedCondition( prepend, condition ) );    
        }

        return (R) this;
    }

    @SuppressWarnings ("rawtypes")
    protected ExpressionChain newChild( String prepend )
    {
        ExpressionChain child = new ExpressionChain( this, prepend );

        conditions.add( child );

        return child;
    }
    
    protected class Resolver implements ConditionResolver<R>
    {
        protected final String prepend;
        
        protected Resolver(String prepend)
        {
            this.prepend = prepend; 
        }
        
        public R resolve( Condition condition )
        {
            return add( prepend, condition );
        }
    }

    @SuppressWarnings ("rawtypes")
    public static ExpressionChain detached()
    {
        ExpressionChain ge = new ExpressionChain( null, AND );
        ge.parent = ge;
        return ge;
    }

}
