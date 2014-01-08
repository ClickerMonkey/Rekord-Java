package org.magnos.rekord;

public class Logging
{
	
	public static final int SELECTS = 0;
	public static final int INSERTS = 1;
	public static final int UPDATES = 2;
	public static final int DELETES = 3;
	public static final int LAZY_FETCHING = 4;
	public static final int LOADING = 5;
	public static final int ONE_TO_ONE = 6;
	public static final int CACHING = 7;
	public static final int HISTORY = 8;
	public static final int HUMAN_READABLE_QUERY = 9;
	
	public static final int[] ALL = {
		SELECTS, INSERTS, UPDATES, DELETES, LAZY_FETCHING,
		LOADING, ONE_TO_ONE, CACHING, HISTORY, HUMAN_READABLE_QUERY
	};
	
}
