package br.umc.loginseguro.comum;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import br.umc.loginseguro.seguranca.UsuarioAutenticado;

/**
 * /painel e so um despachante: encaminha cada usuario autenticado para a
 * area do seu proprio perfil.
 */
@Controller
public class PainelController {

	@GetMapping("/painel")
	public String painel(@AuthenticationPrincipal UsuarioAutenticado usuario) {
		return switch (usuario.getPerfil()) {
			case ADMINISTRADOR -> "redirect:/admin/usuarios";
			case SECRETARIA -> "redirect:/secretaria/alunos";
			case ALUNO -> "redirect:/aluno";
		};
	}

}
