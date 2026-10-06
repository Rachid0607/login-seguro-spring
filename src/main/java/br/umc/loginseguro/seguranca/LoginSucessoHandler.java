package br.umc.loginseguro.seguranca;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import br.umc.loginseguro.auditoria.AuditoriaService;
import br.umc.loginseguro.auditoria.TipoEvento;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class LoginSucessoHandler implements AuthenticationSuccessHandler {

	private final TentativaLoginService tentativaLoginService;
	private final AuditoriaService auditoriaService;

	public LoginSucessoHandler(TentativaLoginService tentativaLoginService, AuditoriaService auditoriaService) {
		this.tentativaLoginService = tentativaLoginService;
		this.auditoriaService = auditoriaService;
	}

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException {
		UsuarioAutenticado usuario = (UsuarioAutenticado) authentication.getPrincipal();
		tentativaLoginService.registrarSucesso(usuario.getUsername());
		auditoriaService.registrar(TipoEvento.LOGIN_SUCESSO, usuario.getId(), "Login bem-sucedido");
		response.sendRedirect(request.getContextPath() + "/painel");
	}

}
