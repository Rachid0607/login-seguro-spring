package br.umc.loginseguro;

import br.umc.loginseguro.config.MongoUriObrigatoriaInitializer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LoginSeguroApplication {

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(LoginSeguroApplication.class);
		app.addInitializers(new MongoUriObrigatoriaInitializer());
		app.run(args);
	}

}
