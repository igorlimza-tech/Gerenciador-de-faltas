
package view.faltas;

import dao.faltas.MatriculaDAO;
import dao.faltas.TurmaDAO;
import modelo.faltas.Matricula;
import modelo.faltas.Turma;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;

public class TelaMatricula extends JFrame {

    private static final long serialVersionUID = 1L;

    // CORES DO PRESENTE!
    private static final Color ROXO = new Color(79, 70, 229);
    private static final Color ROXO_HOVER = new Color(67, 56, 202);
    private static final Color MARINHO = new Color(30, 27, 75);
    private static final Color FUNDO = new Color(245, 247, 250);
    private static final Color NEUTRO = new Color(241, 245, 249);
    private static final Color BORDA = new Color(203, 213, 225);
    private static final Color VERDE_CLARO = new Color(220, 252, 231);

    private JTextField txtNome;
    private JTextField txtRa;
    private JTextField txtEmail;
    private JTextField txtCurso;

    private final JTextField[] codigo = new JTextField[6];

    private JButton btnEntrar;
    private JButton btnManha;
    private JButton btnTarde;
    private JButton btnNoite;

    private JPanel confirmacao;

    private JLabel lblInstituicao;
    private JLabel lblMateria;
    private JLabel lblDetalhes;

    private Turma turmaEncontrada;

    private int idAlunoLogado = -1;
    private String turno = "Manhã";
    private Runnable aoMatricular;

    private int sequenciaBusca = 0;
    private boolean preenchendoCodigo = false;
    private boolean salvando = false;

    private final TurmaDAO turmaDAO = new TurmaDAO();
    private final MatriculaDAO matriculaDAO = new MatriculaDAO();

    // CONSTRUTORES

    public TelaMatricula(int idAlunoLogado) {
        this();
        this.idAlunoLogado = idAlunoLogado;
    }

    public TelaMatricula(int idAlunoLogado, Runnable aoMatricular) {
        this(idAlunoLogado);
        this.aoMatricular = aoMatricular;
    }

