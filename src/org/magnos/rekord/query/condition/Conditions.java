package org.magnos.rekord.query.condition;

import org.magnos.rekord.Key;
import org.magnos.rekord.Model;
import org.magnos.rekord.Table;
import org.magnos.rekord.field.Column;
import org.magnos.rekord.field.ForeignField;
import org.magnos.rekord.field.ManyToOne;
import org.magnos.rekord.field.OneToOne;
import org.magnos.rekord.query.Operator;
import org.magnos.rekord.query.SelectQuery;
import org.magnos.rekord.query.expr.ColumnExpression;
import org.magnos.rekord.query.expr.ExpressionChain;
import org.magnos.rekord.query.expr.GivenExpression;
import org.magnos.rekord.query.expr.ModelExpression;
import org.magnos.rekord.query.expr.QueryExpression;
import org.magnos.rekord.query.expr.StringColumnExpression;


public class Conditions
{

    public static ConditionResolver<Condition> RESOLVER = new ConditionResolver<Condition>() 
    {
        @Override
        public Condition resolve( Condition condition )
        {
            return condition;
        }
    };
    
    public static GivenExpression<Condition> is( String expression )
    {
        return new GivenExpression<Condition>( RESOLVER, expression );
    }
    
    public static <T> QueryExpression<Condition, T> is( SelectQuery<?> subquery )
    {
        return new QueryExpression<Condition, T>( RESOLVER, subquery );
    }

    public static <T> ColumnExpression<Condition, T> is( Column<T> column )
    {
        return new ColumnExpression<Condition, T>( RESOLVER, column );
    }
    
    public static StringColumnExpression<Condition> isString( Column<String> column )
    {
        return new StringColumnExpression<Condition>( RESOLVER, column );
    }
    
    public static <M extends Model> ModelExpression<Condition, M> is( OneToOne<M> oneToOne )
    {
        return new ModelExpression<Condition, M>( RESOLVER, oneToOne, oneToOne.getJoinColumns() );
    }
    
    public static <M extends Model> ModelExpression<Condition, M> is( ManyToOne<M> manyToOne )
    {
        return new ModelExpression<Condition, M>( RESOLVER, manyToOne, manyToOne.getJoinColumns() );
    }

    public static Condition is( String expression, Object... values )
    {
        return newStringExpressionCustom( RESOLVER, expression, values );
    }

    public static Condition isKey( Key key )
    {
        return newKeyCondition( RESOLVER, key );
    }

    public static Condition isForeignKey( Key key )
    {
        return newForeignKeyCondition( RESOLVER, key );
    }

    public static Condition isKeyBind( Table table )
    {
        return newColumnsBindExpression( RESOLVER, table.getKeyColumns() );
    }
    
    public static Condition isKeyBind( ForeignField<?> ... columns )
    {
        return newColumnsForeignBindExpression( RESOLVER, columns );
    }

    public static Condition isColumnBind( Column<?>... columns )
    {
        return newColumnsBindExpression( RESOLVER, columns );
    }
    
    public static Condition isBind( Column<?> column )
    {
        return newColumnBindExpression( RESOLVER, column );
    }
    
    public static Condition and( Condition ... conditions )
    {
        return new GroupCondition( ExpressionChain.AND, conditions );
    }
    
    public static Condition andNot( Condition ... conditions )
    {
        return new GroupCondition( ExpressionChain.AND_NOT, conditions );
    }
    
    public static Condition or( Condition ... conditions )
    {
        return new GroupCondition( ExpressionChain.OR, conditions );
    }
    
    public static Condition orNot( Condition ... conditions )
    {
        return new GroupCondition( ExpressionChain.OR_NOT, conditions );
    }
    

    public static <R> R newStringExpressionCustom( ConditionResolver<R> resolver, String expression, Object[] values )
    {
        return resolver.resolve( new CustomCondition( expression, values ) );    
    }

    @SuppressWarnings ("rawtypes" )
    public static <R> R newKeyCondition( ConditionResolver<R> resolver, Key key )
    {
        final int keySize = key.size();
        Condition[] conditions = new Condition[keySize];

        for (int i = 0; i < keySize; i++)
        {
            conditions[i] = new OperatorCondition( key.fieldAt( i ), Operator.EQ, key.valueAt( i ) );
        }

        return resolver.resolve( new GroupCondition( ExpressionChain.AND, conditions ) );
    }

    @SuppressWarnings ("rawtypes" )
    public static <R> R newForeignKeyCondition( ConditionResolver<R> resolver, Key key )
    {
        final int keySize = key.size();
        Condition[] conditions = new Condition[keySize];

        for (int i = 0; i < keySize; i++)
        {
        	ForeignField<?> foreign = (ForeignField<?>)key.fieldAt( i );

            conditions[i] = new OperatorCondition( foreign.getForeignColumn(), Operator.EQ, key.valueAt( i ) );
        }

        return resolver.resolve( new GroupCondition( ExpressionChain.AND, conditions ) );
    }

    public static <R> R newColumnsBindExpression( ConditionResolver<R> resolver, Column<?>... columns )
    {
        Condition[] conditions = new Condition[columns.length];

        for (int i = 0; i < columns.length; i++)
        {
            conditions[i] = OperatorCondition.forColumnBind( columns[i], Operator.EQ );
        }

        return resolver.resolve( new GroupCondition( ExpressionChain.AND, conditions ) );
    }

    public static <R> R newColumnsForeignBindExpression( ConditionResolver<R> resolver, ForeignField<?>... columns )
    {
        Condition[] conditions = new Condition[columns.length];

        for (int i = 0; i < columns.length; i++)
        {
            conditions[i] = OperatorCondition.forForeignColumnBind( columns[i], Operator.EQ );
        }

        return resolver.resolve( new GroupCondition( ExpressionChain.AND, conditions ) );
    }

    public static <R> R newColumnBindExpression( ConditionResolver<R> resolver, Column<?> column )
    {
        return resolver.resolve( OperatorCondition.forColumnBind( column, Operator.EQ ) );
    }
    
}
