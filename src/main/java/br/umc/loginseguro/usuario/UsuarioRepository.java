package br.umc.loginseguro.usuario;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface UsuarioRepository extends MongoRepository<Usuario, String> {

	Optional<Usuario> findByEmail(String email);

	boolean existsByEmail(String email);

	boolean existsByPerfil(Perfil perfil);

	List<Usuario> findByPerfil(Perfil perfil);

	long countByPerfil(Perfil perfil);

}
