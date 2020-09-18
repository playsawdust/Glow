package com.playsawdust.chipper.glow.text;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that this value is given in an abstract, size-independent "Font Units" unit.
 * 
 * <p>To convert between a FontUnit and more convenient, concrete units, use the font's Em size,
 * which because it's expressed in FontUnits, is "font units per Em". For instance, if you want to
 * get to a point size, and you have a pointsPerEm available, you can use {@code fontUnitsValue * pointsPerEm / emSize}
 */

@Target({ ElementType.PARAMETER, ElementType.FIELD, ElementType.METHOD, ElementType.LOCAL_VARIABLE })
@Retention(RetentionPolicy.RUNTIME)
public @interface FontUnits {
}
