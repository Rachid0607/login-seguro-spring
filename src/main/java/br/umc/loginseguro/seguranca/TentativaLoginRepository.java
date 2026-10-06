package br.umc.loginseguro.seguranca;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface TentativaLoginRepository extends MongoRepository<TentativaLogin, String> {

}
