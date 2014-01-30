package org.magnos.rekord.query;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.magnos.rekord.Field;
import org.magnos.rekord.LoadProfile;
import org.magnos.rekord.Model;
import org.magnos.rekord.Table;
import org.magnos.rekord.field.Column;
import org.magnos.rekord.query.expr.ExpressionChain;


public class Select
{

    public static <M extends Model> SelectQuery<M> from( Table table )
    {
        return new SelectQuery<M>( table );
    }
    
    @SuppressWarnings ("rawtypes" )
    public static <M extends Model> SelectQuery<M> build( Table table, LoadProfile load )
    {
        Map<Table, ExpressionChain<?>> chainMap = new HashMap<Table, ExpressionChain<?>>();
        Map<Table, TableAlias> aliasMap = new HashMap<Table, TableAlias>();
        
        SelectQuery<M> sq = new SelectQuery<M>( table );
        chainMap.put( table, sq );
        aliasMap.put( table, sq.getTableAlias() );
        
        Field<?>[] fields = load.getFields();

        for (Field<?> f : fields)
        {
            TableAlias alias = aliasMap.get( f.getTable() );
            
            if (alias == null)
            {
                alias = sq.alias( f.getTable() );
                
                ExpressionChain<?> chain = sq.join( Join.INNER, alias );
                
                Column[] key0 = f.getTable().getKeyColumns();
                Column[] key1 = table.getKeyColumns();
                
                for (int i = 0; i < key0.length; i++) 
                {
                    chain.where( key0[i] ).eq( sq.getTableAlias().alias( key1[i] ) );
                }
                
                chainMap.put( f.getTable(), chain );
                aliasMap.put( f.getTable(), alias );
            }
            
            if (f instanceof Column)
            {
                sq.select( alias.alias( (Column<?>)f ) );
            }
            else
            {
                sq.selectFields.add( f );
            }
        }
        
        return sq;
    }
    
    public static <M extends Model> SelectQuery<M> find( M model )
    {
        return new SelectQuery<M>( model );
    }
    
    public static <M extends Model> List<M> all( Table table ) throws SQLException 
    {
        return new SelectQuery<M>( table ).create().list();
    }
    
    public static <M extends Model, T> List<T> all( Table table, Column<T> column) throws SQLException 
    {
        return new SelectQuery<M>( table ).create().list( column );
    }
    
    public static <M extends Model> List<M> all( Table table, LoadProfile load ) throws SQLException 
    {
        return new SelectQuery<M>( table ).select( load ).create().list();
    }
    
    public static <M extends Model, T> List<M> by( Table table, Column<T> column, T value ) throws SQLException 
    {
        return new SelectQuery<M>( table ).where( column ).eq( value ).create().list();
    }
    
    public static <M extends Model, T> List<M> by( Table table, LoadProfile load, Column<T> column, T value ) throws SQLException 
    {
        return new SelectQuery<M>( table ).select( load ).where( column ).eq( value ).create().list();
    }
    
    public static <M extends Model, T> M byUnique( Table table, Column<T> column, T value ) throws SQLException 
    {
        return new SelectQuery<M>( table ).where( column ).eq( value ).create().first();
    }
    
    public static <M extends Model, T> M byUnique( Table table, LoadProfile load, Column<T> column, T value ) throws SQLException 
    {
        return new SelectQuery<M>( table ).select( load ).where( column ).eq( value ).create().first();
    }
    
}
