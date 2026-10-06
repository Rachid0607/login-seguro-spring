package br.umc.loginseguro.auditoria;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class AuditoriaService {

	private final AuditoriaRepository auditoriaRepository;

	public AuditoriaService(AuditoriaRepository auditoriaRepository) {
		this.auditoriaRepository = auditoriaRepository;
	}

	public void registrar(TipoEvento tipo, String usuarioId, String motivo) {
		EventoAuditoria evento = new EventoAuditoria();
		evento.setTipo(tipo);
		evento.setUsuarioId(usuarioId);
		evento.setDataHora(Instant.now());
		evento.setMotivo(motivo);
		auditoriaRepository.save(evento);
	}

	public List<EventoAuditoria> listarRecentes() {
		return auditoriaRepository.findTop100ByOrderByDataHoraDesc();
	}

}
