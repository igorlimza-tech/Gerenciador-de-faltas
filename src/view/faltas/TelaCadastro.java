package view.faltas;
import conexao.ConexaoMySQL;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class TelaCadastro extends JFrame {

    private static final long serialVersionUID = 1L;

    private static final Color ROXO = new Color(79, 70, 229);
    private static final Color ROXO_HOVER = new Color(67, 56, 202);
    private static final Color MARINHO = new Color(30, 27, 75);
    private static final Color ROXO_CLARO = new Color(129, 140, 248);
    private static final Color NEUTRO = new Color(245, 247, 250);
    private static final Color CINZA = new Color(108, 117, 125);
    private static final Color BORDA = new Color(200, 200, 200);

    private JTextField txtNomeCadastro;
    private JTextField txtEmailCadastro;
    private JPasswordField txtSenhaCadastro;
    private JPasswordField txtConfirmaSenhaCadastro;
    private JButton btnCriarConta;

    private String perfilSelecionado = "ALUNO";

    private static class RoundedBorder implements Border {

        private final int radius;
        private final Color color;

        RoundedBorder(int radius, Color color) {
            this.radius = radius;
            this.color = color;
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(10, 14, 10, 14);
        }

        @Override
        public boolean isBorderOpaque() {
            return false;
        }

        @Override
        public void paintBorder(
                Component c, Graphics g,
                int x, int y, int width, int height) {

            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
            );

            g2.setColor(color);
            g2.drawRoundRect(
                x + 1, y + 1,
                width - 3, height - 3,
                radius, radius
            );
            g2.dispose();
        }
    }

    private static class PainelAvisoArredondado extends JPanel {

        private static final long serialVersionUID = 1L;

        PainelAvisoArredondado() {
            setOpaque(false);
            setPreferredSize(new Dimension(320, 85));
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
                0, 0, getWidth(), getHeight(), 16, 16
            );

            g2.setColor(new Color(199, 210, 254));
            g2.drawRoundRect(
                0, 0, getWidth() - 1,
                getHeight() - 1, 16, 16
            );

            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class BotaoPerfil extends JButton {

        private static final long serialVersionUID = 1L;
        private boolean selecionado;

        BotaoPerfil(String texto, boolean selecionado) {
            super(texto);

            this.selecionado = selecionado;
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);

            setCursor(
                Cursor.getPredefinedCursor(
                    Cursor.HAND_CURSOR
                )
            );

            setPreferredSize(new Dimension(150, 65));
            atualizarVisual();
        }

        void setSelecionado(boolean valor) {
            selecionado = valor;
            atualizarVisual();
            repaint();
        }

        private void atualizarVisual() {
            setBackground(
                selecionado ? ROXO_CLARO : NEUTRO
            );
            setForeground(
                selecionado ? MARINHO : CINZA
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
                0, 0,
                getWidth() - 1,
                getHeight() - 1,
                16, 16
            );

            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class BotaoRoxo extends JButton {

        private static final long serialVersionUID = 1L;

        BotaoRoxo(String texto) {
            super(texto);

            setBackground(ROXO);
            setForeground(Color.WHITE);
            setFont(new Font(
                "SansSerif", Font.BOLD, 15
            ));

            setPreferredSize(new Dimension(320, 45));
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);

            setCursor(
                Cursor.getPredefinedCursor(
                    Cursor.HAND_CURSOR
                )
            );

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (isEnabled()) {
                        setBackground(ROXO_HOVER);
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    setBackground(ROXO);
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

            g2.setColor(
                isEnabled()
                    ? getBackground()
                    : new Color(165, 160, 236)
            );

            g2.fillRoundRect(
                0, 0,
                getWidth() - 1,
                getHeight() - 1,
                12, 12
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
            new TelaCadastro().setVisible(true);
        });
    }

    public TelaCadastro() {

        setTitle("Presente! - Criar Conta");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);

        getContentPane().setLayout(
            new GridLayout(1, 2)
        );

        JPanel painelEsquerda = new JPanel();
        painelEsquerda.setBackground(MARINHO);
        painelEsquerda.setLayout(new GridBagLayout());

        JLabel lblBrand = new JLabel("✔  Presente!");
        lblBrand.setFont(
            new Font("SansSerif", Font.BOLD, 22)
        );
        lblBrand.setForeground(Color.WHITE);

        adicionar(
            painelEsquerda, lblBrand,
            0, new Insets(30, 50, 20, 50)
        );

        JLabel lblSlogan = new JLabel(
            "<html><h1 style='font-family:SansSerif;" +
            "font-size:32px;color:#FFFFFF;'>" +
            "Chamada simples.<br>Falta " +
            "<span style='background-color:#818CF8;" +
            "color:#1E1B4B;font-weight:bold;'>" +
            "sob controle.</span></h1></html>"
        );

        adicionar(
            painelEsquerda, lblSlogan,
            1, new Insets(10, 50, 15, 50)
        );

        JLabel lblDesc = new JLabel(
            "<html><p style='font-family:SansSerif;" +
            "font-size:14px;color:#C7D2FE;'>" +
            "Professores lançam a presença em segundos." +
            "<br>Alunos acompanham o limite de faltas " +
            "em tempo real.</p></html>"
        );

        adicionar(
            painelEsquerda, lblDesc,
            2, new Insets(10, 50, 40, 50)
        );

        JPanel painelDireita = new JPanel(
            new GridBagLayout()
        );
        painelDireita.setBackground(Color.WHITE);

        JLabel lblTitulo = new JLabel("Criar sua conta");
        lblTitulo.setFont(
            new Font("SansSerif", Font.BOLD, 26)
        );
        lblTitulo.setForeground(
            new Color(33, 37, 41)
        );

        adicionar(
            painelDireita, lblTitulo,
            0, new Insets(10, 40, 3, 40)
        );

        JLabel lblSub = new JLabel(
            "Escolha como você vai usar o Presente!"
        );
        lblSub.setFont(
            new Font("SansSerif", Font.PLAIN, 13)
        );
        lblSub.setForeground(CINZA);

        adicionar(
            painelDireita, lblSub,
            1, new Insets(0, 40, 10, 40)
        );

        JPanel painelPerfil = new JPanel(
            new GridLayout(1, 2, 10, 0)
        );
        painelPerfil.setBackground(Color.WHITE);

        BotaoPerfil btnAluno = new BotaoPerfil(
            "<html><center><b>Sou Aluno</b><br>" +
            "<font size='2'>Entro em turmas</font>" +
            "</center></html>",
            true
        );

        BotaoPerfil btnProfessor = new BotaoPerfil(
            "<html><center><b>Sou Professor</b><br>" +
            "<font size='2'>Crio turmas</font>" +
            "</center></html>",
            false
        );

        btnAluno.addActionListener(e -> {
            perfilSelecionado = "ALUNO";
            btnAluno.setSelecionado(true);
            btnProfessor.setSelecionado(false);
        });

        btnProfessor.addActionListener(e -> {
            perfilSelecionado = "PROFESSOR";
            btnProfessor.setSelecionado(true);
            btnAluno.setSelecionado(false);
        });

        painelPerfil.add(btnAluno);
        painelPerfil.add(btnProfessor);

        adicionar(
            painelDireita, painelPerfil,
            2, new Insets(0, 40, 10, 40)
        );

        adicionar(
            painelDireita, criarLabel("Nome completo"),
            3, new Insets(5, 40, 2, 40)
        );

        txtNomeCadastro = criarCampo();
        adicionar(
            painelDireita, txtNomeCadastro,
            4, new Insets(0, 40, 10, 40)
        );
        
        adicionar(
            painelDireita, criarLabel("E-mail"),
            5, new Insets(5, 40, 2, 40)
        );

        txtEmailCadastro = criarCampo();
        adicionar(
            painelDireita, txtEmailCadastro,
            6, new Insets(0, 40, 10, 40)
        );

    
        JPanel painelSenhasLabels = new JPanel(
            new GridLayout(1, 2, 12, 0)
        );
        painelSenhasLabels.setBackground(Color.WHITE);

        painelSenhasLabels.add(criarLabel("Senha"));
        painelSenhasLabels.add(
            criarLabel("Confirmar senha")
        );

        adicionar(
            painelDireita, painelSenhasLabels,
            7, new Insets(5, 40, 2, 40)
        );

        JPanel painelSenhasCampos = new JPanel(
            new GridLayout(1, 2, 12, 0)
        );
        painelSenhasCampos.setBackground(Color.WHITE);

        txtSenhaCadastro = criarCampoSenha();
        txtConfirmaSenhaCadastro = criarCampoSenha();

        painelSenhasCampos.add(txtSenhaCadastro);
        painelSenhasCampos.add(txtConfirmaSenhaCadastro);

        adicionar(
            painelDireita, painelSenhasCampos,
            8, new Insets(0, 40, 10, 40)
        );

        PainelAvisoArredondado painelAviso =
            new PainelAvisoArredondado();

        painelAviso.setLayout(new GridBagLayout());

        JLabel lblAviso = new JLabel(
            "<html><div style='color:#1E1B4B;" +
            "font-family:SansSerif;font-size:11px;" +
            "padding:6px;'>" +
            "<b>ⓘ Instituição, RA e e-mail institucional</b>" +
            " ficam para depois: você informa quando entrar " +
            "em uma turma (aluno) ou criar uma (professor)." +
            "</div></html>"
        );

        GridBagConstraints gbcAviso = new GridBagConstraints();
        gbcAviso.insets = new Insets(2, 6, 2, 6);
        gbcAviso.fill = GridBagConstraints.HORIZONTAL;
        gbcAviso.weightx = 1.0;

        painelAviso.add(lblAviso, gbcAviso);

        adicionar(
            painelDireita, painelAviso,
            9, new Insets(0, 40, 12, 40)
        );

        btnCriarConta = new BotaoRoxo("Criar conta");

        adicionar(
            painelDireita, btnCriarConta,
            10, new Insets(0, 40, 15, 40)
        );

        btnCriarConta.addActionListener(
            e -> realizarCadastro()
        );

        getRootPane().setDefaultButton(btnCriarConta);

        JLabel lblVoltarLogin = new JLabel(
            "Já tem conta? Entrar",
            SwingConstants.CENTER
        );

        lblVoltarLogin.setCursor(
            Cursor.getPredefinedCursor(
                Cursor.HAND_CURSOR
            )
        );

        lblVoltarLogin.setForeground(ROXO);
        lblVoltarLogin.setFont(
            new Font("SansSerif", Font.BOLD, 13)
        );

        lblVoltarLogin.addMouseListener(
            new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    new TelaLogin().setVisible(true);
                    dispose();
                }
            }
        );

        adicionar(
            painelDireita, lblVoltarLogin,
            11, new Insets(0, 40, 10, 40)
        );

        getContentPane().add(painelEsquerda);
        getContentPane().add(painelDireita);
    }

    private static JLabel criarLabel(String texto) {
        JLabel label = new JLabel(texto);

        label.setFont(
            new Font("SansSerif", Font.BOLD, 12)
        );
        label.setForeground(
            new Color(33, 37, 41)
        );

        return label;
    }

    private static JTextField criarCampo() {
        JTextField campo = new JTextField(20);

        campo.setFont(
            new Font("SansSerif", Font.PLAIN, 14)
        );

        campo.setPreferredSize(
            new Dimension(320, 42)
        );

        campo.setBorder(
            new RoundedBorder(12, BORDA)
        );

        return campo;
    }

    private static JPasswordField criarCampoSenha() {
        JPasswordField campo = new JPasswordField();

        campo.setFont(
            new Font("SansSerif", Font.PLAIN, 14)
        );

        campo.setPreferredSize(
            new Dimension(154, 42)
        );

        campo.setBorder(
            new RoundedBorder(12, BORDA)
        );

        return campo;
    }

    private static void adicionar(
            JPanel painel,
            JComponent componente,
            int linha,
            Insets margens) {

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = linha;
        gbc.insets = margens;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        painel.add(componente, gbc);
    }
    
    private void realizarCadastro() {

        String nome = txtNomeCadastro.getText().trim();

        String email = txtEmailCadastro.getText()
            .trim()
            .toLowerCase(Locale.ROOT);

        char[] senha = txtSenhaCadastro.getPassword();
        char[] confirma = txtConfirmaSenhaCadastro.getPassword();

        if (nome.isEmpty() || email.isEmpty()
                || senha.length == 0
                || confirma.length == 0) {

            mensagem("Preencha todos os campos!",
                JOptionPane.WARNING_MESSAGE);

            limparSenhas(senha, confirma);
            return;
        }

        if (nome.length() < 3) {
            mensagem(
                "Informe seu nome completo.",
                JOptionPane.WARNING_MESSAGE
            );
            limparSenhas(senha, confirma);
            return;
        }

        if (!email.matches(
                "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {

            mensagem(
                "Digite um e-mail válido.",
                JOptionPane.WARNING_MESSAGE
            );
            limparSenhas(senha, confirma);
            return;
        }

        if (senha.length < 8) {
            mensagem(
                "A senha deve ter pelo menos 8 caracteres.",
                JOptionPane.WARNING_MESSAGE
            );
            limparSenhas(senha, confirma);
            return;
        }

        if (!Arrays.equals(senha, confirma)) {
            mensagem(
                "As senhas não coincidem!",
                JOptionPane.ERROR_MESSAGE
            );
            limparSenhas(senha, confirma);
            return;
        }
        
        final String senhaCadastro = new String(senha);
        final String perfil = perfilSelecionado;

        limparSenhas(senha, confirma);

        btnCriarConta.setEnabled(false);
        btnCriarConta.setText("Cadastrando...");

        SwingWorker<String, Void> tarefa =
            new SwingWorker<String, Void>() {

            @Override
            protected String doInBackground()
                    throws Exception {

                String verificar =
                    "SELECT idUSUARIOS FROM usuarios " +
                    "WHERE email = ? LIMIT 1";

                String inserir =
                    "INSERT INTO usuarios " +
                    "(nome, email, senha, perfil) " +
                    "VALUES (?, ?, ?, ?)";

                try (Connection conexao =
                        ConexaoMySQL.obterConexao()) {

                    if (conexao == null) {
                        throw new SQLException(
                            "Não foi possível conectar ao MySQL."
                        );
                    }

                    try (PreparedStatement consulta =
                            conexao.prepareStatement(verificar)) {

                        consulta.setString(1, email);

                        try (ResultSet rs =
                                consulta.executeQuery()) {

                            if (rs.next()) {
                                return "DUPLICADO";
                            }
                        }
                    }

                    try (PreparedStatement stmt =
                            conexao.prepareStatement(inserir)) {

                        stmt.setString(1, nome);
                        stmt.setString(2, email);
                        stmt.setString(3, senhaCadastro);
                        stmt.setString(4, perfil);

                        int linhas = stmt.executeUpdate();

                        return linhas == 1
                            ? "SUCESSO"
                            : "FALHA";
                    }

                } catch (SQLIntegrityConstraintViolationException ex) {
                    return "DUPLICADO";
                }
            }

            @Override
            protected void done() {

                btnCriarConta.setEnabled(true);
                btnCriarConta.setText("Criar conta");

                try {
                    String resultado = get();

                    if ("DUPLICADO".equals(resultado)) {
                        mensagem(
                            "Este e-mail já está cadastrado. " +
                            "Faça login ou utilize outro e-mail.",
                            JOptionPane.WARNING_MESSAGE
                        );
                        return;
                    }

                    if ("SUCESSO".equals(resultado)) {

                        JOptionPane.showMessageDialog(
                            TelaCadastro.this,
                            "Conta criada com sucesso!\n" +
                            "Agora você pode fazer login.",
                            "Cadastro realizado",
                            JOptionPane.INFORMATION_MESSAGE
                        );

                        new TelaLogin().setVisible(true);
                        dispose();
                        return;
                    }

                    mensagem(
                        "Não foi possível criar a conta.",
                        JOptionPane.ERROR_MESSAGE
                    );

                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();

                    mensagem(
                        "Cadastro interrompido.",
                        JOptionPane.ERROR_MESSAGE
                    );

                } catch (ExecutionException ex) {

                    Throwable causa = ex.getCause() != null
                        ? ex.getCause() : ex;

                    mensagem(
                        "Erro ao cadastrar:\n" +
                        causa.getMessage(),
                        JOptionPane.ERROR_MESSAGE
                    );

                    causa.printStackTrace();
                }
            }
        };

        tarefa.execute();
    }

    private void mensagem(String texto, int tipo) {
        JOptionPane.showMessageDialog(
            this,
            texto,
            "Presente!",
            tipo
        );
    }

    private void limparSenhas(
            char[] senha,
            char[] confirmacao) {

        Arrays.fill(senha, '\0');
        Arrays.fill(confirmacao, '\0');
    }
}
