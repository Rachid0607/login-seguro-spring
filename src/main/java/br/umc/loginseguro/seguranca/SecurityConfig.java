package br.umc.loginseguro.seguranca;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer.SessionFixationConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import br.umc.loginseguro.auditoria.AuditoriaService;
import br.umc.loginseguro.auditoria.TipoEvento;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

	private static final int CUSTO_BCRYPT = 12;

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder(CUSTO_BCRYPT);
	}

	/**
	 * Montado explicitamente, nessa ordem, para garantir que o bloqueio por
	 * forca bruta seja checado ANTES do provider real que consulta o usuario
	 * e verifica a senha (ver BloqueioLoginAuthenticationProvider).
	 */
	@Bean
	public AuthenticationManager authenticationManager(BloqueioLoginAuthenticationProvider bloqueioLoginAuthenticationProvider,
			UsuarioDetailsService usuarioDetailsService, PasswordEncoder passwordEncoder) {
		DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider(usuarioDetailsService);
		daoAuthenticationProvider.setPasswordEncoder(passwordEncoder);
		return new ProviderManager(List.of(bloqueioLoginAuthenticationProvider, daoAuthenticationProvider));
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationManager authenticationManager,
			AuthenticationSuccessHandler authenticationSuccessHandler,
			AuthenticationFailureHandler authenticationFailureHandler, AuditoriaService auditoriaService) throws Exception {
		http
			.authenticationManager(authenticationManager)
			.authorizeHttpRequests(auth -> auth
				.requestMatchers("/", "/login", "/cadastro", "/tema", "/error", "/css/**", "/temas/**").permitAll()
				.requestMatchers("/admin/**").hasRole("ADMINISTRADOR")
				.requestMatchers("/secretaria/**").hasAnyRole("SECRETARIA", "ADMINISTRADOR")
				.requestMatchers("/aluno/**").hasRole("ALUNO")
				.anyRequest().authenticated())
			.sessionManagement(session -> session
				.sessionFixation(SessionFixationConfigurer::changeSessionId))
			.formLogin(form -> form
				.loginPage("/login")
				.usernameParameter("email")
				.passwordParameter("senha")
				.successHandler(authenticationSuccessHandler)
				.failureHandler(authenticationFailureHandler)
				.permitAll())
			.logout(logout -> logout
				.logoutUrl("/logout")
				.addLogoutHandler((request, response, authentication) -> {
					if (authentication != null && authentication.getPrincipal() instanceof UsuarioAutenticado usuario) {
						auditoriaService.registrar(TipoEvento.LOGOUT, usuario.getId(), "Logout");
					}
				})
				.logoutSuccessUrl("/login?logout")
				.invalidateHttpSession(true)
				.deleteCookies("SESSION")
				.permitAll());
		return http.build();
	}

}
