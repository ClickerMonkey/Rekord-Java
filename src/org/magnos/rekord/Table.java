
package org.magnos.rekord;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.magnos.rekord.field.Column;
import org.magnos.rekord.field.JoinField;
import org.magnos.rekord.key.MultiModelKey;
import org.magnos.rekord.key.MultiValueKey;
import org.magnos.rekord.key.SingleModelKey;
import org.magnos.rekord.key.SingleValueKey;
import org.magnos.rekord.query.NativeQuery;
import org.magnos.rekord.query.QueryTemplate;
import org.magnos.rekord.query.model.ModelDeleteQuery;
import org.magnos.rekord.query.model.ModelInsertQuery;
import org.magnos.rekord.query.model.ModelQuery;
import org.magnos.rekord.query.model.ModelUpdateQuery;
import org.magnos.rekord.util.ArrayUtil;
import org.magnos.rekord.util.SqlUtil;


public class Table
{

    public static final int NONE = 0;
    public static final int RELATIONSHIP_TABLE = 1 << 0;
    public static final int SUB_TABLE = 1 << 1;
    public static final int COMPLETELY_GENERATED = 1 << 2;
    public static final int DYNAMICALLY_INSERTED = 1 << 3;
    public static final int DYNAMICALLY_UPDATED = 1 << 4;
    public static final int TRANSACTION_CACHED = 1 << 5;
    public static final int APPLICATION_CACHED = 1 << 6;

    private static final Field<?>[] NO_FIELDS = {};

    protected final int index;
    protected final String table;
    protected final String quotedName;
    protected final int flags;
    protected Factory<? extends Model> factory;
    protected Column<?>[] keyColumns = {};          // key columns specified for this table
    protected Field<?>[] fields = {};               // all fields (columns + join fields)
    protected Column<?>[] columns = {};             // all columns
    protected JoinField<?>[] joinFields = {};       // all join fields
    protected Field<?>[] inheritedFields = {};      // all fields inherited from parent table
    protected Field<?>[] givenFields = {};          // all fields defined specifically on this table
    protected ModelQuery insert;
    protected ModelQuery update;
    protected ModelQuery delete;
    protected Map<String, Field<?>> fieldMap;
    protected LoadProfile[] loadProfiles;
    protected Map<String, LoadProfile> loadProfileMap;
    protected SaveProfile[] saveProfiles;
    protected Map<String, SaveProfile> saveProfileMap;
    protected HistoryTable history;
    protected LoadProfile loadProfileAll;
    protected LoadProfile loadProfileId;
    protected Map<String, QueryTemplate<?>> queries;
    protected Listener<Model>[][] listeners;

    protected Column<?>[] lastModifiedColumns;
    
    protected Table parentTable;
    protected Object discriminatorValue;
    
    protected Column<?> discriminatorColumn;
    protected Map<Object, Table> childTables;
    
    protected ModelResolver resolver;
    
    public Table( String table, int flags, Column<?>... keyColumns )
    {
        this( table, flags, null, keyColumns, NO_FIELDS );
    }

    public Table( String table, int flags, Table extension )
    {
        this( table, flags, extension, extension.keyColumns, extension.fields );
    }

    private Table( String table, int flags, Table extension, Column<?>[] id, Field<?>[] existingFields )
    {
        this.table = table;
        this.quotedName = SqlUtil.namify( table );
        this.parentTable = extension;
        this.flags = flags;
        this.index = Rekord.newTable( this );
        this.fieldMap = new HashMap<String, Field<?>>();
        this.loadProfileMap = new HashMap<String, LoadProfile>();
        this.saveProfileMap = new HashMap<String, SaveProfile>();
        this.queries = new HashMap<String, QueryTemplate<?>>();
        this.keyColumns = id;
        this.fields = existingFields;
        this.listeners = new Listener[ ListenerEvent.values().length ][];
        this.mapFields( existingFields );
    }

    public void setFields( Field<?>... newFields )
    {
        int fieldCount = fields.length;

        givenFields = newFields;
        inheritedFields = fields;
        
        fields = ArrayUtil.join( Field.class, fields, newFields );
        registerFields( fieldCount );
        mapFields( newFields );

        insert = new ModelInsertQuery( this, is( DYNAMICALLY_INSERTED ) );
        update = new ModelUpdateQuery( this, is( DYNAMICALLY_UPDATED ) );
        delete = new ModelDeleteQuery( this );
        
        List<Column<?>> columnList = new ArrayList<Column<?>>();
        List<JoinField<?>> joinFieldList = new ArrayList<JoinField<?>>();

        for (Field<?> f : fields)
        {
            if (f instanceof Column)
            {
                columnList.add( (Column<?>)f );
            }
            if (f instanceof JoinField)
            {
                joinFieldList.add( (JoinField<?>)f );
            }
        }
        
        columns = columnList.toArray( new Column[ columnList.size() ] );
        joinFields = joinFieldList.toArray( new JoinField[ joinFieldList.size() ] );
    }
    
