// Objetivo: Gerencia a coleção de objetos ItemRequisicao
// Persistência em Arquivos JSON

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GerenciadorDados {
    // Lista para armazenar todos os itens de requisição em memória (Simula o Banco de Dados)
    private static List<ItemRequisicao> listaRequisicoes = new ArrayList<>();
    private final String NOME_ARQUIVO = "requisicoes.dat"; // Arquivo de Persistência

    public GerenciadorDados() {
        carregarDados();
    }

    // -------- Persistência de Dados -------------

    // Transforma a lista de objetos em JSON e salva no arquivo
    private void salvarDados() {
        try (FileWriter writer = new FileWriter(NOME_ARQUIVO)) {
            String dadosParaSalvar = listaRequisicoes.stream() // Leitura básica
                    .map(ItemRequisicao::toString) // Pega a representação String
                    .collect(Collectors.joining("\n")); // Junta com quebra de linha
            writer.write(dadosParaSalvar);
            System.out.println("Dados salvos no arquivo: " + NOME_ARQUIVO);
        } catch (IOException e) {
            System.err.println("Erro ao salvar dados: " + e.getMessage());
        }
    }

    // Lê o arquivo e popula a lista
    private void carregarDados() {
        File arquivo = new File(NOME_ARQUIVO);
        if (arquivo.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(arquivo))) {
                String line;
                listaRequisicoes.clear(); // Garante que a lista está vazia antes de carregar

                // Lê cada linha do arquivo e converte de volta para ItemRequisicao
                while ((line = reader.readLine()) != null) {
                    if (line.trim().isEmpty()) continue; // Ignora linhas vazias

                    // O ItemRequisicao.toString() usa o separador " | " e 10 campos
                    String[] parts = line.split(" \\| ");

                    if (parts.length != 10) {
                        System.err.println("Erro de formato ao carregar linha: " + line);
                        continue;
                    }

                    try {
                        // 1. Extração e limpeza do prefixo de cada campo (Ex: "Obra: " sai)
                        String nomeObra = parts[0].substring("Obra: ".length()).trim();
                        String data = parts[1].substring("Data: ".length()).trim();
                        String numeroRequisicao = parts[2].substring("Requisição: ".length()).trim();
                        String tipologia = parts[3].substring("Tipologia: ".length()).trim();

                        // 2. Extração e conversão de L x A (Ex: "1000.0 x 600.0")
                        String larguraAlturaStr = parts[4].substring("L x A: ".length()).trim();
                        String[] dim = larguraAlturaStr.split(" x ");
                        double largura = Double.parseDouble(dim[0].trim());
                        double altura = Double.parseDouble(dim[1].trim());

                        // 3. Extração e conversão de outros numéricos e strings
                        int quantidade = Integer.parseInt(parts[5].substring("Qtd: ".length()).trim());
                        String localizacao = parts[6].substring("Local: ".length()).trim();
                        double pesoKg = Double.parseDouble(parts[7].substring("Peso (kg): ".length()).trim());
                        String linhaMaterial = parts[8].substring("Linha: ".length()).trim();
                        String observacoes = parts[9].substring("Obs: ".length()).trim();

                        // 4. Recriação do objeto e adição à lista em memória
                        ItemRequisicao item = new ItemRequisicao(nomeObra, data, numeroRequisicao, tipologia,
                                largura, altura, quantidade, localizacao, pesoKg, linhaMaterial, observacoes);

                        listaRequisicoes.add(item);

                    } catch (NumberFormatException e) {
                        System.err.println("Erro de formato de número ao parsear linha: " + line);
                    } catch (Exception e) {
                        System.err.println("Erro inesperado ao parsear dados: " + e.getMessage());
                    }
                }

                System.out.println("Dados carregados com sucesso. Total de itens: " + listaRequisicoes.size());

            } catch (IOException e) {
                System.err.println("Erro ao carregar dados do arquivo: " + e.getMessage());
            }
        } else {
            System.out.println("Arquivo de persistência não encontrado. Iniciando com lista vazia.");
        }

    }

    // ---------------- Metódos CRUD ----------------

    // Função para criar um novo item de Requisição
    public void adicionarItem(ItemRequisicao item) {
        listaRequisicoes.add(item);
        salvarDados(); // Salva toda vez que um item é adicionado
        System.out.println("Item registrado com sucesso! Total de itens: " + listaRequisicoes.size());
    }

    // Função para poder LER todas as Requisições
    public List<ItemRequisicao> getTodosItens() {
        return listaRequisicoes;
    }

    // Função para exibir todos os itens no console (fins de debug)
    public void exibirTodosItens() {
        System.out.println("\n---- LISTA DE TODAS AS REQUISIÇÕES ----");
        if (listaRequisicoes.isEmpty()) {
            System.out.println("Nenhuma requisição registrada.");
            return;
        }
        for (int i = 0; i < listaRequisicoes.size(); i++) {
            System.out.println("Item [" + (i + 1) + "]: " + listaRequisicoes.get(i));
        }
        System.out.println("---------------------------");
    }

    // Função para deletar um item pela sua Requisição
    public boolean removerItemPorRequisicao(String numeroRequisicao) {
        boolean removido = listaRequisicoes.removeIf(item -> item.getNumeroRequisicao().
                equals(numeroRequisicao));
        if (removido) {
            salvarDados(); // Salva após a exclusão
        }
        return removido;
    }
}
