package br.umc.loginseguro.usuario;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

/**
 * Operacoes de leitura e persistencia de usuarios. Hash de senha e regras de
 * cadastro/autenticacao ficam a cargo dos services das etapas seguintes
 * (seguranca, auth); aqui so a normalizacao de e-mail e a persistencia basica.
 */
@Service
public class UsuarioService {

	private final UsuarioRepository usuarioRepository;

	public UsuarioService(UsuarioRepository usuarioRepository) {
		this.usuarioRepository = usuarioRepository;
	}

	public Optional<Usuario> buscarPorEmail(String email) {
		return usuarioRepository.findByEmail(normalizarEmail(email));
	}

	public boolean existePorEmail(String email) {
		return usuarioRepository.existsByEmail(normalizarEmail(email));
	}

	public Usuario salvar(Usuario usuario) {
		usuario.setEmail(normalizarEmail(usuario.getEmail()));
		if (usuario.getCriadoEm() == null) {
			usuario.setCriadoEm(Instant.now());
		}
		return usuarioRepository.save(usuario);
	}

	public static String normalizarEmail(String email) {
		return email == null ? null : email.trim().toLowerCase();
	}

	@PreAuthorize("hasAnyRole('SECRETARIA', 'ADMINISTRADOR')")
	public List<Usuario> listarPorPerfil(Perfil perfil) {
		return usuarioRepository.findByPerfil(perfil);
	}

	@PreAuthorize("hasRole('ADMINISTRADOR')")
	public long contarTodos() {
		return usuarioRepository.count();
	}

	@PreAuthorize("hasRole('ADMINISTRADOR')")
	public long contarPorPerfil(Perfil perfil) {
		return usuarioRepository.countByPerfil(perfil);
	}

}
