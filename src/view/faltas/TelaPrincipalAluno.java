package view.faltas;
import java.awt.*;



import java.awt.event.*;



import java.util.ArrayList;



import java.util.List;



import javax.swing.*;
import util.faltas.CalculoCargaHoraria;

public class TelaPrincipalAluno extends JFrame {

    private static final long serialVersionUID = 1L;
    private static final Color MARINHO = new Color(30, 27, 75);
    private static final Color ROXO = new Color(79, 70, 229);
    private static final Color ROXO_HOVER = new Color(67, 56, 202);
    private static final Color FUNDO = new Color(248, 250, 252);
    private static final Color CINZA = new Color(100, 116, 139);
    private JPanel contentPane;
    private JPanel painelGrid;
    private JPanel painelFiltros;
    private JLabel lblStatus;



    private String filtroSelecionado = "Todas";



    private int idAlunoLogado = -1;



    private String nomeAlunoLogado = "Aluno";



    private final List<DadosMateria> materias = new ArrayList<>();



    private static class DadosMateria {



        int idTurma;



        String instituicao;



        String nome;



        String turma;



        String professor;



        int faltas;



        int limite;



        String status;



        Color fundoStatus;



        Color corStatus;



        DadosMateria(



                String instituicao,



                String nome,



                String turma,



                String professor,



                int faltas,



                int limite,



                String status,



                Color fundoStatus,



                Color corStatus) {



            this.instituicao = instituicao;



            this.nome = nome;



            this.turma = turma;



            this.professor = professor;



            this.faltas = faltas;



            this.limite = limite;



            this.status = status;



            this.fundoStatus = fundoStatus;



            this.corStatus = corStatus;



        }



    }



    private String buscarNomeAluno() {



        if (idAlunoLogado <= 0) return "Aluno";



        String sql = "SELECT nome FROM usuarios WHERE idUSUARIOS = ?";



        try (java.sql.Connection con = conexao.ConexaoMySQL.obterConexao();



             java.sql.PreparedStatement ps = con.prepareStatement(sql)) {



            ps.setInt(1, idAlunoLogado);



            try (java.sql.ResultSet rs = ps.executeQuery()) {



                if (rs.next()) {



                    String nome = rs.getString("nome");



                    if (nome != null && !nome.trim().isEmpty()) return nome.trim();



                }



            }



        } catch (java.sql.SQLException ex) {



            System.err.println("Erro ao buscar nome do aluno: " + ex.getMessage());



        }



        return "Aluno";



    }







    private static String gerarIniciais(String nome) {



        if (nome == null || nome.trim().isEmpty()) return "AL";



        String[] partes = nome.trim().split("\\s+");



        String primeira = partes[0].substring(0, 1);



        String segunda = partes.length > 1 ? partes[partes.length - 1].substring(0, 1) : "";



        return (primeira + segunda).toUpperCase();



    }







    private static String escaparHtml(String texto) {
        if (texto == null) return "";
        return texto.replace("&", "&amp;").replace("<", "&lt;")
                    .replace(">", "&gt;").replace("\"", "&quot;");
    }

    // PAINEL ARREDONDADO



    private static class PilulaArredondada extends JPanel {



        private static final long serialVersionUID = 1L;



        private final Color corFundo;



        private final Color corBorda;



        PilulaArredondada(Color fundo, Color borda) {



            corFundo = fundo;



            corBorda = borda;



            setOpaque(false);



        }



        @Override



        protected void paintComponent(Graphics g) {



            Graphics2D g2 = (Graphics2D) g.create();



            g2.setRenderingHint(



                RenderingHints.KEY_ANTIALIASING,



                RenderingHints.VALUE_ANTIALIAS_ON



            );



            g2.setColor(corFundo);



            g2.fillRoundRect(



                0, 0, getWidth() - 1,



                getHeight() - 1,



                getHeight(), getHeight()



            );



            if (corBorda != null) {



                g2.setColor(corBorda);



                g2.drawRoundRect(



                    0, 0, getWidth() - 1,



                    getHeight() - 1,



                    getHeight(), getHeight()



                );



            }



            g2.dispose();



            super.paintComponent(g);



        }



    }



    // CARD DA MATÉRIA



    private static class CardMateriaArredondado extends JPanel {



        private static final long serialVersionUID = 1L;



