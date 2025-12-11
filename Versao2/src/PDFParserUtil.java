import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PDFParserUtil {

    // Regex 1: Extrair nome da Obra
    // Procura por "Obra: [CÓDIGO] [NOME DA OBRA] Endereço:"
    private static final Pattern OBRA_PATTERN = Pattern.compile(
            "Obra:\\s*[A-Z0-9-]+\\s*(.+?)\\s*Endereço:",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );

    // Regex 2: Extrair Data de Emissão
    private static final Pattern DATA_PATTERN = Pattern.compile(
            "ETAPA:\\s*LIB.*-(\\d{2}/\\d{2}/\\d{4})",
            Pattern.CASE_INSENSITIVE
    );

    // Regex 3: Extrair Linha do Material
    private static final Pattern LINHA_PATTERN = Pattern.compile(
            "LINHA\\s*(.+?)[\\r\\n]",
            Pattern.CASE_INSENSITIVE
    );

    // Regex 4: Tabela de Itens (O mais critico) -> Adaptado para meus PDFs
    // Captura: (1)Tipo, (2)Qtde, (3)L, (4)H, (5)Localização, (6)Peso (kg)
    private static final Pattern ITEM_PATTERN = Pattern.compile(
        "\"([A-Z0-9.-]+)\"\r?\n?,\s*\"(\\d+)\"\r?\n?,\s*\"([\\d.,]+)\"\r?\n?,\s*(?:\"[\\d.,]+\"\r?\n?,){0,3}\s*\"([\\d.,]+)\"\r?\n?,{0,2}\s*\"([^\"]+)\"\r?\n?,\s*\"[\\d.,]+\"\r?\n?,\s*\"[\\d.,]+\"\r?\n?,\s*\"([\\d.,]+)\"\r?\n?",
        Pattern.DOTALL
    );

    // Função com APACHE PDFBOX para extrair o texto
    public static String extrairTextoDoPDF(File arquivo) throws IOException {
        PDDocument doc = null;
        try {
            doc = PDDocument.load(arquivo);
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(doc);
        } finally {
            if (doc != null) {
                doc.close();
            }
        }
    }

    // Função Principal de Parsing
    public static List<ItemRequisicao> parsePDF(File arquivo) throws IOException, RuntimeException {

        String rawText = extrairTextoDoPDF(arquivo);
        List<ItemRequisicao> itens = new ArrayList<>();

        // 1. Extração de Num. da Requisição (do nome do arquivo)
        String numeroRequisicao = "N/A";
        Matcher reqMatcher = Pattern.compile("REQ\\.(\\d+)").matcher(arquivo.getName());
        if (reqMatcher.find()) {
            numeroRequisicao = reqMatcher.group(1);
        }

        // 2. Extração de Dados de Cabeçalho

        // Data
        Matcher dataMatcher = DATA_PATTERN.matcher(rawText);
        String data = dataMatcher.find() ? dataMatcher.group(1).trim() : "N/A";

        // Nome da obra
        Matcher obraMatcher = OBRA_PATTERN.matcher(rawText);
        String nomeObra = "N/A";
        if (obraMatcher.find() && obraMatcher.group(1) != null) {
            nomeObra = obraMatcher.group(1).trim().replaceAll("[\r\n]+", " ");
        } else {
            // Fallback: tenta a primeira linha do PDF se o Regex principal falhar
            String[] lines = rawText.split("[\r\n]+");
            if (lines.length > 0 && lines[0].contains(" - ")) {
                nomeObra = lines[0].split(" - ")[0].trim();
            }
        }

        // Linha do material
        Matcher linhaMatcher = LINHA_PATTERN.matcher(rawText);
        String linhaMaterial = "";
        if (linhaMatcher.find() && linhaMatcher.group(1) != null) {
            // Remove quebra de linha e espaços múltiplos
            linhaMaterial = linhaMatcher.group(1).trim().replaceAll("[\r\n]+", " ");
        }

        // 3. Extração de Itens da Tabela
        Matcher itemMatcher = ITEM_PATTERN.matcher(rawText);

        while (itemMatcher.find()) {
            try {
                // Grupos da Regex: (1)Tipo, (2)Qtde, (3)L, (4)H, (5)Localização, (6)Peso (kg)
                String tipologia = itemMatcher.group(1).trim();
                int quantidade = Integer.parseInt(itemMatcher.group(2).trim());

                // Trata vírgula decimal (remove ponto de milhar se existir pra não ter erro)
                double largura = Double.parseDouble(itemMatcher.group(3).trim().replaceAll("\\.", "")
                        .replace(",", "."));
                double altura = Double.parseDouble(itemMatcher.group(4).trim().replaceAll("\\.", "")
                        .replace(",", "."));

                String localizacao = itemMatcher.group(5).trim().replaceAll("[\r\n]+", " ");

                double pesoKg = Double.parseDouble(itemMatcher.group(6).trim().replace(".", "")
                        .replace(",", "."));

                // Criação do Objeto ItemRequisicao
                ItemRequisicao item = new ItemRequisicao(
                        nomeObra, data, numeroRequisicao, tipologia,
                        largura, altura, quantidade, localizacao,
                        pesoKg, linhaMaterial, "Importado de PDF. Req: " + numeroRequisicao
                );
            } catch (NumberFormatException e) {
                System.err.println("Erro de conversão (número) na linha do PDF. Ignorando item.");
            } catch (Exception e) {
                System.err.println("Erro inesperado ao processar item: " + e.getMessage());
            }
        }
        if (itens.isEmpty()) {
            throw new RuntimeException("Nenhum item válido encontrado no PDF.");
        }

        return itens;
    }
}
