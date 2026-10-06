package br.umc.loginseguro.usuario;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.umc.loginseguro.auditoria.AuditoriaService;
import br.umc.loginseguro.seguranca.UsuarioAutenticado;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/admin")
public class AdminController {

	private final AdminUsuarioService adminUsuarioService;
	private final UsuarioService usuarioService;
	private final AuditoriaService auditoriaService;

	public AdminController(AdminUsuarioService adminUsuarioService, UsuarioService usuarioService,
			AuditoriaService auditoriaService) {
		this.adminUsuarioService = adminUsuarioService;
		this.usuarioService = usuarioService;
		this.auditoriaService = auditoriaService;
	}

	@GetMapping
	public String raiz() {
		return "redirect:/admin/usuarios";
	}

	@GetMapping("/auditoria")
	public String auditoria(Model model) {
		model.addAttribute("eventos", auditoriaService.listarRecentes());
		return "auditoria/admin-auditoria";
	}

	@GetMapping("/usuarios")
	public String listar(Model model, @AuthenticationPrincipal UsuarioAutenticado admin) {
		model.addAttribute("usuarios", adminUsuarioService.listarTodos());
		model.addAttribute("idAdminLogado", admin.getId());
		adicionarContadores(model);
		if (!model.containsAttribute("novoUsuarioForm")) {
			model.addAttribute("novoUsuarioForm", new NovoUsuarioAdminForm());
		}
		return "usuario/admin-usuarios";
	}

	private void adicionarContadores(Model model) {
		model.addAttribute("totalUsuarios", usuarioService.contarTodos());
		model.addAttribute("totalAdministradores", usuarioService.contarPorPerfil(Perfil.ADMINISTRADOR));
		model.addAttribute("totalSecretarias", usuarioService.contarPorPerfil(Perfil.SECRETARIA));
		model.addAttribute("totalAlunos", usuarioService.contarPorPerfil(Perfil.ALUNO));
	}

	@PostMapping("/usuarios")
	public String criar(@Valid @ModelAttribute("novoUsuarioForm") NovoUsuarioAdminForm form, BindingResult bindingResult,
			Model model, @AuthenticationPrincipal UsuarioAutenticado admin, RedirectAttributes redirectAttributes) {

		if (form.getPerfil() == Perfil.ALUNO) {
			bindingResult.rejectValue("perfil", "perfil.invalido", "Selecione SECRETARIA ou ADMINISTRADOR.");
		} else if (!bindingResult.hasFieldErrors("email") && usuarioService.existePorEmail(form.getEmail())) {
			bindingResult.rejectValue("email", "email.duplicado", "Este e-mail já está cadastrado.");
		}

		if (bindingResult.hasErrors()) {
			form.setSenha(null);
			model.addAttribute("usuarios", adminUsuarioService.listarTodos());
			model.addAttribute("idAdminLogado", admin.getId());
			adicionarContadores(model);
			return "usuario/admin-usuarios";
		}

		adminUsuarioService.criar(form.getNome(), form.getEmail(), form.getSenha(), form.getPerfil());
		redirectAttributes.addFlashAttribute("mensagemSucesso", "Usuário criado com sucesso.");
		return "redirect:/admin/usuarios";
	}

	@PostMapping("/usuarios/{id}/perfil")
	public String alterarPerfil(@PathVariable String id, @RequestParam Perfil perfil,
			@AuthenticationPrincipal UsuarioAutenticado admin, RedirectAttributes redirectAttributes) {
		try {
			adminUsuarioService.alterarPerfil(id, perfil, admin.getId());
			redirectAttributes.addFlashAttribute("mensagemSucesso", "Perfil atualizado.");
		} catch (OperacaoNaoPermitidaException e) {
			redirectAttributes.addFlashAttribute("mensagemErro", e.getMessage());
		}
		return "redirect:/admin/usuarios";
	}

	@PostMapping("/usuarios/{id}/ativo")
	public String alterarAtivo(@PathVariable String id, @RequestParam boolean ativo,
			@AuthenticationPrincipal UsuarioAutenticado admin, RedirectAttributes redirectAttributes) {
		try {
			adminUsuarioService.alterarAtivo(id, ativo, admin.getId());
			redirectAttributes.addFlashAttribute("mensagemSucesso", "Status atualizado.");
		} catch (OperacaoNaoPermitidaException e) {
			redirectAttributes.addFlashAttribute("mensagemErro", e.getMessage());
		}
		return "redirect:/admin/usuarios";
	}

}
