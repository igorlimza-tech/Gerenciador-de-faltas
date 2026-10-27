package view.faltas;



import java.awt.*;

import java.awt.event.*;

import java.util.*;

import javax.swing.*;

import java.sql.Connection;

import java.sql.PreparedStatement;

import java.sql.ResultSet;

import java.sql.SQLException;

import java.sql.Statement;

import java.security.SecureRandom;

import java.math.BigDecimal;

import conexao.ConexaoMySQL;
import util.faltas.CalculoCargaHoraria;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;



/** Tela de criação de turmas do Presente! (Java Swing). */

public class TelaCriarTurma extends JFrame {

    private static final long serialVersionUID = 1L;

    private static final Color FUNDO = new Color(248, 250, 252);

    private static final Color ROXO_ESCURO = new Color(30, 27, 75);

    private static final Color ROXO = new Color(79, 70, 229);

    private static final Color BORDA = new Color(226, 232, 240);

    private static final Color CINZA = new Color(100, 116, 139);

    private static final String[] DIAS = {"Segunda", "Terça", "Quarta", "Quinta", "Sexta"};



    private JTextField txtRF, txtEmail, txtInstituicao, txtDisciplina, txtCurso, txtTurma;

    private JTextField txtCargaHoraria, txtLimiteFaltas;

    private final Map<String, JButton> botoesDias = new LinkedHashMap<>();

    private final Set<String> diasSelecionados = new LinkedHashSet<>();

    private final Map<String, JPanel> cardsDias = new LinkedHashMap<>();
    private final Map<String, JPanel> linhasPorDia = new LinkedHashMap<>();

    private JPanel painelDiasContainer;

    private JButton btnManha, btnTarde, btnNoite;

    private String turnoSelecionado = "Manhã";

    private final int idProfessor;
    private final String nomeProfessor;

    private final Runnable aoCriar;

