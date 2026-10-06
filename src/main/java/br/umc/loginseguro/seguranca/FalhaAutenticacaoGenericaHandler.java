package br.umc.loginseguro.seguranca;

import java.io.IOException;

import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import br.umc.loginseguro.auditoria.AuditoriaService;
import br.umc.loginseguro.auditoria.TipoEvento;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Credenciais erradas, conta inexistente ou conta inativa levam sempre ao
 * mesmo redirecionamento ("E-mail ou senha inválidos."), para nunca revelar
 * se o e-mail existe. Bloqueio por forca bruta (conta ou IP) tem sua propria
 * mensagem generica ("Muitas tentativas..."), igual para conta existente ou
 * nao - so muda de "credenciais invalidas" para "bloqueado", nunca revela
 * qual dos dois (conta ou IP) causou o bloqueio.
 * Quando a falha NAO for um bloqueio (que ja foi registrado e auditado pelo
 * BloqueioLoginAuthenticationProvider), conta a tentativa errada para o
 * controle de forca bruta e audita LOGIN_FALHA, sem nunca gravar o e-mail
 * digitado nem a senha.
 */
@Component
public class FalhaAutenticacaoGenericaHandler implements AuthenticationFailureHandler {

	private final TentativaLoginService tentativaLoginService;
	private final AuditoriaService auditoriaService;

	public FalhaAutenticacaoGenericaHandler(TentativaLoginService tentativaLoginService,
			AuditoriaService auditoriaService) {
		this.tentativaLoginService = tentativaLoginService;
		this.auditoriaService = auditoriaService;
	}

	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException exception) throws IOException {
		if (exception instanceof LockedException) {
			response.sendRedirect(request.getContextPath() + "/login?bloqueado");
			return;
		}

		String email = request.getParameter("email");
		tentativaLoginService.registrarFalha(email, request.getRemoteAddr());
		auditoriaService.registrar(TipoEvento.LOGIN_FALHA, null, "Credenciais invalidas");
		response.sendRedirect(request.getContextPath() + "/login?erro");
	}

}
