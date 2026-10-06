package br.umc.loginseguro.seguranca;

import java.io.IOException;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Qualquer falha de login (credenciais erradas, conta inexistente ou conta
 * inativa) leva sempre ao mesmo redirecionamento, para nunca revelar se o
 * e-mail existe ou por que a autenticacao falhou.
 */
@Component
public class FalhaAutenticacaoGenericaHandler implements AuthenticationFailureHandler {

	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException exception) throws IOException {
		response.sendRedirect(request.getContextPath() + "/login?erro");
	}

}
