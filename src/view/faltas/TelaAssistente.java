package view.faltas;
import api.gemini.GeminiService;
import assistente.local.Inteligente;
import dao.faltas.AssistenteDAO;
import javax.swing.*;
import javax.swing.border.AbstractBorder;
import java.awt.*;
import java.awt.geom.Path2D;
import java.util.Locale;
import java.sql.SQLException;
import dao.faltas.AssistenteDAO.DadosAluno;

public class TelaAssistente extends JFrame {
    private static final long serialVersionUID = 1L;
    private static final Color MARINHO = new Color(30, 27, 75);
    private static final Color ROXO = new Color(79, 70, 229);
    private static final Color ROXO_HOVER = new Color(67, 56, 202);
    private static final Color FUNDO = new Color(248, 250, 252);
    private static final Color CINZA = new Color(100, 116, 139);
    private static final Color BORDA = new Color(199, 210, 254);
    private final GeminiService gemini = new GeminiService();
    private final Inteligente local = new Inteligente();
    private final AssistenteDAO assistenteDAO = new AssistenteDAO();
    private int idAlunoLogado = -1;
    private Integer materiaSelecionada = null;
    private String perguntaPendente = null;
    private String ultimaIntencao = null;
    private final JPanel historico = new JPanel();
    private final JTextField entrada = new JTextField();
    private final JButton enviar = new BotaoEnviar();
    private final JScrollPane rolagem;

    public TelaAssistente(int idAlunoLogado) {
        this();
        this.idAlunoLogado = idAlunoLogado;
        mostrarAlertasAoAbrir();
    }
   
