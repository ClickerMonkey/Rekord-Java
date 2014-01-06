package org.magnos.rekord.query;

import java.util.ArrayList;
import java.util.List;

public class Where
{
	public List<StringBuilder> text = new ArrayList<StringBuilder>();
	public List<Where> children = new ArrayList<Where>();
	
	public Where()
	{
		
	}
	
}
