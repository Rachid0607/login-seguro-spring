package br.umc.loginseguro.seguranca;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;

import org.springframework.stereotype.Service;

import br.umc.loginseguro.usuario.UsuarioService;

/**
 * Bloqueio de 5 tentativas / 15 minutos, contado por conta (hash SHA-256 do
 * e-mail normalizado - nunca o e-mail puro) e por IP, o que estourar
 * primeiro. Login certo so zera o contador da conta.
 */
@Service
public class TentativaLoginService {

	private static final int MAX_TENTATIVAS = 5;
	private static final Duration JANELA_BLOQUEIO = Duration.ofMinutes(15);

	private final TentativaLoginRepository tentativaLoginRepository;

	public TentativaLoginService(TentativaLoginRepository tentativaLoginRepository) {
		this.tentativaLoginRepository = tentativaLoginRepository;
	}

	public boolean estaBloqueado(String email, String ip) {
		return estaBloqueadaChave(chaveConta(email)) || estaBloqueadaChave(chaveIp(ip));
	}

	private boolean estaBloqueadaChave(String chave) {
		return tentativaLoginRepository.findById(chave)
				.map(t -> t.getBloqueadoAte() != null && t.getBloqueadoAte().isAfter(Instant.now()))
				.orElse(false);
	}

	public void registrarFalha(String email, String ip) {
		incrementar(chaveConta(email));
		incrementar(chaveIp(ip));
	}

	public void registrarSucesso(String email) {
		tentativaLoginRepository.deleteById(chaveConta(email));
	}

	private void incrementar(String chave) {
		TentativaLogin tentativa = tentativaLoginRepository.findById(chave).orElseGet(() -> {
			TentativaLogin nova = new TentativaLogin();
			nova.setId(chave);
			nova.setTentativas(0);
			return nova;
		});

		tentativa.setTentativas(tentativa.getTentativas() + 1);
		Instant agora = Instant.now();
		if (tentativa.getTentativas() >= MAX_TENTATIVAS) {
			tentativa.setBloqueadoAte(agora.plus(JANELA_BLOQUEIO));
		}
		tentativa.setExpiraEm(agora.plus(JANELA_BLOQUEIO));
		tentativaLoginRepository.save(tentativa);
	}

	private static String chaveConta(String email) {
		return "conta:" + sha256(UsuarioService.normalizarEmail(email));
	}

	private static String chaveIp(String ip) {
		return "ip:" + (ip == null || ip.isBlank() ? "desconhecido" : ip);
	}

	private static String sha256(String valor) {
		if (valor == null) {
			valor = "";
		}
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(valor.getBytes(StandardCharsets.UTF_8));
			StringBuilder sb = new StringBuilder(hash.length * 2);
			for (byte b : hash) {
				sb.append(String.format("%02x", b));
			}
			return sb.toString();
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("SHA-256 indisponivel", e);
		}
	}

}
