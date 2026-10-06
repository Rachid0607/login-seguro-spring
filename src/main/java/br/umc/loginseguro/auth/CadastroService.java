package br.umc.loginseguro.auth;

import java.time.Instant;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import br.umc.loginseguro.usuario.Perfil;
import br.umc.loginseguro.usuario.Usuario;
import br.umc.loginseguro.usuario.UsuarioService;

@Service
public class CadastroService {

	private final UsuarioService usuarioService;
	private final PasswordEncoder passwordEncoder;

	public CadastroService(UsuarioService usuarioService, PasswordEncoder passwordEncoder) {
		this.usuarioService = usuarioService;
		this.passwordEncoder = passwordEncoder;
	}

	public boolean emailJaCadastrado(String email) {
		return usuarioService.existePorEmail(email);
	}

	/**
	 * Cadastro publico: perfil e sempre ALUNO, nunca o que vier do formulario.
	 */
	public Usuario cadastrar(CadastroForm form) {
		Usuario usuario = new Usuario();
		usuario.setNome(form.getNome().trim());
		usuario.setEmail(form.getEmail());
		usuario.setSenhaHash(passwordEncoder.encode(form.getSenha()));
		usuario.setPerfil(Perfil.ALUNO);
		usuario.setAtivo(true);
		usuario.setConsentimentoTermosEm(Instant.now());
		return usuarioService.salvar(usuario);
	}

}