    private final SecureRandom aleatorio = new SecureRandom();



    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }

            catch (Exception ignored) { }

            new TelaCriarTurma().setVisible(true);

        });

    }



    public TelaCriarTurma() {

        this(-1, null);

    }



    public TelaCriarTurma(int idProfessor) {

        this(idProfessor, null);

    }



    public TelaCriarTurma(int idProfessor, Runnable aoCriar) {

        this.idProfessor = idProfessor;
        this.nomeProfessor = buscarNomeProfessor(idProfessor);

        this.aoCriar = aoCriar;

        setTitle("Presente! - Criar Nova Turma");

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        setMinimumSize(new Dimension(1100, 700));

        setSize(1300, 850);

        setLocationRelativeTo(null);

        setExtendedState(JFrame.MAXIMIZED_BOTH);



        JPanel raiz = new JPanel(new BorderLayout());

        raiz.setBackground(FUNDO);

        setContentPane(raiz);

        raiz.add(criarBarraSuperior(), BorderLayout.NORTH);



        JPanel conteudo = new JPanel() {

            private static final long serialVersionUID = 1L;

            @Override public Dimension getPreferredSize() {

                Dimension d = super.getPreferredSize();

                if (getParent() instanceof JViewport) {

                    d.width = Math.max(d.width, getParent().getWidth());

                }

                return d;

            }

        };

        conteudo.setBackground(FUNDO);

        conteudo.setBorder(BorderFactory.createEmptyBorder(22, 28, 35, 28));

        conteudo.setLayout(new BoxLayout(conteudo, BoxLayout.Y_AXIS));

        conteudo.setMinimumSize(new Dimension(1000, 0));

        conteudo.add(criarCabecalho());

        conteudo.add(Box.createVerticalStrut(18));



        // As duas colunas dividem toda a largura disponível igualmente.

        JPanel colunas = new JPanel(new GridLayout(1, 2, 22, 0));

        colunas.setOpaque(false);

        colunas.setAlignmentX(Component.LEFT_ALIGNMENT);

        colunas.add(criarColunaEsquerda());

        colunas.add(criarColunaDireita());

        conteudo.add(colunas);



        JScrollPane rolagem = new JScrollPane(conteudo,

                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,

                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        rolagem.setBorder(null);

        rolagem.getVerticalScrollBar().setUnitIncrement(18);

        raiz.add(rolagem, BorderLayout.CENTER);

        rolagem.getViewport().addChangeListener(e -> {

            int largura = rolagem.getViewport().getWidth();

            if (largura > 0 && conteudo.getWidth() != largura) {

                conteudo.setSize(largura, conteudo.getPreferredSize().height);

                conteudo.revalidate();

            }

        });



        selecionarDia("Segunda");

        selecionarDia("Terça");

    }



    private static String buscarNomeProfessor(int id) {
        if (id <= 0) return "Professor";
        try (Connection c = ConexaoMySQL.obterConexao();
             PreparedStatement ps = c.prepareStatement("SELECT nome FROM usuarios WHERE idUSUARIOS = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString(1);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return "Professor";
    }
    private static String iniciais(String nome) {
        String[] p = nome.trim().split("\\s+");
        return p.length > 1 ? (p[0].substring(0,1) + p[p.length-1].substring(0,1)).toUpperCase()
            : (p[0].isEmpty() ? "P" : p[0].substring(0,1).toUpperCase());
    }
    private JPanel criarBarraSuperior() {

        JPanel barra = new JPanel(new BorderLayout());

        barra.setBackground(ROXO_ESCURO);

        barra.setBorder(BorderFactory.createEmptyBorder(14, 28, 14, 28));

        JLabel logo = new JLabel("✓ Presente!");

        logo.setFont(new Font("SansSerif", Font.BOLD, 23));

        logo.setForeground(Color.WHITE);

        barra.add(logo, BorderLayout.WEST);



        JPanel usuario = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));

        usuario.setOpaque(false);

        JLabel nome = new JLabel("<html><div style='text-align:right'><b>" + nomeProfessor.replace("&", "&amp;").replace("<", "&lt;") + "</b>"

                + "<br><font color='#C7D2FE'>Professor</font></div></html>");

        nome.setForeground(Color.WHITE);

        nome.setFont(new Font("SansSerif", Font.PLAIN, 13));

        usuario.add(nome);

        JLabel avatar = new JLabel(iniciais(nomeProfessor), SwingConstants.CENTER) {

            private static final long serialVersionUID = 1L;

            @Override protected void paintComponent(Graphics g) {

                Graphics2D g2 = (Graphics2D) g.create();

                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(new Color(129, 140, 248));

                g2.fillOval(0, 0, getWidth(), getHeight());

                g2.dispose();

                super.paintComponent(g);

            }

        };

        avatar.setPreferredSize(new Dimension(40, 40));

        avatar.setForeground(ROXO_ESCURO);

        avatar.setFont(new Font("SansSerif", Font.BOLD, 13));

        usuario.add(avatar);

        barra.add(usuario, BorderLayout.EAST);

        return barra;

    }



    private JPanel criarCabecalho() {

        JPanel p = new JPanel();

        p.setOpaque(false);

        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        p.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel voltar = new JLabel("‹ Minhas turmas");

        voltar.setForeground(ROXO);

        voltar.setFont(new Font("SansSerif", Font.BOLD, 12));

        voltar.setAlignmentX(Component.LEFT_ALIGNMENT);

        voltar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        voltar.addMouseListener(new MouseAdapter() {

            @Override

            public void mouseClicked(MouseEvent e) {

                dispose();

            }



            @Override

            public void mouseEntered(MouseEvent e) {

                voltar.setText("<html><u>‹ Minhas turmas</u></html>");

            }



            @Override

            public void mouseExited(MouseEvent e) {

                voltar.setText("‹ Minhas turmas");

            }

        });

        p.add(voltar);

        p.add(Box.createVerticalStrut(7));

        JLabel titulo = new JLabel("Criar nova turma");

        titulo.setFont(new Font("SansSerif", Font.BOLD, 24));

        titulo.setForeground(ROXO_ESCURO);

        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        p.add(titulo);

        return p;

    }



    private JPanel criarColunaEsquerda() {

        JPanel coluna = new JPanel();

        coluna.setOpaque(false);

        coluna.setLayout(new BoxLayout(coluna, BoxLayout.Y_AXIS));

        // A coluna usa a altura natural dos cartões.



        PainelArredondado dados = criarCard(510);

        dados.add(tituloSecao("1", "Seus dados nesta instituição"));

        dados.add(Box.createVerticalStrut(14));

        txtRF = adicionarCampo(dados, "Registro funcional", "RF 0048213");

        dados.add(Box.createVerticalStrut(12));

        txtEmail = adicionarCampo(dados, "E-mail institucional", "carlos.menezes@horizonte.edu.br");

        dados.add(Box.createVerticalStrut(6));

        JLabel notaEmail = nota("Os alunos da turma vão ver este e-mail na página da disciplina.");

        dados.add(notaEmail);

        dados.setMaximumSize(new Dimension(Integer.MAX_VALUE, dados.getPreferredSize().height));

        coluna.add(dados);

        coluna.add(Box.createVerticalStrut(16));



        PainelArredondado turma = criarCard(510);

        turma.add(tituloSecao("2", "Dados da turma"));

        turma.add(Box.createVerticalStrut(14));

        txtInstituicao = adicionarCampo(turma, "Instituição", "Faculdade Horizonte");

        turma.add(Box.createVerticalStrut(12));

        txtDisciplina = adicionarCampo(turma, "Nome da disciplina", "Banco de Dados");

        turma.add(Box.createVerticalStrut(14));



        // Curso, turma e turno na mesma linha. O curso ocupa todo o espaço restante.

        JPanel linha = new JPanel(new GridBagLayout());

        linha.setOpaque(false);

        linha.setAlignmentX(Component.LEFT_ALIGNMENT);

        linha.setMaximumSize(new Dimension(Integer.MAX_VALUE, 66));



        GridBagConstraints g = new GridBagConstraints();

        g.gridy = 0;

        g.anchor = GridBagConstraints.NORTHWEST;

        g.fill = GridBagConstraints.HORIZONTAL;

        g.insets = new Insets(0, 0, 0, 12);



        JPanel curso = colunaCampo("Curso", 320);

        curso.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));

        txtCurso = campo("Análise e Desenvolvimento de Sistemas", 320);

        txtCurso.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));

        curso.add(txtCurso);

        g.gridx = 0;

        g.weightx = 1.0;

        linha.add(curso, g);



        JPanel letra = colunaCampo("Turma", 70);

        txtTurma = campo("B", 70);

        letra.add(txtTurma);

        g.gridx = 1;

        g.weightx = 0;

        linha.add(letra, g);



        JPanel turno = colunaCampo("Turno", 270);

        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));

        botoes.setOpaque(false);

        botoes.setAlignmentX(Component.LEFT_ALIGNMENT);

        botoes.setPreferredSize(new Dimension(270, 36));

        botoes.setMaximumSize(new Dimension(270, 36));

        btnManha = botaoTurno("Manhã");

        btnTarde = botaoTurno("Tarde");

        btnNoite = botaoTurno("Noite");

        botoes.add(btnManha);

        botoes.add(btnTarde);

        botoes.add(btnNoite);

        turno.add(botoes);

        g.gridx = 2;

        g.insets = new Insets(0, 0, 0, 0);

        linha.add(turno, g);



        turma.add(linha);

        turma.setMaximumSize(new Dimension(Integer.MAX_VALUE, turma.getPreferredSize().height));

        coluna.add(turma);

        coluna.add(Box.createVerticalStrut(18));



        JPanel acoes = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));

        acoes.setOpaque(false);

        acoes.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton cancelar = botao("Cancelar", 115, 38, new Color(241, 245, 249), new Color(71, 85, 105));

        JButton criar = botao("Criar turma", 140, 38, ROXO, Color.WHITE);

        cancelar.addActionListener(e -> dispose());

        criar.addActionListener(e -> salvarTurma(criar));

        acoes.add(cancelar);

        acoes.add(criar);

        coluna.add(acoes);

        return coluna;

    }



    private JPanel criarColunaDireita() {

        JPanel coluna = new JPanel();

        coluna.setOpaque(false);

        coluna.setLayout(new BoxLayout(coluna, BoxLayout.Y_AXIS));

        // A altura da coluna acompanha os campos e os dias selecionados.

        PainelArredondado card = criarCard(530);

        card.add(tituloSecao("3", "Grade e Carga Horária"));

        card.add(Box.createVerticalStrut(17));

        card.add(rotulo("Dias em que você dá aula:"));

        card.add(Box.createVerticalStrut(7));



        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));

        botoes.setOpaque(false);

        botoes.setAlignmentX(Component.LEFT_ALIGNMENT);

        botoes.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        for (String dia : DIAS) {

            JButton b = botao(dia, 91, 34, new Color(241, 245, 249), new Color(71, 85, 105));

            b.setFont(new Font("SansSerif", Font.BOLD, 11));

            b.addActionListener(e -> selecionarDia(dia));

            botoesDias.put(dia, b);

            botoes.add(b);

        }

        card.add(botoes);

        card.add(Box.createVerticalStrut(19));

        txtCargaHoraria = adicionarCampo(card, "Carga horária total (horas de 60 min):", "80");

        card.add(Box.createVerticalStrut(6));

        card.add(nota("Cada aula tem 50 min. Ex.: 100 horas = 120 aulas. A grade define quantas aulas há por semana."));

        card.add(Box.createVerticalStrut(15));

        txtLimiteFaltas = adicionarCampo(card, "Limite de faltas permitido (%)", "25");

        card.add(Box.createVerticalStrut(17));

        card.add(rotulo("Horários das aulas por dia:"));

        card.add(Box.createVerticalStrut(9));



        painelDiasContainer = new JPanel();

        painelDiasContainer.setOpaque(false);

        painelDiasContainer.setLayout(new BoxLayout(painelDiasContainer, BoxLayout.Y_AXIS));

        painelDiasContainer.setAlignmentX(Component.LEFT_ALIGNMENT);

        // A rolagem principal da janela acompanha a altura dos cartões.

        card.add(painelDiasContainer);

        coluna.add(card);

        return coluna;

    }



    private void selecionarDia(String dia) {

        if (!diasSelecionados.add(dia)) diasSelecionados.remove(dia);

        for (Map.Entry<String, JButton> entry : botoesDias.entrySet()) {

            boolean ativo = diasSelecionados.contains(entry.getKey());

            entry.getValue().setBackground(ativo ? ROXO : new Color(241, 245, 249));

            entry.getValue().setForeground(ativo ? Color.WHITE : new Color(71, 85, 105));

            entry.getValue().repaint();

        }

        painelDiasContainer.removeAll();

        // Os cartões são reutilizados, preservando os horários digitados.

        for (String nome : DIAS) {

            if (diasSelecionados.contains(nome)) {

                JPanel card = cardsDias.computeIfAbsent(nome, this::criarCardDia);

                painelDiasContainer.add(card);

                painelDiasContainer.add(Box.createVerticalStrut(10));

            }

        }

        painelDiasContainer.revalidate();

        painelDiasContainer.repaint();

    }



    private JPanel criarCardDia(String dia) {

        PainelArredondado card = new PainelArredondado(FUNDO, BORDA, 16);

        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        card.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));

        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));



        JPanel cabecalho = new JPanel(new BorderLayout());

        cabecalho.setOpaque(false);

        cabecalho.setAlignmentX(Component.LEFT_ALIGNMENT);

        cabecalho.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));

        cabecalho.setPreferredSize(new Dimension(466, 34));

        JLabel titulo = rotulo(dia);

        titulo.setFont(new Font("SansSerif", Font.BOLD, 13));

        cabecalho.add(titulo, BorderLayout.WEST);

        JButton adicionar = botao("+ Adicionar horário", 158, 30,

                new Color(224, 231, 255), ROXO);

        adicionar.setFont(new Font("SansSerif", Font.BOLD, 11));

        cabecalho.add(adicionar, BorderLayout.EAST);

        card.add(cabecalho);

        card.add(Box.createVerticalStrut(9));



        JPanel linhas = new JPanel();

        linhas.setOpaque(false);

        linhas.setLayout(new BoxLayout(linhas, BoxLayout.Y_AXIS));

        linhas.setAlignmentX(Component.LEFT_ALIGNMENT);

        linhas.add(criarLinhaHorario("07:30", "11:10", false, linhas));
        linhasPorDia.put(dia, linhas);

        adicionar.addActionListener(e -> {

            linhas.add(Box.createVerticalStrut(7));

            linhas.add(criarLinhaHorario("13:00", "16:40", true, linhas));

            linhas.revalidate();

            card.setMaximumSize(new Dimension(Integer.MAX_VALUE, card.getPreferredSize().height));

            card.revalidate();

            card.repaint();

        });

        card.add(linhas);

        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, card.getPreferredSize().height));

        return card;

    }



    private JPanel criarLinhaHorario(String inicio, String fim, boolean removivel, JPanel container) {

        JPanel linha = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));

        linha.setOpaque(false);

        linha.setAlignmentX(Component.LEFT_ALIGNMENT);

        linha.setPreferredSize(new Dimension(466, 56));

        linha.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));

        linha.add(colunaHorario("Início", inicio));

        linha.add(colunaHorario("Fim", fim));

        if (removivel) {

            JButton remover = botao("Remover", 86, 30, new Color(254, 226, 226), new Color(185, 28, 28));

            remover.addActionListener(e -> {

                int indice = Arrays.asList(container.getComponents()).indexOf(linha);

                if (indice >= 0) {

                    container.remove(indice);

                    if (indice > 0 && container.getComponent(indice - 1) instanceof Box.Filler)

                        container.remove(indice - 1);

                }

                container.revalidate();

                container.repaint();

            });

            JPanel alinhar = new JPanel(new BorderLayout());

            alinhar.setOpaque(false);

            alinhar.setPreferredSize(new Dimension(86, 56));

            alinhar.add(remover, BorderLayout.SOUTH);

            linha.add(alinhar);

        }

        return linha;

    }



    private JPanel colunaHorario(String nome, String valor) {

        JPanel p = new JPanel();

        p.setOpaque(false);

        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        p.setPreferredSize(new Dimension(90, 56));

        JLabel l = new JLabel(nome);

        l.setForeground(CINZA);

        l.setFont(new Font("SansSerif", Font.PLAIN, 11));

        l.setAlignmentX(Component.LEFT_ALIGNMENT);

        p.add(l);

        p.add(Box.createVerticalStrut(4));

        p.add(campo(valor, 90));

        return p;

    }



    private PainelArredondado criarCard(int largura) {

        PainelArredondado p = new PainelArredondado(Color.WHITE, BORDA, 20);

        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        p.setBorder(BorderFactory.createEmptyBorder(20, 18, 20, 18));

        p.setAlignmentX(Component.LEFT_ALIGNMENT);

        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        return p;

    }



    private JPanel tituloSecao(String numero, String texto) {

        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 9, 0));

        p.setOpaque(false);

        p.setAlignmentX(Component.LEFT_ALIGNMENT);

        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));

        JLabel n = new JLabel(numero, SwingConstants.CENTER) {

            private static final long serialVersionUID = 1L;

            @Override protected void paintComponent(Graphics g) {

                Graphics2D g2 = (Graphics2D) g.create();

                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(new Color(224, 231, 255));

                g2.fillOval(0, 0, getWidth(), getHeight());

                g2.dispose();

                super.paintComponent(g);

            }

        };

        n.setPreferredSize(new Dimension(25, 25));

        n.setForeground(ROXO);

        n.setFont(new Font("SansSerif", Font.BOLD, 12));

        JLabel t = new JLabel(texto);

        t.setForeground(ROXO_ESCURO);

        t.setFont(new Font("SansSerif", Font.BOLD, 15));

        p.add(n);

        p.add(t);

        return p;

    }



    private JLabel rotulo(String texto) {

        JLabel l = new JLabel(texto);

        l.setFont(new Font("SansSerif", Font.BOLD, 12));

        l.setForeground(ROXO_ESCURO);

        l.setAlignmentX(Component.LEFT_ALIGNMENT);

        return l;

    }



    private JLabel nota(String texto) {

        JLabel l = new JLabel(texto);

        l.setFont(new Font("SansSerif", Font.PLAIN, 11));

        l.setForeground(new Color(148, 163, 184));

        l.setAlignmentX(Component.LEFT_ALIGNMENT);

        return l;

    }



    private JTextField adicionarCampo(JPanel p, String titulo, String valor) {

        p.add(rotulo(titulo));

        p.add(Box.createVerticalStrut(5));

        JTextField t = campo(valor, 470);

        t.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));

        p.add(t);

        return t;

    }



    private JPanel colunaCampo(String titulo, int largura) {

        JPanel p = new JPanel();

        p.setOpaque(false);

        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        p.setPreferredSize(new Dimension(largura, 64));

        p.setMaximumSize(new Dimension(largura, 64));

        p.add(rotulo(titulo));

        p.add(Box.createVerticalStrut(5));

        return p;

    }



    private JTextField campo(String valor, int largura) {

        JTextField t = new JTextField(valor) {

            private static final long serialVersionUID = 1L;

            @Override protected void paintComponent(Graphics g) {

                Graphics2D g2 = (Graphics2D) g.create();

                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(Color.WHITE);

                g2.fillRoundRect(1, 1, getWidth()-3, getHeight()-3, 10, 10);

                g2.setColor(new Color(203, 213, 225));

                g2.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, 10, 10);

                g2.dispose();

                super.paintComponent(g);

            }

        };

        t.setOpaque(false);

        t.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));

        t.setFont(new Font("SansSerif", Font.PLAIN, 13));

        t.setForeground(ROXO_ESCURO);

        t.setPreferredSize(new Dimension(largura, 34));

        t.setMaximumSize(new Dimension(largura, 34));

        t.setAlignmentX(Component.LEFT_ALIGNMENT);

        return t;

    }



    private JButton botaoTurno(String texto) {

        JButton b = botao(texto, 70, 34, new Color(241, 245, 249), new Color(71, 85, 105));

        b.setFont(new Font("SansSerif", Font.BOLD, 11));

        b.addActionListener(e -> {

            turnoSelecionado = texto;

            atualizarBotoesTurno();

        });

        SwingUtilities.invokeLater(this::atualizarBotoesTurno);

        return b;

    }



    private void atualizarBotoesTurno() {

        for (JButton b : new JButton[]{btnManha, btnTarde, btnNoite}) {

            if (b == null) continue;

            boolean ativo = b.getText().equals(turnoSelecionado);

            b.setBackground(ativo ? ROXO : new Color(241, 245, 249));

            b.setForeground(ativo ? Color.WHITE : new Color(71, 85, 105));

            b.repaint();

        }

    }



    private JButton botao(String texto, int largura, int altura, Color fundo, Color frente) {

        JButton b = new JButton(texto) {

            private static final long serialVersionUID = 1L;

            @Override protected void paintComponent(Graphics g) {

                Graphics2D g2 = (Graphics2D) g.create();

                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(getBackground());

                g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);

                g2.dispose();

                super.paintComponent(g);

            }

        };

        b.setOpaque(false);

        b.setContentAreaFilled(false);

        b.setBorderPainted(false);

        b.setFocusPainted(false);

        b.setMargin(new Insets(0, 4, 0, 4));

        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        b.setBackground(fundo);

        b.setForeground(frente);

        b.setFont(new Font("SansSerif", Font.BOLD, 12));

        b.setPreferredSize(new Dimension(largura, altura));

        b.setMinimumSize(new Dimension(largura, altura));

        b.setMaximumSize(new Dimension(largura, altura));

        return b;

    }



    private static class PainelArredondado extends JPanel {

        private static final long serialVersionUID = 1L;

        private final Color fundo, borda;

        private final int raio;

        PainelArredondado(Color fundo, Color borda, int raio) {

            this.fundo = fundo;

            this.borda = borda;

            this.raio = raio;

            setOpaque(false);

        }

        @Override protected void paintComponent(Graphics g) {

            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(fundo);

            g2.fillRoundRect(1, 1, getWidth()-3, getHeight()-3, raio, raio);

            g2.setColor(borda);

            g2.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, raio, raio);

            g2.dispose();

            super.paintComponent(g);

        }

    }



    /** Lê o JTextField de início (0) ou fim (1) sem alterar o layout. */
    private LocalTime lerHorario(JPanel linha, int coluna) {
        try {
            JPanel grupo = (JPanel) linha.getComponent(coluna);
            for (Component componente : grupo.getComponents()) {
                if (componente instanceof JTextField) {
                    String texto = ((JTextField) componente).getText().trim();
                    return LocalTime.parse(texto,
                            java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
                }
            }
        } catch (DateTimeParseException | ClassCastException | IndexOutOfBoundsException ex) {
            throw new IllegalArgumentException(
                "Informe os horários no formato HH:mm, por exemplo 07:40 e 08:30.");
        }
        throw new IllegalArgumentException("Campo de horário não encontrado.");
    }

    private void salvarTurma(JButton botaoCriar) {

        if (idProfessor <= 0) {

            JOptionPane.showMessageDialog(this,

                "Abra esta tela pelo login do professor, passando seu ID.\n" +

                "Exemplo: new TelaCriarTurma(idProfessor).setVisible(true);",

                "Professor não identificado", JOptionPane.WARNING_MESSAGE);

            return;

        }



        String rf = txtRF.getText().trim();

        String email = txtEmail.getText().trim();

        String instituicao = txtInstituicao.getText().trim();

        String materia = txtDisciplina.getText().trim();

        String curso = txtCurso.getText().trim();

        String nomeTurma = txtTurma.getText().trim();

        if (rf.isEmpty() || email.isEmpty() || instituicao.isEmpty() ||

            materia.isEmpty() || curso.isEmpty() || nomeTurma.isEmpty()) {

            JOptionPane.showMessageDialog(this, "Preencha todos os campos obrigatórios.");

            return;

        }

        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {

            JOptionPane.showMessageDialog(this, "Informe um e-mail institucional válido.");

            return;

        }

        if (diasSelecionados.isEmpty()) {

            JOptionPane.showMessageDialog(this, "Selecione pelo menos um dia de aula.");

            return;

        }

        final int carga;

        final BigDecimal limite;

        try {

            carga = Integer.parseInt(txtCargaHoraria.getText().trim());

            limite = new BigDecimal(txtLimiteFaltas.getText().trim().replace(',', '.'));

            if (carga <= 0 || limite.compareTo(BigDecimal.ZERO) < 0 ||

                limite.compareTo(new BigDecimal("100")) > 0) throw new NumberFormatException();

        } catch (NumberFormatException ex) {

            JOptionPane.showMessageDialog(this,

                "Carga horária deve ser maior que zero e limite de faltas deve estar entre 0 e 100.");

            return;

        }



        // Cada bloco de 50 minutos equivale a uma aula; tempo excedente é intervalo.
        final Map<String, Integer> aulasPorDia = new LinkedHashMap<>();
        int aulasSemana = 0;
        try {
            for (String dia : diasSelecionados) {
                JPanel linhas = linhasPorDia.get(dia);
                int quantidade = 0;
                java.util.List<LocalTime[]> intervalos = new ArrayList<>();
                if (linhas != null) {
                    for (Component componente : linhas.getComponents()) {
                        if (!(componente instanceof JPanel)) continue;
                        JPanel linha = (JPanel) componente;
                        LocalTime inicio = lerHorario(linha, 0);
                        LocalTime fim = lerHorario(linha, 1);
                        int minutos = (int) java.time.Duration.between(inicio, fim).toMinutes();
                        if (minutos < CalculoCargaHoraria.MINUTOS_POR_AULA) {
                            throw new IllegalArgumentException(
                                dia + ": intervalo " + inicio + "–" + fim
                                + " precisa ter pelo menos 50 minutos e não pode terminar antes de começar.");
                        }
                        for (LocalTime[] anterior : intervalos) {
                            if (inicio.isBefore(anterior[1]) && fim.isAfter(anterior[0])) {
                                throw new IllegalArgumentException(
                                    dia + ": existem horários sobrepostos na grade.");
                            }
                        }
                        intervalos.add(new LocalTime[]{inicio, fim});
                        quantidade += CalculoCargaHoraria.aulasEmIntervalo(minutos);
                    }
                }
                if (quantidade == 0) {
                    throw new IllegalArgumentException("Adicione pelo menos uma aula de 50 minutos em " + dia + ".");
                }
                aulasPorDia.put(dia, quantidade);
                aulasSemana += quantidade;
            }
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                "Corrija os horários", JOptionPane.WARNING_MESSAGE);
            return;
        }

        final int totalAulasCurso = CalculoCargaHoraria.totalAulas(carga);
        final int limiteAulas = CalculoCargaHoraria.limiteFaltas(carga, limite);
        final int totalAulasSemana = aulasSemana;
        final int semanasEstimadas = CalculoCargaHoraria.semanasEstimadas(carga, aulasSemana);
        final java.util.List<String> dias = new ArrayList<>(diasSelecionados);

        botaoCriar.setEnabled(false);

        SwingWorker<String, Void> tarefa = new SwingWorker<String, Void>() {

            @Override protected String doInBackground() throws Exception {

                Connection conexao = ConexaoMySQL.obterConexao();

                if (conexao == null) throw new SQLException("Não foi possível conectar ao MySQL.");

                try (Connection c = conexao) {

                    c.setAutoCommit(false);

                    try {

                        // Confirma que o usuário logado é um professor real.

                        try (PreparedStatement ps = c.prepareStatement(

                            "SELECT perfil FROM usuarios WHERE idUSUARIOS = ?")) {

                            ps.setInt(1, idProfessor);

                            try (ResultSet rs = ps.executeQuery()) {

                                if (!rs.next() || !"PROFESSOR".equalsIgnoreCase(rs.getString(1))) {

                                    throw new SQLException("Usuário não encontrado ou não é professor.");

                                }

                            }

                        }



                        // codigo_acesso é UNIQUE. Repetimos caso haja colisão.

                        String codigo = null;

                        int idTurma = -1;

                        String sql = "INSERT INTO turma " +

                            "(professor_id, registro_funcional, email_institucional, " +

                            "instituicao, nome_curso, nome_turma, nome_materia, " +

                            "carga_horaria, codigo_acesso, periodo, limites_faltas, usuarios_idUSUARIOS) " +

                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

                        for (int tentativa = 0; tentativa < 15; tentativa++) {

                            codigo = String.format("%06d", aleatorio.nextInt(1000000));

                            try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                                ps.setInt(1, idProfessor);

                                ps.setString(2, rf);

                                ps.setString(3, email);

                                ps.setString(4, instituicao);

                                ps.setString(5, curso);

                                ps.setString(6, nomeTurma);

                                ps.setString(7, materia);

                                ps.setInt(8, carga);

                                ps.setString(9, codigo);

                                ps.setString(10, turnoSelecionado);

                                ps.setBigDecimal(11, limite);

                                ps.setInt(12, idProfessor);

                                try {

                                    ps.executeUpdate();

                                } catch (SQLException ex) {

                                    // Apenas repetição do código; outras restrições são erros reais.

                                    if (ex.getErrorCode() == 1062 &&

                                        ex.getMessage() != null &&

                                        ex.getMessage().toLowerCase().contains("codigo_acesso")) continue;

                                    throw ex;

                                }

                                try (ResultSet chaves = ps.getGeneratedKeys()) {

                                    if (!chaves.next()) throw new SQLException("ID da turma não retornado.");

                                    idTurma = chaves.getInt(1);

                                }

                            }

                            break;

                        }

                        if (idTurma <= 0) throw new SQLException("Não foi possível gerar um código único.");



                        String sqlGrade = "INSERT INTO grade (turma_id, dia_semana, peso_aula, turma_idTURMA) " +

                            "VALUES (?, ?, ?, ?)";

                        try (PreparedStatement ps = c.prepareStatement(sqlGrade)) {

                            for (String dia : dias) {

                                ps.setInt(1, idTurma);

                                ps.setString(2, dia);

                                ps.setInt(3, aulasPorDia.get(dia));

                                ps.setInt(4, idTurma);

                                ps.addBatch();

                            }

                            ps.executeBatch();

                        }

                        c.commit();

                        return codigo;

                    } catch (Exception ex) {

                        c.rollback();

                        throw ex;

                    }

                }

            }

            @Override protected void done() {

                botaoCriar.setEnabled(true);

                try {

                    String codigo = get();

                    JOptionPane.showMessageDialog(TelaCriarTurma.this,

                        "Turma cadastrada no MySQL!\nCódigo de acesso: " + codigo +

                        "\nCarga horária: " + carga + " horas (60 min)" +
                        "\nTotal equivalente: " + totalAulasCurso + " aulas de 50 min" +
                        "\nAulas por semana: " + totalAulasSemana +
                        "\nSemanas letivas estimadas: " + semanasEstimadas +
                        "\nLimite permitido: " + limiteAulas + " faltas (" + limite + "%)" +
                        "\n\nO MySQL guarda as horas totais e a quantidade de aulas por dia." +
                        "\nOs horários exatos ainda não ficam salvos no banco.",

                        "Turma criada", JOptionPane.INFORMATION_MESSAGE);

                    if (aoCriar != null) aoCriar.run();

                    dispose();

                } catch (Exception ex) {

                    Throwable causa = ex.getCause() != null ? ex.getCause() : ex;

                    String mensagem = causa.getMessage();

                    if (mensagem != null && mensagem.toLowerCase().contains("duplicate") &&

                        mensagem.toLowerCase().contains("email_institucional")) {

                        mensagem = "O banco exige e-mail institucional único por turma. " +

                            "Esse e-mail já foi utilizado. A restrição precisa ser revista " +

                            "para permitir várias turmas do mesmo professor.";

                    }

                    JOptionPane.showMessageDialog(TelaCriarTurma.this,

                        "Não foi possível salvar a turma:\n" + mensagem,

                        "Erro ao cadastrar", JOptionPane.ERROR_MESSAGE);

                }

            }

        };

        tarefa.execute();

    }

}


