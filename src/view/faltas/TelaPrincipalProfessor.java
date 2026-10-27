package view.faltas;

import java.awt.BorderLayout;

import java.awt.Color;

import java.awt.Component;

import java.awt.Cursor;

import java.awt.Dimension;

import java.awt.EventQueue;

import java.awt.FlowLayout;

import java.awt.Font;

import java.awt.Graphics;

import java.awt.Graphics2D;

import java.awt.GridBagLayout;

import java.awt.GridLayout;

import java.awt.RenderingHints;

import java.awt.datatransfer.StringSelection;

import java.awt.Toolkit;

import java.awt.event.MouseAdapter;

import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;

import javax.swing.Box;

import javax.swing.BoxLayout;

import javax.swing.JButton;

import javax.swing.JFrame;

import javax.swing.JLabel;

import javax.swing.JPanel;

import javax.swing.JScrollPane;

import javax.swing.SwingConstants;

import javax.swing.UIManager;

import javax.swing.JOptionPane;

import java.sql.*;

import conexao.ConexaoMySQL;

public class TelaPrincipalProfessor extends JFrame {

	private static final long serialVersionUID = 1L;

	private JPanel contentPane;

    private final int idProfessorLogado;

    private final String nomeProfessorLogado;

    private JLabel lblNomeUser;

    private JLabel lblAvatar;

    private JPanel painelFiltros, painelGrid;

    private JLabel lblStatus;

    private String filtroAtual = "Todas";

    private final java.util.List<String[]> turmas = new java.util.ArrayList<>();

	public static void main(String[] args) {

		try {

			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

		} catch (Exception e) {

			e.printStackTrace();

		}

		EventQueue.invokeLater(new Runnable() {

			public void run() {

				try {

					TelaLogin frame = new TelaLogin();

					frame.setVisible(true);

				} catch (Exception e) {

					e.printStackTrace();

				}

			}

		});

	}

	// Pílula totalmente arredondada

	private static class PilulaArredondada extends JPanel {

		private static final long serialVersionUID = 1L;

		private Color corFundo;

		private Color corBorda;

		public PilulaArredondada(Color corFundo, Color corBorda) {

			this.corFundo = corFundo;

			this.corBorda = corBorda;

			setOpaque(false);

		}

		@Override

		protected void paintComponent(Graphics g) {

			Graphics2D g2 = (Graphics2D) g.create();

			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			g2.setColor(corFundo);

			g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, getHeight(), getHeight());

			if (corBorda != null) {

				g2.setColor(corBorda);

				g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, getHeight(), getHeight());

			}

			g2.dispose();

