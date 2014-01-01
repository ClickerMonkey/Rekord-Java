package org.magnos.rekord.util;

import java.util.Map;

import org.magnos.rekord.Key;
import org.magnos.rekord.Logging;
import org.magnos.rekord.Model;
import org.magnos.rekord.Rekord;

public class ModelCache
{
	
	private final Map<Key, Model> cache;
	
	public ModelCache(Map<Key, Model> cacheMap)
	{
		this.cache = cacheMap;
	}
	
	public <T extends Model> T get(Key key)
	{
		return (cache == null ? null : (T)cache.get( key ));
	}
	
	public boolean put(Model model)
	{
		boolean cached = false;
		
		if (cache != null)
		{
			Key key = model.getKey();
			
			if (key.exists())
			{
				cache.put( key, model );
				cached = true;
			}
			else
			{
				Rekord.log( Logging.CACHING, "You cannot cache the following model without having a key: " + model );
			}
		}
		
		return cached;
	}
	
	public <T extends Model> Map<Key, T> getMap()
	{
		return (Map<Key, T>) cache;
	}
	

}
