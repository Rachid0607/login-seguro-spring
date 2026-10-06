package br.umc.loginseguro.seguranca;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import br.umc.loginseguro.usuario.Perfil;
import br.umc.loginseguro.usuario.Usuario;
import br.umc.loginseguro.usuario.UsuarioRepository;
import br.umc.loginseguro.usuario.UsuarioService;

/**
 * Cria o primeiro ADMINISTRADOR a partir de ADMIN_NOME/ADMIN_EMAIL/ADMIN_SENHA,
 * somente se ainda nao existir nenhum administrador. Nunca ha senha fixa no
 * codigo: sem essas variaveis definidas, nenhum administrador e criado.
 */
@Component
public class BootstrapAdministradorRunner implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(BootstrapAdministradorRunner.class);

	private final UsuarioRepository usuarioRepository;
	private final UsuarioService usuarioService;
	private final PasswordEncoder passwordEncoder;
	private final Environment environment;

	public BootstrapAdministradorRunner(UsuarioRepository usuarioRepository, UsuarioService usuarioService,
			PasswordEncoder passwordEncoder, Environment environment) {
		this.usuarioRepository = usuarioRepository;
		this.usuarioService = usuarioService;
		this.passwordEncoder = passwordEncoder;
		this.environment = environment;
	}

	@Override
	public void run(ApplicationArguments args) {
		if (usuarioRepository.existsByPerfil(Perfil.ADMINISTRADOR)) {
			return;
		}

		String nome = environment.getProperty("ADMIN_NOME");
		String email = environment.getProperty("ADMIN_EMAIL");
		String senha = environment.getProperty("ADMIN_SENHA");

		if (isBlank(nome) || isBlank(email) || isBlank(senha)) {
			log.warn("Nenhum ADMINISTRADOR existe e ADMIN_NOME/ADMIN_EMAIL/ADMIN_SENHA nao foram definidos. "
					+ "Defina essas variaveis no .env e reinicie a aplicacao para criar o administrador inicial.");
			return;
		}

		Usuario administrador = new Usuario();
		administrador.setNome(nome);
		administrador.setEmail(email);
		administrador.setSenhaHash(passwordEncoder.encode(senha));
		administrador.setPerfil(Perfil.ADMINISTRADOR);
		administrador.setAtivo(true);
		administrador.setConsentimentoTermosEm(Instant.now());
		usuarioService.salvar(administrador);

		log.info("Administrador inicial criado ({})", UsuarioService.normalizarEmail(email));
	}

	private static boolean isBlank(String valor) {
		return valor == null || valor.isBlank();
	}

}
