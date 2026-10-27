package view.faltas;

import java.awt.Color;

import java.awt.Component;

import java.awt.Cursor;

import java.awt.Dimension;

import java.awt.EventQueue;

import java.awt.Font;

import java.awt.Graphics;

import java.awt.Graphics2D;

import java.awt.GridBagConstraints;

import java.awt.GridBagLayout;

import java.awt.GridLayout;

import java.awt.Insets;

import java.awt.RenderingHints;

import java.util.Arrays;

import javax.swing.JButton;

import javax.swing.JFrame;

import javax.swing.JLabel;

import javax.swing.JOptionPane;

import javax.swing.JPanel;

import javax.swing.JPasswordField;

import javax.swing.JTextField;

import javax.swing.SwingConstants;

import javax.swing.SwingWorker;

import javax.swing.UIManager;

import javax.swing.border.Border;

import java.sql.Connection;

import java.sql.PreparedStatement;

import java.sql.ResultSet;

import java.sql.SQLException;

import conexao.ConexaoMySQL;

public class TelaLogin extends JFrame {

    private static final long serialVersionUID = 1L;

    private JTextField txtEmail;

    private JPasswordField txtSenha;

    private JButton btnEntrar;

    private JLabel lblCriarConta;

    private JLabel lblEsqueceuSenha;

    private static final Color ROXO = new Color(79, 70, 229);

    private static final Color ROXO_HOVER = new Color(67, 56, 202);

    private static final Color AZUL_ESCURO = new Color(30, 27, 75);

    // Borda arredondada dos campos

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

