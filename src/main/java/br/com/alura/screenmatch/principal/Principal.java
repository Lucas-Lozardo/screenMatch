package br.com.alura.screenmatch.principal;

import br.com.alura.screenmatch.model.*;
import br.com.alura.screenmatch.repository.SerieRepository;
import br.com.alura.screenmatch.service.ConsumoApi;
import br.com.alura.screenmatch.service.ConverteDados;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.stream.Collectors;

public class Principal {

    private Scanner leitura = new Scanner(System.in);
    private ConsumoApi consumo = new ConsumoApi();
    private ConverteDados conversor = new ConverteDados();
    private final String ENDERECO = "https://www.omdbapi.com/?t=";
    private final String API_KEY = "&apikey=6585022c";
    private List<DadosSerie> dadosSeries = new ArrayList<>();
    private List<Serie> series = new ArrayList<>();

    @Autowired
    private SerieRepository repository;


    public Principal(SerieRepository repository) {
        this.repository = repository;
    }

    public void exibeMenu() {
        var opcao = -1;
        while(opcao != 0) {
            var menu = """
                    1 - Buscar séries na Web
                    2 - Buscar episódios
                    3 - Listar Séries Buscadas
                    4 - Buscar Série por título
                    5 - Buscar Série por ator
                    6 - Buscar Top Séries 5
                    7 - Buscar Séries por Categoria
                    8 - Buscar Episódios por Trecho
                    9 - Buscar Séries por total de temporadas e avaliação desejada
                    10 - Apagar séries
                    
                    0 - Sair
                    """;

            System.out.println(menu);

            try{
                opcao = Integer.parseInt(leitura.nextLine());
            } catch (NumberFormatException e){
                System.out.println("Digite um número válido! \n");
                opcao = -1;
                continue;
            }


            switch (opcao) {
                case 1:
                    buscarSerieWeb();
                    break;
                case 2:
                    buscarEpisodioPorSerie();
                    break;
                case 3:
                    listarSeriesBuscadas();
                    break;
                case 4:
                    buscarSeriePorTitulo();
                    break;
                case 5:
                    buscarSeriePorAtor();
                case 6:
                    buscarTop5Series();
                    break;
                case 7:
                    buscarSeriesPorCategoria();
                    break;
                case 8:
                    buscarSeriesPorTotalDeTemporadasEAvaliacao();
                    break;
                case 9:
                    buscarEpisodioPorTrecho();
                    break;
                case 10:
                    apagarSerieWeb();
                    break;
                case 0:
                    System.out.println("Saindo...");
                    break;
                default:
                    System.out.println("Opção inválida");
            }
        }
    }


    private void buscarSerieWeb() {
        DadosSerie dados = getDadosSerie();
        try{
            Serie serie = new Serie(dados);
            repository.save(serie);
            System.out.println(dados);
        } catch (RuntimeException e) {
            System.out.println("Esta série já consta no banco de dados: " + dados.titulo());
        }

    }

