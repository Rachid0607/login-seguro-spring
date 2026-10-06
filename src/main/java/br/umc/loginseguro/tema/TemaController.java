package br.umc.loginseguro.tema;

import java.time.Duration;
import java.util.Set;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletResponse;

/**
 * Troca o tema visual (so cosmetico - sem relacao com autenticacao ou
 * autorizacao) gravando a escolha num cookie de longa duracao. Puramente
 * uma preferencia de exibicao; nenhuma regra de negocio depende disso.
 */
@Controller
public class TemaController {

	private static final Set<String> TEMAS_VALIDOS = Set.of("padrao", "escuro");
	private static final Duration VALIDADE_COOKIE = Duration.ofDays(365);

	@PostMapping("/tema")
	public String alternar(@RequestParam String tema, @RequestParam(required = false) String destino,
			HttpServletResponse response) {

		String temaEscolhido = TEMAS_VALIDOS.contains(tema) ? tema : "padrao";

		ResponseCookie cookie = ResponseCookie.from("tema", temaEscolhido)
				.httpOnly(true)
				.sameSite("Lax")
				.path("/")
				.maxAge(VALIDADE_COOKIE)
				.build();
		response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

		return "redirect:" + caminhoSeguro(destino);
	}

	/**
	 * So aceita caminhos internos (começando com "/" e sem "://" ou "//"),
	 * para nunca funcionar como open redirect.
	 */
	private static String caminhoSeguro(String destino) {
		if (destino != null && destino.startsWith("/") && !destino.startsWith("//") && !destino.contains("://")) {
			return destino;
		}
		return "/";
	}

}
