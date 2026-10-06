package br.umc.loginseguro.seguranca;

import org.mongodb.spring.session.MongoIndexedSessionRepository;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.stereotype.Service;

/**
 * Encerra as sessoes ativas de um usuario (usado quando ele e desativado ou
 * tem o perfil alterado), aproveitando o indice por nome de principal que o
 * FindByIndexNameSessionRepository mantem automaticamente.
 */
@Service
public class SessaoUsuarioService {

	private final MongoIndexedSessionRepository sessionRepository;

	public SessaoUsuarioService(MongoIndexedSessionRepository sessionRepository) {
		this.sessionRepository = sessionRepository;
	}

	public void encerrarSessoesDoUsuario(String email) {
		sessionRepository
				.findByIndexNameAndIndexValue(FindByIndexNameSessionRepository.PRINCIPAL_NAME_INDEX_NAME, email)
				.keySet()
				.forEach(sessionRepository::deleteById);
	}

}
