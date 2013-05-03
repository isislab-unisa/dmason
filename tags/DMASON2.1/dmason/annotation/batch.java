package dmason.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface batch {
	
	 String domain() default "";
	 String suggestedValue() default "";
}

