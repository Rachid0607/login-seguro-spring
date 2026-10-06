package br.umc.loginseguro.config;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

/**
 * Faz um ping no MongoDB Atlas na subida da aplicacao e confirma a conexao no log.
 * A URI de conexao nunca e logada, apenas o nome do banco.
 */
@Component
public class VerificadorConexaoMongo implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(VerificadorConexaoMongo.class);

	private final MongoTemplate mongoTemplate;

	public VerificadorConexaoMongo(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Override
	public void run(ApplicationArguments args) {
		mongoTemplate.getDb().runCommand(new Document("ping", 1));
		log.info("Conectado ao MongoDB Atlas (banco {})", mongoTemplate.getDb().getName());
	}

}