        CardMateriaArredondado() {



            setOpaque(false);



            Dimension tamanho = new Dimension(280, 135);



            setPreferredSize(tamanho);



            setMinimumSize(tamanho);



            setMaximumSize(tamanho);



            setCursor(



                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)



            );



        }



        @Override



        protected void paintComponent(Graphics g) {



            Graphics2D g2 = (Graphics2D) g.create();



            g2.setRenderingHint(



                RenderingHints.KEY_ANTIALIASING,



                RenderingHints.VALUE_ANTIALIAS_ON



            );



            g2.setColor(new Color(238, 242, 255));



            g2.fillRoundRect(



                0, 0, getWidth() - 1,



                getHeight() - 1, 24, 24



            );



            g2.setColor(new Color(199, 210, 254));



            g2.drawRoundRect(



                0, 0, getWidth() - 1,



                getHeight() - 1, 24, 24



            );



            g2.setColor(new Color(129, 140, 248));



            g2.fillRoundRect(



                2, 2, getWidth() - 5, 5, 5, 5



            );



            g2.dispose();



            super.paintComponent(g);



        }



    }



    // CARD DE NOVA TURMA



    private static class CardNovaTurmaArredondado extends JPanel {



        private static final long serialVersionUID = 1L;



        CardNovaTurmaArredondado() {



            setOpaque(false);



            Dimension tamanho = new Dimension(280, 135);



            setPreferredSize(tamanho);



            setMinimumSize(tamanho);



            setMaximumSize(tamanho);



            setCursor(



                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)



            );



        }



        @Override



        protected void paintComponent(Graphics g) {



            Graphics2D g2 = (Graphics2D) g.create();



            g2.setRenderingHint(



                RenderingHints.KEY_ANTIALIASING,



                RenderingHints.VALUE_ANTIALIAS_ON



            );



            g2.setColor(new Color(245, 247, 250));



            g2.fillRoundRect(



                0, 0, getWidth() - 1,



                getHeight() - 1, 24, 24



            );



            g2.setColor(new Color(199, 210, 254));



            float[] tracejado = {6f, 6f};



            g2.setStroke(new BasicStroke(



                1.5f,



                BasicStroke.CAP_ROUND,



                BasicStroke.JOIN_ROUND,



                10f,



                tracejado,



                0f



            ));



            g2.drawRoundRect(



                2, 2, getWidth() - 5,



                getHeight() - 5, 22, 22



            );



            g2.dispose();



            super.paintComponent(g);



        }



    }



    // ÍCONE +



    private static class BolinhaMaisIcone extends JPanel {



        private static final long serialVersionUID = 1L;



        BolinhaMaisIcone() {



            setOpaque(false);



            Dimension tamanho = new Dimension(36, 36);



            setPreferredSize(tamanho);



            setMinimumSize(tamanho);



            setMaximumSize(tamanho);



        }



        @Override



        protected void paintComponent(Graphics g) {



            Graphics2D g2 = (Graphics2D) g.create();



            g2.setRenderingHint(



                RenderingHints.KEY_ANTIALIASING,



                RenderingHints.VALUE_ANTIALIAS_ON



            );



            g2.setColor(new Color(224, 231, 255));



            g2.fillOval(



                0, 0, getWidth(), getHeight()



            );



            g2.setColor(ROXO);



            g2.setStroke(new BasicStroke(



                2.2f,



                BasicStroke.CAP_ROUND,



                BasicStroke.JOIN_ROUND



            ));



            int cx = getWidth() / 2;



            int cy = getHeight() / 2;



            g2.drawLine(cx - 5, cy, cx + 5, cy);



            g2.drawLine(cx, cy - 5, cx, cy + 5);



            g2.dispose();



        }



    }



    // BOTÃO ARREDONDADO



    private static class BotaoArredondado extends JButton {



        private static final long serialVersionUID = 1L;



        private final int raio;



        BotaoArredondado(String texto, int raio) {



            super(texto);



            this.raio = raio;



            setOpaque(false);



            setContentAreaFilled(false);



            setBorderPainted(false);



            setFocusPainted(false);



            setCursor(



                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)



            );



        }



        @Override



        protected void paintComponent(Graphics g) {



            Graphics2D g2 = (Graphics2D) g.create();



            g2.setRenderingHint(



                RenderingHints.KEY_ANTIALIASING,



                RenderingHints.VALUE_ANTIALIAS_ON



            );



            g2.setColor(getBackground());



            g2.fillRoundRect(



                0, 0, getWidth() - 1,



                getHeight() - 1,



                raio, raio



            );



            g2.dispose();



            super.paintComponent(g);



        }



    }



    public static void main(String[] args) {



        try {



            UIManager.setLookAndFeel(



                UIManager.getSystemLookAndFeelClassName()



            );



        } catch (Exception e) {



            e.printStackTrace();



        }



        EventQueue.invokeLater(() -> {



            new TelaLogin().setVisible(true);



        });



    }



    public TelaPrincipalAluno(int idAlunoLogado) {



        this.idAlunoLogado = idAlunoLogado;



        nomeAlunoLogado = buscarNomeAluno();



        inicializarTela();



        carregarMateriasDoBanco();



    }



    public TelaPrincipalAluno() {



        inicializarTela();



    }



    private void inicializarTela() {



        setTitle("Presente! - Painel do Aluno");



        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);



        setExtendedState(JFrame.MAXIMIZED_BOTH);



        setMinimumSize(new Dimension(1000, 700));



        setLocationRelativeTo(null);



        contentPane = new JPanel(new BorderLayout());



        contentPane.setBackground(FUNDO);



        setContentPane(contentPane);



        // BARRA SUPERIOR



        JPanel topBar = new JPanel(new BorderLayout());



        topBar.setBackground(MARINHO);



        topBar.setBorder(



            BorderFactory.createEmptyBorder(16, 35, 16, 35)



        );



        JLabel lblLogo = new JLabel("✔ Presente!");



        lblLogo.setFont(



            new Font("SansSerif", Font.BOLD, 22)



        );



        lblLogo.setForeground(Color.WHITE);



        topBar.add(lblLogo, BorderLayout.WEST);



        JPanel painelUsuario = new JPanel(



            new FlowLayout(FlowLayout.RIGHT, 12, 0)



        );



        painelUsuario.setOpaque(false);



        JLabel lblNomeUser = new JLabel(



            "<html><div style='text-align:right;'>" +



            "<font color='white'><b>" + escaparHtml(nomeAlunoLogado) + "</b></font>" +



            "<br><font size='2' color='#C7D2FE'>" +



            "Painel do aluno</font></div></html>"



        );



        painelUsuario.add(lblNomeUser);



        JLabel lblAvatar = new JLabel(



            gerarIniciais(nomeAlunoLogado), SwingConstants.CENTER



        ) {



            private static final long serialVersionUID = 1L;



            @Override



            protected void paintComponent(Graphics g) {



                Graphics2D g2 = (Graphics2D) g.create();



                g2.setRenderingHint(



                    RenderingHints.KEY_ANTIALIASING,



                    RenderingHints.VALUE_ANTIALIAS_ON



                );



                g2.setColor(new Color(129, 140, 248));



                g2.fillOval(



                    0, 0, getWidth(), getHeight()



                );



                g2.dispose();



                super.paintComponent(g);



            }



        };



        lblAvatar.setPreferredSize(



            new Dimension(38, 38)



        );



        lblAvatar.setForeground(MARINHO);



        lblAvatar.setFont(



            new Font("SansSerif", Font.BOLD, 14)



        );



        painelUsuario.add(lblAvatar);



        topBar.add(painelUsuario, BorderLayout.EAST);



        contentPane.add(topBar, BorderLayout.NORTH);



        // CONTEÚDO PRINCIPAL



        JPanel painelConteudoPrincipal = new JPanel(



            new BorderLayout()



        );



        painelConteudoPrincipal.setBackground(FUNDO);



        JPanel painelConteudo = new JPanel();



        painelConteudo.setBackground(FUNDO);



        painelConteudo.setLayout(



            new BoxLayout(painelConteudo, BoxLayout.Y_AXIS)



        );



        painelConteudo.setBorder(



            BorderFactory.createEmptyBorder(



                30, 50, 40, 50



            )



        );



        // CABEÇALHO



        JPanel headerConteudo = new JPanel(



            new BorderLayout()



        );



        headerConteudo.setOpaque(false);



        headerConteudo.setMaximumSize(



            new Dimension(Integer.MAX_VALUE, 65)



        );



        JPanel painelTitulos = new JPanel();



        painelTitulos.setOpaque(false);



        painelTitulos.setLayout(



            new BoxLayout(painelTitulos, BoxLayout.Y_AXIS)



        );



        JLabel lblSaudacao = new JLabel(



            "Minhas matérias"



        );



        lblSaudacao.setFont(



            new Font("SansSerif", Font.BOLD, 28)



        );



        lblSaudacao.setForeground(MARINHO);



        painelTitulos.add(lblSaudacao);



        painelTitulos.add(



            Box.createVerticalStrut(4)



        );



        lblStatus = new JLabel();



        lblStatus.setFont(



            new Font("SansSerif", Font.PLAIN, 14)



        );



        lblStatus.setForeground(CINZA);



        painelTitulos.add(lblStatus);



        headerConteudo.add(



            painelTitulos, BorderLayout.WEST



        );



        // BOTÃO NOVA TURMA



        BotaoArredondado btnEntrarMateria =



            new BotaoArredondado(



                "+ Entrar em nova turma", 16



            );



        btnEntrarMateria.setBackground(ROXO);



        btnEntrarMateria.setForeground(Color.WHITE);



        btnEntrarMateria.setFont(



            new Font("SansSerif", Font.BOLD, 13)



        );



        btnEntrarMateria.setPreferredSize(



            new Dimension(210, 44)



        );



        btnEntrarMateria.addMouseListener(



            new MouseAdapter() {



                @Override



                public void mouseEntered(MouseEvent e) {



                    btnEntrarMateria.setBackground(ROXO_HOVER);



                    btnEntrarMateria.repaint();



                }



                @Override



                public void mouseExited(MouseEvent e) {



                    btnEntrarMateria.setBackground(ROXO);



                    btnEntrarMateria.repaint();



                }



            }



        );



        btnEntrarMateria.addActionListener(



            e -> entrarNovaTurma()



        );



        JPanel painelBotaoTop = new JPanel(



            new GridBagLayout()



        );



        painelBotaoTop.setOpaque(false);



        painelBotaoTop.add(btnEntrarMateria);



        headerConteudo.add(



            painelBotaoTop, BorderLayout.EAST



        );



        painelConteudo.add(headerConteudo);



        painelConteudo.add(



            Box.createVerticalStrut(22)



        );



        // FILTROS



        painelFiltros = new JPanel(



            new FlowLayout(FlowLayout.LEFT, 10, 0)



        );



        painelFiltros.setOpaque(false);



        painelFiltros.add(



            criarBotaoFiltro("Todas", true)



        );



        painelFiltros.add(



            criarBotaoFiltro("Unisa", false)



        );



        painelFiltros.add(



            criarBotaoFiltro("Unip", false)



        );



        painelFiltros.add(



            criarBotaoFiltro("Uninove", false)



        );



        painelConteudo.add(painelFiltros);



        painelConteudo.add(



            Box.createVerticalStrut(22)



        );



        // GRADE DE MATÉRIAS



        painelGrid = new JPanel(



            new GridLayout(0, 4, 24, 24)



        );



        painelGrid.setOpaque(false);



        // As matérias são carregadas do banco para o aluno autenticado.



        JPanel painelGridWrapper = new JPanel(



            new FlowLayout(FlowLayout.LEFT, 0, 0)



        );



        painelGridWrapper.setOpaque(false);



        painelGridWrapper.add(painelGrid);



        painelConteudo.add(painelGridWrapper);



        atualizarMaterias();



        JScrollPane scrollPane = new JScrollPane(



            painelConteudo



        );



        scrollPane.setBorder(null);



        scrollPane.getVerticalScrollBar()



            .setUnitIncrement(16);



        painelConteudoPrincipal.add(



            scrollPane, BorderLayout.CENTER



        );



        // ASSISTENTE



        JPanel painelFlutuante = new JPanel(



            new FlowLayout(FlowLayout.RIGHT, 30, 20)



        );



        painelFlutuante.setOpaque(false);



        BotaoArredondado btnAssistente =



            new BotaoArredondado(



                "Pergunte ao assistente ✨", 28



            );



        btnAssistente.setBackground(ROXO_HOVER);



        btnAssistente.setForeground(Color.WHITE);



        btnAssistente.setFont(



            new Font("SansSerif", Font.BOLD, 13)



        );



        btnAssistente.setPreferredSize(



            new Dimension(215, 52)



        );



        btnAssistente.addActionListener(e -> {

            if (idAlunoLogado <= 0) {

                JOptionPane.showMessageDialog(

                    this,

                    "Entre com sua conta de aluno para usar o assistente.",

                    "Assistente",

                    JOptionPane.INFORMATION_MESSAGE

                );

                return;

            }



            TelaAssistente assistente = new TelaAssistente(idAlunoLogado);

            assistente.setLocationRelativeTo(this);

            assistente.setVisible(true);

        });



        painelFlutuante.add(btnAssistente);



        painelConteudoPrincipal.add(



            painelFlutuante, BorderLayout.SOUTH



        );



        contentPane.add(



            painelConteudoPrincipal, BorderLayout.CENTER



        );



    }



    // FILTROS CLICÁVEIS



    private JButton criarBotaoFiltro(



            String texto, boolean selecionado) {



        BotaoArredondado btn =



            new BotaoArredondado(texto, 12);



        btn.setFont(



            new Font("SansSerif", Font.BOLD, 12)



        );



        btn.setPreferredSize(



            new Dimension(



                texto.equals("Todas") ? 80 : 90,



                32



            )



        );



        aplicarVisualFiltro(btn, selecionado);



        btn.addActionListener(e -> {



            filtroSelecionado = texto;



            for (Component componente :



                    painelFiltros.getComponents()) {



                if (componente instanceof JButton) {



                    JButton botao = (JButton) componente;



                    aplicarVisualFiltro(



                        botao,



                        botao.getText().equalsIgnoreCase(



                            filtroSelecionado



                        )



                    );



                }



            }



            atualizarMaterias();



        });



        return btn;



    }



    private void aplicarVisualFiltro(



            JButton botao, boolean ativo) {



        botao.setBackground(



            ativo ? ROXO : Color.WHITE



        );



        botao.setForeground(



            ativo ? Color.WHITE :



                new Color(71, 85, 105)



        );



        botao.repaint();



    }



    // ATUALIZAR CARDS PELO FILTRO



    private void atualizarMaterias() {



        painelGrid.removeAll();



        int quantidadeVisivel = 0;



        for (DadosMateria materia : materias) {



            if (!filtroSelecionado.equalsIgnoreCase("Todas")



                    && !materia.instituicao.equalsIgnoreCase(



                        filtroSelecionado)) {



                continue;



            }



            JPanel card = montarCardMateria(materia);



            MouseAdapter clique = new MouseAdapter() {



                @Override



                public void mouseClicked(MouseEvent e) {



                    abrirHistorico(materia);



                }



            };



            adicionarCliqueRecursivo(card, clique);



            painelGrid.add(card);



            quantidadeVisivel++;



        }



        // CARD PARA ENTRAR EM NOVA TURMA



        JPanel cardPlus = montarCardNovaTurma();



        MouseAdapter cliqueNovaTurma = new MouseAdapter() {



            @Override



            public void mouseClicked(MouseEvent e) {



                entrarNovaTurma();



            }



        };



        adicionarCliqueRecursivo(



            cardPlus, cliqueNovaTurma



        );



        painelGrid.add(cardPlus);



        if (quantidadeVisivel == 0) {



            lblStatus.setText(



                "Nenhuma matéria encontrada neste filtro."



            );



        } else {



            lblStatus.setText(



                quantidadeVisivel +



                (quantidadeVisivel == 1



                    ? " matéria encontrada."



                    : " matérias encontradas.")



            );



        }



        painelGrid.revalidate();



        painelGrid.repaint();



    }



    // CLIQUE EM QUALQUER PARTE DO CARD



    private void adicionarCliqueRecursivo(



            Component componente,



            MouseAdapter clique) {



        componente.addMouseListener(clique);



        if (componente instanceof Container) {



            Container container = (Container) componente;



            for (Component filho :



                    container.getComponents()) {



                adicionarCliqueRecursivo(



                    filho, clique



                );



            }



        }



    }



    // CARD DA MATÉRIA



    private JPanel montarCardMateria(



            DadosMateria materia) {



        CardMateriaArredondado card =



            new CardMateriaArredondado();



        card.setLayout(



            new BoxLayout(card, BoxLayout.Y_AXIS)



        );



        card.setBorder(



            BorderFactory.createEmptyBorder(



                12, 16, 12, 16



            )



        );



        JLabel lblInst = new JLabel(



            "<html><font color='#4F46E5'>●</font>" +



            "  <font color='#1E1B4B'><b>" +



            materia.instituicao.toUpperCase() +



            "</b></font></html>"



        );



        lblInst.setFont(



            new Font("SansSerif", Font.BOLD, 10)



        );



        lblInst.setAlignmentX(



            Component.LEFT_ALIGNMENT



        );



        JLabel lblMateria = new JLabel(



            materia.nome



        );



        lblMateria.setFont(



            new Font("SansSerif", Font.BOLD, 15)



        );



        lblMateria.setForeground(MARINHO);



        lblMateria.setAlignmentX(



            Component.LEFT_ALIGNMENT



        );



        JPanel painelPilulas = new JPanel(



            new FlowLayout(FlowLayout.LEFT, 6, 0)



        );



        painelPilulas.setOpaque(false);



        painelPilulas.setAlignmentX(



            Component.LEFT_ALIGNMENT



        );



        PilulaArredondada pilulaTurma =



            new PilulaArredondada(



                new Color(224, 231, 255), null



            );



        pilulaTurma.setLayout(



            new FlowLayout(FlowLayout.CENTER, 6, 2)



        );



        JLabel lblTurma = new JLabel(



            materia.turma



        );



        lblTurma.setFont(



            new Font("SansSerif", Font.BOLD, 10)



        );



        lblTurma.setForeground(MARINHO);



        pilulaTurma.add(lblTurma);



        PilulaArredondada pilulaProf =



            new PilulaArredondada(



                new Color(224, 231, 255), null



            );



        pilulaProf.setLayout(



            new FlowLayout(FlowLayout.CENTER, 6, 2)



        );



        JLabel lblProf = new JLabel(



            "Prof. " + materia.professor



        );



        lblProf.setFont(



            new Font("SansSerif", Font.PLAIN, 10)



        );



        lblProf.setForeground(MARINHO);



        pilulaProf.add(lblProf);



        painelPilulas.add(pilulaTurma);



        painelPilulas.add(pilulaProf);



        JPanel painelStatus = new JPanel(



            new BorderLayout()



        );



        painelStatus.setOpaque(false);



        painelStatus.setAlignmentX(



            Component.LEFT_ALIGNMENT



        );



        painelStatus.setMaximumSize(



            new Dimension(Integer.MAX_VALUE, 22)



        );



        int percentual = materia.limite > 0



            ? (int) ((double) materia.faltas /



                materia.limite * 100)



            : 0;



        JLabel lblFaltas = new JLabel(



            "<html>Faltas: <b>" +



            materia.faltas +



            "</b> de " +



            materia.limite +



            " (" + percentual + "% do limite)</html>"



        );



        lblFaltas.setFont(



            new Font("SansSerif", Font.PLAIN, 11)



        );



        lblFaltas.setForeground(MARINHO);



        PilulaArredondada badgeStatus =



            new PilulaArredondada(



                materia.fundoStatus, null



            );



        badgeStatus.setLayout(



            new FlowLayout(FlowLayout.CENTER, 8, 2)



        );



        JLabel lblBadge = new JLabel(



            materia.status



        );



        lblBadge.setFont(



            new Font("SansSerif", Font.BOLD, 10)



        );



        lblBadge.setForeground(



            materia.corStatus



        );



        badgeStatus.add(lblBadge);



        painelStatus.add(



            lblFaltas, BorderLayout.WEST



        );



        painelStatus.add(



            badgeStatus, BorderLayout.EAST



        );



        JPanel barra = new JPanel() {



            private static final long serialVersionUID = 1L;



            @Override



            protected void paintComponent(Graphics g) {



                Graphics2D g2 = (Graphics2D) g.create();



                g2.setRenderingHint(



                    RenderingHints.KEY_ANTIALIASING,



                    RenderingHints.VALUE_ANTIALIAS_ON



                );



                int largura = getWidth();



                int altura = getHeight();



                g2.setColor(



                    new Color(216, 222, 238)



                );



                g2.fillRoundRect(



                    0, 0, largura, altura,



                    altura, altura



                );



                double proporcao = materia.limite > 0



                    ? (double) materia.faltas /



                        materia.limite



                    : 0;



                proporcao = Math.max(



                    0, Math.min(1, proporcao)



                );



                int preenchimento =



                    (int) (largura * proporcao);



                if (preenchimento > 0) {



                    g2.setColor(materia.corStatus);



                    g2.fillRoundRect(



                        0, 0,



                        Math.max(preenchimento, altura),



                        altura, altura, altura



                    );



                }



                g2.dispose();



            }



        };



        barra.setOpaque(false);



        barra.setPreferredSize(



            new Dimension(0, 8)



        );



        barra.setMaximumSize(



            new Dimension(Integer.MAX_VALUE, 8)



        );



        barra.setAlignmentX(



            Component.LEFT_ALIGNMENT



        );



        card.add(lblInst);



        card.add(Box.createVerticalStrut(2));



        card.add(lblMateria);



        card.add(Box.createVerticalStrut(6));



        card.add(painelPilulas);



        card.add(Box.createVerticalStrut(8));



        card.add(painelStatus);



        card.add(Box.createVerticalStrut(6));



        card.add(barra);



        return card;



    }



    // CARD DE ENTRAR EM NOVA TURMA



    private JPanel montarCardNovaTurma() {



        CardNovaTurmaArredondado card =



            new CardNovaTurmaArredondado();



        card.setLayout(new GridBagLayout());



        JPanel centro = new JPanel();



        centro.setOpaque(false);



        centro.setLayout(



            new BoxLayout(centro, BoxLayout.Y_AXIS)



        );



        BolinhaMaisIcone bolinha =



            new BolinhaMaisIcone();



        bolinha.setAlignmentX(



            Component.CENTER_ALIGNMENT



        );



        centro.add(bolinha);



        centro.add(



            Box.createVerticalStrut(8)



        );



        JLabel lblTitulo = new JLabel(



            "Entrar em nova turma"



        );



        lblTitulo.setFont(



            new Font("SansSerif", Font.BOLD, 12)



        );



        lblTitulo.setForeground(MARINHO);



        lblTitulo.setAlignmentX(



            Component.CENTER_ALIGNMENT



        );



        centro.add(lblTitulo);



        centro.add(



            Box.createVerticalStrut(2)



        );



        JLabel lblSub = new JLabel(



            "Tenha em mãos o código de 6 dígitos"



        );



        lblSub.setFont(



            new Font("SansSerif", Font.PLAIN, 10)



        );



        lblSub.setForeground(CINZA);



        lblSub.setAlignmentX(



            Component.CENTER_ALIGNMENT



        );



        centro.add(lblSub);



        card.add(centro);



        return card;



    }



    // ABRIR HISTÓRICO DA MATÉRIA



    private void abrirHistorico(DadosMateria materia) {



        if (idAlunoLogado <= 0 || materia.idTurma <= 0) {

            JOptionPane.showMessageDialog(

                this,

                "Não foi possível identificar o aluno ou a turma. Entre novamente no sistema.",

                "Histórico da matéria",

                JOptionPane.WARNING_MESSAGE

            );

            return;

        }



        TelaHistoricoAluno historico = new TelaHistoricoAluno(idAlunoLogado, materia.idTurma);

        historico.setLocationRelativeTo(this);

        historico.setVisible(true);



    }



    // ENTRAR EM NOVA TURMA



    private void entrarNovaTurma() {



        if (idAlunoLogado <= 0) {



            JOptionPane.showMessageDialog(this,



                "Entre pelo login do aluno para acessar suas matrículas.");



            return;



        }



        TelaMatricula tela = new TelaMatricula(idAlunoLogado, this::carregarMateriasDoBanco);



        tela.setLocationRelativeTo(this);



        tela.setVisible(true);



    }



    private void carregarMateriasDoBanco() {



        if (idAlunoLogado <= 0) return;



        lblStatus.setText("Carregando suas matérias...");



        new SwingWorker<List<DadosMateria>, Void>() {



            @Override



            protected List<DadosMateria> doInBackground() throws Exception {



                List<DadosMateria> resultado = new ArrayList<>();



                // Carga horária está em HORAS. Limite cadastrado é PERCENTUAL.
                String sql = "SELECT t.idTURMA, t.instituicao, t.nome_materia, "
                    + "t.nome_turma, t.periodo, u.nome AS professor, "
                    + "t.carga_horaria, t.limites_faltas, "
                    + "(SELECT COUNT(*) FROM registros r WHERE r.turma_id = t.idTURMA) AS chamadas, "
                    + "(SELECT COUNT(*) FROM `presença` p "
                    + "JOIN registros r ON r.idREGISTROS = p.registro_id "
                    + "WHERE r.turma_id = t.idTURMA AND p.aluno_id = m.aluno_id) AS marcadas, "
                    + "(SELECT COALESCE(SUM(CASE "
                    + "WHEN UPPER(TRIM(p.status_presenca)) IN ('FALTA','F','AUSENTE','FALTOU','FALTOSO') "
                    + "THEN COALESCE(r.quantidade_aula, 1) ELSE 0 END), 0) "
                    + "FROM `presença` p JOIN registros r ON r.idREGISTROS = p.registro_id "
                    + "WHERE r.turma_id = t.idTURMA AND p.aluno_id = m.aluno_id) AS faltas "
                    + "FROM matricula m JOIN turma t ON t.idTURMA = m.turma_id "
                    + "LEFT JOIN usuarios u ON u.idUSUARIOS = t.professor_id "
                    + "WHERE m.aluno_id = ? ORDER BY t.instituicao, t.nome_materia";

                try (java.sql.Connection con = conexao.ConexaoMySQL.obterConexao()) {



                    if (con == null) throw new java.sql.SQLException("Conexão não disponível.");



                    try (java.sql.PreparedStatement ps = con.prepareStatement(sql)) {



                        ps.setInt(1, idAlunoLogado);



                        try (java.sql.ResultSet rs = ps.executeQuery()) {



                            while (rs.next()) {



                                int carga = rs.getInt("carga_horaria");
                                boolean cargaValida = !rs.wasNull() && carga > 0;
                                java.math.BigDecimal porcentagem = rs.getBigDecimal("limites_faltas");
                                boolean limiteValido = cargaValida && porcentagem != null;
                                int limite = limiteValido
                                    ? CalculoCargaHoraria.limiteFaltas(carga, porcentagem) : 0;
                                int faltas = Math.max(0, rs.getInt("faltas"));
                                int chamadas = rs.getInt("chamadas");
                                int marcadas = rs.getInt("marcadas");

                                String status;
                                Color fundoStatus;
                                Color corStatus;
                                if (chamadas == 0) {
                                    status = "Sem chamadas";
                                    fundoStatus = new Color(241, 245, 249);
                                    corStatus = new Color(100, 116, 139);
                                } else if (marcadas < chamadas || !limiteValido) {
                                    status = "A apurar";
                                    fundoStatus = new Color(254, 249, 195);
                                    corStatus = new Color(161, 98, 7);
                                } else if (faltas > limite) {
                                    status = "Crítico";
                                    fundoStatus = new Color(254, 226, 226);
                                    corStatus = new Color(185, 28, 28);
                                } else if (faltas == limite) {
                                    status = "No limite";
                                    fundoStatus = new Color(254, 226, 226);
                                    corStatus = new Color(185, 28, 28);
                                } else if (limite > 0 && faltas * 100L >= limite * 75L) {
                                    status = "Atenção";
                                    fundoStatus = new Color(254, 249, 195);
                                    corStatus = new Color(161, 98, 7);
                                } else {
                                    status = "Regular";
                                    fundoStatus = new Color(220, 252, 231);
                                    corStatus = new Color(22, 163, 74);
                                }

                                DadosMateria m = new DadosMateria(
                                    rs.getString("instituicao"),
                                    rs.getString("nome_materia"),
                                    rs.getString("nome_turma") + " · " + rs.getString("periodo"),
                                    rs.getString("professor") == null ? "Não informado" : rs.getString("professor"),
                                    faltas, limite, status, fundoStatus, corStatus);
                                m.idTurma = rs.getInt("idTURMA");

                                resultado.add(m);



                            }



                        }



                    }



                }



                return resultado;



            }



            @Override



            protected void done() {



                try {



                    materias.clear();



                    materias.addAll(get());



                    atualizarMaterias();



                } catch (Exception ex) {



                    lblStatus.setText("Não foi possível carregar as matérias.");



                    JOptionPane.showMessageDialog(TelaPrincipalAluno.this,



                        "Erro ao consultar suas matrículas: " + ex.getMessage());



                }



            }



        }.execute();



    }



}
