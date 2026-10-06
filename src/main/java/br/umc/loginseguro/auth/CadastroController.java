package br.umc.loginseguro.auth;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;

@Controller
public class CadastroController {

	private final CadastroService cadastroService;

	public CadastroController(CadastroService cadastroService) {
		this.cadastroService = cadastroService;
	}

	@GetMapping("/cadastro")
	public String formulario(Model model) {
		if (!model.containsAttribute("cadastroForm")) {
			model.addAttribute("cadastroForm", new CadastroForm());
		}
		return "auth/cadastro";
	}

	@PostMapping("/cadastro")
	public String cadastrar(@Valid @ModelAttribute("cadastroForm") CadastroForm form, BindingResult bindingResult,
			RedirectAttributes redirectAttributes) {

		if (!bindingResult.hasFieldErrors("email") && cadastroService.emailJaCadastrado(form.getEmail())) {
			bindingResult.rejectValue("email", "email.duplicado", "Este e-mail já está cadastrado.");
		}

		if (bindingResult.hasErrors()) {
			form.setSenha(null);
			form.setConfirmacaoSenha(null);
			return "auth/cadastro";
		}

		cadastroService.cadastrar(form);
		redirectAttributes.addFlashAttribute("mensagemSucesso", "Cadastro realizado com sucesso. Faça login para continuar.");
		return "redirect:/login";
	}

}
