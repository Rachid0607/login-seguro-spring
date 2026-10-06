package br.umc.loginseguro.tema;

import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.ModelAttribute;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Disponibiliza o nome, o tema e o caminho atual da requisicao em todas as
 * paginas Thymeleaf. O tema vem do cookie "tema" quando ele existir e for
 * valido (escolha do usuario via TemaController); senao, cai para
 * app.tema/APP_TEMA. O caminho atual e exposto aqui (em vez de usar o
 * objeto de expressao #request do Thymeleaf, que o Thymeleaf 3.1 nao
 * disponibiliza mais por padrao) para o destaque do link ativo no menu e
 * para o redirecionamento de volta ao trocar de tema.
 */
@ControllerAdvice
public class AtributosGlobaisAdvice {

	private static final Set<String> TEMAS_VALIDOS = Set.of("padrao", "escuro");

	@Value("${app.nome}")
	private String appNome;

	@Value("${app.tema}")
	private String appTemaPadrao;

	@ModelAttribute("appNome")
	public String appNome() {
		return appNome;
	}

	@ModelAttribute("appTema")
	public String appTema(@CookieValue(name = "tema", required = false) String temaCookie) {
		if (temaCookie != null && TEMAS_VALIDOS.contains(temaCookie)) {
			return temaCookie;
		}
		return appTemaPadrao;
	}

	@ModelAttribute("caminhoAtual")
	public String caminhoAtual(HttpServletRequest request) {
		return request.getRequestURI();
	}

}