    public void setAsParent(Column<?> column)
    {
    	discriminatorColumn = column;
    	childTables = new HashMap<Object, Table>();
    }

    public void setAsChild(Object value)
    {
    	discriminatorValue = value;
    	parentTable.childTables.put( value, this );
    }
    
    public void setLoadProfiles( LoadProfile... newLoadProfiles )
    {
        loadProfiles = newLoadProfiles;

        for (LoadProfile v : loadProfiles)
        {
            loadProfileMap.put( v.getName(), v );
        }

        loadProfileAll = loadProfileMap.get( "all" );
        loadProfileId = loadProfileMap.get( "id" );
    }

    public void setSaveProfiles( SaveProfile... newSaveProfiles )
    {
        saveProfiles = newSaveProfiles;

        for (SaveProfile v : saveProfiles)
        {
            saveProfileMap.put( v.getName(), v );
        }
    }
    
    public void addListener(Listener<Model> listener, ListenerEvent e)
    {
    	int i = e.ordinal();
    	listeners[i] = ArrayUtil.add( listener, listeners[i] );
    }
    
    public void notifyListeners(Model model, ListenerEvent e) throws SQLException
    {
    	int i = e.ordinal();
    	
    	Listener<Model>[] lm = listeners[i];
    	
    	if (lm != null && lm.length > 0)
    	{
    	    for (Listener<Model> l : lm)
    	    {
    	        l.onEvent( model, e );
    	    }
    	}
    }

    private void registerFields( int start )
    {
        while (start < fields.length)
        {
            Field<?> f = fields[start];
            f.setIndex( start );
            f.setTable( this );
            start++;
        }
    }

    private void mapFields( Field<?>[] fields )
    {
        if (fields != null)
        {
            for (Field<?> f : fields)
            {
                fieldMap.put( f.getName(), f );
            }
        }
    }

    public Column<?>[] getLastModifiedColumns()
	{
		return lastModifiedColumns;
	}
    
    public boolean hasLastModifiedColumns()
    {
        return lastModifiedColumns != null && lastModifiedColumns.length > 0;
    }

	public void setLastModifiedColumns( Column<?>[] lastModifiedColumns )
	{
		this.lastModifiedColumns = lastModifiedColumns;
	}

	public Table addNativeQuery( String name, String query, String loadProfileName )
    {
    	queries.put( name, NativeQuery.parse( this, query, getLoadProfile( loadProfileName ) ) );
    	
    	return this;
    }
    
    public Table addQuery( String name, QueryTemplate<Model> queryTemplate )
    {
    	queries.put( name, queryTemplate );
    	
    	return this;
    }
    
    public ModelResolver getResolver()
    {
        return resolver;
    }
    
    public void setResolver( ModelResolver resolver )
    {
        this.resolver = resolver;
    }

    public Value<?>[] newValues( Model model )
    {
        final int valueCount = fields.length;
        Value<?>[] values = new Value[valueCount];

        for (int i = 0; i < valueCount; i++)
        {
            values[i] = fields[i].newValue( model );
        }

        return values;
    }

    public Key keyForModel( Model model )
    {
        return getKeySize() == 1 ? new SingleModelKey( model ) : new MultiModelKey( model );
    }

    public Key keyForCaching()
    {
        return getKeySize() == 1 ? new SingleValueKey( this ) : new MultiValueKey( this );
    }

    public Key keyFromResults( ResultSet results ) throws SQLException
    {
        Key key = keyForCaching();
        key.fromResultSet( results );
        return key;
    }

    public Key keyForFields( Model model, Field<?>... fields )
    {
        return fields.length == 1 ? new SingleModelKey( model, fields[0] ) : new MultiModelKey( model, fields );
    }
    
    public Table getParentTable()
    {
        return parentTable;
    }

    public Object getDiscriminatorValue()
    {
        return discriminatorValue;
    }
    