			super.paintComponent(g);

		}

	}

	// Card de Turma do Professor padronizado exatamente com o tamanho do card do aluno

	private static class CardTurmaProfessorArredondado extends JPanel {

		private static final long serialVersionUID = 1L;

		public CardTurmaProfessorArredondado() {

			setOpaque(false);

			setPreferredSize(new Dimension(280, 185));

			setMinimumSize(new Dimension(280, 185));

			setMaximumSize(new Dimension(280, 185));

		}

		@Override

		protected void paintComponent(Graphics g) {

			Graphics2D g2 = (Graphics2D) g.create();

			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			g2.setColor(new Color(238, 242, 255));

			g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 24, 24);

			g2.setColor(new Color(199, 210, 254));

			g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 24, 24);

			g2.setColor(new Color(129, 140, 248));

			g2.fillRoundRect(2, 2, getWidth() - 5, 5, 5, 5);

			g2.dispose();

			super.paintComponent(g);

		}

	}

	// Card Final para Criar Nova Turma com o mesmo tamanho padronizado

	private static class CardCriarTurmaArredondado extends JPanel {

		private static final long serialVersionUID = 1L;

		public CardCriarTurmaArredondado() {

			setOpaque(false);

			setCursor(new Cursor(Cursor.HAND_CURSOR));

			setPreferredSize(new Dimension(280, 185));

			setMinimumSize(new Dimension(280, 185));

			setMaximumSize(new Dimension(280, 185));

		}

		@Override

		protected void paintComponent(Graphics g) {

			Graphics2D g2 = (Graphics2D) g.create();

			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			g2.setColor(new Color(245, 247, 250));

			g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 24, 24);

			g2.setColor(new Color(199, 210, 254));

			float[] dashPattern = { 6f, 6f };

			g2.setStroke(new java.awt.BasicStroke(1.5f, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND, 10f, dashPattern, 0f));

			g2.drawRoundRect(2, 2, getWidth() - 5, getHeight() - 5, 22, 22);

			g2.dispose();

			super.paintComponent(g);

		}

	}

	// Bolinha perfeita com ícone de mais (+)

	private static class BolinhaMaisIcone extends JPanel {

		private static final long serialVersionUID = 1L;

		public BolinhaMaisIcone() {

			setOpaque(false);

			setPreferredSize(new Dimension(34, 34));

			setMinimumSize(new Dimension(34, 34));

			setMaximumSize(new Dimension(34, 34));

		}

		@Override

		protected void paintComponent(Graphics g) {

			Graphics2D g2 = (Graphics2D) g.create();

			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			g2.setColor(new Color(224, 231, 255));

			g2.fillOval(0, 0, getWidth(), getHeight());

			g2.setColor(new Color(79, 70, 229));

			g2.setStroke(new java.awt.BasicStroke(2.2f, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));

			int cx = getWidth() / 2;

			int cy = getHeight() / 2;

			g2.drawLine(cx - 5, cy, cx + 5, cy);

			g2.drawLine(cx, cy - 5, cx, cy + 5);

			g2.dispose();

		}

	}

	public TelaPrincipalProfessor() {

        this(-1, "Professor");

    }

    public TelaPrincipalProfessor(int idProfessor) {

        this(idProfessor, buscarNomeProfessor(idProfessor));

    }

    public TelaPrincipalProfessor(int idProfessor, String nomeProfessor) {

        this.idProfessorLogado = idProfessor;

        this.nomeProfessorLogado = nomeProfessor == null || nomeProfessor.trim().isEmpty() ? "Professor" : nomeProfessor.trim();

		setTitle("Presente! - Painel do Professor");

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		setExtendedState(JFrame.MAXIMIZED_BOTH);

		setMinimumSize(new Dimension(1000, 700));

		setLocationRelativeTo(null);

		contentPane = new JPanel(new BorderLayout());

		contentPane.setBackground(new Color(248, 250, 252));

		setContentPane(contentPane);

		// 1. BARRA SUPERIOR

		JPanel topBar = new JPanel(new BorderLayout());

		topBar.setBackground(new Color(30, 27, 75));

		topBar.setBorder(BorderFactory.createEmptyBorder(16, 35, 16, 35));

		JLabel lblLogo = new JLabel("✔ Presente!");

		lblLogo.setFont(new Font("SansSerif", Font.BOLD, 22));

		lblLogo.setForeground(Color.WHITE);

		topBar.add(lblLogo, BorderLayout.WEST);

		JPanel painelUsuario = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));

		painelUsuario.setOpaque(false);

		lblNomeUser = new JLabel("<html><div style='text-align: right;'><font color='white'><b>" + escaparHtml(nomeProfessorLogado) + "</b></font><br><font size='2' color='#C7D2FE'>Professor</font></div></html>");

		lblNomeUser.setFont(new Font("SansSerif", Font.PLAIN, 13));

		painelUsuario.add(lblNomeUser);

		lblAvatar = new JLabel(iniciais(nomeProfessorLogado), SwingConstants.CENTER) {

			private static final long serialVersionUID = 1L;

			@Override

			protected void paintComponent(Graphics g) {

				Graphics2D g2 = (Graphics2D) g.create();

				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

				g2.setColor(new Color(129, 140, 248));

				g2.fillOval(0, 0, getWidth(), getHeight());

				g2.dispose();

				super.paintComponent(g);

			}

		};

		lblAvatar.setPreferredSize(new Dimension(38, 38));

		lblAvatar.setForeground(new Color(30, 27, 75));

		lblAvatar.setFont(new Font("SansSerif", Font.BOLD, 14));

		painelUsuario.add(lblAvatar);

		topBar.add(painelUsuario, BorderLayout.EAST);

		contentPane.add(topBar, BorderLayout.NORTH);

		// 2. CONTEÚDO COM SCROLL

		JPanel painelConteudoPrincipal = new JPanel(new BorderLayout());

		painelConteudoPrincipal.setBackground(new Color(248, 250, 252));

		JPanel painelConteudo = new JPanel();

		painelConteudo.setBackground(new Color(248, 250, 252));

		painelConteudo.setLayout(new BoxLayout(painelConteudo, BoxLayout.Y_AXIS));

		painelConteudo.setBorder(BorderFactory.createEmptyBorder(30, 50, 40, 50));

		// Títulos e Botão Superior centralizados harmonicamente

		JPanel headerConteudo = new JPanel(new BorderLayout());

		headerConteudo.setOpaque(false);

		headerConteudo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));

		JPanel painelTitulos = new JPanel();

		painelTitulos.setOpaque(false);

		painelTitulos.setLayout(new BoxLayout(painelTitulos, BoxLayout.Y_AXIS));

		JLabel lblSaudacao = new JLabel("Minhas turmas");

		lblSaudacao.setFont(new Font("SansSerif", Font.BOLD, 28));

		lblSaudacao.setForeground(new Color(30, 27, 75));

		painelTitulos.add(lblSaudacao);

		painelTitulos.add(Box.createVerticalStrut(4));

		lblStatus = new JLabel("Escolha uma turma para fazer a chamada.");

		lblStatus.setFont(new Font("SansSerif", Font.PLAIN, 14));

		lblStatus.setForeground(new Color(100, 116, 139));

		painelTitulos.add(lblStatus);

		headerConteudo.add(painelTitulos, BorderLayout.WEST);

		JButton btnCriarTurma = new JButton("+ Criar nova turma") {

			private static final long serialVersionUID = 1L;

			@Override

			protected void paintComponent(Graphics g) {

				Graphics2D g2 = (Graphics2D) g.create();

				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

				g2.setColor(getBackground());

				g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);

				super.paintComponent(g);

				g2.dispose();

			}

		};

		btnCriarTurma.setOpaque(false);

		btnCriarTurma.setContentAreaFilled(false);

		btnCriarTurma.setBorderPainted(false);

		btnCriarTurma.setFont(new Font("SansSerif", Font.BOLD, 13));

		btnCriarTurma.setBackground(new Color(79, 70, 229));

		btnCriarTurma.setForeground(Color.WHITE);

		btnCriarTurma.setFocusPainted(false);

		btnCriarTurma.setCursor(new Cursor(Cursor.HAND_CURSOR));

		btnCriarTurma.setPreferredSize(new Dimension(190, 44));

		btnCriarTurma.addMouseListener(new MouseAdapter() {

			public void mouseEntered(MouseEvent e) {

				btnCriarTurma.setBackground(new Color(67, 56, 202));

				btnCriarTurma.repaint();

			}

			public void mouseExited(MouseEvent e) {

				btnCriarTurma.setBackground(new Color(79, 70, 229));

				btnCriarTurma.repaint();

			}

		});

		JPanel painelBotaoTop = new JPanel(new GridBagLayout());

		painelBotaoTop.setOpaque(false);

		painelBotaoTop.add(btnCriarTurma);

		headerConteudo.add(painelBotaoTop, BorderLayout.EAST);

        btnCriarTurma.addActionListener(e -> abrirCriarTurma());

		painelConteudo.add(headerConteudo);

		painelConteudo.add(Box.createVerticalStrut(22));

		// Filtros

		painelFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));

		painelFiltros.setOpaque(false);

		painelFiltros.add(criarBotaoFiltro("Todas", true));

		painelFiltros.add(criarBotaoFiltro("Faculdade Horizonte", false));

		painelFiltros.add(criarBotaoFiltro("Instituto Nova Era", false));

		painelFiltros.add(criarBotaoFiltro("Univ. Alfa", false));

		painelConteudo.add(painelFiltros);

		painelConteudo.add(Box.createVerticalStrut(22));

		// Grelha de Cards de Turmas (4 colunas por fileira)

		painelGrid = new JPanel(new GridLayout(0, 4, 24, 24));

		painelGrid.setOpaque(false);

        carregarTurmasDoBanco();

		JPanel painelGridWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));

		painelGridWrapper.setOpaque(false);

		painelGridWrapper.add(painelGrid);

		painelConteudo.add(painelGridWrapper);

		JScrollPane scrollPane = new JScrollPane(painelConteudo);

		scrollPane.setBorder(null);

		scrollPane.getVerticalScrollBar().setUnitIncrement(16);

		painelConteudoPrincipal.add(scrollPane, BorderLayout.CENTER);

		contentPane.add(painelConteudoPrincipal, BorderLayout.CENTER);

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

    private static String escaparHtml(String valor) {

        return valor.replace("&", "&").replace("<", "<").replace(">", ">");

    }

    private static String iniciais(String nome) {

        String[] partes = nome.trim().split("\\\s+");

        if (partes.length == 0 || partes[0].isEmpty()) return "P";

        String a = partes[0].substring(0, 1);

        String b = partes.length > 1 ? partes[partes.length - 1].substring(0, 1) : "";

        return (a + b).toUpperCase();

    }

    private void carregarTurmasDoBanco() {

        turmas.clear();

        if (idProfessorLogado <= 0) { atualizarFiltros(); atualizarGrade(); return; }

        String sql = "SELECT t.instituicao, t.nome_materia, t.nome_curso, t.nome_turma, " +

            "t.periodo, t.codigo_acesso, " +

            "(SELECT COUNT(*) FROM matricula m WHERE m.turma_idTURMA = t.idTURMA) AS qtd, " +

            "(SELECT GROUP_CONCAT(CONCAT(g.dia_semana, ' · Total: ', g.peso_aula, ' aulas') SEPARATOR ', ') " +

            " FROM grade g WHERE g.turma_idTURMA = t.idTURMA) AS dias " +

            "FROM turma t WHERE t.professor_id = ? ORDER BY t.idTURMA DESC";

        try (Connection c = ConexaoMySQL.obterConexao();

             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, idProfessorLogado);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    String nomeTurma = rs.getString("nome_curso") + " " + rs.getString("nome_turma") +

                        " · " + rs.getString("periodo");

                    String dias = rs.getString("dias");

                    if (dias == null || dias.trim().isEmpty()) dias = "Grade não informada";

                    turmas.add(new String[] { rs.getString("instituicao"), rs.getString("nome_materia"),

                        nomeTurma, rs.getInt("qtd") + " alunos", dias, rs.getString("codigo_acesso") });

                }

            }

        } catch (SQLException ex) {

            JOptionPane.showMessageDialog(this, "Erro ao carregar turmas: " + ex.getMessage(),

                "MySQL", JOptionPane.ERROR_MESSAGE);

        }

        atualizarFiltros();

        atualizarGrade();

    }

    private void atualizarFiltros() {

        painelFiltros.removeAll();

        painelFiltros.add(criarBotaoFiltro("Todas", true));

        java.util.Set<String> instituicoes = new java.util.LinkedHashSet<>();

        for (String[] t : turmas) instituicoes.add(t[0]);

        for (String inst : instituicoes) painelFiltros.add(criarBotaoFiltro(inst, false));

        filtroAtual = "Todas";

        painelFiltros.revalidate();

        painelFiltros.repaint();

    }

