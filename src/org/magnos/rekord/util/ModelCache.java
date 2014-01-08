
package org.magnos.rekord.util;

import java.util.Map;

import org.magnos.rekord.Key;
import org.magnos.rekord.Logging;
import org.magnos.rekord.Model;
import org.magnos.rekord.Rekord;


public class ModelCache
{

    private final Map<Key, Model> cache;
    private final String name;
    
    public ModelCache( Map<Key, Model> cacheMap, String name )
    {
        this.cache = cacheMap;
        this.name = name;
    }

    public <T extends Model> T get( Key key )
    {
        T model = (cache == null ? null : (T)cache.get( key )); 
        
        if (model != null && Rekord.isLogging( Logging.CACHING ))
        {
            Rekord.log( "from-cache %s with key %s: %s", name, key, model );
        }
        
        return model;
    }

    public boolean put( Model model )
    {
        boolean cached = false;

        if (cache != null)
        {
            Key key = model.getKey();

            if (key.exists())
            {
                cache.put( key, model );
                cached = true;
                
                if (Rekord.isLogging( Logging.CACHING ))
                {
                    Rekord.log( "to-cache %s with key %s: %s", name, key, model );
                }
            }
            else
            {
                Rekord.log( Logging.CACHING, "failed-cache %s without key: %s", name, model );
            }
        }

        return cached;
    }

    public <T extends Model> T remove( Key key )
    {
        T model = cache == null ? null : (T)cache.remove( key );
        
        if (model != null && Rekord.isLogging( Logging.CACHING ))
        {
            Rekord.log( "purged-cache %s of key %s: %s", name, key, model );
        }
        
        return model;
    }

    public <T extends Model> Map<Key, T> getMap()
    {
        return (Map<Key, T>)cache;
    }
    
    public String getName()
    {
        return name;
    }

}
