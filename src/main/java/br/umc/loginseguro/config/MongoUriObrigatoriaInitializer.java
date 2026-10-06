package br.umc.loginseguro.config;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Falha a subida da aplicacao com uma mensagem clara quando MONGODB_URI
 * nao foi definida (nem como variavel de ambiente, nem via arquivo .env).
 * Roda apos o ambiente ja ter o .env carregado e antes da auto-configuracao
 * do Mongo tentar resolver a URI, evitando um erro generico de placeholder.
 */
public class MongoUriObrigatoriaInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

	@Override
	public void initialize(ConfigurableApplicationContext applicationContext) {
		String uri = applicationContext.getEnvironment().getProperty("MONGODB_URI");
		if (uri == null || uri.isBlank()) {
			throw new IllegalStateException(
					"A variavel de ambiente MONGODB_URI nao foi definida. "
					+ "Copie o arquivo .env.example para .env na raiz do projeto e preencha "
					+ "a string de conexao do seu cluster do MongoDB Atlas antes de iniciar a aplicacao.");
		}
	}

}