private JButton criarBotaoFiltro(String texto, boolean selecionado) {

		JButton btn = new JButton(texto) {

			private static final long serialVersionUID = 1L;

			@Override

			protected void paintComponent(Graphics g) {

				Graphics2D g2 = (Graphics2D) g.create();

				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

				g2.setColor(getBackground());

				g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);

				super.paintComponent(g2);

				g2.dispose();

			}

		};

		btn.setOpaque(false);

		btn.setContentAreaFilled(false);

		btn.setBorderPainted(false);

		btn.setFont(new Font("SansSerif", Font.BOLD, 12));

		btn.setFocusPainted(false);

		btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

		if (selecionado) {

			btn.setBackground(new Color(79, 70, 229));

			btn.setForeground(Color.WHITE);

			btn.setPreferredSize(new Dimension(80, 32));

		} else {

			btn.setBackground(Color.WHITE);

			btn.setForeground(new Color(71, 85, 105));

			btn.setPreferredSize(new Dimension(140, 32));

		}

        btn.addActionListener(e -> {

            filtroAtual = texto;

            for (Component c : painelFiltros.getComponents()) {

                if (c instanceof JButton) {

                    JButton b = (JButton) c;

                    boolean ativo = b.getText().equalsIgnoreCase(filtroAtual);

                    b.setBackground(ativo ? new Color(79,70,229) : Color.WHITE);

                    b.setForeground(ativo ? Color.WHITE : new Color(71,85,105));

                    b.repaint();

                }

            }

            atualizarGrade();

        });

        return btn;

    }

    private void atualizarGrade() {

        painelGrid.removeAll();

        int visiveis = 0;

        for (String[] t : turmas) {

            if (!filtroAtual.equalsIgnoreCase("Todas") && !t[0].equalsIgnoreCase(filtroAtual)) continue;

            painelGrid.add(montarCardTurma(t[0], t[1], t[2], t[3], t[4], t[5]));

            visiveis++;

        }

        CardCriarTurmaArredondado novo = new CardCriarTurmaArredondado();

        novo.setLayout(new GridBagLayout());

        JPanel miolo = new JPanel();

        miolo.setOpaque(false);

        miolo.setLayout(new BoxLayout(miolo, BoxLayout.Y_AXIS));

        BolinhaMaisIcone mais = new BolinhaMaisIcone();

        mais.setAlignmentX(Component.CENTER_ALIGNMENT);

        miolo.add(mais);

        JLabel titulo = new JLabel("Criar nova turma");

        titulo.setFont(new Font("SansSerif", Font.BOLD, 12));

        titulo.setForeground(new Color(30,27,75));

        titulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        miolo.add(Box.createVerticalStrut(8));

        miolo.add(titulo);

        JLabel subtitulo = new JLabel("O código de 6 dígitos é gerado na hora");

        subtitulo.setFont(new Font("SansSerif", Font.PLAIN, 10));

        subtitulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        miolo.add(Box.createVerticalStrut(3));

        miolo.add(subtitulo);

        novo.add(miolo);

        MouseAdapter clique = new MouseAdapter() {

            @Override public void mouseClicked(MouseEvent e) { abrirCriarTurma(); }

        };

        adicionarClique(novo, clique);

        painelGrid.add(novo);

        lblStatus.setText(visiveis + " turma(s) neste filtro. Escolha uma para fazer a chamada.");

        painelGrid.revalidate();

        painelGrid.repaint();

    }

    private void adicionarClique(Component c, MouseAdapter evento) {

        c.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        c.addMouseListener(evento);

        if (c instanceof java.awt.Container) {

            for (Component filho : ((java.awt.Container)c).getComponents()) adicionarClique(filho, evento);

        }

    }

    private void abrirCriarTurma() {

        if (idProfessorLogado <= 0) {

            JOptionPane.showMessageDialog(this, "Abra o painel pelo login para identificar o professor.");

            return;

        }

        TelaCriarTurma tela = new TelaCriarTurma(idProfessorLogado, this::carregarTurmasDoBanco);

        tela.setVisible(true);

        // A lista deverá ser recarregada do banco após salvar uma turma.

    }

    private void abrirChamada(String materia, String turma, String codigo, String aulas) {

        int total = 1;

        java.util.regex.Matcher m = java.util.regex.Pattern.compile("Total: (\\d+)").matcher(aulas);

        if (m.find()) total = Integer.parseInt(m.group(1));

        new TelaChamada(materia, turma, codigo, total).setVisible(true);

    }

    private JPanel montarCardTurma(String inst, String materia, String turmaAlunos, String qtdAlunosTxt, String infoAulas, String codigoTurma) {

		CardTurmaProfessorArredondado card = new CardTurmaProfessorArredondado();

		card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

		card.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

		// Instituição

		JLabel lblInst = new JLabel("<html><font color='#4F46E5'>●</font>  <font color='#1E1B4B'><b>" + inst.toUpperCase() + "</b></font></html>");

		lblInst.setFont(new Font("SansSerif", Font.BOLD, 10));

		lblInst.setAlignmentX(Component.LEFT_ALIGNMENT);

		// Nome da Matéria

		JLabel lblMateria = new JLabel(materia);

		lblMateria.setFont(new Font("SansSerif", Font.BOLD, 15));

		lblMateria.setForeground(new Color(30, 27, 75));

		lblMateria.setAlignmentX(Component.LEFT_ALIGNMENT);

		// Pílulas (Turma e Qtd Alunos)

		JPanel painelPilulas = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));

		painelPilulas.setOpaque(false);

		painelPilulas.setAlignmentX(Component.LEFT_ALIGNMENT);

		PilulaArredondada pilulaTurma = new PilulaArredondada(new Color(224, 231, 255), null);

		pilulaTurma.setLayout(new FlowLayout(FlowLayout.CENTER, 6, 2));

		JLabel lblTurmaText = new JLabel(turmaAlunos);

		lblTurmaText.setFont(new Font("SansSerif", Font.BOLD, 10));

		lblTurmaText.setForeground(new Color(30, 27, 75));

		pilulaTurma.add(lblTurmaText);

		PilulaArredondada pilulaAlunos = new PilulaArredondada(new Color(224, 231, 255), null);

		pilulaAlunos.setLayout(new FlowLayout(FlowLayout.CENTER, 6, 2));

		JLabel lblAlunosText = new JLabel(qtdAlunosTxt);

		lblAlunosText.setFont(new Font("SansSerif", Font.PLAIN, 10));

		lblAlunosText.setForeground(new Color(30, 27, 75));

		pilulaAlunos.add(lblAlunosText);

		painelPilulas.add(pilulaTurma);

		painelPilulas.add(pilulaAlunos);

		// Informações de Aulas e Dias da Semana

		JLabel lblAulas = new JLabel("<html>📅  <font color='#475569'>" + infoAulas + "</font></html>");

		lblAulas.setFont(new Font("SansSerif", Font.PLAIN, 11));

		lblAulas.setAlignmentX(Component.LEFT_ALIGNMENT);

		// Caixa de Código da Turma com botão de copiar funcional

		JPanel painelCodigo = new JPanel(new BorderLayout());

		painelCodigo.setBackground(new Color(245, 247, 250));

		painelCodigo.setBorder(BorderFactory.createCompoundBorder(

			BorderFactory.createLineBorder(new Color(199, 210, 254), 1),

			BorderFactory.createEmptyBorder(3, 8, 3, 6)

		));

		painelCodigo.setAlignmentX(Component.LEFT_ALIGNMENT);

		painelCodigo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));

		JLabel lblCodLabel = new JLabel("<html><font size='1' color='#64748B'>CÓDIGO DA TURMA</font><br><b><font size='3' color='#1E1B4B'>" + codigoTurma + "</font></b></html>");

		JButton btnCopiar = new JButton("📋") {

			private static final long serialVersionUID = 1L;

			@Override

			protected void paintComponent(Graphics g) {

				Graphics2D g2 = (Graphics2D) g.create();

				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

				g2.setColor(getBackground());

				g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);

				super.paintComponent(g2);

				g2.dispose();

			}

		};

		btnCopiar.setOpaque(false);

		btnCopiar.setContentAreaFilled(false);

		btnCopiar.setBorderPainted(false);

		btnCopiar.setFocusPainted(false);

		btnCopiar.setBackground(new Color(224, 231, 255));

		btnCopiar.setCursor(new Cursor(Cursor.HAND_CURSOR));

		btnCopiar.setPreferredSize(new Dimension(30, 26));

		// Ação funcional para copiar o código da turma para a área de transferência

		btnCopiar.addActionListener(e -> {

			StringSelection stringSelection = new StringSelection(codigoTurma);

            try {

                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, null);

                JOptionPane.showMessageDialog(this, "Código copiado: " + codigoTurma);

            } catch (IllegalStateException ex) {

                JOptionPane.showMessageDialog(this, "Não foi possível acessar a área de transferência.");

            }

		});

        MouseAdapter copiarAoClicar = new MouseAdapter() {

            @Override public void mouseClicked(MouseEvent e) { btnCopiar.doClick(); }

        };

        lblCodLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        lblCodLabel.addMouseListener(copiarAoClicar);

        painelCodigo.add(lblCodLabel, BorderLayout.WEST);

		painelCodigo.add(btnCopiar, BorderLayout.EAST);

		// Botão Fazer Chamada

		JButton btnFazerChamada = new JButton("Fazer chamada") {

			private static final long serialVersionUID = 1L;

			@Override

			protected void paintComponent(Graphics g) {

				Graphics2D g2 = (Graphics2D) g.create();

				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

				g2.setColor(getBackground());

				g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);

				super.paintComponent(g);

				g2.dispose();

			}

		};

		btnFazerChamada.setOpaque(false);

		btnFazerChamada.setContentAreaFilled(false);

		btnFazerChamada.setBorderPainted(false);

		btnFazerChamada.setFont(new Font("SansSerif", Font.BOLD, 12));

		btnFazerChamada.setBackground(new Color(79, 70, 229));

		btnFazerChamada.setForeground(Color.WHITE);

		btnFazerChamada.setFocusPainted(false);

		btnFazerChamada.setCursor(new Cursor(Cursor.HAND_CURSOR));

		btnFazerChamada.setAlignmentX(Component.LEFT_ALIGNMENT);

		btnFazerChamada.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));

		btnFazerChamada.setPreferredSize(new Dimension(0, 32));

		btnFazerChamada.addMouseListener(new MouseAdapter() {

			public void mouseEntered(MouseEvent e) {

				btnFazerChamada.setBackground(new Color(67, 56, 202));

				btnFazerChamada.repaint();

			}

			public void mouseExited(MouseEvent e) {

				btnFazerChamada.setBackground(new Color(79, 70, 229));

				btnFazerChamada.repaint();

			}

		});

        btnFazerChamada.addActionListener(e -> abrirChamada(materia, turmaAlunos, codigoTurma, infoAulas));

        // Clique no card abre chamada, mas controles internos têm ações próprias.

        MouseAdapter abrir = new MouseAdapter() {

            @Override public void mouseClicked(MouseEvent e) {

                abrirChamada(materia, turmaAlunos, codigoTurma, infoAulas);

            }

        };

        card.addMouseListener(abrir);

        lblInst.addMouseListener(abrir);

        lblMateria.addMouseListener(abrir);

        lblAulas.addMouseListener(abrir);

        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        card.add(lblInst);

		card.add(Box.createVerticalStrut(2));

		card.add(lblMateria);

		card.add(Box.createVerticalStrut(4));

		card.add(painelPilulas);

		card.add(Box.createVerticalStrut(6));

		card.add(lblAulas);

		card.add(Box.createVerticalStrut(6));

		card.add(painelCodigo);

		card.add(Box.createVerticalStrut(8));

		card.add(btnFazerChamada);

		return card;

	}

}