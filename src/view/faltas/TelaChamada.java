
package view.faltas;

import conexao.ConexaoMySQL;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TelaChamada extends JFrame {

    private static final long serialVersionUID = 1L;

    private static final Color MARINHO =
            new Color(30, 27, 75);

    private static final Color ROXO =
            new Color(79, 70, 229);

    private static final Color ROXO_HOVER =
            new Color(67, 56, 202);

    private static final Color FUNDO =
            new Color(248, 250, 252);

    private static final Color AZUL_CLARO =
            new Color(238, 242, 255);

    private static final Color CINZA =
            new Color(100, 116, 139);

    private static final Color VERDE =
            new Color(22, 163, 74);

    private static final Color VERMELHO =
            new Color(220, 38, 38);

    private static final Color LARANJA =
            new Color(234, 139, 8);

    private final String nomeMateria;
    private final String cursoTurma;
    private final String codigoTurma;

    private int turmaId = -1;
    private int totalAulasDia;

    private JTextField txtBusca;
    private JTextField txtDataChamada;

    private JComboBox<Integer> cbQuantidadeAulas;

    private JLabel lblPresentes;
    private JLabel lblFaltas;
    private JLabel lblSemMarcacao;
    private JLabel lblTotal;
    private JLabel lblInformacao;

    private JTable tabelaAlunos;
    private DefaultTableModel modeloTabela;
    private TableRowSorter<DefaultTableModel> filtro;

    private JButton btnSalvar;

    private final List<Integer> idsAlunos =
            new ArrayList<>();

    private final DateTimeFormatter formatoData =
            DateTimeFormatter.ofPattern("dd/MM/uuuu")
                    .withResolverStyle(ResolverStyle.STRICT);

    public TelaChamada(
            String nomeMateria,
            String cursoTurma,
            String codigoTurma,
            int totalAulasDia) {

        this.nomeMateria = nomeMateria;
        this.cursoTurma = cursoTurma;
        this.codigoTurma = codigoTurma;
        this.totalAulasDia = Math.max(1, totalAulasDia);

        construirTela();
        carregarTurmaEAlunos();
    }


    private JLabel texto(
            String valor,
            int tamanho,
            Color cor,
            boolean negrito) {

        JLabel label = new JLabel(valor);

        label.setFont(new Font(
                "SansSerif",
                negrito ? Font.BOLD : Font.PLAIN,
                tamanho
        ));

        label.setForeground(cor);

        return label;
    }

    private JPanel painelArredondado(
            Color cor,
            int raio) {

        JPanel painel = new JPanel() {

            private static final long serialVersionUID = 1L;

            @Override
            protected void paintComponent(Graphics g) {

                Graphics2D g2 =
                        (Graphics2D) g.create();

                g2.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON
                );

                g2.setColor(cor);

                g2.fillRoundRect(
                        0,
                        0,
                        getWidth(),
                        getHeight(),
                        raio,
                        raio
                );

                g2.dispose();
            }
        };

        painel.setOpaque(false);

        return painel;
    }

    private JPanel card() {

        JPanel painel =
                painelArredondado(Color.WHITE, 22);

        painel.setBorder(
                new EmptyBorder(20, 22, 20, 22)
        );

        return painel;
    }

    private JButton botao(
            String titulo,
            boolean principal) {

        Color normal = principal
                ? ROXO
                : AZUL_CLARO;

        Color hover = principal
                ? ROXO_HOVER
                : new Color(224, 231, 255);

        JButton botao = new JButton(titulo) {

            private static final long serialVersionUID = 1L;

            private boolean mouseSobre = false;

            {
                addMouseListener(new MouseAdapter() {

                    @Override
                    public void mouseEntered(MouseEvent e) {
                        mouseSobre = true;
                        repaint();
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        mouseSobre = false;
                        repaint();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {

                Graphics2D g2 =
                        (Graphics2D) g.create();

                g2.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON
                );

                g2.setColor(
                        mouseSobre ? hover : normal
                );

                g2.fillRoundRect(
                        0,
                        0,
                        getWidth(),
                        getHeight(),
                        16,
                        16
                );

                g2.dispose();

                super.paintComponent(g);
            }
        };

        botao.setFont(
                new Font("SansSerif", Font.BOLD, 12)
        );

        botao.setForeground(
                principal ? Color.WHITE : MARINHO
        );

        botao.setOpaque(false);
        botao.setContentAreaFilled(false);
        botao.setBorderPainted(false);
        botao.setFocusPainted(false);

        botao.setBorder(
                new EmptyBorder(12, 18, 12, 18)
        );

        botao.setCursor(
                Cursor.getPredefinedCursor(
                        Cursor.HAND_CURSOR
                )
        );

        return botao;
    }

    private JPanel criarContador(
            String titulo,
            Color cor,
            JLabel numero) {

        JPanel painel =
                painelArredondado(Color.WHITE, 20);

        painel.setLayout(new BorderLayout(0, 8));

        painel.setBorder(
                new EmptyBorder(16, 18, 16, 18)
        );

        numero.setForeground(cor);

        painel.add(numero, BorderLayout.CENTER);

        painel.add(
                texto(titulo, 12, CINZA, false),
                BorderLayout.SOUTH
        );

        return painel;
    }

    private void construirTela() {

        setTitle("Presente! - Chamada");

        setSize(1180, 780);

        setMinimumSize(
                new Dimension(950, 650)
        );

        setLocationRelativeTo(null);

        setDefaultCloseOperation(
                JFrame.DISPOSE_ON_CLOSE
        );

        JPanel raiz =
                new JPanel(new BorderLayout());

        raiz.setBackground(FUNDO);

        setContentPane(raiz);

        // CABEÇALHO

        JPanel topo =
                new JPanel(new BorderLayout());

        topo.setBackground(MARINHO);

        topo.setBorder(
                new EmptyBorder(18, 32, 18, 32)
        );

        topo.add(
                texto(
                        "✓ Presente!",
                        23,
                        Color.WHITE,
                        true
                ),
                BorderLayout.WEST
        );

        topo.add(
                texto(
                        "REGISTRO DE FREQUÊNCIA",
                        12,
                        Color.WHITE,
                        true
                ),
                BorderLayout.EAST
        );

        raiz.add(topo, BorderLayout.NORTH);

        // CONTEÚDO PRINCIPAL

        JPanel conteudo = new JPanel();

        conteudo.setLayout(
                new BoxLayout(
                        conteudo,
                        BoxLayout.Y_AXIS
                )
        );

        conteudo.setBackground(FUNDO);

        conteudo.setBorder(
                new EmptyBorder(22, 32, 28, 32)
        );

        JScrollPane scroll =
                new JScrollPane(conteudo);

        scroll.setBorder(null);

        scroll.getVerticalScrollBar()
                .setUnitIncrement(18);

        raiz.add(scroll, BorderLayout.CENTER);

        // VOLTAR

        JButton voltar =
                new JButton("‹ Minhas turmas");

        voltar.setFont(
                new Font("SansSerif", Font.BOLD, 13)
        );

        voltar.setForeground(ROXO);
        voltar.setContentAreaFilled(false);
        voltar.setBorderPainted(false);
        voltar.setFocusPainted(false);

        voltar.setCursor(
                Cursor.getPredefinedCursor(
                        Cursor.HAND_CURSOR
                )
        );

        voltar.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseEntered(MouseEvent e) {
                voltar.setForeground(ROXO_HOVER);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                voltar.setForeground(ROXO);
            }
        });

        voltar.addActionListener(
                e -> dispose()
        );

        JPanel linhaVoltar =
                new JPanel(new FlowLayout(
                        FlowLayout.LEFT, 0, 0
                ));

        linhaVoltar.setOpaque(false);
        linhaVoltar.add(voltar);

        adicionar(conteudo, linhaVoltar, 42);

        // CARD DA DISCIPLINA

        JPanel disciplina = card();

        disciplina.setLayout(
                new BoxLayout(
                        disciplina,
                        BoxLayout.Y_AXIS
                )
        );

        JLabel lblInstituicao = texto(
                "REGISTRO DE CHAMADA",
                12,
                ROXO,
                true
        );

        JLabel lblMateria = texto(
                nomeMateria,
                29,
                MARINHO,
                true
        );

        JLabel lblTurma = texto(
                cursoTurma + "  ·  Código: " + codigoTurma,
                13,
                CINZA,
                false
        );

        for (JLabel item : new JLabel[]{
                lblInstituicao,
                lblMateria,
                lblTurma
        }) {

            item.setAlignmentX(
                    Component.LEFT_ALIGNMENT
            );

            disciplina.add(item);

            disciplina.add(
                    Box.createVerticalStrut(9)
            );
        }

        adicionar(conteudo, disciplina, 150);

        // CONFIGURAÇÃO DA CHAMADA

        JPanel configuracao = card();

        configuracao.setLayout(
                new BorderLayout(12, 14)
        );

        configuracao.add(
                texto(
                        "Configurar chamada",
                        19,
                        MARINHO,
                        true
                ),
                BorderLayout.NORTH
        );

        JPanel campos =
                new JPanel(new FlowLayout(
                        FlowLayout.LEFT, 14, 8
                ));

        campos.setOpaque(false);

        campos.add(
                texto("Data:", 13, MARINHO, true)
        );

        txtDataChamada = new JTextField(
                LocalDate.now().format(formatoData),
                11
        );

        txtDataChamada.setFont(
                new Font("SansSerif", Font.PLAIN, 13)
        );

        txtDataChamada.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                AZUL_CLARO
                        ),
                        new EmptyBorder(9, 12, 9, 12)
                )
        );

        campos.add(txtDataChamada);

        campos.add(
                texto(
                        "Aulas nesta chamada:",
                        13,
                        MARINHO,
                        true
                )
        );

        cbQuantidadeAulas = new JComboBox<>();

        cbQuantidadeAulas.setFont(
                new Font("SansSerif", Font.PLAIN, 13)
        );

        preencherQuantidades(totalAulasDia);

        campos.add(cbQuantidadeAulas);

        JButton verificar = botao(
                "Verificar data",
                false
        );

        verificar.addActionListener(
                e -> verificarData()
        );

        campos.add(verificar);

        configuracao.add(
                campos,
                BorderLayout.CENTER
        );

        lblInformacao = texto(
                "Selecione a data e a quantidade de aulas.",
                12,
                CINZA,
                false
        );

        configuracao.add(
                lblInformacao,
                BorderLayout.SOUTH
        );

        adicionar(conteudo, configuracao, 175);

        // CONTADORES

        JPanel contadores = new JPanel(
                new GridLayout(1, 4, 12, 0)
        );

        contadores.setOpaque(false);

        lblPresentes =
                texto("0", 28, VERDE, true);

        lblFaltas =
                texto("0", 28, VERMELHO, true);

        lblSemMarcacao =
                texto("0", 28, LARANJA, true);

        lblTotal =
                texto("0", 28, MARINHO, true);

        contadores.add(
                criarContador(
                        "Presentes",
                        VERDE,
                        lblPresentes
                )
        );

        contadores.add(
                criarContador(
                        "Faltas",
                        VERMELHO,
                        lblFaltas
                )
        );

        contadores.add(
                criarContador(
                        "Sem marcação",
                        LARANJA,
                        lblSemMarcacao
                )
        );

        contadores.add(
                criarContador(
                        "Alunos na turma",
                        MARINHO,
                        lblTotal
                )
        );

        adicionar(conteudo, contadores, 112);

        // CARD DA LISTA

        JPanel lista = card();

        lista.setLayout(
                new BorderLayout(0, 14)
        );

        JPanel cabecalhoLista =
                new JPanel(new BorderLayout(12, 0));

        cabecalhoLista.setOpaque(false);

        cabecalhoLista.add(
                texto(
                        "Lista de chamada",
                        19,
                        MARINHO,
                        true
                ),
                BorderLayout.WEST
        );

        JButton todosPresentes = botao(
                "Marcar todos presentes",
                true
        );

        todosPresentes.addActionListener(
                e -> marcarTodosPresentes()
        );

        cabecalhoLista.add(
                todosPresentes,
                BorderLayout.EAST
        );

        JPanel barraBusca =
                new JPanel(new BorderLayout(10, 0));

        barraBusca.setOpaque(false);

        txtBusca = new JTextField();

        txtBusca.setFont(
                new Font("SansSerif", Font.PLAIN, 13)
        );

        txtBusca.setToolTipText(
                "Buscar pelo nome ou RA do aluno"
        );

        txtBusca.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                AZUL_CLARO
                        ),
                        new EmptyBorder(11, 13, 11, 13)
                )
        );

        barraBusca.add(
                texto(
                        "Buscar por nome ou RA:",
                        12,
                        CINZA,
                        true
                ),
                BorderLayout.WEST
        );

        barraBusca.add(
                txtBusca,
                BorderLayout.CENTER
        );

        JPanel topoLista = new JPanel();

        topoLista.setOpaque(false);

        topoLista.setLayout(
                new BoxLayout(
                        topoLista,
                        BoxLayout.Y_AXIS
                )
        );

        cabecalhoLista.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        barraBusca.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        topoLista.add(cabecalhoLista);

        topoLista.add(
                Box.createVerticalStrut(14)
        );

        topoLista.add(barraBusca);

        lista.add(topoLista, BorderLayout.NORTH);

        modeloTabela = new DefaultTableModel(
                new Object[]{
                        "RA",
                        "ALUNO",
                        "SITUAÇÃO"
                },
                0
        ) {

            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(
                    int linha,
                    int coluna) {

                return coluna == 2;
            }
        };

        tabelaAlunos = new JTable(modeloTabela);

        tabelaAlunos.setFont(
                new Font("SansSerif", Font.PLAIN, 13)
        );

        tabelaAlunos.setRowHeight(42);
        tabelaAlunos.setBackground(Color.WHITE);
        tabelaAlunos.setForeground(MARINHO);
        tabelaAlunos.setGridColor(AZUL_CLARO);

        tabelaAlunos.setShowVerticalLines(false);

        tabelaAlunos.setSelectionBackground(
                AZUL_CLARO
        );

        tabelaAlunos.getTableHeader()
                .setBackground(AZUL_CLARO);

        tabelaAlunos.getTableHeader()
                .setForeground(MARINHO);

        tabelaAlunos.getTableHeader()
                .setFont(
                        new Font(
                                "SansSerif",
                                Font.BOLD,
                                12
                        )
                );

        JComboBox<String> opcoes =
                new JComboBox<>(new String[]{
                        "Presente",
                        "Falta"
                });

        tabelaAlunos.getColumnModel()
                .getColumn(2)
                .setCellEditor(
                        new DefaultCellEditor(opcoes)
                );

        filtro = new TableRowSorter<>(modeloTabela);

        tabelaAlunos.setRowSorter(filtro);

        txtBusca.getDocument()
                .addDocumentListener(
                        new DocumentListener() {

                            @Override
                            public void insertUpdate(
                                    DocumentEvent e) {
                                filtrarAlunos();
                            }

                            @Override
                            public void removeUpdate(
                                    DocumentEvent e) {
                                filtrarAlunos();
                            }

                            @Override
                            public void changedUpdate(
                                    DocumentEvent e) {
                                filtrarAlunos();
                            }
                        }
                );

        modeloTabela.addTableModelListener(
                e -> atualizarContadores()
        );

        JScrollPane scrollTabela =
                new JScrollPane(tabelaAlunos);

        scrollTabela.setBorder(null);

        JPanel areaTabela =
                painelArredondado(
                        AZUL_CLARO,
                        18
                );

        areaTabela.setLayout(
                new BorderLayout()
        );

        areaTabela.setBorder(
                new EmptyBorder(2, 2, 2, 2)
        );

        areaTabela.add(
                scrollTabela,
                BorderLayout.CENTER
        );

        lista.add(
                areaTabela,
                BorderLayout.CENTER
        );

        JPanel rodape =
                new JPanel(new BorderLayout());

        rodape.setOpaque(false);

        rodape.add(
                texto(
                        "Os contadores são atualizados conforme a marcação.",
                        11,
                        CINZA,
                        false
                ),
                BorderLayout.WEST
        );

        btnSalvar = botao(
                "Finalizar e salvar chamada",
                true
        );

        btnSalvar.setEnabled(false);

        btnSalvar.addActionListener(
                e -> salvarChamada()
        );

        rodape.add(
                btnSalvar,
                BorderLayout.EAST
        );

        lista.add(
                rodape,
                BorderLayout.SOUTH
        );

        adicionar(conteudo, lista, 430);
    }

    private void adicionar(
            JPanel destino,
            JComponent componente,
            int altura) {

        componente.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        componente.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        altura
                )
        );

        destino.add(componente);

        destino.add(
                Box.createVerticalStrut(18)
        );
    }

    private void preencherQuantidades(int quantidade) {

        cbQuantidadeAulas.removeAllItems();

        for (int i = 1; i <= quantidade; i++) {
            cbQuantidadeAulas.addItem(i);
        }

        cbQuantidadeAulas.setSelectedItem(quantidade);
    }

    private void filtrarAlunos() {

        String busca = txtBusca.getText().trim();

        if (busca.isEmpty()) {

            filtro.setRowFilter(null);

        } else {

            filtro.setRowFilter(
                    RowFilter.regexFilter(
                            "(?i)" +
                            java.util.regex.Pattern.quote(busca),
                            0,
                            1
                    )
            );
        }
    }

    private void atualizarContadores() {

        if (modeloTabela == null
                || lblPresentes == null) {
            return;
        }

        int presentes = 0;
        int faltas = 0;
        int semMarcacao = 0;

        for (int i = 0;
                i < modeloTabela.getRowCount();
                i++) {

            String situacao = String.valueOf(
                    modeloTabela.getValueAt(i, 2)
            );

            if ("Presente".equals(situacao)) {

                presentes++;

            } else if ("Falta".equals(situacao)) {

                faltas++;

            } else {

                semMarcacao++;
            }
        }

        lblPresentes.setText(
                String.valueOf(presentes)
        );

        lblFaltas.setText(
                String.valueOf(faltas)
        );

        lblSemMarcacao.setText(
                String.valueOf(semMarcacao)
        );

        lblTotal.setText(
                String.valueOf(
                        modeloTabela.getRowCount()
                )
        );
    }


    private void marcarTodosPresentes() {

        if (tabelaAlunos.isEditing()) {
            tabelaAlunos.getCellEditor()
                    .stopCellEditing();
        }

        for (int i = 0;
                i < modeloTabela.getRowCount();
                i++) {

            modeloTabela.setValueAt(
                    "Presente",
                    i,
                    2
            );
        }

        atualizarContadores();
    }


    private void carregarTurmaEAlunos() {

        try (Connection con =
                ConexaoMySQL.obterConexao()) {

            String sqlTurma =
                    "SELECT idTURMA "
                    + "FROM turma "
                    + "WHERE codigo_acesso = ?";

            try (PreparedStatement ps =
                    con.prepareStatement(sqlTurma)) {

                ps.setString(1, codigoTurma);

                try (ResultSet rs =
                        ps.executeQuery()) {

                    if (rs.next()) {
                        turmaId = rs.getInt("idTURMA");
                    }
                }
            }

            if (turmaId <= 0) {

                throw new SQLException(
                        "Não foi possível localizar a turma "
                        + "pelo código " + codigoTurma
                );
            }

            String sqlAlunos =
                    "SELECT m.aluno_id, "
                    + "m.ra_aluno, "
                    + "u.nome "
                    + "FROM matricula m "
                    + "INNER JOIN usuarios u "
                    + "ON u.idUSUARIOS = m.aluno_id "
                    + "WHERE m.turma_id = ? "
                    + "ORDER BY u.nome";

            idsAlunos.clear();
            modeloTabela.setRowCount(0);

            try (PreparedStatement ps =
                    con.prepareStatement(sqlAlunos)) {

                ps.setInt(1, turmaId);

                try (ResultSet rs =
                        ps.executeQuery()) {

                    while (rs.next()) {

                        idsAlunos.add(
                                rs.getInt("aluno_id")
                        );

                        modeloTabela.addRow(
                                new Object[]{
                                        rs.getString("ra_aluno"),
                                        rs.getString("nome"),
                                        "Presente"
                                }
                        );
                    }
                }
            }

            atualizarContadores();

            btnSalvar.setEnabled(
                    !idsAlunos.isEmpty()
            );

            verificarData();

        } catch (SQLException e) {

            e.printStackTrace();

            JOptionPane.showMessageDialog(
                    this,
                    "Erro ao carregar a turma:\n"
                    + e.getMessage(),
                    "Erro no MySQL",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private LocalDate obterData() {

        try {

            return LocalDate.parse(
                    txtDataChamada.getText().trim(),
                    formatoData
            );

        } catch (DateTimeParseException e) {

            throw new IllegalArgumentException(
                    "Informe uma data válida no formato DD/MM/AAAA."
            );
        }
    }

    private String nomeDia(LocalDate data) {

        switch (data.getDayOfWeek()) {

            case MONDAY:
                return "segunda";

            case TUESDAY:
                return "terca";

            case WEDNESDAY:
                return "quarta";

            case THURSDAY:
                return "quinta";

            case FRIDAY:
                return "sexta";

            case SATURDAY:
                return "sabado";

            case SUNDAY:
                return "domingo";

            default:
                return "";
        }
    }

    private String normalizarDia(String dia) {

        if (dia == null) {
            return "";
        }

        String valor = java.text.Normalizer.normalize(
                dia.toLowerCase(Locale.ROOT),
                java.text.Normalizer.Form.NFD
        ).replaceAll("\\p{M}", "");

        return valor
                .replace("-feira", "")
                .replace(" feira", "")
                .trim();
    }

    private int aulasPrevistas(
            Connection con,
            LocalDate data) throws SQLException {

        String sql =
                "SELECT dia_semana, peso_aula "
                + "FROM grade "
                + "WHERE turma_id = ?";

        int total = 0;

        try (PreparedStatement ps =
                con.prepareStatement(sql)) {

            ps.setInt(1, turmaId);

            try (ResultSet rs =
                    ps.executeQuery()) {

                while (rs.next()) {

                    String diaBanco = normalizarDia(
                            rs.getString("dia_semana")
                    );

                    if (diaBanco.equals(nomeDia(data))) {

                        total += Math.max(
                                0,
                                rs.getInt("peso_aula")
                        );
                    }
                }
            }
        }

        return total;
    }

    private int aulasRegistradas(
            Connection con,
            LocalDate data) throws SQLException {

        String sql =
                "SELECT COALESCE(SUM(quantidade_aula), 0) "
                + "FROM registros "
                + "WHERE turma_id = ? "
                + "AND data_aula = ?";

        try (PreparedStatement ps =
                con.prepareStatement(sql)) {

            ps.setInt(1, turmaId);

            ps.setDate(
                    2,
                    java.sql.Date.valueOf(data)
            );

            try (ResultSet rs =
                    ps.executeQuery()) {

                return rs.next()
                        ? rs.getInt(1)
                        : 0;
            }
        }
    }

    private void verificarData() {

        if (turmaId <= 0) {
            return;
        }

        try {

            LocalDate data = obterData();

            try (Connection con =
                    ConexaoMySQL.obterConexao()) {

                int previstas =
                        aulasPrevistas(con, data);

                int registradas =
                        aulasRegistradas(con, data);

                int restantes = Math.max(
                        0,
                        previstas - registradas
                );

                lblInformacao.setText(
                        "Aulas previstas: " + previstas
                        + "  ·  Já registradas: "
                        + registradas
                        + "  ·  Restantes: "
                        + restantes
                );

                if (restantes > 0) {

                    preencherQuantidades(restantes);

                    btnSalvar.setEnabled(
                            !idsAlunos.isEmpty()
                    );

                } else {

                    cbQuantidadeAulas.removeAllItems();

                    btnSalvar.setEnabled(false);
                }
            }

        } catch (Exception e) {

            lblInformacao.setText(
                    "Verifique a data e a grade da turma."
            );

            btnSalvar.setEnabled(false);

            JOptionPane.showMessageDialog(
                    this,
                    e.getMessage(),
                    "Verificar chamada",
                    JOptionPane.WARNING_MESSAGE
            );
        }
    }


    private void salvarChamada() {

        if (turmaId <= 0) {
            return;
        }

        if (tabelaAlunos.isEditing()) {

            tabelaAlunos.getCellEditor()
                    .stopCellEditing();
        }

        final LocalDate data;
        final Integer quantidade;

        try {

            data = obterData();

            quantidade =
                    (Integer) cbQuantidadeAulas
                            .getSelectedItem();

            if (quantidade == null
                    || quantidade <= 0) {

                throw new IllegalArgumentException(
                        "Selecione a quantidade de aulas."
                );
            }

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    this,
                    e.getMessage(),
                    "Dados inválidos",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        if (idsAlunos.isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Não existem alunos matriculados "
                    + "nesta turma."
            );

            return;
        }

        int resposta = JOptionPane.showConfirmDialog(
                this,
                "Confirmar que "
                + quantidade
                + " aula(s) aconteceram em "
                + data.format(formatoData)
                + "?\n\n"
                + "Os alunos marcados como presentes "
                + "receberão presença.\n"
                + "Os marcados como falta "
                + "receberão falta.",
                "Confirmar chamada",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (resposta != JOptionPane.YES_OPTION) {
            return;
        }

        btnSalvar.setEnabled(false);

        Connection con = null;

        try {

            con = ConexaoMySQL.obterConexao();

            con.setAutoCommit(false);

            // BLOQUEIA A TURMA DURANTE A GRAVAÇÃO
            // PARA EVITAR DUAS CHAMADAS SIMULTÂNEAS
            // ULTRAPASSAREM A QUANTIDADE DE AULAS.

            try (PreparedStatement trava =
                    con.prepareStatement(
                            "SELECT idTURMA FROM turma "
                            + "WHERE idTURMA = ? FOR UPDATE"
                    )) {

                trava.setInt(1, turmaId);

                try (ResultSet rs =
                        trava.executeQuery()) {

                    if (!rs.next()) {

                        throw new SQLException(
                                "Turma não encontrada."
                        );
                    }
                }
            }

            int previstas =
                    aulasPrevistas(con, data);

            int registradas =
                    aulasRegistradas(con, data);

            if (previstas <= 0) {

                throw new SQLException(
                        "Não há aulas previstas "
                        + "na grade para essa data."
                );
            }

            if (registradas + quantidade > previstas) {

                throw new SQLException(
                        "A quantidade ultrapassa "
                        + "as aulas previstas.\n"
                        + "Previstas: " + previstas
                        + "\nRegistradas: " + registradas
                        + "\nTentativa: " + quantidade
                );
            }

            // NÚMERO DA PRÓXIMA CHAMADA DO DIA

            int numeroChamada = 1;

            String sqlNumero =
                    "SELECT COALESCE("
                    + "MAX(numero_chamada), 0) + 1 "
                    + "FROM registros "
                    + "WHERE turma_id = ? "
                    + "AND data_aula = ?";

            try (PreparedStatement ps =
                    con.prepareStatement(sqlNumero)) {

                ps.setInt(1, turmaId);

                ps.setDate(
                        2,
                        java.sql.Date.valueOf(data)
                );

                try (ResultSet rs =
                        ps.executeQuery()) {

                    if (rs.next()) {
                        numeroChamada = rs.getInt(1);
                    }
                }
            }

            // INSERE O REGISTRO DA CHAMADA

            int registroId;

            String sqlRegistro =
                    "INSERT INTO registros "
                    + "(turma_id, data_aula, "
                    + "quantidade_aula, numero_chamada, "
                    + "turma_idTURMA) "
                    + "VALUES (?, ?, ?, ?, ?)";

            try (PreparedStatement ps =
                    con.prepareStatement(
                            sqlRegistro,
                            Statement.RETURN_GENERATED_KEYS
                    )) {

                ps.setInt(1, turmaId);

                ps.setDate(
                        2,
                        java.sql.Date.valueOf(data)
                );

                ps.setInt(3, quantidade);
                ps.setInt(4, numeroChamada);
                ps.setInt(5, turmaId);

                ps.executeUpdate();

                try (ResultSet rs =
                        ps.getGeneratedKeys()) {

                    if (!rs.next()) {

                        throw new SQLException(
                                "Não foi possível obter "
                                + "o ID da chamada."
                        );
                    }

                    registroId = rs.getInt(1);
                }
            }

            // INSERE A FREQUÊNCIA DOS ALUNOS

            String sqlPresenca =
                    "INSERT INTO `presença` "
                    + "(registro_id, aluno_id, "
                    + "status_presenca, "
                    + "turma_idTURMA, "
                    + "registros_idREGISTROS) "
                    + "VALUES (?, ?, ?, ?, ?)";

            try (PreparedStatement ps =
                    con.prepareStatement(sqlPresenca)) {

                for (int i = 0;
                        i < modeloTabela.getRowCount();
                        i++) {

                    int alunoId = idsAlunos.get(i);

                    String situacao = String.valueOf(
                            modeloTabela.getValueAt(i, 2)
                    );

                    ps.setInt(1, registroId);
                    ps.setInt(2, alunoId);

                    ps.setString(
                            3,
                            "Falta".equalsIgnoreCase(situacao)
                                    ? "FALTA"
                                    : "PRESENTE"
                    );

                    ps.setInt(4, turmaId);
                    ps.setInt(5, registroId);

                    ps.addBatch();
                }

                ps.executeBatch();
            }

            con.commit();

            JOptionPane.showMessageDialog(
                    this,
                    "Chamada salva no MySQL!\n\n"
                    + "Data: "
                    + data.format(formatoData)
                    + "\nChamada: "
                    + numeroChamada
                    + "\nAulas abrangidas: "
                    + quantidade
                    + "\nPresentes: "
                    + lblPresentes.getText()
                    + "\nFaltas: "
                    + lblFaltas.getText(),
                    "Presente!",
                    JOptionPane.INFORMATION_MESSAGE
            );

            dispose();

        } catch (Exception e) {

            if (con != null) {

                try {
                    con.rollback();
                } catch (SQLException erro) {
                    erro.printStackTrace();
                }
            }

            e.printStackTrace();

            JOptionPane.showMessageDialog(
                    this,
                    "A chamada não foi salva.\n\n"
                    + e.getMessage(),
                    "Erro no MySQL",
                    JOptionPane.ERROR_MESSAGE
            );

            btnSalvar.setEnabled(true);

        } finally {

            if (con != null) {

                try {
                    con.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            new TelaChamada(
                    "Banco de Dados",
                    "Turma B - Noite",
                    "857753",
                    4
            ).setVisible(true);
        });
    }
}
