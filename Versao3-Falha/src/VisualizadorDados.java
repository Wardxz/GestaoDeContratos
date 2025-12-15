import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.List;
import java.util.ArrayList;

public class VisualizadorDados extends JDialog {

    private GerenciadorDados gerenciador;
    private JTable tabelarequisicoes;
    private JButton btnExportar;

    public VisualizadorDados(JFrame parent, GerenciadorDados gerenciador) {
        // bloqueia o formulário principal estiver aberto
        super(parent, "Requisições Registradas (Visualizador/Exportador)", true);
        this.gerenciador = gerenciador;

        // Configurações básica da janela
        setSize(1400,700);
        setLayout(new BorderLayout());
        setLocationRelativeTo(parent);

        // 1. Criar e Popular a Tabela
        tabelarequisicoes = criarTabelaDados();

        //Ajusta a largura das colunas para melhor visualização
        tabelarequisicoes.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        JScrollPane scrollPane = new JScrollPane(tabelarequisicoes);
        add(scrollPane, BorderLayout.CENTER);

        // 2. Criar Painel de Botões
        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnExportar = new JButton("Exportar para Excel (.xlsx)");

        btnExportar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // SingWorker para evitar travar a interface
                exportarParaExcelComFeedback();
            }
        });
        painelBotoes.add(btnExportar);
        add(painelBotoes, BorderLayout.SOUTH);

        setVisible(true);
    }

    // Cria e preenche o JTable com os dados da lista em memória
    private JTable criarTabelaDados() {
        // Define as colunas, seguindo a ordem do ItemRequisicao
        String[] colunas = {
                "Nome Obra", "Data", "Nº Requisição", "Tipologia",
                "Largura (mm)", "Altura (mm)", "Quantidade", "Localização",
                "Peso (kg)", "Linha Material", "Observações"
        };

        DefaultTableModel model = new DefaultTableModel(colunas, 0) {
            // Torna todas as células não editáveis
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            // Garante que as colunas númericas tenham o tipo correto para ordenação
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                switch (columnIndex) {
                    // Técnica fall-through
                    case 4:
                    case 5:
                    case 8: return Double.class; // Largura, Altura e Peso
                    case 6: return Integer.class; // Quantidade
                    default: return String.class;
                }
            }
        };

        // Popula o modelo com os dados do Gerenciador
        List<ItemRequisicao> lista = gerenciador.getTodosItens();
        for (ItemRequisicao item : lista) {
            Object[] linha = {
                    item.getNomeObra(),
                    item.getData(),
                    item.getNumeroRequisicao(),
                    item.getTipologia(),
                    item.getLargura(),
                    item.getAltura(),
                    item.getQuantidade(),
                    item.getLocalizacao(),
                    item.getPesoKg(),
                    item.getLinhaMaterial(),
                    item.getObservacoes()
            };
            model.addRow(linha);
        }

        JTable table = new JTable(model);
        // Habilita a ordenação ao clicar no cabeçalho
        table.setAutoCreateRowSorter(true);
        return table;
    }

    // SingWorker para executar a exportação em segundo plano e atualizar a UI
    private void exportarParaExcelComFeedback() {
        // Desativa o botão enquanto a exportação está em andamento
        btnExportar.setEnabled(false);

        // Exibe um popup simples de carregamento
        final JOptionPane loadingPane = new JOptionPane("Exportando dados... Por favor, aguarde.",
                JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION, null, new Object[]{}, null);
        final JDialog loadingDialog = loadingPane.createDialog(this, "Processando");
        loadingDialog.setModal(false); // Permite interação com a interface se necessário
        loadingDialog.setVisible(true);

        new SwingWorker<File, Void>() {
            String errorMessage = null;

            @Override
            protected File doInBackground() throws Exception {
                return exportarDadosParaArquivo(); // Lógica principal de exportação roda aqui
            }

            @Override
            protected void done() {
                // Remove o popup de carregamento E Reativa o botão
                loadingDialog.dispose();
                btnExportar.setEnabled(true);

                try {
                    File arquivoSalvo = get();
                    if (arquivoSalvo != null) {
                        JOptionPane.showMessageDialog(VisualizadorDados.this,
                                "Dados exportados com sucesso para: \n" + arquivoSalvo.getAbsolutePath(),
                                "Sucesso na Exportação", JOptionPane.INFORMATION_MESSAGE);
                    }
                } catch (InterruptedException | ExecutionException e) {
                    errorMessage = "Erro inesperado durante a exportação: " + e.getCause().getMessage();
                    JOptionPane.showMessageDialog(VisualizadorDados.this,
                            errorMessage, "Erro de I/O", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    // Rotina de exportação: Copia os dados da JTable para um arquivo .xslx usando Apache POI
    private File exportarDadosParaArquivo() throws IOException {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Salvar tabela como Excel");

        // Define o nome do arquivo padrão
        fileChooser.setSelectedFile(new File("Requisicoes_Exportadas_" + System.currentTimeMillis() + ".xlsx" ));

        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File arquivoParaSalvar = fileChooser.getSelectedFile();

            // Garante a extensão .xlsx
            if (!arquivoParaSalvar.getAbsolutePath().toLowerCase().endsWith(".xlsx")) {
                arquivoParaSalvar = new File(arquivoParaSalvar.getAbsolutePath() + ".xlsx");
            }

            // Uso do Apache POI
            try (Workbook workbook = new XSSFWorkbook();
                 FileOutputStream fileOut = new FileOutputStream(arquivoParaSalvar)) {

                Sheet sheet = workbook.createSheet("Requisições");
                TableModel model = tabelarequisicoes.getModel();

                // 1. Cria o Cabeçalho (Header)
                Row headerRow = sheet.createRow(0);
                for (int i = 0; i < model.getColumnCount(); i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(model.getColumnName(i));
                }

                // 2. Preenche os Dados (Data Rows)
                for (int i = 0; i < model.getRowCount(); i++) {
                    Row row = sheet.createRow(i + 1);
                    for (int j = 0; j < model.getColumnCount(); j++) {
                        Cell cell = row.createCell(j);
                        Object value = model.getValueAt(i, j);

                        // Tenta definir o tipo de célula correto
                        if (value instanceof Number) {
                            cell.setCellValue(((Number) value).doubleValue());
                        } else if (value != null) {
                            cell.setCellValue(value.toString());
                        }
                    }
                }

                // Ajusta a largura das colunas
                for (int i = 0; i < model.getColumnCount(); i++) {
                    sheet.autoSizeColumn(i);
                }

                // Salva o arquivo
                workbook.write(fileOut);
                return arquivoParaSalvar;
            }
        }
        return null; // Retorna nulo se o usuário cancelar a operação de salvar
    }
}