package br.umc.loginseguro.auth;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SenhasCoincidemValidator.class)
public @interface SenhasCoincidem {

	String message() default "A confirmação de senha não coincide com a senha.";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

}
