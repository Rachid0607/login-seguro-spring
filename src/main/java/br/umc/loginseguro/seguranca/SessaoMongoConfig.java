package br.umc.loginseguro.seguranca;

import java.time.Duration;

import org.mongodb.spring.session.MongoIndexedSessionRepository;
import org.mongodb.spring.session.config.annotation.web.http.EnableMongoHttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.config.SessionRepositoryCustomizer;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

/**
 * Sessao HTTP persistida no MongoDB (colecao configuravel, padrao "sessoes").
 * O timeout e aplicado via customizer porque o atributo da anotacao e um
 * int fixo em tempo de compilacao, sem suporte a placeholder de propriedade.
 */
@Configuration
@EnableMongoHttpSession(collectionName = "${app.sessao.colecao:sessoes}")
public class SessaoMongoConfig {

	@Bean
	public SessionRepositoryCustomizer<MongoIndexedSessionRepository> personalizadorTimeoutSessao(
			@Value("${app.sessao.timeout-minutos:30}") long timeoutMinutos) {
		return repositorio -> repositorio.setDefaultMaxInactiveInterval(Duration.ofMinutes(timeoutMinutos));
	}

	/**
	 * O cookie de sessao do Spring Session nao le server.servlet.session.cookie.*
	 * (essas propriedades valem so para a sessao nativa do servlet container,
	 * que o Spring Session substitui). Por isso HttpOnly/SameSite/Secure sao
	 * aplicados aqui, explicitamente.
	 */
	@Bean
	public CookieSerializer cookieSerializer(@Value("${app.sessao.cookie-secure:false}") boolean cookieSecure) {
		DefaultCookieSerializer serializer = new DefaultCookieSerializer();
		serializer.setUseHttpOnlyCookie(true);
		serializer.setSameSite("Lax");
		serializer.setUseSecureCookie(cookieSecure);
		return serializer;
	}

}
