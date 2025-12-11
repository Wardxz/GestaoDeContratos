// Objetivo: Criar a interface gráfica para entrada de dados

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class FormularioRequisicao extends JFrame {
    // Reutiliza o Gerenciador de Dados
    private GerenciadorDados gerenciador = new GerenciadorDados();

    private JTextField txtNomeObra = new JTextField(20);
    private JTextField txtData = new JTextField(20);
    private JTextField txtNumeroRequisicao = new JTextField(20);
    private JTextField txtTipologia = new JTextField(20);
    private JTextField txtLargura = new JTextField(20);
    private JTextField txtAltura = new JTextField(20);
    private JTextField txtQuantidade = new JTextField(20);
    private JTextField txtLocalizacao = new JTextField(20);
    private JTextField txtPesoKg = new JTextField(20);
    private JTextField txtLinhaMaterial = new JTextField(20);
    private JTextField txtObservacoes = new JTextField(20);

    private JButton btnSalvar = new JButton("Salvar Requisicao");
    private JButton btnExibir = new JButton("Exibir no Console");
    private JButton btnRemover = new JButton("Remover Requisição");

    // Construtor do Formulário
    public FormularioRequisicao() {
        super("Registro de Perfis de Itens de Requisicao");

        // Configurações básicas da janela
        // Adiciona um listener para salvar os dados ao fechar a janela
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                System.out.println("Fechando o programa. Os dados foram salvos no último registro.");
                System.exit(0);
            }
        });

        // Aumenta o tamanho da janela
        setSize(900, 650);
        setLayout(new BorderLayout());

        JPanel painelEntrada = criarPainelEntrada();
        JScrollPane scrollPane = new JScrollPane(painelEntrada);
        add(scrollPane, BorderLayout.CENTER);
        // JScrollPane caso a janela fique pequena, permite a rolagem

        JPanel painelBotoes = criarPainelBotoes();
        add(painelBotoes, BorderLayout.SOUTH);

        adicionarListeners();

        setVisible(true);
    }

    // Função que cria o Painel com os rótulos e campos de entrada
    private JPanel criarPainelEntrada() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.gray), "Dados da Requisição (Obrigações em negrito)"));
        panel.setBackground(Color.decode("#F5F5F5")); // Borda e Fundo Cinza Claro

        // Configurações Padrão de Espaçamento e Preenchimento
        gbc.insets = new Insets(5, 5, 5, 5); // Margem

        int row = 0; // Controle de Linhas

        // --- RÓTULO E CAMPO  ---

        // Configuração para RÓTULOS (Coluna 0 e 2): Peso Zero, Ancorado na Esquerda
        GridBagConstraints gbc_label = new GridBagConstraints();
        gbc_label.insets = new Insets(5, 5, 5, 5);
        gbc_label.anchor = GridBagConstraints.WEST; // ANCORA O RÓTULO À ESQUERDA DA CÉLULA
        gbc_label.weightx = 0.0;

        // Configuração para CAIXAS DE TEXTO (Coluna 1): Peso MÍNIMO (Impede que o campo se afaste do seu rótulo)
        GridBagConstraints gbc_field_left = new GridBagConstraints();
        gbc_field_left.insets = new Insets(5, 5, 5, 5);
        gbc_field_left.fill = GridBagConstraints.HORIZONTAL;
        gbc_field_left.weightx = 0.3;

        // Configuração para CAIXAS DE TEXTO (Coluna 3): Peso MÁXIMO
        GridBagConstraints gbc_field_right = new GridBagConstraints();
        gbc_field_right.insets = new Insets(5, 5, 5, 5);
        gbc_field_right.fill = GridBagConstraints.HORIZONTAL;
        gbc_field_right.weightx = 0.7;

        // LINHA 1: NOME DA OBRA E DATA
        gbc_label.gridx = 0; gbc_label.gridy = row; panel.add(new JLabel("<html><b>1. " +
                "Nome da Obra:</b></html>"), gbc_label);
        gbc_field_left.gridx = 1; gbc_field_left.gridy = row; panel.add(txtNomeObra, gbc_field_left);
        gbc_label.gridx = 2; gbc_label.gridy = row; panel.add(new JLabel("<html><b>2. " +
                "Data:</b></html>"), gbc_label);
        gbc_field_right.gridx = 3; gbc_field_right.gridy = row; panel.add(txtData, gbc_field_right);
        row++;


        // LINHA 2: N° REQUISIÇÃO E TIPOLOGIA
        gbc_label.gridx = 0; gbc_label.gridy = row; panel.add(new JLabel("<html><b>3. " +
                "Nº Requisição:</b></html>"), gbc_label);
        gbc_field_left.gridx = 1; gbc_field_left.gridy = row; panel.add(txtNumeroRequisicao, gbc_field_left);
        gbc_label.gridx = 2; gbc_label.gridy = row; panel.add(new JLabel("<html><b>4. " +
                "Tipologia:</b></html>"), gbc_label);
        gbc_field_right.gridx = 3; gbc_field_right.gridy = row; panel.add(txtTipologia, gbc_field_right);
        row++;

        // LINHA 3: LARGURA E ALTURA
        gbc_label.gridx = 0; gbc_label.gridy = row; panel.add(new JLabel("<html><b>5. " +
                "Largura (mm):</b></html>"), gbc_label);
        gbc_field_left.gridx = 1; gbc_field_left.gridy = row; panel.add(txtLargura, gbc_field_left);
        gbc_label.gridx = 2; gbc_label.gridy = row; panel.add(new JLabel("<html><b>6. " +
                "Altura (mm):</b></html>"), gbc_label);
        gbc_field_right.gridx = 3; gbc_field_right.gridy = row; panel.add(txtAltura, gbc_field_right);
        row++;

        // LINHA 4: QUANTIDADE E LOCALIZAÇÃO
        gbc_label.gridx = 0; gbc_label.gridy = row; panel.add(new JLabel("<html><b>7. " +
                "Quantidade:</b></html>"), gbc_label);
        gbc_field_left.gridx = 1; gbc_field_left.gridy = row; panel.add(txtQuantidade, gbc_field_left);
        gbc_label.gridx = 2; gbc_label.gridy = row; panel.add(new JLabel("<html><b>8. " +
                "Localização:</b></html>"), gbc_label);
        gbc_field_right.gridx = 3; gbc_field_right.gridy = row; panel.add(txtLocalizacao, gbc_field_right);
        row++;

        // LINHA 5: PESO E LINHA DO MATERIAL
        gbc_label.gridx = 0; gbc_label.gridy = row; panel.add(new JLabel("<html><b>9. " +
                "Peso (kg):</b></html>"), gbc_label);
        gbc_field_left.gridx = 1; gbc_field_left.gridy = row; panel.add(txtPesoKg, gbc_field_left);
        gbc_label.gridx = 2; gbc_label.gridy = row; panel.add(new JLabel("10. Linha (Opcional):"), gbc_label);
        gbc_field_right.gridx = 3; gbc_field_right.gridy = row; panel.add(txtLinhaMaterial, gbc_field_right);
        row++;

        // LINHA 6: OBSERVAÇÕES
        gbc.gridx = 0; gbc.gridy = row;
        gbc.anchor = GridBagConstraints.WEST; // Garante que o rótulo "Observações" fique na esquerda
        gbc.weightx = 0.0;
        panel.add(new JLabel("Observações:"), gbc); // GBC genérico (gbc) para as Observações

        gbc.gridx = 1;
        gbc.gridwidth = 3; // Ocupa as 3 colunas restantes
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(txtObservacoes, gbc);

        gbc.gridwidth = 1; // Reseta o gridwidth

        // Adiciona um componente vazio no final para empurrar o conteúdo para o topo
        gbc.gridx = 0; gbc.gridy = row + 1; gbc.weighty = 1.0; panel.add(new JLabel(""), gbc);

        return panel;
    }

    // Função para criar e organizar botões do painel
    private JPanel criarPainelBotoes() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        panel.add(btnSalvar);
        panel.add(btnExibir);
        panel.add(btnRemover);
        return panel;
    }

    // Função para adicionar funcionalidade aos botões
    private void adicionarListeners() {
        btnSalvar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                salvarItemRequisicao();
            }
        });

        btnExibir.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gerenciador.exibirTodosItens();
            }
        });

        btnRemover.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                removerRequisicao();
            }
        });
    }

    // Função principal de coleta de dados e salvamento de dados - Lógica de Conversão
    private void salvarItemRequisicao() {
        try {
            // 1. Coletar Dados de Texto (String)
            String nomeObra = txtNomeObra.getText().trim();
            String data = txtData.getText().trim();
            String requisicao = txtNumeroRequisicao.getText().trim();
            String tipologia = txtTipologia.getText().trim();
            String localizacao = txtLocalizacao.getText().trim();
            String linhaMaterial = txtLinhaMaterial.getText().trim();
            String observacoes = txtObservacoes.getText().trim();

            // 2. Validação Simples de campos obrigatórios (linha e obs são exceções)
            if (nomeObra.isEmpty() || data.isEmpty() || requisicao.isEmpty() || tipologia.isEmpty() ||
                    txtLargura.getText().trim().isEmpty() || txtAltura.getText().trim().isEmpty() ||
                    txtQuantidade.getText().trim().isEmpty() || txtLocalizacao.getText().trim().isEmpty() ||
                    txtPesoKg.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Campos 1 a 9 são obrigatórios!",
                        "Erro de validação", JOptionPane.ERROR_MESSAGE);
            }

            // 3. Conversão de Tipos (Possibilidade de NumberFormatException)
            double largura = Double.parseDouble(txtLargura.getText().trim());
            double altura = Double.parseDouble(txtAltura.getText().trim());
            int quantidade = Integer.parseInt(txtQuantidade.getText().trim());
            double pesoKg = Double.parseDouble(txtPesoKg.getText().trim());

            // 4. Criação do objeto ItemRequisicao
            ItemRequisicao novoItem = new ItemRequisicao(nomeObra, data, requisicao, tipologia,
                    largura, altura, quantidade, localizacao,
                    pesoKg, linhaMaterial, observacoes);

            // 5. Salvar o objeto
            gerenciador.adicionarItem(novoItem);

            // 6. Feedback e Limpeza
            JOptionPane.showMessageDialog(this, "Requisição salva com sucesso! " +
                    "(Verifique o console)", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            limparCampos();

        } catch (NumberFormatException ex) {
            // Captura o erro se a Largura, Altura, Quantidade ou Peso não forem números válidos
            JOptionPane.showMessageDialog(this, "Erro: Largura, Altura, Quantidade" +
                    " e Peso devem ser números válidos.", "Erro de Entrada", JOptionPane.ERROR_MESSAGE);

        } catch (Exception ex) {
            // Erro para outros casos
            JOptionPane.showMessageDialog(this, "Erro inesperado ao salvar: " + ex.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void removerRequisicao() {
        // Pede ao usuário o número da requisição que deseja remover
        String reqParaRemover = JOptionPane.showInputDialog(this,
                "Digite o Nº de Requisição para remover:");

        if (reqParaRemover != null && !reqParaRemover.trim().isEmpty()) {

            // Confirmação antes de excluir
            int confirmacao = JOptionPane.showConfirmDialog(this,
                    "Tem certeza que deseja remover a Requisição Nº " + reqParaRemover + "?",
                    "Confirmar Exclusão", JOptionPane.YES_NO_OPTION);

            if (confirmacao == JOptionPane.YES_OPTION) {
                // Chama a lógica de remoção no GerenciadorDados
                if (gerenciador.removerItemPorRequisicao(reqParaRemover.trim())) {
                    JOptionPane.showMessageDialog(this, "Requisição Nº " + reqParaRemover +
                            " removida com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                    // Opcional: Chama exibirTodosItens() para mostrar a lista atualizada no console
                    gerenciador.exibirTodosItens();
                } else {
                    JOptionPane.showMessageDialog(this, "Requisição Nº " + reqParaRemover +
                            " não encontrada.", "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    // Limpa todos os campos de texto do Formulário
    private void limparCampos() {
        txtTipologia.setText("");
        txtLargura.setText("");
        txtAltura.setText("");
        txtQuantidade.setText("");
        txtLocalizacao.setText("");
        txtPesoKg.setText("");
        txtLinhaMaterial.setText("");
        txtObservacoes.setText("");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new FormularioRequisicao();
            }
        });
    }
}