    public TelaMatricula() {

        setTitle("Presente! - Entrar em nova turma");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        setSize(760, 790);
        setMinimumSize(new Dimension(640, 710));
        setLocationRelativeTo(null);

        JPanel fundo = new JPanel(new GridBagLayout());
        fundo.setBackground(FUNDO);
        setContentPane(fundo);

        JPanel card = new JPanel();
        card.setBackground(Color.WHITE);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240)),
            BorderFactory.createEmptyBorder(24, 26, 24, 26)
        ));

        card.setPreferredSize(new Dimension(510, 680));
        fundo.add(card);

        // CABEÇALHO

        JPanel cabecalho = new JPanel(new BorderLayout());
        cabecalho.setOpaque(false);

        JLabel titulo = new JLabel("Entrar em nova turma");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 23));
        titulo.setForeground(MARINHO);

        JButton fechar = criarBotao("X", false);
        fechar.setPreferredSize(new Dimension(42, 36));
        fechar.addActionListener(e -> dispose());

        cabecalho.add(titulo, BorderLayout.CENTER);
        cabecalho.add(fechar, BorderLayout.EAST);

        adicionar(card, cabecalho, 45);

        JLabel instrucao = new JLabel(
            "Confirme seus dados e digite o código enviado pelo professor."
        );

        instrucao.setFont(new Font("SansSerif", Font.PLAIN, 12));
        instrucao.setForeground(new Color(100, 116, 139));

        adicionar(card, instrucao, 30);

        adicionar(card, secao("1 · SEUS DADOS NA INSTITUIÇÃO"), 30);

        // NOME E RA

        JPanel nomeRa = new JPanel(new GridLayout(1, 2, 12, 0));
        nomeRa.setOpaque(false);

        txtNome = criarCampo();
        txtRa = criarCampo();

        nomeRa.add(grupo("Nome completo", txtNome));
        nomeRa.add(grupo("RA", txtRa));

        adicionar(card, nomeRa, 68);

        // E-MAIL

        txtEmail = criarCampo();

        adicionar(
            card,
            grupo("E-mail institucional", txtEmail),
            68
        );

        // CURSO E TURNO

        txtCurso = criarCampo();

        JPanel cursoTurno = new JPanel(new GridLayout(1, 2, 12, 0));
        cursoTurno.setOpaque(false);

        cursoTurno.add(grupo("Curso e turma", txtCurso));

        JPanel painelTurnos = new JPanel(new BorderLayout(0, 6));
        painelTurnos.setOpaque(false);

        painelTurnos.add(rotulo("Turno"), BorderLayout.NORTH);

        JPanel botoesTurno = new JPanel(new GridLayout(1, 3, 7, 0));
        botoesTurno.setOpaque(false);

        btnManha = criarBotaoTurno("Manhã");
        btnTarde = criarBotaoTurno("Tarde");
        btnNoite = criarBotaoTurno("Noite");

        botoesTurno.add(btnManha);
        botoesTurno.add(btnTarde);
        botoesTurno.add(btnNoite);

        painelTurnos.add(botoesTurno, BorderLayout.CENTER);
        cursoTurno.add(painelTurnos);

        adicionar(card, cursoTurno, 68);

        JSeparator separador = new JSeparator();
        separador.setForeground(BORDA);

        adicionar(card, separador, 20);

        adicionar(card, secao("2 · CÓDIGO DA TURMA"), 30);

        // SEIS CAMPOS DO CÓDIGO

        JPanel painelCodigo = new JPanel(new GridLayout(1, 6, 8, 0));
        painelCodigo.setOpaque(false);

        for (int i = 0; i < 6; i++) {

            final int indice = i;

            JTextField campo = criarCampo();
            campo.setHorizontalAlignment(JTextField.CENTER);
            campo.setFont(new Font("SansSerif", Font.BOLD, 21));

            ((AbstractDocument) campo.getDocument()).setDocumentFilter(
                new DocumentFilter() {

                    @Override
                    public void replace(
                        FilterBypass fb,
                        int offset,
                        int length,
                        String texto,
                        AttributeSet attrs
                    ) throws BadLocationException {

                        String novo = texto == null
                            ? ""
                            : texto.toUpperCase()
                                   .replaceAll("[^A-Z0-9]", "");

                        if (novo.length() > 1 && !preenchendoCodigo) {

                            final String colado = novo;

                            SwingUtilities.invokeLater(
                                () -> preencherCodigo(colado)
                            );

                            return;
                        }

                        if (fb.getDocument().getLength()
                                - length + novo.length() <= 1) {

                            super.replace(
                                fb, offset, length, novo, attrs
                            );
                        }
                    }

                    @Override
                    public void insertString(
                        FilterBypass fb,
                        int offset,
                        String texto,
                        AttributeSet attrs
                    ) throws BadLocationException {

                        replace(fb, offset, 0, texto, attrs);
                    }
                }
            );

            campo.getDocument().addDocumentListener(
                new DocumentListener() {

                    @Override
                    public void insertUpdate(DocumentEvent e) {

                        SwingUtilities.invokeLater(() -> {

                            if (preenchendoCodigo) return;

                            if (!campo.getText().isEmpty()
                                    && indice < 5) {

                                codigo[indice + 1]
                                    .requestFocusInWindow();
                            }

                            verificarCodigo();
                        });
                    }

                    @Override
                    public void removeUpdate(DocumentEvent e) {

                        SwingUtilities.invokeLater(() -> {

                            if (!preenchendoCodigo) {
                                verificarCodigo();
                            }
                        });
                    }

                    @Override
                    public void changedUpdate(DocumentEvent e) {
                    }
                }
            );

            codigo[i] = campo;
            painelCodigo.add(campo);
        }

        adicionar(card, painelCodigo, 53);

        // CONFIRMAÇÃO DA TURMA

        confirmacao = new JPanel();

        confirmacao.setLayout(
            new BoxLayout(confirmacao, BoxLayout.Y_AXIS)
        );

        confirmacao.setBackground(VERDE_CLARO);

        confirmacao.setBorder(
            BorderFactory.createEmptyBorder(12, 14, 12, 14)
        );

        lblInstituicao = new JLabel(
            "Digite os seis caracteres para localizar a turma."
        );

        lblMateria = new JLabel(" ");
        lblDetalhes = new JLabel(" ");

        lblInstituicao.setFont(
            new Font("SansSerif", Font.PLAIN, 12)
        );

        lblMateria.setFont(
            new Font("SansSerif", Font.BOLD, 16)
        );

        lblMateria.setForeground(MARINHO);

        lblDetalhes.setFont(
            new Font("SansSerif", Font.PLAIN, 11)
        );

        confirmacao.add(lblInstituicao);
        confirmacao.add(Box.createVerticalStrut(5));
        confirmacao.add(lblMateria);
        confirmacao.add(Box.createVerticalStrut(5));
        confirmacao.add(lblDetalhes);

        adicionar(card, confirmacao, 93);

        // BOTÕES INFERIORES

        JPanel acoes = new JPanel(
            new FlowLayout(FlowLayout.LEFT, 12, 0)
        );

        acoes.setOpaque(false);

        JButton cancelar = criarBotao("Cancelar", false);
        cancelar.setPreferredSize(new Dimension(140, 40));
        cancelar.addActionListener(e -> dispose());

        btnEntrar = criarBotao("Sim, entrar na turma", true);
        btnEntrar.setPreferredSize(new Dimension(185, 40));
        btnEntrar.setEnabled(false);

        btnEntrar.addActionListener(e -> realizarMatricula());

        acoes.add(cancelar);
        acoes.add(btnEntrar);

        adicionar(card, acoes, 48);

        atualizarTurnos();
    }

    // CAMPOS

    private static JTextField criarCampo() {

        JTextField campo = new JTextField();

        campo.setFont(new Font("SansSerif", Font.PLAIN, 13));
        campo.setForeground(MARINHO);
        campo.setCaretColor(ROXO);
        campo.setBackground(Color.WHITE);
        campo.setOpaque(false);

        campo.setBorder(
            new BordaArredondada(BORDA, 12, 10, 8)
        );

        campo.addFocusListener(new FocusAdapter() {

            @Override
            public void focusGained(FocusEvent e) {
                campo.repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                campo.repaint();
            }
        });

        return campo;
    }

    // BORDA ARREDONDADA

    private static class BordaArredondada extends AbstractBorder {

        private static final long serialVersionUID = 1L;

        private final Color cor;
        private final int raio;
        private final int horizontal;
        private final int vertical;

        BordaArredondada(
            Color cor,
            int raio,
            int horizontal,
            int vertical
        ) {
            this.cor = cor;
            this.raio = raio;
            this.horizontal = horizontal;
            this.vertical = vertical;
        }

        @Override
        public Insets getBorderInsets(Component c) {

            return new Insets(
                vertical, horizontal, vertical, horizontal
            );
        }

        @Override
        public Insets getBorderInsets(
            Component c,
            Insets insets
        ) {

            Insets i = getBorderInsets(c);

            insets.top = i.top;
            insets.left = i.left;
            insets.bottom = i.bottom;
            insets.right = i.right;

            return insets;
        }

        @Override
        public void paintBorder(
            Component c,
            Graphics g,
            int x,
            int y,
            int largura,
            int altura
        ) {

            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
            );

            g2.setColor(c.isFocusOwner() ? ROXO : cor);

            g2.drawRoundRect(
                x + 1,
                y + 1,
                largura - 3,
                altura - 3,
                raio,
                raio
            );

            g2.dispose();
        }
    }

    // BOTÕES

    private static JButton criarBotao(
        String texto,
        boolean principal
    ) {

        JButton botao = new JButton(texto) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void paintComponent(Graphics g) {

                Graphics2D g2 = (Graphics2D) g.create();

                g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
                );

                Color cor = isEnabled()
                    ? getBackground()
                    : principal
                        ? new Color(165, 160, 236)
                        : NEUTRO;

                g2.setColor(cor);

                g2.fillRoundRect(
                    0,
                    0,
                    getWidth(),
                    getHeight(),
                    12,
                    12
                );

                g2.dispose();

                super.paintComponent(g);
            }
        };

        Color normal = principal ? ROXO : NEUTRO;

        Color hover = principal
            ? ROXO_HOVER
            : new Color(224, 231, 255);

        botao.setBackground(normal);

        botao.setForeground(
            principal ? Color.WHITE : MARINHO
        );

        botao.setFont(
            new Font("SansSerif", Font.BOLD, 12)
        );

        botao.setOpaque(false);
        botao.setContentAreaFilled(false);
        botao.setBorderPainted(false);
        botao.setFocusPainted(false);

        botao.setCursor(
            Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );

        botao.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseEntered(MouseEvent e) {

                if (botao.isEnabled()) {
                    botao.setBackground(hover);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                botao.setBackground(normal);
            }
        });

        return botao;
    }

    // BOTÕES DE TURNO

    private JButton criarBotaoTurno(String nome) {

        JButton botao = criarBotao(nome, false);

        botao.setFont(
            new Font("SansSerif", Font.BOLD, 10)
        );

        botao.addActionListener(e -> {

            turno = nome;
            atualizarTurnos();
        });

        botao.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseExited(MouseEvent e) {
                atualizarTurnos();
            }
        });

        return botao;
    }

    private void atualizarTurnos() {

        for (JButton botao : new JButton[]{
            btnManha, btnTarde, btnNoite
        }) {

            boolean selecionado =
                botao.getText().equals(turno);

            botao.setBackground(
                selecionado ? ROXO : NEUTRO
            );

            botao.setForeground(
                selecionado ? Color.WHITE : MARINHO
            );

            botao.repaint();
        }
    }

    // ELEMENTOS AUXILIARES

    private static JLabel rotulo(String texto) {

        JLabel label = new JLabel(texto);

        label.setFont(
            new Font("SansSerif", Font.BOLD, 12)
        );

        label.setForeground(MARINHO);

        return label;
    }

    private static JLabel secao(String texto) {

        JLabel label = new JLabel(texto);

        label.setFont(
            new Font("SansSerif", Font.BOLD, 11)
        );

        label.setForeground(ROXO);

        return label;
    }

    private static JPanel grupo(
        String titulo,
        JTextField campo
    ) {

        JPanel painel = new JPanel(
            new BorderLayout(0, 6)
        );

        painel.setOpaque(false);

        painel.add(
            rotulo(titulo),
            BorderLayout.NORTH
        );

        painel.add(
            campo,
            BorderLayout.CENTER
        );

        return painel;
    }

    private static void adicionar(
        JPanel destino,
        JComponent componente,
        int altura
    ) {

        componente.setAlignmentX(
            Component.LEFT_ALIGNMENT
        );

        componente.setPreferredSize(
            new Dimension(450, altura)
        );

        componente.setMaximumSize(
            new Dimension(Integer.MAX_VALUE, altura)
        );

        destino.add(componente);
        destino.add(Box.createVerticalStrut(7));
    }

    // PREENCHER OS SEIS CAMPOS

    private void preencherCodigo(String texto) {

        String limpo = texto.toUpperCase()
            .replaceAll("[^A-Z0-9]", "");

        preenchendoCodigo = true;

        try {

            for (int i = 0; i < codigo.length; i++) {

                codigo[i].setText(
                    i < limpo.length()
                        ? String.valueOf(limpo.charAt(i))
                        : ""
                );
            }

        } finally {
            preenchendoCodigo = false;
        }

        codigo[Math.min(limpo.length(), 5)]
            .requestFocusInWindow();

        verificarCodigo();
    }

    // BUSCAR TURMA NO MYSQL

    private void verificarCodigo() {

        StringBuilder sb = new StringBuilder();

        for (JTextField campo : codigo) {
            sb.append(campo.getText().trim());
        }

        final String codigoDigitado = sb.toString();
        final int buscaAtual = ++sequenciaBusca;

        turmaEncontrada = null;
        btnEntrar.setEnabled(false);

        if (codigoDigitado.length() != 6) {

            lblInstituicao.setText(
                "Digite os seis caracteres para localizar a turma."
            );

            lblMateria.setText(" ");
            lblDetalhes.setText(" ");

            return;
        }

        lblInstituicao.setText(
            "Consultando o código no banco de dados..."
        );

        lblMateria.setText(" ");
        lblDetalhes.setText(" ");

        new SwingWorker<Turma, Void>() {

            @Override
            protected Turma doInBackground() {

                return turmaDAO.buscarPorCodigo(
                    codigoDigitado
                );
            }

            @Override
            protected void done() {

                if (buscaAtual != sequenciaBusca
                        || !isDisplayable()) {
                    return;
                }

                try {

                    turmaEncontrada = get();

                    if (turmaEncontrada == null) {

                        lblInstituicao.setText(
                            "Nenhuma turma encontrada com esse código."
                        );

                        lblMateria.setText(" ");

                        lblDetalhes.setText(
                            "Confira o código e tente novamente."
                        );

                        return;
                    }

                    String instituicao =
                        turmaEncontrada.getInstituicao();

                    String materia =
                        turmaEncontrada.getNomeMateria();

                    String nomeTurma =
                        turmaEncontrada.getNomeTurma();

                    String curso =
                        turmaEncontrada.getPeriodo();

                    if (instituicao == null
                            || instituicao.isBlank()) {
                        instituicao = "Instituição não informada";
                    }

                    if (materia == null
                            || materia.isBlank()) {
                        materia = "Disciplina não informada";
                    }

                    if (nomeTurma == null
                            || nomeTurma.isBlank()) {
                        nomeTurma = "Não informada";
                    }

                    if (curso == null
                            || curso.isBlank()) {
                        curso = "Curso não informado";
                    }

                    lblInstituicao.setText(
                        "✓ Turma encontrada · " + instituicao
                    );

                    lblMateria.setText(materia);

                    lblDetalhes.setText(
                        "Turma: " + nomeTurma + " · " + curso
                    );

                    btnEntrar.setEnabled(true);

                } catch (Exception ex) {

                    turmaEncontrada = null;

                    lblInstituicao.setText(
                        "Não foi possível consultar a turma."
                    );

                    lblMateria.setText(" ");

                    lblDetalhes.setText(
                        "Verifique a conexão com o MySQL."
                    );

                    ex.printStackTrace();
                }
            }

        }.execute();
    }

    // REALIZAR MATRÍCULA

    private void realizarMatricula() {

        if (salvando) {
            return;
        }

        if (idAlunoLogado <= 0) {

            JOptionPane.showMessageDialog(
                this,
                "Abra esta tela pela conta do aluno autenticado."
            );

            return;
        }

        if (turmaEncontrada == null) {

            JOptionPane.showMessageDialog(
                this,
                "Informe um código de turma válido."
            );

            return;
        }

        String nome = txtNome.getText().trim();
        String ra = txtRa.getText().trim();
        String email = txtEmail.getText().trim();
        String curso = txtCurso.getText().trim();

        if (nome.isEmpty()
                || ra.isEmpty()
                || email.isEmpty()
                || curso.isEmpty()) {

            JOptionPane.showMessageDialog(
                this,
                "Preencha nome, RA, e-mail institucional e curso/turma."
            );

            return;
        }

        if (!email.matches(
                "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"
        )) {

            JOptionPane.showMessageDialog(
                this,
                "Digite um e-mail institucional válido."
            );

            return;
        }

        final Turma turmaSelecionada = turmaEncontrada;

        final Matricula matricula = new Matricula();

        matricula.setAlunoId(idAlunoLogado);
        matricula.setTurmaId(turmaSelecionada.getId());
        matricula.setRaAluno(ra);

        salvando = true;
        btnEntrar.setEnabled(false);

        new SwingWorker<Boolean, Void>() {

            @Override
            protected Boolean doInBackground() {

                return matriculaDAO.cadastrarMatricula(
                    matricula
                );
            }

            @Override
            protected void done() {

                salvando = false;

                try {

                    boolean sucesso =
                        Boolean.TRUE.equals(get());

                    if (sucesso) {

                        JOptionPane.showMessageDialog(
                            TelaMatricula.this,
                            "Matrícula realizada em "
                                + turmaSelecionada.getNomeMateria()
                                + "!"
                        );

                        if (aoMatricular != null) {
                            aoMatricular.run();
                        }

                        dispose();

                    } else {

                        String erro =
                            matriculaDAO.getUltimoErro();

                        if (erro == null || erro.isBlank()) {
                            erro = "O banco não confirmou o cadastro.";
                        }

                        JOptionPane.showMessageDialog(
                            TelaMatricula.this,
                            "Não foi possível concluir a matrícula.\n\n"
                                + "Detalhes do MySQL:\n"
                                + erro,
                            "Erro na matrícula",
                            JOptionPane.ERROR_MESSAGE
                        );

                        btnEntrar.setEnabled(
                            turmaEncontrada != null
                        );
                    }

                } catch (Exception ex) {

                    JOptionPane.showMessageDialog(
                        TelaMatricula.this,
                        "Erro ao salvar a matrícula:\n"
                            + ex.getMessage(),
                        "Erro",
                        JOptionPane.ERROR_MESSAGE
                    );

                    ex.printStackTrace();

                    btnEntrar.setEnabled(
                        turmaEncontrada != null
                    );
                }
            }

        }.execute();
    }

    // EXECUÇÃO DE TESTE
    // Para matricular, abra esta tela pelo painel do aluno.

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            new TelaMatricula().setVisible(true);
        });
    }
}