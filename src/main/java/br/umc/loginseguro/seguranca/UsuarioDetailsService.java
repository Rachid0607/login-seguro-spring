package br.umc.loginseguro.seguranca;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import br.umc.loginseguro.usuario.UsuarioRepository;
import br.umc.loginseguro.usuario.UsuarioService;

@Service
public class UsuarioDetailsService implements UserDetailsService {

	private final UsuarioRepository usuarioRepository;

	public UsuarioDetailsService(UsuarioRepository usuarioRepository) {
		this.usuarioRepository = usuarioRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		return usuarioRepository.findByEmail(UsuarioService.normalizarEmail(email))
				.map(UsuarioAutenticado::new)
				.orElseThrow(() -> new UsernameNotFoundException("Credenciais invalidas"));
	}

}
