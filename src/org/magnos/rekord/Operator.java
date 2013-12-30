package org.magnos.rekord;

public enum Operator
{
	EQ( " = " ), 
	LT( " < " ), 
	LTEQ( " <= " ), 
	GT( " > " ), 
	GTEQ( " >= " ), 
	NEQ( " <> " );
	
	private final String symbol;
	
	private Operator(String symbol)
	{
		this.symbol = symbol;
	}
	
	public String getSymbol()
	{
		return symbol;
	}
	
}
