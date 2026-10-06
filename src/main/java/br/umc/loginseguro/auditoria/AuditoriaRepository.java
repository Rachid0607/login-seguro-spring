package br.umc.loginseguro.auditoria;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface AuditoriaRepository extends MongoRepository<EventoAuditoria, String> {

	List<EventoAuditoria> findTop100ByOrderByDataHoraDesc();

}
