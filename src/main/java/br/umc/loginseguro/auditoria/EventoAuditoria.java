package br.umc.loginseguro.auditoria;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Evento de auditoria. Nunca guarda senha nem o e-mail digitado numa
 * tentativa que falhou - so o motivo generico e, quando existir, o id do
 * usuario (nao o e-mail).
 */
@Document(collection = "auditoria")
public class EventoAuditoria {

	@Id
	private String id;

	private TipoEvento tipo;

	private String usuarioId;

	private Instant dataHora;

	private String motivo;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public TipoEvento getTipo() {
		return tipo;
	}

	public void setTipo(TipoEvento tipo) {
		this.tipo = tipo;
	}

	public String getUsuarioId() {
		return usuarioId;
	}

	public void setUsuarioId(String usuarioId) {
		this.usuarioId = usuarioId;
	}

	public Instant getDataHora() {
		return dataHora;
	}

	public void setDataHora(Instant dataHora) {
		this.dataHora = dataHora;
	}

	public String getMotivo() {
		return motivo;
	}

	public void setMotivo(String motivo) {
		this.motivo = motivo;
	}

}
