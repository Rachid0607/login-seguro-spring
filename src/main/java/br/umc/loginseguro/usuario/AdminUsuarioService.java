package br.umc.loginseguro.usuario;

import java.time.Instant;
import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Operacoes administrativas sobre usuarios (criar SECRETARIA/ADMINISTRADOR,
 * promover/rebaixar perfil, ativar/desativar). Protegidas por @PreAuthorize
 * como reforco as regras ja aplicadas por URL no SecurityConfig.
 */
@Service
public class AdminUsuarioService {

	private final UsuarioRepository usuarioRepository;
	private final UsuarioService usuarioService;
	private final PasswordEncoder passwordEncoder;

	public AdminUsuarioService(UsuarioRepository usuarioRepository, UsuarioService usuarioService,
			PasswordEncoder passwordEncoder) {
		this.usuarioRepository = usuarioRepository;
		this.usuarioService = usuarioService;
		this.passwordEncoder = passwordEncoder;
	}

	@PreAuthorize("hasRole('ADMINISTRADOR')")
	public List<Usuario> listarTodos() {
		return usuarioRepository.findAll();
	}

	@PreAuthorize("hasRole('ADMINISTRADOR')")
	public Usuario criar(String nome, String email, String senha, Perfil perfil) {
		if (perfil == Perfil.ALUNO) {
			throw new OperacaoNaoPermitidaException("Contas ALUNO sao criadas apenas pelo cadastro publico.");
		}
		Usuario usuario = new Usuario();
		usuario.setNome(nome);
		usuario.setEmail(email);
		usuario.setSenhaHash(passwordEncoder.encode(senha));
		usuario.setPerfil(perfil);
		usuario.setAtivo(true);
		usuario.setConsentimentoTermosEm(Instant.now());
		return usuarioService.salvar(usuario);
	}

	@PreAuthorize("hasRole('ADMINISTRADOR')")
	public void alterarPerfil(String idUsuarioAlvo, Perfil novoPerfil, String idAdminLogado) {
		if (idUsuarioAlvo.equals(idAdminLogado)) {
			throw new OperacaoNaoPermitidaException("Você não pode alterar o seu próprio perfil.");
		}
		Usuario usuario = buscarOuFalhar(idUsuarioAlvo);
		usuario.setPerfil(novoPerfil);
		usuarioRepository.save(usuario);
	}

	@PreAuthorize("hasRole('ADMINISTRADOR')")
	public void alterarAtivo(String idUsuarioAlvo, boolean ativo, String idAdminLogado) {
		if (idUsuarioAlvo.equals(idAdminLogado) && !ativo) {
			throw new OperacaoNaoPermitidaException("Você não pode desativar a sua própria conta.");
		}
		Usuario usuario = buscarOuFalhar(idUsuarioAlvo);
		usuario.setAtivo(ativo);
		usuarioRepository.save(usuario);
	}

	private Usuario buscarOuFalhar(String id) {
		return usuarioRepository.findById(id)
				.orElseThrow(() -> new OperacaoNaoPermitidaException("Usuário não encontrado."));
	}

}
