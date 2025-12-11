// Objetivo: Definir a estrutura de dados para um item de requisição

public class ItemRequisicao {
    // Parâmetros para identificação
    private String nomeObra;
    private String data;
    private String numeroRequisicao;
    private String tipologia;
    private double largura; // Obs.: as medidas são em mm
    private double altura;
    private int quantidade;
    private String localizacao;
    private double pesoKg;
    private String linhaMaterial;

    private String observacoes; // Campo Extra

    public ItemRequisicao(String nomeObra, String data, String numeroRequisicao,
                          String tipologia, double largura, double altura, int quantidade,
                          String localizacao, double pesoKg, String linhaMaterial, String observacoes) {
        this.nomeObra = nomeObra;
        this.data = data;
        this.numeroRequisicao = numeroRequisicao;
        this.tipologia = tipologia;
        this.largura = largura;
        this.altura = altura;
        this.quantidade = quantidade;
        this.localizacao = localizacao;
        this.pesoKg = pesoKg;
        this.linhaMaterial = linhaMaterial;
        this.observacoes = observacoes;
    }

    public String getNomeObra() {
        return nomeObra;
    }

    public String getData() {
        return data;
    }

    public String getNumeroRequisicao() {
        return numeroRequisicao;
    }

    public String getTipologia() {
        return tipologia;
    }

    public double getLargura() {
        return largura;
    }

    public double getAltura() {
        return altura;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public String getLocalizacao() {
        return localizacao;
    }

    public double getPesoKg() {
        return pesoKg;
    }

    public String getLinhaMaterial() {
        return linhaMaterial;
    }

    public String getObservacoes() {
        return observacoes;
    }

    @Override
    public String toString() {
        return "Obra: " + nomeObra +
                " | Data: " + data +
                " | Requisição: " + numeroRequisicao +
                " | Tipologia: " + tipologia +
                " | L x A: " + largura + " x " + altura +
                " | Qtd: " + quantidade +
                " | Local: " + localizacao +
                " | Peso (kg): " + pesoKg +
                " | Linha: " + linhaMaterial +
                " | Obs: " + observacoes;
    }
}