    private DadosSerie getDadosSerie() {
        System.out.println("Digite o nome da série para busca");
        var nomeSerie = leitura.nextLine();
        var json = consumo.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + API_KEY);
        DadosSerie dados = conversor.obterDados(json, DadosSerie.class);
        return dados;
    }

    private void buscarEpisodioPorSerie(){
        listarSeriesBuscadas();
        System.out.println("Escolha uma série pelo nome:");
        var nomeSerie = leitura.nextLine();

        Optional<Serie> serie = repository.findByTituloContainingIgnoreCase(nomeSerie); //MAIS OTIMIZADO

        // PODERIA SER FEITO DESSA FORMA Utilizando STREAM
        // Optional<Serie> serie = series.stream()
        //        .filter(s -> s.getTitulo().toLowerCase().contains(nomeSerie.toLowerCase()))
        //       .findFirst();

        if(serie.isPresent()){
            var serieEncontrada = serie.get();
            List<DadosTemporada> temporadas = new ArrayList<>();

            for (int i = 1; i <= serieEncontrada.getTotalTemporadas(); i++) {
                var json = consumo.obterDados(ENDERECO
                        + serieEncontrada.getTitulo().replace(" ", "+")
                        + "&season="
                        + i
                        + API_KEY);
                DadosTemporada dadosTemporada = conversor.obterDados(json, DadosTemporada.class);
                temporadas.add(dadosTemporada);
            }
            temporadas.forEach(System.out::println);

            List<Episodio> episodios = temporadas.stream()
                    .flatMap(d -> d.episodios().stream()
                            .map(e -> new Episodio(d.numero(), e)))
                    .collect(Collectors.toList());

            serieEncontrada.setEpisodios(episodios);
            repository.save(serieEncontrada);
        } else {
            System.out.println("Série não encontrada!!!");
        }
    }

    private void buscarSeriePorTitulo(){
        System.out.println("Digite o nome da série: ");
        String serie = leitura.nextLine();
        Optional<Serie> series = repository.findByTituloContainingIgnoreCase(serie);
        if (series.isEmpty()){
            System.out.println("""
                    Série não localizada no banco de dados!!!\s
                    Favor digitar 1 no menu principal e adicionar a série da web no banco de dados primeiro!\s
                    """);
        } else {
            System.out.println(series);
        }


    }

    private void listarSeriesBuscadas(){
        series = repository.findAll();
        if (series.isEmpty()){
            System.out.println("Nào existem séries listadas nesse banco de dados!!! \n");
        } else {
            series.stream()
                    .sorted(Comparator.comparing(Serie::getGenero, Comparator.nullsLast(Comparator.naturalOrder())))
                    .forEach(System.out::println);
        }


    }

    private void buscarSeriePorAtor() {
        System.out.println("Digite o nome de um ator/atriz da série: ");
        String nomeAtor = leitura.nextLine();
        List<Serie> seriesEncontradas = repository.findByAtorContainingIgnoreCase(nomeAtor);
        if (seriesEncontradas.isEmpty()){
            System.out.println("Nenhuma série encontrada com este ator/atriz");
        } else {
            System.out.println("Série(s) encontrada(s) com " + nomeAtor + ": ");
            seriesEncontradas.forEach(s -> System.out.println(s.getTitulo() + " Avaliação da Série: "
                    + s.getAvaliacao()
                    + " poster: " + s.getPoster()));
        }
    }

    private void buscarTop5Series(){
        List<Serie> top5 = repository.findTop5ByOrderByAvaliacaoDesc();
        System.out.println("Séries Top 5:");
        top5.forEach(t -> System.out.println(t.getTitulo() + " Avaliação: " + t.getAvaliacao()));
    }

    private void buscarSeriesPorCategoria(){
        System.out.println("Digite a categoria da série que deseja: ");
        String categoriaEscolhida = leitura.nextLine();
        Categoria categoria = Categoria.fromStringPortugues(categoriaEscolhida);
        List<Serie> series = repository.findByGenero(categoria);
        System.out.println("Séries da categoria " + categoriaEscolhida + " encontradas: ");
        series.forEach(System.out::println);
    }

    private void buscarSeriesPorTotalDeTemporadasEAvaliacao(){
        System.out.println("Digite o total de temporadas da série que deseja: ");
        int totalTemporadasEscolhida = leitura.nextInt();
        System.out.println("Digite a avaliação mínima da série que deseja: ");
        double avaliacaoEscolhida = leitura.nextDouble();
        List<Serie> series = repository
                .seriesPorTemporadaEAvaliacao(totalTemporadasEscolhida, avaliacaoEscolhida);
               // .findByTotalTemporadasLessThanEqualAndAvaliacaoGreaterThanEqual(totalTemporadasEscolhida, avaliacaoEscolhida); PODE SER FEITO DESSA FORMA
        System.out.println("Séries encontradas:");
        series.forEach(serie -> System.out.println(serie.getTitulo()
                + " Total de Temporadas: "
                + serie.getTotalTemporadas()
                + " Avaliação: "
                + serie.getAvaliacao()));
    }

    private void buscarEpisodioPorTrecho(){
        System.out.println("Qual o nome do episódio para busca?");
        var trechoEpisodio = leitura.nextLine();
        List<Episodio> episodiosEncontrados = repository.episodiosPorTrecho(trechoEpisodio);
        episodiosEncontrados.forEach(System.out::println);

    }

    private void apagarSerieWeb() {
        System.out.println("Digite o nome da série para apagar");
        String serie = leitura.nextLine();
        Optional<Serie> series = repository.findByTituloContainingIgnoreCase(serie);
        if (series.isPresent()){
            repository.deleteById(series.get().getId());
            System.out.println(series + "\n Série deletada com sucesso!!!");
        } else {
            System.out.println(series + "\n Série não encontrada!!!");
        }
    }
}