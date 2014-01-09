package org.magnos.rekord.query;

import java.sql.SQLException;
import java.util.List;

import org.magnos.rekord.LoadProfile;
import org.magnos.rekord.Model;
import org.magnos.rekord.Table;
import org.magnos.rekord.field.Column;


public class Select
{

    public static <M extends Model> SelectQuery<M> from( Table table )
    {
        return new SelectQuery<M>( table );
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
