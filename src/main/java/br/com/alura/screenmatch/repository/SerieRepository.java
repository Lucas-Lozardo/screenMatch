package br.com.alura.screenmatch.repository;

import br.com.alura.screenmatch.model.Categoria;
import br.com.alura.screenmatch.model.Serie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.List;
import java.util.Optional;

@EnableJpaRepositories
public interface SerieRepository extends JpaRepository<Serie, Long> {

    Optional<Serie> findByTituloContainingIgnoreCase(String nomeSerie);

    List<Serie> findByAtorContainingIgnoreCase(String nomeAtor);

    List<Serie> findTop5ByOrderByAvaliacaoDesc();

    List<Serie> findByGenero(Categoria categoria);
}
