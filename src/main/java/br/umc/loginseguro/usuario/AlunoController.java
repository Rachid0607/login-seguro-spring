package br.umc.loginseguro.usuario;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import br.umc.loginseguro.seguranca.UsuarioAutenticado;

@Controller
@RequestMapping("/aluno")
public class AlunoController {

	@GetMapping
	public String meusDados(Model model, @AuthenticationPrincipal UsuarioAutenticado usuario) {
		model.addAttribute("usuario", usuario);
		return "usuario/aluno-meus-dados";
	}

}
