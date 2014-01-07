
package org.magnos.rekord.query.expr;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.magnos.rekord.Field;
import org.magnos.rekord.Key;
import org.magnos.rekord.Operator;
import org.magnos.rekord.Table;
import org.magnos.rekord.field.Column;
import org.magnos.rekord.field.ForeignColumn;
import org.magnos.rekord.query.condition.Condition;
import org.magnos.rekord.query.condition.CustomCondition;
import org.magnos.rekord.query.condition.GroupCondition;
import org.magnos.rekord.query.condition.LiteralCondition;
import org.magnos.rekord.query.condition.OperatorCondition;
import org.magnos.rekord.query.condition.PrependedCondition;

@SuppressWarnings ({ "cast", "rawtypes" } )
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

    public void toQuery( StringBuilder query )
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
            query.append( "(" );
            
            for (int i = 0; i < conditions.size(); i++)
            {
                PrependedCondition pc = conditions.get( i );
                
                if (i > 0)
                {
                    query.append( pc.prepend );
                }
                
                pc.toQuery( query );
            }
            
            query.append( ")" );
        }
    }

    public int toPreparedstatement( PreparedStatement stmt, int paramIndex ) throws SQLException
    {
        for (PrependedCondition pc : conditions)
        {
            paramIndex = pc.toPreparedstatement( stmt, paramIndex );
        }

        return paramIndex;
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

    protected <T> Expression<T> newFieldExpression( String prepend, Field<T> field )
    {
        if (field instanceof Column)
        {
            return new ColumnExpression<T>( this, prepend, (Column<T>)field );
        }
        else if (field.is( Field.MODEL ))
        {
            return new ModelExpression( this, prepend, (Column<T>)field );
        }
        else if (field.is( Field.MODEL_LIST ))
        {
            throw new UnsupportedOperationException();
        }

        throw new UnsupportedOperationException();
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
        return new LiteralCondition( getColumnBind( c ) );
    }

    protected Condition getColumnForeignBindCondition( ForeignColumn<?> c )
    {
        return new LiteralCondition( getColumnForeignBind( c ) );
    }

    protected String getColumnBind( Column<?> c )
    {
        return c.getQuotedName() + " = ?" + c.getName();
    }

    protected String getColumnForeignBind( ForeignColumn<?> c )
    {
        return c.getQuotedName() + " = ?" + c.getForeignColumn().getName();
    }
    
    

    public GroupExpression end()
    {
        return parent;
    }
    

    public Expression<Object> where( String expression )
    {
        return newStringExpression( AND, expression );
    }

    public <T> Expression<T> where( Field<T> field )
    {
        return newFieldExpression( AND, field );
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

    public <T> Expression<T> and( Field<T> field )
    {
        return newFieldExpression( AND, field );
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

    public <T> Expression<T> andNot( Field<T> field )
    {
        return newFieldExpression( AND_NOT, field );
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

    public <T> Expression<T> or( Field<T> field )
    {
        return newFieldExpression( OR, field );
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

    public <T> Expression<T> orNot( Field<T> field )
    {
        return newFieldExpression( OR_NOT, field );
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
