package br.umc.loginseguro.seguranca;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import br.umc.loginseguro.usuario.Perfil;
import br.umc.loginseguro.usuario.Usuario;

/**
 * Principal autenticado guardado na sessao. Precisa ser serializavel porque,
 * a partir da Etapa 7, a sessao passa a ser persistida no MongoDB.
 */
public class UsuarioAutenticado implements UserDetails, Serializable {

	private static final long serialVersionUID = 1L;

	private final String id;
	private final String nome;
	private final String email;
	private final String senhaHash;
	private final Perfil perfil;
	private final boolean ativo;

	public UsuarioAutenticado(Usuario usuario) {
		this.id = usuario.getId();
		this.nome = usuario.getNome();
		this.email = usuario.getEmail();
		this.senhaHash = usuario.getSenhaHash();
		this.perfil = usuario.getPerfil();
		this.ativo = usuario.isAtivo();
	}

	public String getId() {
		return id;
	}

	public String getNome() {
		return nome;
	}

	public Perfil getPerfil() {
		return perfil;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of(new SimpleGrantedAuthority("ROLE_" + perfil.name()));
	}

	@Override
	public String getPassword() {
		return senhaHash;
	}

	@Override
	public String getUsername() {
		return email;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return ativo;
	}

}
