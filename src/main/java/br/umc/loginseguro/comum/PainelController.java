package br.umc.loginseguro.comum;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import br.umc.loginseguro.seguranca.UsuarioAutenticado;

@Controller
public class PainelController {

	@GetMapping("/painel")
	public String painel(Model model, @AuthenticationPrincipal UsuarioAutenticado usuario) {
		model.addAttribute("usuarioNome", usuario.getNome());
		model.addAttribute("usuarioPerfil", usuario.getPerfil());
		return "painel";
	}

}