    public Column<?> getDiscriminatorColumn()
    {
        return discriminatorColumn;
    }
    
    public Map<Object, Table> getChildTables()
    {
        return childTables;
    }

    public int getIndex()
    {
        return index;
    }

    public String getName()
    {
        return table;
    }

    public String getQuotedName()
    {
        return quotedName;
    }

    public Field<?>[] getFields()
    {
        return fields;
    }

    public Column<?>[] getKeyColumns()
    {
        return keyColumns;
    }

    public int getKeySize()
    {
        return keyColumns.length;
    }

    public ModelQuery getInsert()
    {
        return insert;
    }

    public ModelQuery getUpdate()
    {
        return update;
    }

    public ModelQuery getDelete()
    {
        return delete;
    }
    
    public Column<?>[] getColumns()
    {
        return columns;
    }

    public JoinField<?>[] getJoinFields()
    {
        return joinFields;
    }

    public Field<?>[] getInheritedFields()
    {
        return inheritedFields;
    }

    public Field<?>[] getGivenFields()
    {
        return givenFields;
    }

    public void setInsert( ModelQuery insert )
	{
		this.insert = insert;
	}

	public void setUpdate( ModelQuery update )
	{
		this.update = update;
	}

	public void setDelete( ModelQuery delete )
	{
		this.delete = delete;
	}

	public Factory<? extends Model> getFactory()
    {
        return factory;
    }

    public void setFactory( Factory<? extends Model> factory )
    {
        this.factory = factory;
    }

    public <T extends Model> T newModel()
    {
        return (T)factory.create();
    }

    public <F extends Field<?>> F getField( String name )
    {
        return (F)fieldMap.get( name );
    }

    public LoadProfile getLoadProfile( String name )
    {
        return loadProfileMap.get( name );
    }
    
    public SaveProfile getSaveProfile( String name )
    {
        return saveProfileMap.get( name );
    }

    public <T extends Model> QueryTemplate<T> getQuery( String name )
    {
        return (QueryTemplate<T>)queries.get( name );
    }

    public HistoryTable getHistory()
    {
        return history;
    }

    public void setHistory( HistoryTable history )
    {
        this.history = history;
    }

    public boolean hasHistory()
    {
        return (history != null);
    }

    public LoadProfile getLoadProfileAll()
    {
        return loadProfileAll;
    }

    public LoadProfile getLoadProfileId()
    {
        return loadProfileId;
    }

    public int getFlags()
    {
        return flags;
    }

    public boolean is( int flag )
    {
        return (flags & flag) == flag;
    }
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append( table );
        sb.append( ": {" );
        sb.append( "index=" ).append( index );

        if (is( COMPLETELY_GENERATED ))
        {
            sb.append( ", completely-generated" );
        }

        if (is( RELATIONSHIP_TABLE ))
        {
            sb.append( ", relationship-table" );
        }

        if (is( SUB_TABLE ))
        {
            sb.append( ", sub-table" );
        }

        if (is( DYNAMICALLY_INSERTED ))
        {
            sb.append( ", dynamically-inserted" );
        }

        if (is( DYNAMICALLY_UPDATED ))
        {
            sb.append( ", dynamically-updated" );
        }

        if (is( TRANSACTION_CACHED ))
        {
            sb.append( ", transaction-cached" );
        }

        if (is( APPLICATION_CACHED ))
        {
            sb.append( ", application-cached" );
        }

        sb.append( ", fields=[" );
        for (int i = 0; i < fields.length; i++)
        {
            if (i > 0) sb.append( ", " );
            sb.append( fields[i] );
        }
        sb.append( "]" );
        
        appendFieldNames( sb, "columns", columns );
        appendFieldNames( sb, "join-fields", joinFields );
        appendFieldNames( sb, "inherited-fields", inheritedFields );
        appendFieldNames( sb, "given-fields", givenFields );

        sb.append( ", loads=[" );
        for (int i = 0; i < loadProfiles.length; i++)
        {
            if (i > 0) sb.append( ", " );
            sb.append( loadProfiles[i] );
        }
        sb.append( "]" );

        sb.append( "}" );
        return sb.toString();
    }
    
    private void appendFieldNames(StringBuilder sb, String property, Field<?>[] fields)
    {
        sb.append( ", " ).append( property ).append( "=[" );
        
        for (int i = 0; i < fields.length; i++)
        {
            if (i > 0) sb.append( ", " );
            sb.append( fields[i].getName() );
        }
        
        sb.append( "]" );
    }

}
