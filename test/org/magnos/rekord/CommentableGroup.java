package org.magnos.rekord;

import org.magnos.rekord.field.ForeignColumn;
import org.magnos.rekord.field.InheritColumn;
import org.magnos.rekord.field.OneToOne;

public class CommentableGroup extends Group
{
	private static final long serialVersionUID = 1L;

	public static final Factory<CommentableGroup> FACTORY			= new Factory<CommentableGroup>() {
		public CommentableGroup create() {
			return new CommentableGroup();
		}
	};
	
	public static final Table           		TABLE 				= Rekord.getTable( "commentable_group", FACTORY );
	public static final InheritColumn<Long>		GROUP_ID			= TABLE.getField( "group_id" );
	public static final ForeignColumn<Long> 	COMMENTABLE_ID		= TABLE.getField( "commentable_id" );
	public static final OneToOne<Commentable> 	COMMENTABLE 		= TABLE.getField( "commentable" );

	public CommentableGroup()
	{
		super( TABLE );
	}
	
	public Long getCommentableId()				{ return get( COMMENTABLE_ID ); }
	public Commentable getCommentable()			{ return get( COMMENTABLE ); }
	public void setCommentable(Commentable x)	{ set( COMMENTABLE, x ); }
	
}
