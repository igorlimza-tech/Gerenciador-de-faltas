
package view.faltas;

import conexao.ConexaoMySQL;
import util.faltas.CalculoCargaHoraria;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.sql.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TelaHistoricoAluno extends JFrame {

    private static final long serialVersionUID = 1L;

    // PALETA DO PRESENTE!
    private static final Color ROXO = new Color(79, 70, 229);
    private static final Color ROXO_HOVER = new Color(67, 56, 202);
    private static final Color MARINHO = new Color(30, 27, 75);
    private static final Color FUNDO = new Color(248, 250, 252);
    private static final Color BORDA = new Color(226, 232, 240);
    private static final Color CINZA = new Color(100, 116, 139);
    private static final Color VERDE = new Color(22, 163, 74);
    private static final Color LARANJA = new Color(234, 139, 8);
    private static final Color VERMELHO = new Color(220, 38, 38);
    private static final Color AZUL_CLARO = new Color(238, 242, 255);

    private final int alunoId;
    private final int turmaId;

    private JLabel nomeAlunoTopo;
    private JLabel iniciaisAlunoTopo;

    private JLabel instituicao;
    private JLabel materia;
    private JLabel detalhes;
    private JLabel professor;
    private JLabel email;
    private JLabel dias;

    private JLabel percentual;
    private JLabel limite;
    private JLabel situacao;

    private JLabel dadas;
    private JLabel presentes;
    private JLabel faltas;
    private JLabel disponiveis;
    private JLabel futuras;

    private JProgressBar barra;
    private DefaultTableModel modelo;

    public TelaHistoricoAluno(int alunoId, int turmaId) {
        this.alunoId = alunoId;
        this.turmaId = turmaId;

        construir();

        if (alunoId > 0 && turmaId > 0) {
            carregarDados();
        }
    }

    public TelaHistoricoAluno() {
        this(-1, -1);
    }

    // COMPONENTES VISUAIS

    private static JLabel label(
            String texto,
            int tamanho,
            Color cor,
            boolean negrito) {

        JLabel l = new JLabel(texto);

        l.setFont(new Font(
                "SansSerif",
                negrito ? Font.BOLD : Font.PLAIN,
                tamanho
        ));

        l.setForeground(cor);

        return l;
    }

    // PAINEL COM CANTOS ARREDONDADOS

    private static JPanel arredondado(Color cor, int raio) {

        return new JPanel() {

            private static final long serialVersionUID = 1L;

            @Override
            protected void paintComponent(Graphics g) {

                Graphics2D g2 = (Graphics2D) g.create();

                g2.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON
                );

                g2.setColor(cor);

                g2.fillRoundRect(
                        0,
                        0,
                        getWidth() - 1,
                        getHeight() - 1,
                        raio,
                        raio
                );

                g2.dispose();
            }
        };
    }

    private static JPanel card() {

        JPanel p = arredondado(Color.WHITE, 22);

        p.setOpaque(false);

        p.setBorder(
                new EmptyBorder(22, 24, 22, 24)
        );

        return p;
    }

    private static JPanel bloco(
            String titulo,
            JLabel numero) {

        JPanel p = arredondado(AZUL_CLARO, 18);

        p.setOpaque(false);
        p.setLayout(new BorderLayout(0, 8));

        p.setBorder(
                new EmptyBorder(14, 12, 14, 12)
        );

        p.add(numero, BorderLayout.CENTER);

        JLabel legenda = label(
                titulo,
                11,
                CINZA,
                false
        );

        p.add(legenda, BorderLayout.SOUTH);

        return p;
    }

    // BOTÃO ROXO ARREDONDADO COM HOVER

    private static JButton botaoArredondado(
            String texto,
            boolean principal) {

        Color normal = principal
                ? ROXO
                : AZUL_CLARO;

        Color hover = principal
                ? ROXO_HOVER
                : new Color(224, 231, 255);

        JButton b = new JButton(texto) {

            private static final long serialVersionUID = 1L;
            private boolean sobre = false;

            {
                addMouseListener(new MouseAdapter() {

                    @Override
                    public void mouseEntered(MouseEvent e) {
                        sobre = true;
                        repaint();
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        sobre = false;
                        repaint();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {

                Graphics2D g2 = (Graphics2D) g.create();

                g2.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON
                );

                g2.setColor(sobre ? hover : normal);

                g2.fillRoundRect(
                        0,
                        0,
                        getWidth() - 1,
                        getHeight() - 1,
                        16,
                        16
                );

                g2.dispose();

                super.paintComponent(g);
            }
        };

        b.setForeground(
                principal ? Color.WHITE : MARINHO
        );

        b.setFont(
                new Font("SansSerif", Font.BOLD, 12)
        );

        b.setOpaque(false);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);

        b.setBorder(
                new EmptyBorder(11, 19, 11, 19)
        );

        b.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );

        return b;
    }

    // BOTÃO VOLTAR: PADRÃO DO PAINEL DO PROFESSOR

    private static JButton botaoVoltar(String texto) {

        JButton botao = new JButton(texto);

        botao.setFont(
                new Font("SansSerif", Font.BOLD, 13)
        );

        botao.setForeground(ROXO);
        botao.setOpaque(false);
        botao.setContentAreaFilled(false);
        botao.setBorderPainted(false);
        botao.setFocusPainted(false);

        botao.setBorder(
                new EmptyBorder(8, 0, 8, 12)
        );

        botao.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );

        botao.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseEntered(MouseEvent e) {
                botao.setForeground(ROXO_HOVER);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                botao.setForeground(ROXO);
            }
        });

        return botao;
    }

    // INICIAIS DO ALUNO

    private static String iniciais(String nome) {

        if (nome == null || nome.isBlank()) {
            return "AL";
        }

        String[] partes = nome.trim().split("\\s+");

        String primeira = partes[0].substring(0, 1);
        String segunda = "";

        for (int i = 1; i < partes.length; i++) {

            if (!partes[i].matches(
                    "(?i)da|de|do|das|dos|e")) {

                segunda = partes[i].substring(0, 1);
                break;
            }
        }

        return (primeira + segunda)
                .toUpperCase(Locale.ROOT);
    }

    // CONSTRUÇÃO DA TELA

    private void construir() {

        setTitle("Presente! - Histórico da disciplina");

        setSize(1220, 800);
        setMinimumSize(new Dimension(930, 640));
        setLocationRelativeTo(null);

        setDefaultCloseOperation(
                JFrame.DISPOSE_ON_CLOSE
        );

        JPanel raiz = new JPanel(new BorderLayout());
        raiz.setBackground(FUNDO);
        setContentPane(raiz);

        // TOPO

        JPanel topo = new JPanel(new BorderLayout());

        topo.setBackground(MARINHO);

        topo.setBorder(
                new EmptyBorder(17, 36, 17, 36)
        );

        topo.add(
                label("✓ Presente!", 22, Color.WHITE, true),
                BorderLayout.WEST
        );

        JPanel perfil = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 14, 0)
        );

        perfil.setOpaque(false);

        nomeAlunoTopo = label(
                "Aluno",
                14,
                Color.WHITE,
                true
        );

        perfil.add(nomeAlunoTopo);

        JPanel circulo = arredondado(
                new Color(129, 140, 248),
                100
        );

        circulo.setOpaque(false);
        circulo.setPreferredSize(new Dimension(46, 46));
        circulo.setLayout(new GridBagLayout());

        iniciaisAlunoTopo = label(
                "AL",
                13,
                MARINHO,
                true
        );

        circulo.add(iniciaisAlunoTopo);
        perfil.add(circulo);

        topo.add(perfil, BorderLayout.EAST);
        raiz.add(topo, BorderLayout.NORTH);

        // CONTEÚDO

        JPanel conteudo = new JPanel();

        conteudo.setLayout(
                new BoxLayout(conteudo, BoxLayout.Y_AXIS)
        );

        conteudo.setBackground(FUNDO);

        conteudo.setBorder(
                new EmptyBorder(25, 36, 35, 36)
        );

        JScrollPane scroll = new JScrollPane(conteudo);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(18);

        raiz.add(scroll, BorderLayout.CENTER);

        // BOTÃO MINHAS TURMAS

        JButton voltar = botaoVoltar("‹ Minhas turmas");

        voltar.addActionListener(e -> dispose());

        JPanel linha = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 0, 0)
        );

        linha.setOpaque(false);
        linha.add(voltar);

        adicionar(conteudo, linha, 40);

        // CARD DA DISCIPLINA

        JPanel cab = card();

        cab.setLayout(
                new BoxLayout(cab, BoxLayout.Y_AXIS)
        );

        instituicao = label(
                "INSTITUIÇÃO", 12, ROXO, true
        );

        materia = label(
                "Histórico da disciplina", 30, MARINHO, true
        );

        detalhes = label(
                "Turma e período", 13, CINZA, false
        );

        professor = label(
                "Professor: —", 13, MARINHO, false
        );

        email = label(
                "E-mail institucional: —", 13, CINZA, false
        );

        dias = label(
                "Dias de aula: —", 13, CINZA, false
        );

        for (JLabel l : new JLabel[]{
                instituicao,
                materia,
                detalhes,
                professor,
                email,
                dias
        }) {

            l.setAlignmentX(Component.LEFT_ALIGNMENT);
            cab.add(l);
            cab.add(Box.createVerticalStrut(8));
        }

        adicionar(conteudo, cab, 205);

        // PAINEL PRINCIPAL

        JPanel corpo = new JPanel(
                new BorderLayout(20, 0)
        );

        corpo.setOpaque(false);

        // RESUMO DE FREQUÊNCIA

        JPanel resumo = card();
        resumo.setPreferredSize(new Dimension(335, 490));

        resumo.setLayout(
                new BoxLayout(resumo, BoxLayout.Y_AXIS)
        );

        JLabel tituloLimite = label(
                "Limite de faltas utilizado",
                13,
                CINZA,
                true
        );

        tituloLimite.setAlignmentX(Component.LEFT_ALIGNMENT);
        resumo.add(tituloLimite);
        resumo.add(Box.createVerticalStrut(8));

        JPanel porcentagens = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 5, 0)
        );

        porcentagens.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        porcentagens.setOpaque(false);

        percentual = label(
                "—", 44, MARINHO, true
        );

        limite = label(
                "de —", 13, CINZA, false
        );

        porcentagens.add(percentual);
        porcentagens.add(limite);

        resumo.add(porcentagens);
        resumo.add(Box.createVerticalStrut(10));

        barra = new JProgressBar(0, 100);
        barra.setForeground(ROXO);
        barra.setBackground(new Color(224, 231, 255));
        barra.setBorderPainted(false);

        barra.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 12)
        );

        resumo.add(barra);
        resumo.add(Box.createVerticalStrut(14));

        situacao = label(
                "Aguardando informações",
                12,
                CINZA,
                true
        );

        resumo.add(situacao);
        resumo.add(Box.createVerticalStrut(20));

        // INDICADORES

        JPanel numeros = new JPanel(
                new GridLayout(3, 2, 10, 10)
        );

        numeros.setOpaque(false);

        dadas = label("—", 27, MARINHO, true);
        presentes = label("—", 27, VERDE, true);
        faltas = label("—", 27, VERMELHO, true);
        disponiveis = label("—", 27, LARANJA, true);
        futuras = label("—", 27, ROXO, true);

        numeros.add(bloco("Aulas dadas", dadas));
        numeros.add(bloco("Aulas presentes", presentes));
        numeros.add(bloco("Faltas (aulas)", faltas));
        numeros.add(bloco("Faltas disponíveis", disponiveis));
        numeros.add(bloco("Carga não registrada", futuras));

        JPanel espaco = new JPanel();
        espaco.setOpaque(false);
        numeros.add(espaco);

        resumo.add(numeros);

        // CARD DO HISTÓRICO

        JPanel historico = card();
        historico.setLayout(new BorderLayout(0, 14));

        JPanel tituloTabela = new JPanel(
                new BorderLayout()
        );

        tituloTabela.setOpaque(false);

        tituloTabela.add(
                label(
                        "Histórico de frequência",
                        20,
                        MARINHO,
                        true
                ),
                BorderLayout.WEST
        );

        JButton atualizar = botaoArredondado(
                "Atualizar",
                true
        );

        atualizar.addActionListener(
                e -> carregarDados()
        );

        tituloTabela.add(
                atualizar,
                BorderLayout.EAST
        );

        historico.add(
                tituloTabela,
                BorderLayout.NORTH
        );

        // TABELA

        modelo = new DefaultTableModel(
                new Object[]{
                        "DATA",
                        "AULAS ABRANGIDAS",
                        "SITUAÇÃO"
                },
                0
        ) {

            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(
                    int linha,
                    int coluna) {
                return false;
            }
        };

        JTable tabela = new JTable(modelo);

        tabela.setRowHeight(45);

        tabela.setFont(
                new Font("SansSerif", Font.PLAIN, 13)
        );

        tabela.setForeground(MARINHO);
        tabela.setBackground(Color.WHITE);
        tabela.setGridColor(BORDA);
        tabela.setShowVerticalLines(false);

        tabela.setSelectionBackground(
                new Color(224, 231, 255)
        );

        tabela.getTableHeader().setBackground(AZUL_CLARO);
        tabela.getTableHeader().setForeground(MARINHO);

        tabela.getTableHeader().setFont(
                new Font("SansSerif", Font.BOLD, 12)
        );

        tabela.getTableHeader().setPreferredSize(
                new Dimension(0, 40)
        );

        tabela.getColumnModel()
                .getColumn(2)
                .setCellRenderer(new DefaultTableCellRenderer() {

                    private static final long serialVersionUID = 1L;

                    @Override
                    public Component getTableCellRendererComponent(
                            JTable t,
                            Object v,
                            boolean selecionado,
                            boolean foco,
                            int linha,
                            int coluna) {

                        super.getTableCellRendererComponent(
                                t,
                                v,
                                selecionado,
                                foco,
                                linha,
                                coluna
                        );

                        String s = String.valueOf(v);

                        if (s.equalsIgnoreCase("Presente")) {
                            setForeground(VERDE);

                        } else if (s.equalsIgnoreCase("Falta")) {
                            setForeground(VERMELHO);

                        } else {
                            setForeground(CINZA);
                        }

                        setFont(
                                new Font(
                                        "SansSerif",
                                        Font.BOLD,
                                        12
                                )
                        );

                        return this;
                    }
                });

        // SCROLL DA TABELA

        JScrollPane tabelaScroll = new JScrollPane(tabela);

        tabelaScroll.setBorder(
                BorderFactory.createEmptyBorder()
        );

        tabelaScroll.setOpaque(false);
        tabelaScroll.getViewport().setOpaque(false);

        // RECORTE ARREDONDADO DA ÁREA DA TABELA

        JPanel painelTabela = new JPanel(new BorderLayout()) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void paintComponent(Graphics g) {

                Graphics2D g2 = (Graphics2D) g.create();

                g2.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON
                );

                g2.setColor(AZUL_CLARO);

                g2.fillRoundRect(
                        0,
                        0,
                        getWidth() - 1,
                        getHeight() - 1,
                        18,
                        18
                );

                g2.dispose();
            }

            @Override
            protected void paintChildren(Graphics g) {

                Graphics2D g2 = (Graphics2D) g.create();

                g2.setClip(new RoundRectangle2D.Double(
                        0,
                        0,
                        getWidth(),
                        getHeight(),
                        18,
                        18
                ));

                super.paintChildren(g2);
                g2.dispose();
            }
        };

        painelTabela.setOpaque(false);

        painelTabela.add(
                tabelaScroll,
                BorderLayout.CENTER
        );

        historico.add(
                painelTabela,
                BorderLayout.CENTER
        );

        JLabel observacao = label(
                "Cada registro pode abranger uma ou várias aulas. "
                + "As faltas são contadas em aulas, não em chamadas.",
                11,
                CINZA,
                false
        );

        historico.add(
                observacao,
                BorderLayout.SOUTH
        );

        corpo.add(resumo, BorderLayout.WEST);
        corpo.add(historico, BorderLayout.CENTER);

        adicionar(conteudo, corpo, 505);
    }

    private static void adicionar(
            JPanel destino,
            JComponent componente,
            int altura) {

        componente.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        componente.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, altura)
        );

        destino.add(componente);
        destino.add(Box.createVerticalStrut(18));
    }

    private static String seguro(String texto) {

        if (texto == null || texto.isBlank()) {
            return "Não informado";
        }

        return texto;
    }

    // CONSULTAS AO MYSQL

    private void carregarDados() {

        if (alunoId <= 0 || turmaId <= 0) {
            return;
        }

        modelo.setRowCount(0);
        situacao.setText("Consultando o banco...");

        new SwingWorker<Resultado, Void>() {

            @Override
            protected Resultado doInBackground()
                    throws Exception {

                Resultado r = new Resultado();

                try (Connection con =
                        ConexaoMySQL.obterConexao()) {

                    if (con == null) {
                        throw new SQLException(
                                "Conexão indisponível."
                        );
                    }

                    // NOME DO ALUNO

                    String sqlAluno =
                            "SELECT nome FROM usuarios "
                            + "WHERE idUSUARIOS = ?";

                    try (PreparedStatement psNome =
                            con.prepareStatement(sqlAluno)) {

                        psNome.setInt(1, alunoId);

                        try (ResultSet rsNome =
                                psNome.executeQuery()) {

                            if (rsNome.next()) {
                                r.nomeAluno = seguro(
                                        rsNome.getString(1)
                                );
                            }
                        }
                    }

                    // DADOS DA DISCIPLINA

                    String sqlTurma =
                            "SELECT t.instituicao, "
                            + "t.nome_materia, "
                            + "t.nome_turma, "
                            + "t.periodo, "
                            + "t.email_institucional, "
                            + "t.carga_horaria, "
                            + "t.limites_faltas, "
                            + "u.nome AS professor "
                            + "FROM turma t "
                            + "LEFT JOIN usuarios u "
                            + "ON u.idUSUARIOS = t.professor_id "
                            + "WHERE t.idTURMA = ? "
                            + "AND EXISTS ("
                            + "SELECT 1 FROM matricula m "
                            + "WHERE m.turma_id = t.idTURMA "
                            + "AND m.aluno_id = ?)";

                    try (PreparedStatement ps =
                            con.prepareStatement(sqlTurma)) {

                        ps.setInt(1, turmaId);
                        ps.setInt(2, alunoId);

                        try (ResultSet rs =
                                ps.executeQuery()) {

                            if (!rs.next()) {
                                throw new SQLException(
                                        "Matrícula do aluno não encontrada "
                                        + "nesta turma."
                                );
                            }

                            r.inst = seguro(
                                    rs.getString("instituicao")
                            );

                            r.materia = seguro(
                                    rs.getString("nome_materia")
                            );

                            r.turma = seguro(
                                    rs.getString("nome_turma")
                            );

                            r.periodo = seguro(
                                    rs.getString("periodo")
                            );

                            r.prof = seguro(
                                    rs.getString("professor")
                            );

                            r.email = seguro(
                                    rs.getString(
                                            "email_institucional"
                                    )
                            );

                            r.carga = Math.max(
                                    0,
                                    rs.getInt("carga_horaria")
                            );

                            r.limite = rs.getInt(
                                    "limites_faltas"
                            );
                        }
                    }

                    // DIAS DA SEMANA

                    LinkedHashSet<String> diasSemana =
                            new LinkedHashSet<>();

                    int aulasSemana = 0;

                    String sqlGrade =
                            "SELECT dia_semana, peso_aula "
                            + "FROM grade WHERE turma_id = ?";

                    try (PreparedStatement ps =
                            con.prepareStatement(sqlGrade)) {

                        ps.setInt(1, turmaId);

                        try (ResultSet rs =
                                ps.executeQuery()) {

                            while (rs.next()) {

                                diasSemana.add(
                                        seguro(rs.getString(1))
                                );

                                aulasSemana += Math.max(
                                        0,
                                        rs.getInt(2)
                                );
                            }
                        }
                    }

                    r.aulasSemana = aulasSemana;

                    if (diasSemana.isEmpty()) {
                        r.dias =
                                "Dias de aula: não informados";

                    } else {
                        r.dias =
                                "Dias de aula: "
                                + String.join(", ", diasSemana)
                                + " · "
                                + aulasSemana
                                + " aulas por semana";
                    }

                    // TOTAL DE AULAS DADAS

                    String sqlDadas =
                            "SELECT COALESCE("
                            + "SUM(quantidade_aula), 0) "
                            + "FROM registros "
                            + "WHERE turma_id = ?";

                    try (PreparedStatement ps =
                            con.prepareStatement(sqlDadas)) {

                        ps.setInt(1, turmaId);

                        try (ResultSet rs =
                                ps.executeQuery()) {

                            if (rs.next()) {
                                r.dadas = rs.getInt(1);
                            }
                        }
                    }

                    // PRESENÇAS E FALTAS

                    String sqlFrequencia =
                            "SELECT r.data_aula, "
                            + "r.quantidade_aula, "
                            + "r.idREGISTROS, "
                            + "p.status_presenca "
                            + "FROM registros r "
                            + "LEFT JOIN `presença` p "
                            + "ON p.registro_id = r.idREGISTROS "
                            + "AND p.aluno_id = ? "
                            + "WHERE r.turma_id = ? "
                            + "ORDER BY r.data_aula DESC, "
                            + "r.idREGISTROS DESC";

                    try (PreparedStatement ps =
                            con.prepareStatement(sqlFrequencia)) {

                        ps.setInt(1, alunoId);
                        ps.setInt(2, turmaId);

                        try (ResultSet rs =
                                ps.executeQuery()) {

                            Map<String, Integer> ordem =
                                    new HashMap<>();

                            while (rs.next()) {

                                java.sql.Date data =
                                        rs.getDate("data_aula");

                                if (data == null) {
                                    continue;
                                }

                                String chave = data.toString();

                                int numero =
                                        ordem.getOrDefault(
                                                chave, 0
                                        ) + 1;

                                ordem.put(chave, numero);

                                int aulas = Math.max(
                                        0,
                                        rs.getInt(
                                                "quantidade_aula"
                                        )
                                );

                                String estado =
                                        rs.getString(
                                                "status_presenca"
                                        );

                                String s = "Não lançada";

                                if (estado != null
                                        && (
                                        estado.equalsIgnoreCase("PRESENTE")
                                        || estado.equalsIgnoreCase("P")
                                        )) {

                                    r.presentes += aulas;
                                    s = "Presente";

                                } else if (estado != null
                                        && (
                                        estado.equalsIgnoreCase("FALTA")
                                        || estado.equalsIgnoreCase("F")
                                        || estado.equalsIgnoreCase("AUSENTE")
                                        )) {

                                    r.faltas += aulas;
                                    s = "Falta";
                                }

                                String dataFormatada =
                                        data.toLocalDate()
                                        .format(
                                                DateTimeFormatter.ofPattern(
                                                        "dd/MM/yyyy"
                                                )
                                        );

                                String descricao =
                                        numero
                                        + "ª chamada · "
                                        + aulas
                                        + (aulas == 1
                                        ? " aula"
                                        : " aulas");

                                r.linhas.add(
                                        new Object[]{
                                                dataFormatada,
                                                descricao,
                                                s
                                        }
                                );
                            }
                        }
                    }
                }

                return r;
            }

            @Override
            protected void done() {

                try {
                    apresentar(get());

                } catch (Exception e) {

                    Throwable causa =
                            e.getCause() == null
                            ? e
                            : e.getCause();

                    situacao.setText(
                            "Erro ao carregar dados"
                    );

                    JOptionPane.showMessageDialog(
                            TelaHistoricoAluno.this,
                            causa.getMessage(),
                            "Histórico",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }

        }.execute();
    }

    // ATUALIZAR COMPONENTES

    private void apresentar(Resultado r) {

        if (r.nomeAluno != null) {

            nomeAlunoTopo.setText(r.nomeAluno);

            iniciaisAlunoTopo.setText(
                    iniciais(r.nomeAluno)
            );
        }

        instituicao.setText(
                r.inst.toUpperCase()
        );

        materia.setText(r.materia);

        detalhes.setText(
                "Turma: "
                + r.turma
                + " · "
                + r.periodo
        );

        professor.setText(
                "Professor: " + r.prof
        );

        email.setText(
                "E-mail institucional: " + r.email
        );

        int totalAulasCurso = CalculoCargaHoraria.totalAulas(r.carga);
        int semanas = CalculoCargaHoraria.semanasEstimadas(r.carga, r.aulasSemana);
        dias.setText(r.dias + (semanas > 0
                ? " · aprox. " + semanas + " semanas letivas (sem feriados)" : ""));

        modelo.setRowCount(0);

        for (Object[] linha : r.linhas) {
            modelo.addRow(linha);
        }

        dadas.setText(
                r.dadas + " de " + totalAulasCurso
        );

        presentes.setText(
                String.valueOf(r.presentes)
        );

        faltas.setText(
                String.valueOf(r.faltas)
        );

        futuras.setText(
                String.valueOf(
                        Math.max(0, totalAulasCurso - r.dadas)
                )
        );

        int maxFaltas = CalculoCargaHoraria.limiteFaltas(
                r.carga, java.math.BigDecimal.valueOf(r.limite));

        int restantes =
                Math.max(
                        0,
                        maxFaltas - r.faltas
                );

        disponiveis.setText(
                String.valueOf(restantes)
        );

        if (r.carga <= 0 || r.limite <= 0) {

            percentual.setText("—");

            limite.setText(
                    "Limite não configurado"
            );

            situacao.setText(
                    "Configure a carga horária "
                    + "e o limite de faltas"
            );

            situacao.setForeground(CINZA);
            barra.setValue(0);

            return;
        }

        double pct =
                r.faltas * 100.0 / totalAulasCurso;

        percentual.setText(
                String.format(
                        Locale.forLanguageTag("pt-BR"),
                        "%.1f%%",
                        pct
                )
        );

        limite.setText(
                "de " + r.limite + "%"
        );

        barra.setValue(
                (int) Math.min(
                        100,
                        Math.round(pct)
                )
        );

        if (pct > r.limite) {

            situacao.setText(
                    "Crítico · limite ultrapassado"
            );

            situacao.setForeground(VERMELHO);
            barra.setForeground(VERMELHO);

        } else if (restantes <= 2) {

            situacao.setText(
                    "Atenção · restam "
                    + restantes
                    + " aulas de falta"
            );

            situacao.setForeground(LARANJA);
            barra.setForeground(LARANJA);

        } else {

            situacao.setText(
                    "Regular · dentro do limite"
            );

            situacao.setForeground(VERDE);
            barra.setForeground(ROXO);
        }
    }

    // CLASSE AUXILIAR

    private static class Resultado {

        String inst;
        String materia;
        String turma;
        String periodo;
        String prof;
        String email;
        String dias;
        String nomeAluno;

        int carga;
        int limite;
        int aulasSemana;
        int dadas;
        int presentes;
        int faltas;

        List<Object[]> linhas = new ArrayList<>();
    }

    // TESTE VISUAL

    public static void main(String[] args) {

        SwingUtilities.invokeLater(
                () -> new TelaHistoricoAluno()
                        .setVisible(true)
        );
    }
}
