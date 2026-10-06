package br.umc.loginseguro.seguranca;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import br.umc.loginseguro.auditoria.AuditoriaService;
import br.umc.loginseguro.auditoria.TipoEvento;

/**
 * Checa o bloqueio por forca bruta ANTES de qualquer consulta ao usuario.
 * Se bloqueado (por conta ou por IP), lanca LockedException, que o
 * ProviderManager trata como AccountStatusException e interrompe a cadeia
 * de autenticacao imediatamente - o DaoAuthenticationProvider real nunca
 * chega a ser chamado. Se nao estiver bloqueado, devolve null para que o
 * proximo provider (o real) decida.
 */
@Component
public class BloqueioLoginAuthenticationProvider implements AuthenticationProvider {

	private final TentativaLoginService tentativaLoginService;
	private final AuditoriaService auditoriaService;

	public BloqueioLoginAuthenticationProvider(TentativaLoginService tentativaLoginService,
			AuditoriaService auditoriaService) {
		this.tentativaLoginService = tentativaLoginService;
		this.auditoriaService = auditoriaService;
	}

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		String email = authentication.getName();
		String ip = extrairIp(authentication);

		if (tentativaLoginService.estaBloqueado(email, ip)) {
			auditoriaService.registrar(TipoEvento.LOGIN_BLOQUEADO, null,
					"Bloqueio temporario por excesso de tentativas");
			throw new LockedException("Conta temporariamente bloqueada.");
		}

		return null;
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
	}

	private static String extrairIp(Authentication authentication) {
		if (authentication.getDetails() instanceof WebAuthenticationDetails details) {
			return details.getRemoteAddress();
		}
		return "desconhecido";
	}

}
