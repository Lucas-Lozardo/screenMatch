package br.com.alura.screenmatch.model;

import br.com.alura.screenmatch.service.ConsultaChatGPT;
import com.theokanning.openai.OpenAiHttpException;
import jakarta.persistence.*;


import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;

@Entity
@Table(name = "series")
public class Serie {

    public Serie() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String titulo;
    private Integer totalTemporadas;
    private Double avaliacao;

    @Enumerated(EnumType.STRING)
    private Categoria genero;
    private String ator;
    private String poster;
    private String sinops;

    @OneToMany(mappedBy = "serie", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Episodio> episodios = new ArrayList<>();

    public Serie(DadosSerie dadosSerie) {
        this.titulo = dadosSerie.titulo();
        this.totalTemporadas = dadosSerie.totalTemporadas();
        this.avaliacao = OptionalDouble.of(Double.parseDouble(dadosSerie.avaliacao())).orElse(0);
        try{
            this.genero = Categoria.valueOf(dadosSerie.genero());
        } catch (IllegalArgumentException e) {
        // Lidar com o erro, talvez atribuir um valor padrão ou lançar uma exceção personalizada
        System.out.println("Gênero inválido: " + dadosSerie.genero());
    }
        this.ator = dadosSerie.ator();
        this.poster = dadosSerie.poster();
        try{
            //this.sinops = ConsultaChatGPT.obterTraducao(dadosSerie.sinopse().trim());
            this.sinops = dadosSerie.sinopse().trim();
        } catch (OpenAiHttpException e) {
            System.out.println("Erro ao obter tradução: " + e.getMessage());
            this.sinops = "Tradução não disponível"; // ou algum valor padrão
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public Integer getTotalTemporadas() {
        return totalTemporadas;
    }

    public void setTotalTemporadas(Integer totalTemporadas) {
        this.totalTemporadas = totalTemporadas;
    }

    public Double getAvaliacao() {
        return avaliacao;
    }

    public void setAvaliacao(Double avaliacao) {
        this.avaliacao = avaliacao;
    }

    public Categoria getGenero() {
        return genero;
    }

    public void setGenero(Categoria genero) {
        this.genero = genero;
    }

    public String getAtor() {
        return ator;
    }

    public void setAtor(String ator) {
        this.ator = ator;
    }

    public String getPoster() {
        return poster;
    }

    public void setPoster(String poster) {
        this.poster = poster;
    }

    public String getSinops() {
        return sinops;
    }

    public void setSinops(String sinops) {
        this.sinops = sinops;
    }

    public List<Episodio> getEpisodios() {
        return episodios;
    }

    public void setEpisodios(List<Episodio> episodios) {
        episodios.forEach(e -> e.setSerie(this));  //Associação da CHAVE ESTRANGEIRA de episodios para série.
        this.episodios = episodios;
    }

    @Override
    public String toString() {
        return "Serie: " + "\n" +
                "Genero = " + genero  + "\n" +
                "Titulo = " + titulo + "\n" +
                "TotaldeTemporadas = " + totalTemporadas + "\n" +
                "Avaliacao = " + avaliacao + "\n" +
                "Atores = " + ator + "\n" +
                "Poster = " + poster + "\n" +
                "Sinops = " + sinops + "\n";


    }
}
