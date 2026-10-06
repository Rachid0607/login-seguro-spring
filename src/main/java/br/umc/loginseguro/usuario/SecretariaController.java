package br.umc.loginseguro.usuario;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/secretaria")
public class SecretariaController {

	private final UsuarioService usuarioService;

	public SecretariaController(UsuarioService usuarioService) {
		this.usuarioService = usuarioService;
	}

	@GetMapping
	public String raiz() {
		return "redirect:/secretaria/alunos";
	}

	@GetMapping("/alunos")
	public String listarAlunos(Model model) {
		model.addAttribute("alunos", usuarioService.listarPorPerfil(Perfil.ALUNO));
		return "usuario/secretaria-alunos";
	}

}
