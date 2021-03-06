package com.teamwizardry.wizardry.api.spell.annotation;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * This annotation is used for parameters of an override implementation (not an interface method).
 * An annotated parameter is declared to point to an overriden method and is technically something
 * like a <code>super</code> pointer. <br />
 * <b>NOTE</b>: It is not allowed for default override methods as they don't have a super method. <br />
 * <b>NOTE</b>: It is not allowed to be used multiple times. <br />
 *
 * @author Avatair
 */
@Retention(RUNTIME)
@Target(PARAMETER)
public @interface ContextSuper {
}
