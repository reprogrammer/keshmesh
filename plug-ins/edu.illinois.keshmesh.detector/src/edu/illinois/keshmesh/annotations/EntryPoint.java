/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.keshmesh.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * @author Mohsen Vakilian
 * @author Stas Negara
 * 
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.CLASS)
public @interface EntryPoint {

}