    public TelaAssistente() {
        setTitle("Presente! - Assistente");
        setSize(460, 640);
        setMinimumSize(new Dimension(390, 500));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        JPanel raiz = new JPanel(new BorderLayout());
        raiz.setBackground(FUNDO);
        setContentPane(raiz);
        
        JPanel topo = new JPanel(new BorderLayout());
        topo.setBackground(MARINHO);
        topo.setBorder(
            BorderFactory.createEmptyBorder(18, 22, 18, 18)
        );
        JLabel titulo = new JLabel("✦  Assistente");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 19));
        titulo.setForeground(Color.WHITE);
        topo.add(titulo, BorderLayout.WEST);
        JButton fechar = new JButton("X");
        fechar.setForeground(Color.WHITE);
        fechar.setFont(new Font("SansSerif", Font.BOLD, 15));
        fechar.setContentAreaFilled(false);
        fechar.setBorderPainted(false);
        fechar.setFocusPainted(false);
        fechar.setCursor(
            Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );
        fechar.addActionListener(e -> dispose());
        topo.add(fechar, BorderLayout.EAST);
        raiz.add(topo, BorderLayout.NORTH);
        
        historico.setLayout(
            new BoxLayout(historico, BoxLayout.Y_AXIS)
        );
        historico.setBackground(FUNDO);
        historico.setBorder(
            BorderFactory.createEmptyBorder(18, 14, 18, 14)
        );
        rolagem = new JScrollPane(historico);
        rolagem.setBorder(null);
        rolagem.getViewport().setBackground(FUNDO);
        rolagem.setHorizontalScrollBarPolicy(
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );
        rolagem.getVerticalScrollBar().setUnitIncrement(16);
        raiz.add(rolagem, BorderLayout.CENTER);
        
        JPanel rodape = new JPanel(new BorderLayout(0, 12));
        rodape.setBackground(Color.WHITE);
        rodape.setBorder(
            BorderFactory.createEmptyBorder(12, 14, 16, 14)
        );
        
        JPanel sugestoes = new JPanel(
            new FlowLayout(FlowLayout.LEFT, 7, 0)
        );
        sugestoes.setOpaque(false);
        sugestoes.add(
            chip("Minhas matérias", "Quais são minhas matérias?")
        );
        sugestoes.add(
            chip("Minhas faltas", "Como estão minhas faltas?")
        );
        sugestoes.add(
            chip("Aulas amanhã", "Posso faltar amanhã?")
        );
        rodape.add(sugestoes, BorderLayout.NORTH);
        
        JPanel campo = new JPanel(new BorderLayout(8, 0));
        campo.setOpaque(false);
        entrada.setFont(
            new Font("SansSerif", Font.PLAIN, 13)
        );
        entrada.setForeground(MARINHO);
        entrada.setCaretColor(ROXO);
        entrada.setOpaque(false);
        entrada.setBorder(
            BorderFactory.createCompoundBorder(
                new BordaArredondada(BORDA, 18),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
            )
        );
        entrada.addActionListener(e -> enviarMensagem());
        campo.add(entrada, BorderLayout.CENTER);
        
        enviar.setPreferredSize(new Dimension(48, 42));
        enviar.addActionListener(e -> enviarMensagem());
        campo.add(enviar, BorderLayout.EAST);
        rodape.add(campo, BorderLayout.CENTER);
        raiz.add(rodape, BorderLayout.SOUTH);
        
        adicionarMensagem(
            "Olá! Sou o Assistente do Presente! "
            + "Como posso ajudar você?",
            false
        );
    }
    
    private JButton chip(String rotulo, String pergunta) {
        JButton botao = new BotaoChip(rotulo);
        botao.addActionListener(e -> {
            entrada.setText(pergunta);
            enviarMensagem();
        });
        return botao;
    }
    
    private void enviarMensagem() {
        String pergunta = entrada.getText().trim();
        if (pergunta.isEmpty() || !enviar.isEnabled()) return;

        entrada.setText("");
        adicionarMensagem(pergunta, true);
        entrada.setEnabled(false);
        enviar.setEnabled(false);
        JLabel aguardando = adicionarMensagem("Escrevendo...", false);

        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() {
                return responder(pergunta);
            }

            @Override
            protected void done() {
                try {
                    aguardando.setText(formatarHtml(get()));
                } catch (Exception ex) {
                    ex.printStackTrace();
                    aguardando.setText(formatarHtml(
                        "Não consegui responder agora. Tente novamente."));
                } finally {
                    entrada.setEnabled(true);
                    enviar.setEnabled(true);
                    entrada.requestFocusInWindow();
                    rolarFim();
                }
            }
        }.execute();
    }
    
    private String responder(String pergunta) {
        if (idAlunoLogado > 0) {
            try {
                DadosAluno dados = assistenteDAO.carregarDados(idAlunoLogado);
                Inteligente.Resposta resultado = local.responderAcademico(
                    pergunta, dados, materiaSelecionada, perguntaPendente, ultimaIntencao);
                if (resultado.reconhecida) {
                    materiaSelecionada = resultado.idMateria;
                    perguntaPendente = resultado.perguntaPendente;
                    ultimaIntencao = resultado.ultimaIntencao;
                    return resultado.mensagem;
                }
            } catch (SQLException erro) {
                System.err.println("Erro na consulta ao MySQL: " + erro.getMessage());
                String frase = pergunta.toLowerCase(Locale.forLanguageTag("pt-BR"));
                if (frase.contains("falta") || frase.contains("presença")
                        || frase.contains("presenca") || frase.contains("aula")
                        || frase.contains("frequência") || frase.contains("frequencia")
                        || frase.contains("chamada") || frase.contains("matéria")
                        || frase.contains("materia") || frase.contains("turma")) {
                    return "Não consegui acessar seus dados acadêmicos agora. "
                           + "Confira a conexão com o MySQL e tente novamente.";
                }
                //dúvida geral pode ir para o gemini sem o MySQL
            }
        } else {
            String normal = pergunta.toLowerCase(Locale.forLanguageTag("pt-BR"));
            if (normal.contains("minha") || normal.contains("meu") || normal.contains("falta")
                    || normal.contains("frequência") || normal.contains("frequencia")
                    || normal.contains("matéria") || normal.contains("materia")) {
                return "Entre com sua conta de aluno e abra o assistente pelo seu painel "
                     + "para consultar informações pessoais.";
            }
        }

        //nenhum dado de aluno é enviado ao gemini!!!! apenas a pergunta geral!!
        try {
            String resposta = gemini.chamarGemini(pergunta);
            if (resposta != null && !resposta.isBlank()
                    && !resposta.startsWith("Erro na API:")
                    && !resposta.startsWith("Erro ao conectar com o Gemini:")) {
                return resposta.trim();
            }
        } catch (Exception ex) {
            System.err.println("Gemini indisponível: " + ex.getMessage());
        }
        return local.responderLocal(pergunta);
    }

    //alertas consultados uma vez, ao abrir o assist...sem avisos em seg plano
    private void mostrarAlertasAoAbrir() {
        if (idAlunoLogado <= 0) return;
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() {
                try {
                    return local.alertasIniciais(assistenteDAO.carregarDados(idAlunoLogado));
                } catch (SQLException ex) {
                    return "";
                }
            }
            @Override
            protected void done() {
                try {
                    String alertas = get();
                    if (alertas != null && !alertas.isBlank()) {
                        adicionarMensagem("Atualizações da sua frequência:\n" + alertas, false);
                    }
                } catch (Exception ignored) { }
            }
        }.execute();
    }

    private JLabel adicionarMensagem(
            String texto, boolean aluno) {
        JPanel linha = new JPanel(
            new FlowLayout(
                aluno ? FlowLayout.RIGHT : FlowLayout.LEFT,
                0,
                4
            )
        );
        linha.setOpaque(false);
        linha.setAlignmentX(Component.LEFT_ALIGNMENT);
        linha.setMaximumSize(
            new Dimension(Integer.MAX_VALUE, 1000)
        );
        JPanel balao = new PainelArredondado(
            aluno
                ? new Color(224, 231, 255)
                : Color.WHITE,
            aluno
                ? BORDA
                : new Color(226, 232, 240)
        );
        balao.setLayout(new BorderLayout());
        balao.setBorder(
            BorderFactory.createEmptyBorder(11, 13, 11, 13)
        );
        JLabel mensagem = new JLabel(formatarHtml(texto));
        mensagem.setForeground(MARINHO);
        mensagem.setFont(
            new Font("SansSerif", Font.PLAIN, 13)
        );
        balao.add(mensagem, BorderLayout.CENTER);
        linha.add(balao);
        historico.add(linha);
        historico.add(Box.createVerticalStrut(5));
        historico.revalidate();
        historico.repaint();
        rolarFim();
        return mensagem;
    }
    
    private String formatarHtml(String texto) {
        if (texto == null) texto = "";
        String seguro = texto
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("\n", "<br>");
        return "<html><div style='width:270px;'>" + seguro + "</div></html>";
    }

    private void rolarFim() {
        SwingUtilities.invokeLater(() -> {
            JScrollBar barra =
                rolagem.getVerticalScrollBar();
            barra.setValue(barra.getMaximum());
        });
    }
    
    private static class PainelArredondado extends JPanel {
        private static final long serialVersionUID = 1L;
        private final Color fundo;
        private final Color borda;
        PainelArredondado(Color fundo, Color borda) {
            this.fundo = fundo;
            this.borda = borda;
            setOpaque(false);
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
            );
            g2.setColor(fundo);
            g2.fillRoundRect(
                0, 0,
                getWidth() - 1,
                getHeight() - 1,
                18, 18
            );
            g2.setColor(borda);
            g2.drawRoundRect(
                0, 0,
                getWidth() - 1,
                getHeight() - 1,
                18, 18
            );
            g2.dispose();
            super.paintComponent(g);
        }
    }
    
    private static class BordaArredondada
            extends AbstractBorder {
        private static final long serialVersionUID = 1L;
        private final Color cor;
        private final int raio;
        BordaArredondada(Color cor, int raio) {
            this.cor = cor;
            this.raio = raio;
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
            g2.setColor(cor);
            g2.drawRoundRect(
                x, y,
                width - 1,
                height - 1,
                raio, raio
            );
            g2.dispose();
        }
        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(2, 2, 2, 2);
        }
    }
    
    private static class BotaoChip extends JButton {
        private static final long serialVersionUID = 1L;
        BotaoChip(String texto) {
            super(texto);
            setFont(new Font("SansSerif", Font.PLAIN, 11));
            setForeground(MARINHO);
            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setOpaque(false);
            setBorder(
                BorderFactory.createEmptyBorder(8, 11, 8, 11)
            );
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
            g2.setColor(
                getModel().isRollover()
                    ? new Color(224, 231, 255)
                    : new Color(238, 242, 255)
            );
            g2.fillRoundRect(
                0, 0,
                getWidth() - 1,
                getHeight() - 1,
                20, 20
            );
            g2.setColor(BORDA);
            g2.drawRoundRect(
                0, 0,
                getWidth() - 1,
                getHeight() - 1,
                20, 20
            );
            g2.dispose();
            super.paintComponent(g);
        }
    }
    
    private static class BotaoEnviar extends JButton {
        private static final long serialVersionUID = 1L;
        BotaoEnviar() {
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
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
            Color cor = !isEnabled()
                ? CINZA
                : getModel().isRollover()
                    ? ROXO_HOVER
                    : ROXO;
            g2.setColor(cor);
            g2.fillRoundRect(
                0, 0,
                getWidth() - 1,
                getHeight() - 1,
                14, 14
            );
            //avião de papel desenhado em java kkkk
            double cx = getWidth() / 2.0;
            double cy = getHeight() / 2.0;
            Path2D aviao = new Path2D.Double();
            aviao.moveTo(cx - 11, cy - 7);
            aviao.lineTo(cx + 12, cy);
            aviao.lineTo(cx - 11, cy + 8);
            aviao.lineTo(cx - 5, cy + 1);
            aviao.lineTo(cx + 5, cy);
            aviao.lineTo(cx - 5, cy - 1);
            aviao.closePath();
            g2.setColor(Color.WHITE);
            g2.fill(aviao);
            g2.dispose();
            super.paintComponent(g);
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new TelaAssistente().setVisible(true);
        });
    }
}
