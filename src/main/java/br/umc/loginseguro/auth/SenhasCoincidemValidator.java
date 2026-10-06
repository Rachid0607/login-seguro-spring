package br.umc.loginseguro.auth;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class SenhasCoincidemValidator implements ConstraintValidator<SenhasCoincidem, CadastroForm> {

	@Override
	public boolean isValid(CadastroForm form, ConstraintValidatorContext context) {
		if (form.getSenha() == null || form.getConfirmacaoSenha() == null) {
			return true;
		}

		boolean valido = form.getSenha().equals(form.getConfirmacaoSenha());
		if (!valido) {
			context.disableDefaultConstraintViolation();
			context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
					.addPropertyNode("confirmacaoSenha")
					.addConstraintViolation();
		}
		return valido;
	}

}
