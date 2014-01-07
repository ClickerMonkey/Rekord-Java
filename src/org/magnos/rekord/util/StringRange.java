package org.magnos.rekord.util;


public class StringRange
{

    public int start;
    public int end;
    
    public StringRange()
    {
        this( -1, -1 );
    }
    
    public StringRange(int start, int end)
    {
        this.start = start;
        this.end = end;
    }
    
    public boolean exists()
    {
        return start < end;
    }
    
    public String replace(String text, String replacement)
    {
        if (exists())
        {
            text = text.substring( 0, start ) + replacement + text.substring( end );
        }
        
        return text;
    }
    
}
