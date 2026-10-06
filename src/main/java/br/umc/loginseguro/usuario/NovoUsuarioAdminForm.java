package br.umc.loginseguro.usuario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO do formulario de criacao de usuario pelo administrador. Diferente do
 * cadastro publico, aqui o perfil vem do formulario, mas so SECRETARIA e
 * ADMINISTRADOR sao aceitos (reforcado no AdminUsuarioService).
 */
public class NovoUsuarioAdminForm {

	@NotBlank(message = "Informe o nome.")
	@Size(min = 3, max = 100, message = "O nome deve ter entre 3 e 100 caracteres.")
	@Pattern(regexp = "^[\\p{L} ]+$", message = "O nome deve conter apenas letras e espaços.")
	private String nome;

	@NotBlank(message = "Informe o e-mail.")
	@Email(message = "Informe um e-mail válido.")
	private String email;

	@NotBlank(message = "Informe a senha.")
	@Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,}$",
			message = "A senha deve ter no mínimo 8 caracteres, com ao menos uma letra e um número.")
	private String senha;

	@NotNull(message = "Selecione um perfil.")
	private Perfil perfil;

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

	public Perfil getPerfil() {
		return perfil;
	}

	public void setPerfil(Perfil perfil) {
		this.perfil = perfil;
	}

}
