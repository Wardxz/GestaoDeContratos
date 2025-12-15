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
    private static final Pattern OBRA_PATTERN = Pattern.compile(
            "Obra:\\s*[A-Z0-9-]+\\s*([A-Z0-9\\s.-]+?)\\s*Endereço:",
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

    // Regex 4: Tenta encontrar o cabeçalho para localizar o inicio dos dados
    private static final Pattern HEADER_PATTERN = Pattern.compile(
            "\"Tipo\"\\s*,\"[^,]+?\"\\s*,\"[^,]+?\"\\s*,\"[^,]+?\"\\s*,",
            Pattern.DOTALL
    );

    // Regex 5: Captura uma linha de dados inteira
    private static final Pattern RECORD_PATTERN = Pattern.compile(
            "(\"[A-Z0-9._-]+?\"\\s*,.*?\"[\\d.,]+?\")" // Começa com "TIPO", termina com "PESO (KG)"
    );

    // Indices dos campos chave, após o split (pode variar de PDF para PDF!)
    private static final int IDX_TIPO = 0;
    private static final int IDX_QTDE = 1;

    // Colunas de L e H
    private static final int IDX_L_DEFAULT = 3;
    private static final int IDX_H_DEFAULT = 4;
    private static final int IDX_L_COMPRESSED = 2;
    // (No padrão comprimido, LH é o campo 2)

    // Colunas de Localização e Peso (exemplo de como elas variam)
    // L, H, A, B antes de Localização (4 campos no meio, 4+4=8, Localização 9, Peso 11)
    private static final int OFFSET_DEFAULT = 4;
    private static final int IDX_PESO_DEFAULT = 11; // 12 campos no total (10 visíveis + 2 Area/Peso Unit)

    // Função com Apache PDFBox para extrair o texto
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

        // Limpa quebras de linha para tornar um registro CSV em uma única linha, facilitando o split
        String cleanText = rawText
                .replaceAll("\r?\n\"([A-Z])", " \"$1") // remove quebra antes de nova linha de item
                .replaceAll("\"\r?\n\"", "\", \"") // Substitui "\n" entre campos por ","
                .replaceAll("\r?\n", " "); // Substitui quebras de linha restantes por espaço

        Matcher headerMatcher = HEADER_PATTERN.matcher(rawText); // Usa o rawText para a detecção de padrão

        // Detecta o cabeçalho e ajusta os índices
        boolean isCompressed = rawText.contains("\"LH\n\"") || rawText.contains("\"LH\r\n\"");
        System.out.println(isCompressed ? "Detectado Padrão Comprimido." : "Detectado Padrão Default.");

        // Extração de Dados do Cabeçalho
        String numeroRequisicao = "N/A";
        Matcher reqMatcher = Pattern.compile("REQ\\.(\\d+)").matcher(arquivo.getName());
        if (reqMatcher.find()) {
            numeroRequisicao = reqMatcher.group(1);
        }

        Matcher dataMatcher = DATA_PATTERN.matcher(cleanText);
        String data = dataMatcher.find() ? dataMatcher.group(1).trim() : "N/A";

        Matcher obraMatcher = OBRA_PATTERN.matcher(cleanText);
        String nomeObra = "N/A";
        if (obraMatcher.find() && obraMatcher.group(1) != null) {
            nomeObra = obraMatcher.group(1).trim().replaceAll("\\s+", "");
        }

        // Fallback da Obra
        if (nomeObra.equals("N/A")) {
            Matcher csvObraMatcher = Pattern.compile("(POE|DF STAR)\\s*[0-9A-Z-]+\\s*(.+?)\\s*[\\r\\n]",
                    Pattern.CASE_INSENSITIVE).matcher(rawText);
        }

        Matcher linhaMatcher = LINHA_PATTERN.matcher(cleanText);
        String linhaMaterial = "";
        if (linhaMatcher.find() && linhaMatcher.group(1) != null) {
            linhaMaterial = linhaMatcher.group(1).trim().replaceAll("\\s+", "");
        }

        // Extração de Itens (Heurística de Parsing)
        Matcher itemMatcher = RECORD_PATTERN.matcher(cleanText);

        while (itemMatcher.find()) {
            String record = itemMatcher.group(1);

            // Remove as aspas do inicio e do fim do Registro
            record = record.substring(1, record.length() - 1);

            // CSV-like Split
            String[] fields = record.split("\",\"|,,"); // Separa por "," ou por campo vazio

            try {
                // A tipologia (Tipo) é sempre o primeiro campo
                String tipologia = removeQuotes(fields[IDX_TIPO]);
                int quantidade = Integer.parseInt(removeQuotes(fields[IDX_QTDE]));

                double largura;
                double altura;
                String localizacao;
                double pesoKg;

                if (isCompressed) {
                    String lhField = fields[IDX_L_COMPRESSED].trim().replaceAll("\"", " ");
                    String[] lh = lhField.split("\\s+"); // Separa por espaço

                    if (lh.length < 2) continue; // Pula se não conseguir separar

                    largura = parseDimension(lh[0]);
                    altura = parseDimension(lh[1]);

                    // Padrão COMPRIMIDO é mais simples: Tipo, Qtde, LH, A, B Loc, Área, Peso Unit, Peso(Kg)
                    localizacao = removeQuotes(fields[4]).replaceAll("[\r\n]+", " ");

                    // Peso (Kg) é o último campo
                    pesoKg = parseDimension(fields[7]);
                } else {
                    // Padrão Default: L e H nos campos 3 e 4
                    largura = parseDimension(fields[IDX_L_DEFAULT]);
                    altura = parseDimension(fields[IDX_H_DEFAULT]);

                    // Variação grande.
                    int locIdx = fields.length > 8 ? 8 : 6; // Heurística
                    int pesoIdx = fields.length > 10 ? 10 : 8;

                    if (locIdx >= fields.length || pesoIdx >= fields.length) continue; // Pula se o array for menor

                    localizacao = removeQuotes(fields[locIdx]).replaceAll("[\r\n]+", " ");
                    pesoKg = parseDimension(fields[pesoIdx]);
                }

                // Criação do Objeto ItemRequisição
                ItemRequisicao item = new ItemRequisicao(
                        nomeObra, data, numeroRequisicao, tipologia,
                        largura, altura, quantidade, localizacao,
                        pesoKg, linhaMaterial, "Importado de PDF. Req: " + numeroRequisicao
                );

                itens.add(item);

            } catch (NumberFormatException e) {
                System.err.println("Erro de conversão (número) na linha do PDF. Ignorando item: " + record);
            } catch (ArrayIndexOutOfBoundsException e) {
                System.err.println("Erro de estrutura (índices) na linha do PDF. Ignorando item: " + record);
            } catch (Exception e) {
                System.err.println("Erro inesperado ao processar item: " + e.getMessage());
            }
        }

        if (itens.isEmpty()) {
            throw new RuntimeException("Nenhum item válido encontrado no PDF. " +
                    "O padrão do layout está muito fora do esperado.");
        }

        return itens;
    }

    // Função auxiliar para limpar as aspas e campos vazios
    private static String removeQuotes(String field) {
        if (field == null) return "";
        return field.trim().replaceAll("\"", " ").replaceAll("[\r\n]+", " ");
    }

    // Função auxiliar para parsear as dimensões
    private static double parseDimension(String dim) throws NumberFormatException {
        return Double.parseDouble(removeQuotes(dim).replaceAll("\\.", "").replace(",", "."));
    }
}