package org.magnos.rekord.query;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.magnos.rekord.Model;


public class SectionedQuery<M extends Model> extends Query<M>
{

    protected Map<String, Boolean> visibleMap;
    
    public SectionedQuery( QueryTemplate<M> template )
    {
        super( template );
        
        this.visibleMap = new HashMap<String, Boolean>();
    }
    
    public String getFinalQuery( boolean withRestrictions )
    {
        String query = super.getFinalQuery( withRestrictions );
        StringBuilder filtered = new StringBuilder();
        
        Scanner in = new Scanner( query );
        
        while (in.hasNextLine())
        {
            String line = in.nextLine();
            
            int comment = line.indexOf( "--" );
            
            if (comment != -1)
            {
                String section = line.substring( comment + 2 );
                Boolean visible = visibleMap.get( section );
                
                if (visible == null || visible)
                {
                    filtered.append( line );
                }
                else
                {
                    // TODO pull out bindings
                }
            }
            else
            {
                filtered.append( line );
            }
        }
        
        return filtered.toString();
    }
    
    public void hide( String section )
    {
        setVisible( section, false );
    }
    
    public void show( String section )
    {
        setVisible( section, true );
    }
    
    public void setVisible( String section, boolean visible )
    {
        visibleMap.put( section, visible );
    }
    
    public boolean isVisible( String section )
    {
        Boolean v = visibleMap.get( section );
        
        return (v != null && v);
    }

}
