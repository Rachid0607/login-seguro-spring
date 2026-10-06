package br.umc.loginseguro.seguranca;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Contador de tentativas de login por conta (hash SHA-256 do e-mail) ou por
 * IP. O id ja e a chave ("conta:<hash>" ou "ip:<endereco>"). O campo expiraEm
 * tem indice TTL (expireAfterSeconds = 0: expira exatamente no valor salvo),
 * atualizado a cada falha, para o registro desaparecer sozinho apos um
 * periodo de inatividade.
 */
@Document(collection = "tentativas_login")
public class TentativaLogin {

	@Id
	private String id;

	private int tentativas;

	private Instant bloqueadoAte;

	@Indexed(name = "tentativas_login_ttl", expireAfterSeconds = 0)
	private Instant expiraEm;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getTentativas() {
		return tentativas;
	}

	public void setTentativas(int tentativas) {
		this.tentativas = tentativas;
	}

	public Instant getBloqueadoAte() {
		return bloqueadoAte;
	}

	public void setBloqueadoAte(Instant bloqueadoAte) {
		this.bloqueadoAte = bloqueadoAte;
	}

	public Instant getExpiraEm() {
		return expiraEm;
	}

	public void setExpiraEm(Instant expiraEm) {
		this.expiraEm = expiraEm;
	}

}
