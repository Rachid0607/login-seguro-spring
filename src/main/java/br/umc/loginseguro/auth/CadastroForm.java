package br.umc.loginseguro.auth;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO do formulario publico de cadastro. Nao e o documento Usuario: o
 * service converte este DTO num Usuario (perfil sempre ALUNO, senha so
 * como hash).
 */
@SenhasCoincidem
public class CadastroForm {

	@NotBlank(message = "Informe o nome.")
	@Size(min = 3, max = 100, message = "O nome deve ter entre 3 e 100 caracteres.")
	@Pattern(regexp = "^[\\p{L} ]+$", message = "O nome deve conter apenas letras e espaços.")
	private String nome;

	@NotBlank(message = "Informe o e-mail.")
	@Email(message = "Informe um e-mail válido.")
	@DominioPermitido
	private String email;

	@NotBlank(message = "Informe a senha.")
	@Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,}$",
			message = "A senha deve ter no mínimo 8 caracteres, com ao menos uma letra e um número.")
	private String senha;

	@NotBlank(message = "Confirme a senha.")
	private String confirmacaoSenha;

	@AssertTrue(message = "É necessário aceitar os termos de uso e a política de privacidade.")
	private boolean aceiteTermos;

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getSenha() {
		return senha;
	}

	public void setSenha(String senha) {
		this.senha = senha;
	}

	public String getConfirmacaoSenha() {
		return confirmacaoSenha;
	}

	public void setConfirmacaoSenha(String confirmacaoSenha) {
		this.confirmacaoSenha = confirmacaoSenha;
	}

	public boolean isAceiteTermos() {
		return aceiteTermos;
	}

	public void setAceiteTermos(boolean aceiteTermos) {
		this.aceiteTermos = aceiteTermos;
	}

}
