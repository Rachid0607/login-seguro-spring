package br.umc.loginseguro.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Restringe o dominio de e-mail aceito no cadastro publico, configuravel em
 * app.cadastro.dominio-email. Vazio (padrao) aceita qualquer dominio.
 */
@Component
public class DominioPermitidoValidator implements ConstraintValidator<DominioPermitido, String> {

	private final String dominioPermitido;

	public DominioPermitidoValidator(@Value("${app.cadastro.dominio-email:}") String dominioPermitido) {
		this.dominioPermitido = dominioPermitido == null ? "" : dominioPermitido.trim();
	}

	@Override
	public boolean isValid(String email, ConstraintValidatorContext context) {
		if (email == null || email.isBlank() || dominioPermitido.isBlank()) {
			return true;
		}
		String dominio = dominioPermitido.startsWith("@") ? dominioPermitido : "@" + dominioPermitido;
		return email.trim().toLowerCase().endsWith(dominio.toLowerCase());
	}

}
