package org.magnos.rekord.query.condition;



public interface ConditionResolver<R>
{
    public R resolve( Condition condition );
}