            return true;

        }

        @Override

        public void paintBorder(

                Component c, Graphics g,

                int x, int y, int width, int height) {

            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(

                    RenderingHints.KEY_ANTIALIASING,

                    RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(color);

            g2.drawRoundRect(

                    x + 1, y + 1,

                    width - 3, height - 3,

                    radius, radius);

            g2.dispose();

        }

    }

    public static void main(String[] args) {

        try {

            UIManager.setLookAndFeel(

                    UIManager.getSystemLookAndFeelClassName());

        } catch (Exception e) {

            e.printStackTrace();

        }

        EventQueue.invokeLater(() -> {

            TelaLogin frame = new TelaLogin();

            frame.setVisible(true);

        });

    }

    public TelaLogin() {

        setTitle("Presente! - Login");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setExtendedState(JFrame.MAXIMIZED_BOTH);

        setMinimumSize(new Dimension(850, 600));

        setLocationRelativeTo(null);

        getContentPane().setLayout(new GridLayout(1, 2));

        // PAINEL ESQUERDO

        JPanel painelEsquerda = new JPanel();

        painelEsquerda.setBackground(AZUL_ESCURO);

        painelEsquerda.setLayout(new GridBagLayout());

        JLabel lblBrand = new JLabel("✔️  Presente!");

        lblBrand.setFont(new Font("SansSerif", Font.BOLD, 22));

        lblBrand.setForeground(Color.WHITE);

        GridBagConstraints gbcBrand = new GridBagConstraints();

        gbcBrand.anchor = GridBagConstraints.NORTHWEST;

        gbcBrand.insets = new Insets(30, 50, 20, 50);

        gbcBrand.gridx = 0;

        gbcBrand.gridy = 0;

        gbcBrand.fill = GridBagConstraints.HORIZONTAL;

        painelEsquerda.add(lblBrand, gbcBrand);

        JLabel lblSlogan = new JLabel(

            "<html><h1 style='font-family: SansSerif; " +

            "font-size: 32px; color: #FFFFFF; line-height: 1.3;'>" +

            "Chamada simples.<br>Falta " +

            "<span style='background-color: #818CF8; " +

            "color: #1E1B4B; padding: 2px 8px; " +

            "font-weight: bold;'>sob controle.</span>" +

            "</h1></html>"

        );

        GridBagConstraints gbcSlogan = new GridBagConstraints();

        gbcSlogan.insets = new Insets(10, 50, 15, 50);

        gbcSlogan.gridx = 0;

        gbcSlogan.gridy = 1;

        gbcSlogan.fill = GridBagConstraints.HORIZONTAL;

        painelEsquerda.add(lblSlogan, gbcSlogan);

        JLabel lblDesc = new JLabel(

            "<html><p style='font-family: SansSerif; " +

            "font-size: 14px; color: #C7D2FE;'>" +

            "Professores lançam a presença em segundos.<br>" +

            "Alunos acompanham o limite de faltas em tempo real." +

            "</p></html>"

        );

        GridBagConstraints gbcDesc = new GridBagConstraints();

        gbcDesc.insets = new Insets(10, 50, 40, 50);

        gbcDesc.gridx = 0;

        gbcDesc.gridy = 2;

        gbcDesc.fill = GridBagConstraints.HORIZONTAL;

        painelEsquerda.add(lblDesc, gbcDesc);

        // PAINEL DIREITO

        JPanel painelDireita = new JPanel();

        painelDireita.setBackground(Color.WHITE);

        painelDireita.setLayout(new GridBagLayout());

        JLabel lblBemVindo = new JLabel("Bem-vindo de volta");

        lblBemVindo.setFont(

                new Font("SansSerif", Font.BOLD, 28));

        lblBemVindo.setForeground(new Color(33, 37, 41));

        GridBagConstraints gbc0 = new GridBagConstraints();

        gbc0.insets = new Insets(10, 40, 5, 40);

        gbc0.gridx = 0;

        gbc0.gridy = 0;

        gbc0.fill = GridBagConstraints.HORIZONTAL;

        painelDireita.add(lblBemVindo, gbc0);

        JLabel lblSub = new JLabel(

                "Entre com sua conta para continuar.");

        lblSub.setFont(

                new Font("SansSerif", Font.PLAIN, 13));

        lblSub.setForeground(new Color(108, 117, 125));

        GridBagConstraints gbc1 = new GridBagConstraints();

        gbc1.insets = new Insets(0, 40, 25, 40);

        gbc1.gridx = 0;

        gbc1.gridy = 1;

        gbc1.fill = GridBagConstraints.HORIZONTAL;

        painelDireita.add(lblSub, gbc1);

        // CAMPO E-MAIL

        JLabel lblEmail = new JLabel("E-mail");

        lblEmail.setFont(

                new Font("SansSerif", Font.BOLD, 12));

        lblEmail.setForeground(new Color(33, 37, 41));

        GridBagConstraints gbc2 = new GridBagConstraints();

        gbc2.insets = new Insets(5, 40, 2, 40);

        gbc2.gridx = 0;

        gbc2.gridy = 2;

        gbc2.fill = GridBagConstraints.HORIZONTAL;

        painelDireita.add(lblEmail, gbc2);

        txtEmail = new JTextField(20);

        txtEmail.setFont(

                new Font("SansSerif", Font.PLAIN, 14));

        txtEmail.setPreferredSize(new Dimension(320, 42));

        txtEmail.setBorder(

                new RoundedBorder(12, new Color(200, 200, 200)));

        GridBagConstraints gbc3 = new GridBagConstraints();

        gbc3.insets = new Insets(0, 40, 15, 40);

        gbc3.gridx = 0;

        gbc3.gridy = 3;

        gbc3.fill = GridBagConstraints.HORIZONTAL;

        painelDireita.add(txtEmail, gbc3);

        // CAMPO SENHA

        JLabel lblSenha = new JLabel("Senha");

        lblSenha.setFont(

                new Font("SansSerif", Font.BOLD, 12));

        lblSenha.setForeground(new Color(33, 37, 41));

        GridBagConstraints gbc4 = new GridBagConstraints();

        gbc4.insets = new Insets(5, 40, 2, 40);

        gbc4.gridx = 0;

        gbc4.gridy = 4;

        gbc4.fill = GridBagConstraints.HORIZONTAL;

        painelDireita.add(lblSenha, gbc4);

        txtSenha = new JPasswordField(20);

        txtSenha.setFont(

                new Font("SansSerif", Font.PLAIN, 14));

        txtSenha.setPreferredSize(new Dimension(320, 42));

        txtSenha.setBorder(

                new RoundedBorder(12, new Color(200, 200, 200)));

        GridBagConstraints gbc5 = new GridBagConstraints();

        gbc5.insets = new Insets(0, 40, 5, 40);

        gbc5.gridx = 0;

        gbc5.gridy = 5;

        gbc5.fill = GridBagConstraints.HORIZONTAL;

        painelDireita.add(txtSenha, gbc5);

        // ESQUECI MINHA SENHA

        lblEsqueceuSenha = new JLabel(

                "Esqueci minha senha",

                SwingConstants.RIGHT);

        lblEsqueceuSenha.setCursor(

                new Cursor(Cursor.HAND_CURSOR));

        lblEsqueceuSenha.setForeground(ROXO);

        lblEsqueceuSenha.setFont(

                new Font("SansSerif", Font.BOLD, 12));

        GridBagConstraints gbcSenha = new GridBagConstraints();

        gbcSenha.insets = new Insets(0, 40, 20, 40);

        gbcSenha.gridx = 0;

        gbcSenha.gridy = 6;

        gbcSenha.anchor = GridBagConstraints.EAST;

        painelDireita.add(lblEsqueceuSenha, gbcSenha);

        // A tela de recuperação será integrada posteriormente.

        lblEsqueceuSenha.addMouseListener(

                new java.awt.event.MouseAdapter() {

                    @Override

                    public void mouseClicked(

                            java.awt.event.MouseEvent e) {

                        JOptionPane.showMessageDialog(

                                TelaLogin.this,

                                "A recuperação de senha ainda " +

                                "não foi configurada.");

                    }

                });

        // BOTÃO ENTRAR COM BORDAS ARREDONDADAS

        btnEntrar = new JButton("Entrar") {

            private static final long serialVersionUID = 1L;

            @Override

            protected void paintComponent(Graphics g) {

                Graphics2D g2 = (Graphics2D) g.create();

                g2.setRenderingHint(

                        RenderingHints.KEY_ANTIALIASING,

                        RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(getBackground());

                g2.fillRoundRect(

                        0, 0,

                        getWidth() - 1,

                        getHeight() - 1,

                        12, 12);

                super.paintComponent(g2);

                g2.dispose();

            }

        };

        btnEntrar.setOpaque(false);

        btnEntrar.setContentAreaFilled(false);

        btnEntrar.setBorderPainted(false);

        btnEntrar.setBackground(ROXO);

        btnEntrar.setForeground(Color.WHITE);

        btnEntrar.setFont(

                new Font("SansSerif", Font.BOLD, 15));

        btnEntrar.setPreferredSize(

                new Dimension(320, 45));

        btnEntrar.setFocusPainted(false);

        btnEntrar.setCursor(

                new Cursor(Cursor.HAND_CURSOR));

        // HOVER

        btnEntrar.addMouseListener(

                new java.awt.event.MouseAdapter() {

                    @Override

                    public void mouseEntered(

                            java.awt.event.MouseEvent e) {

                        if (btnEntrar.isEnabled()) {

                            btnEntrar.setBackground(ROXO_HOVER);

                            btnEntrar.repaint();

                        }

                    }

                    @Override

                    public void mouseExited(

                            java.awt.event.MouseEvent e) {

                        btnEntrar.setBackground(ROXO);

                        btnEntrar.repaint();

                    }

                });

        // AÇÃO DO LOGIN

        btnEntrar.addActionListener(e -> realizarLogin());

        // ENTER TAMBÉM REALIZA O LOGIN

        getRootPane().setDefaultButton(btnEntrar);

        GridBagConstraints gbc6 = new GridBagConstraints();

        gbc6.insets = new Insets(5, 40, 15, 40);

        gbc6.gridx = 0;

        gbc6.gridy = 7;

        gbc6.fill = GridBagConstraints.HORIZONTAL;

        painelDireita.add(btnEntrar, gbc6);

        // CRIAR CONTA

        lblCriarConta = new JLabel(

                "Ainda não tem conta? Criar conta",

                SwingConstants.CENTER);

        lblCriarConta.setCursor(

                new Cursor(Cursor.HAND_CURSOR));

        lblCriarConta.setForeground(ROXO);

        lblCriarConta.setFont(

                new Font("SansSerif", Font.BOLD, 13));

        lblCriarConta.addMouseListener(

                new java.awt.event.MouseAdapter() {

                    @Override

                    public void mouseClicked(

                            java.awt.event.MouseEvent e) {

                        new TelaCadastro().setVisible(true);

                        dispose();

                    }

                });

        GridBagConstraints gbc7 = new GridBagConstraints();

        gbc7.insets = new Insets(10, 40, 10, 40);

        gbc7.gridx = 0;

        gbc7.gridy = 8;

        gbc7.fill = GridBagConstraints.HORIZONTAL;

        painelDireita.add(lblCriarConta, gbc7);

        getContentPane().add(painelEsquerda);

        getContentPane().add(painelDireita);

    }

    // ==========================================

    // LOGIN INTEGRADO AO BANCO DE DADOS MYSQL

    // ==========================================

    private void realizarLogin() {

        String email = txtEmail.getText().trim();

        char[] senhaDigitada = txtSenha.getPassword();

        if (email.isEmpty() || senhaDigitada.length == 0) {

            JOptionPane.showMessageDialog(

                    this,

                    "Preencha e-mail e senha!",

                    "Atenção",

                    JOptionPane.WARNING_MESSAGE);

            Arrays.fill(senhaDigitada, '\0');

            return;

        }

        String senha = new String(senhaDigitada);

        Arrays.fill(senhaDigitada, '\0');

        btnEntrar.setEnabled(false);

        btnEntrar.setText("Entrando...");

        SwingWorker<ResultadoLogin, Void> tarefa =

                new SwingWorker<ResultadoLogin, Void>() {

            @Override

            protected ResultadoLogin doInBackground()

                    throws Exception {

                // Compatível com senhas em texto simples

                // do cadastro atual. Deve ser substituído

                // por verificação de hash seguro.

                String sql =

                        "SELECT idUSUARIOS, nome, perfil " +

                        "FROM usuarios " +

                        "WHERE email = ? AND senha = ? " +

                        "LIMIT 1";

                try (Connection conexao =

                        ConexaoMySQL.obterConexao()) {

                    if (conexao == null) {

                        throw new SQLException(

                                "Não foi possível conectar ao MySQL.");

                    }

                    try (PreparedStatement stmt =

                            conexao.prepareStatement(sql)) {

                        stmt.setString(1, email);

                        stmt.setString(2, senha);

                        try (ResultSet rs =

                                stmt.executeQuery()) {

                            if (rs.next()) {

                                int id = rs.getInt("idUSUARIOS");

                                String nome = rs.getString("nome");

                                String perfil = rs.getString("perfil");

                                return new ResultadoLogin(

                                        id, nome, perfil);

                            }

                            return null;

                        }

                    }

                }

            }

            @Override

            protected void done() {

                btnEntrar.setEnabled(true);

                btnEntrar.setText("Entrar");

                try {

                    ResultadoLogin usuario = get();

                    if (usuario == null) {

                        JOptionPane.showMessageDialog(

                                TelaLogin.this,

                                "E-mail ou senha incorretos!",

                                "Erro de Login",

                                JOptionPane.ERROR_MESSAGE);

                        return;

                    }

                    JFrame proximaTela;

                    if ("PROFESSOR".equalsIgnoreCase(

                            usuario.perfil)) {

                        proximaTela =

                                new TelaPrincipalProfessor(usuario.id, usuario.nome);

                    } else if ("ALUNO".equalsIgnoreCase(

                            usuario.perfil)) {

                        proximaTela =

                                new TelaPrincipalAluno(usuario.id);

                    } else {

                        JOptionPane.showMessageDialog(

                                TelaLogin.this,

                                "Perfil não reconhecido: " +

                                usuario.perfil);

                        return;

                    }

                    // Guarda o ID e o nome do usuário

                    // para integração das próximas telas.

                    proximaTela.getRootPane().putClientProperty(

                            "usuarioId", usuario.id);

                    proximaTela.getRootPane().putClientProperty(

                            "usuarioNome", usuario.nome);

                    proximaTela.setVisible(true);

                    dispose();

                } catch (Exception ex) {

                    Throwable causa =

                            ex.getCause() != null

                            ? ex.getCause() : ex;

                    JOptionPane.showMessageDialog(

                            TelaLogin.this,

                            "Erro ao fazer login: " +

                            causa.getMessage(),

                            "Erro",

                            JOptionPane.ERROR_MESSAGE);

                    causa.printStackTrace();

                }

            }

        };

        tarefa.execute();

    }

    // ==========================================

    // DADOS DO USUÁRIO AUTENTICADO

    // ==========================================

    private static class ResultadoLogin {

        final int id;

        final String nome;

        final String perfil;

        ResultadoLogin(

                int id,

                String nome,

                String perfil) {

            this.id = id;

            this.nome = nome;

            this.perfil = perfil;

        }

    }

}
