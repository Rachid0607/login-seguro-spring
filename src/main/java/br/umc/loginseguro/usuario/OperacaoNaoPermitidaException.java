package br.umc.loginseguro.usuario;

/**
 * Sinaliza uma operacao administrativa invalida (ex.: admin tentando
 * desativar ou alterar o proprio perfil, ou usuario alvo inexistente).
 */
public class OperacaoNaoPermitidaException extends RuntimeException {

	public OperacaoNaoPermitidaException(String mensagem) {
		super(mensagem);
	}

}
