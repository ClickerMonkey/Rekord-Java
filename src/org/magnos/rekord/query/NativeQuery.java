
package org.magnos.rekord.query;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.magnos.rekord.Field;
import org.magnos.rekord.Model;
import org.magnos.rekord.Table;
import org.magnos.rekord.View;
import org.magnos.rekord.field.Column;


public class NativeQuery
{

	public static final Pattern TOKEN_PATTERN = Pattern.compile( "(#[\\w_][\\w_\\d\\$]*|\\?[\\w_][\\w_\\d\\$]*)" );

	public static QueryTemplate<Model> parse( Table table, String nativeQuery, View view )
	{
	    return parse( table, nativeQuery, view, new ArrayList<Field<?>>() );
	}
	
	public static QueryTemplate<Model> parse( Table table, String nativeQuery, View view, List<Field<?>> selectList )
	{
		List<QueryBind> bindList = new ArrayList<QueryBind>();

		StringBuilder queryBuilder = new StringBuilder();
		Matcher matcher = TOKEN_PATTERN.matcher( nativeQuery );

		int start = 0;

		while (matcher.find())
		{
			String token = matcher.group();
			char indicator = token.charAt( 0 );
			String tokenName = token.substring( 1 );
			String tokenReplacement = null;
			Column<?> column = table.getField( tokenName );

			switch (indicator)
			{
			case '?':
				bindList.add( new QueryBind( tokenName, bindList.size(), column, null, matcher.start(), matcher.end() ) );
				tokenReplacement = "?";
				break;
			case '#':
				if (column == null)
				{
					throw new RuntimeException( "Error parsing NativeQuery, column " + tokenName + " does not exist on table " + table.getName() );
				}
				selectList.add( column );
				tokenReplacement = column.getQuotedName();
				break;
			}

			queryBuilder.append( nativeQuery.substring( start, matcher.start() ) );
			queryBuilder.append( tokenReplacement );
			start = matcher.end();
		}

		if (start < nativeQuery.length())
		{
			queryBuilder.append( nativeQuery.substring( start ) );
		}

		String query = queryBuilder.toString();
		QueryBind[] bindArray = bindList.toArray( new QueryBind[bindList.size()] );
		Field<?>[] selectArray = selectList.toArray( new Field[selectList.size()] );

		return new QueryTemplate<Model>( table, query, view, bindArray, selectArray );
	}

}
