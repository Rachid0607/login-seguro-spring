package br.umc.loginseguro.seguranca;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer.SessionFixationConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

	private static final int CUSTO_BCRYPT = 12;

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder(CUSTO_BCRYPT);
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http,
			AuthenticationFailureHandler authenticationFailureHandler) throws Exception {
		http
			.authorizeHttpRequests(auth -> auth
				.requestMatchers("/", "/login", "/cadastro", "/error", "/css/**", "/temas/**").permitAll()
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
				.failureHandler(authenticationFailureHandler)
				.defaultSuccessUrl("/painel", true)
				.permitAll())
			.logout(logout -> logout
				.logoutUrl("/logout")
				.logoutSuccessUrl("/login?logout")
				.invalidateHttpSession(true)
				.deleteCookies("SESSION")
				.permitAll());
		return http.build();
	}

}
