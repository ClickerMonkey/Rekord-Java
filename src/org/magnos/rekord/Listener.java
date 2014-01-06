
package org.magnos.rekord;

import java.sql.SQLException;
import java.util.Map;

public interface Listener<M extends Model>
{
	public void onEvent( M model, ListenerEvent e ) throws SQLException;

    public void configure( Map<String, String> attributes ) throws Exception;
}
