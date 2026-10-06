package br.umc.loginseguro.tema;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Disponibiliza o nome e o tema configurados da aplicacao (app.nome, app.tema)
 * em todas as paginas Thymeleaf, para uso nos fragments de layout.
 */
@ControllerAdvice
public class AtributosGlobaisAdvice {

	@Value("${app.nome}")
	private String appNome;

	@Value("${app.tema}")
	private String appTema;

	@ModelAttribute("appNome")
	public String appNome() {
		return appNome;
	}

	@ModelAttribute("appTema")
	public String appTema() {
		return appTema;
	}

}
