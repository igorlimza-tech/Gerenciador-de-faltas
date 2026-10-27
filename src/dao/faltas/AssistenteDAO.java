package dao.faltas;
import conexao.ConexaoMySQL;
import util.faltas.CalculoCargaHoraria;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AssistenteDAO {

    public static final class Chamada {
        public final LocalDate data;
        public final int quantidade;
        public final String status;

        public Chamada(LocalDate data, int quantidade, String status) {
            this.data = data;
            this.quantidade = Math.max(0, quantidade);
            this.status = status == null ? "" : status.trim().toUpperCase(Locale.ROOT);
        }

        public boolean presente() {
            return status.equals("PRESENTE") || status.equals("P");
        }

        public boolean falta() {
            return status.equals("FALTA") || status.equals("F")
                    || status.equals("AUSENTE") || status.equals("FALTOU")
                    || status.equals("FALTOSO");
        }

        public boolean semMarcacao() {
            return !presente() && !falta();
        }
    }

    public static final class Materia {
        public final int id;
        public final String nome;
        public final String turma;
        public final String instituicao;
        public final String professor;
        public final int cargaHoraria;
        public final BigDecimal percentualLimite;
        public final int totalAulas;
        public final int limiteAulas;
        public final Map<DayOfWeek, Integer> grade = new LinkedHashMap<>();
        public final List<Chamada> chamadas = new ArrayList<>();

        public Materia(int id, String nome, String turma, String instituicao,
                       String professor, int cargaHoraria, BigDecimal percentualLimite) {
            this.id = id;
            this.nome = nome == null ? "Disciplina sem nome" : nome;
            this.turma = turma == null ? "" : turma;
            this.instituicao = instituicao == null ? "" : instituicao;
            this.professor = professor == null ? "" : professor;
            this.cargaHoraria = Math.max(0, cargaHoraria);
            this.percentualLimite = percentualLimite;
            this.totalAulas = CalculoCargaHoraria.totalAulas(this.cargaHoraria);
            this.limiteAulas = percentualLimite == null || this.cargaHoraria <= 0
                    ? -1 : CalculoCargaHoraria.limiteFaltas(this.cargaHoraria, percentualLimite);
        }

        public int faltas() {
            int total = 0;
            for (Chamada c : chamadas) if (c.falta()) total += c.quantidade;
            return total;
        }

        public int presencas() {
            int total = 0;
            for (Chamada c : chamadas) if (c.presente()) total += c.quantidade;
            return total;
        }

        public int aulasSemMarcacao() {
            int total = 0;
            for (Chamada c : chamadas) if (c.semMarcacao()) total += c.quantidade;
            return total;
        }

        public int aulasRegistradas() {
            return presencas() + faltas();
        }

        public int restantes() {
            return limiteAulas < 0 ? -1 : Math.max(0, limiteAulas - faltas());
        }

        public double frequencia() {
            int lancadas = aulasRegistradas();
            return lancadas <= 0 ? -1 : presencas() * 100.0 / lancadas;
        }

        public double percentualDoLimite() {
            if (limiteAulas < 0) return -1;
            if (limiteAulas == 0) return faltas() > 0 ? 100.0 : 0;
            return faltas() * 100.0 / limiteAulas;
        }

        public int aulasNoDia(DayOfWeek dia) {
            return grade.getOrDefault(dia, 0);
        }

        public int chamadasNoDia(LocalDate data) {
            int total = 0;
            for (Chamada c : chamadas) if (data.equals(c.data)) total++;
            return total;
        }

        public int faltasNoDia(LocalDate data) {
            int total = 0;
            for (Chamada c : chamadas) if (data.equals(c.data) && c.falta()) total += c.quantidade;
            return total;
        }

        public int presencasNoDia(LocalDate data) {
            int total = 0;
            for (Chamada c : chamadas) if (data.equals(c.data) && c.presente()) total += c.quantidade;
            return total;
        }

        public int naoMarcadasNoDia(LocalDate data) {
            int total = 0;
            for (Chamada c : chamadas) if (data.equals(c.data) && c.semMarcacao()) total += c.quantidade;
            return total;
        }

        public LocalDate ultimaFalta() {
            LocalDate data = null;
            for (Chamada c : chamadas) {
                if (c.falta() && (data == null || c.data.isAfter(data))) data = c.data;
            }
            return data;
        }

        public int aulasNaSemana() {
            int total = 0;
            for (int n : grade.values()) total += n;
            return total;
        }
    }

    public static final class DadosAluno {
        public final int idAluno;
        public final String nomeAluno;
        public final List<Materia> materias;

        public DadosAluno(int idAluno, String nomeAluno, List<Materia> materias) {
            this.idAluno = idAluno;
            this.nomeAluno = nomeAluno;
            this.materias = Collections.unmodifiableList(new ArrayList<>(materias));
        }

        public Materia buscarPorId(int id) {
            for (Materia m : materias) if (m.id == id) return m;
            return null;
        }
    }

    public DadosAluno carregarDados(int idAluno) throws SQLException {
        if (idAluno <= 0) throw new SQLException("Aluno não identificado. Entre pela tela de login.");

        try (Connection con = ConexaoMySQL.obterConexao()) {
            if (con == null) throw new SQLException("Não foi possível conectar ao MySQL.");

            String nome = "Aluno";
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT nome FROM usuarios WHERE idUSUARIOS = ? AND perfil = 'ALUNO'")) {
                ps.setInt(1, idAluno);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) throw new SQLException("Conta de aluno não encontrada.");
                    nome = rs.getString("nome");
                }
            }

            Map<Integer, Materia> materias = new LinkedHashMap<>();
            String sqlTurmas =
                    "SELECT t.idTURMA, t.nome_materia, t.nome_turma, t.instituicao, "
                    + "t.carga_horaria, t.limites_faltas, u.nome AS nome_professor "
                    + "FROM turma t LEFT JOIN usuarios u ON u.idUSUARIOS = t.professor_id "
                    + "WHERE EXISTS (SELECT 1 FROM matricula m "
                    + "WHERE m.turma_id = t.idTURMA AND m.aluno_id = ?) "
                    + "ORDER BY t.nome_materia, t.nome_turma";

            try (PreparedStatement ps = con.prepareStatement(sqlTurmas)) {
                ps.setInt(1, idAluno);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int carga = rs.getInt("carga_horaria");
                        if (rs.wasNull()) carga = 0;
                        Materia m = new Materia(rs.getInt("idTURMA"),
                                rs.getString("nome_materia"), rs.getString("nome_turma"),
                                rs.getString("instituicao"), rs.getString("nome_professor"),
                                carga, rs.getBigDecimal("limites_faltas"));
                        materias.put(m.id, m);
                    }
                }
            }

            String sqlGrade = "SELECT g.turma_id, g.dia_semana, g.peso_aula "
                    + "FROM grade g WHERE EXISTS (SELECT 1 FROM matricula m "
                    + "WHERE m.turma_id = g.turma_id AND m.aluno_id = ?)";
            try (PreparedStatement ps = con.prepareStatement(sqlGrade)) {
                ps.setInt(1, idAluno);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Materia m = materias.get(rs.getInt("turma_id"));
                        if (m == null) continue;
                        DayOfWeek dia = converterDia(rs.getString("dia_semana"));
                        int qtd = rs.getInt("peso_aula");
                        if (dia != null && qtd > 0) m.grade.merge(dia, qtd, Integer::sum);
                    }
                }
            }

            String sqlChamadas = "SELECT r.turma_id, r.data_aula, r.quantidade_aula, p.status_presenca "
                    + "FROM registros r LEFT JOIN `presença` p "
                    + "ON p.registro_id = r.idREGISTROS AND p.aluno_id = ? "
                    + "WHERE r.data_aula <= CURRENT_DATE() AND EXISTS "
                    + "(SELECT 1 FROM matricula m WHERE m.turma_id = r.turma_id AND m.aluno_id = ?) "
                    + "ORDER BY r.data_aula DESC, r.idREGISTROS DESC";
            try (PreparedStatement ps = con.prepareStatement(sqlChamadas)) {
                ps.setInt(1, idAluno);
                ps.setInt(2, idAluno);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Materia m = materias.get(rs.getInt("turma_id"));
                        java.sql.Date data = rs.getDate("data_aula");
                        if (m == null || data == null) continue;
                        int qtd = rs.getInt("quantidade_aula");
                        m.chamadas.add(new Chamada(data.toLocalDate(), qtd,
                                rs.getString("status_presenca")));
                    }
                }
            }
            return new DadosAluno(idAluno, nome, new ArrayList<>(materias.values()));
        }
    }

    public String buscarNomeAluno(int aluno) {
        try { return carregarDados(aluno).nomeAluno; }
        catch (SQLException e) { return null; }
    }

    public String buscarMaterias(int aluno) {
        try {
            DadosAluno d = carregarDados(aluno);
            if (d.materias.isEmpty()) return "Nenhuma matrícula encontrada.";
            StringBuilder s = new StringBuilder();
            for (Materia m : d.materias) s.append("• ").append(m.nome)
                    .append(" — turma ").append(m.turma).append(" ( ")
                    .append(m.instituicao).append(" )\n");
            return s.toString().trim();
        } catch (SQLException e) { return "Não consegui consultar suas matérias: " + e.getMessage(); }
    }

    public String consultarFaltas(int aluno) { return resumo(aluno, false); }
    public String consultarSituacao(int aluno) { return resumo(aluno, true); }

    private String resumo(int aluno, boolean incluirSituacao) {
        try {
            DadosAluno d = carregarDados(aluno);
            if (d.materias.isEmpty()) return "Nenhuma matrícula encontrada.";
            StringBuilder s = new StringBuilder();
            for (Materia m : d.materias) {
                s.append(m.nome).append(": ").append(m.faltas()).append(" falta(s)");
                if (m.limiteAulas >= 0) {
                    s.append(" de ").append(m.limiteAulas).append(" permitidas");
                    if (incluirSituacao) {
                        if (m.faltas() > m.limiteAulas) s.append(" — ACIMA do limite cadastrado");
                        else if (m.faltas() == m.limiteAulas) s.append(" — NO limite cadastrado");
                        else if (m.percentualDoLimite() >= 70) s.append(" — ATENÇÃO: 70% ou mais do limite usado");
                    }
                }
                if (m.chamadas.isEmpty()) s.append(" (sem chamadas lançadas)");
                s.append("\n");
            }
            return s.toString().trim();
        } catch (SQLException e) { return "Não consegui consultar suas faltas: " + e.getMessage(); }
    }

    public String consultarAulasAmanha(int aluno) { return consultarDia(aluno, LocalDate.now().plusDays(1)); }

    public String consultarAulasDiaSemana(int aluno, DayOfWeek dia) {
        try {
            DadosAluno d = carregarDados(aluno);
            StringBuilder s = new StringBuilder();
            for (Materia m : d.materias) {
                if (m.aulasNoDia(dia) > 0) s.append(m.nome).append(": ")
                        .append(m.aulasNoDia(dia)).append(" aula(s)\n");
            }
            return s.length() == 0 ? "Nenhuma aula cadastrada para " + nomeDia(dia) + "."
                    : "Na " + nomeDia(dia) + ", sua grade prevê:\n" + s.toString().trim();
        } catch (SQLException e) { return "Não consegui consultar sua grade: " + e.getMessage(); }
    }

    public String consultarGradeSemanal(int aluno) {
        try {
            DadosAluno d = carregarDados(aluno);
            StringBuilder s = new StringBuilder();
            for (Materia m : d.materias) {
                s.append(m.nome).append(": ");
                for (DayOfWeek dia : DayOfWeek.values())
                    if (m.aulasNoDia(dia) > 0) s.append(nomeDia(dia)).append(" (")
                            .append(m.aulasNoDia(dia)).append("), ");
                s.append("\n");
            }
            return s.length() == 0 ? "Nenhuma grade cadastrada." : s.toString().trim();
        } catch (SQLException e) { return "Não consegui consultar sua grade: " + e.getMessage(); }
    }

    public String consultarProximaAula(int aluno) {
        try {
            DadosAluno d = carregarDados(aluno);
            for (int i = 1; i <= 7; i++) {
                LocalDate data = LocalDate.now().plusDays(i);
                StringBuilder s = new StringBuilder();
                for (Materia m : d.materias) {
                    int n = m.aulasNoDia(data.getDayOfWeek());
                    if (n > 0) s.append(m.nome).append(": ").append(n).append(" aula(s)\n");
                }
                if (s.length() > 0) return "Próximo dia com aulas na grade: " + data
                        + "\n" + s.toString().trim() + "\nNão considera feriados e cancelamentos.";
            }
            return "Nenhuma próxima aula identificada na grade semanal.";
        } catch (SQLException e) { return "Não consegui consultar sua grade: " + e.getMessage(); }
    }

    private String consultarDia(int aluno, LocalDate data) {
        try {
            DadosAluno d = carregarDados(aluno);
            StringBuilder s = new StringBuilder();
            for (Materia m : d.materias) {
                int n = m.aulasNoDia(data.getDayOfWeek());
                if (n <= 0) continue;
                s.append(m.nome).append(": ").append(n).append(" aulas previstas");
                if (m.limiteAulas >= 0) s.append("; caso falte a todas, serão ")
                        .append(m.faltas() + n).append(" de ").append(m.limiteAulas)
                        .append(" faltas permitidas");
                s.append("\n");
            }
            return s.length() == 0 ? "Nenhuma aula na grade para " + data + "."
                    : "Grade prevista para " + data + ":\n" + s.toString().trim()
                    + "\nA grade não confirma feriados ou cancelamentos.";
        } catch (SQLException e) { return "Não consegui consultar sua grade: " + e.getMessage(); }
    }

    public static String nomeDia(DayOfWeek dia) {
        switch (dia) {
            case MONDAY: return "segunda-feira";
            case TUESDAY: return "terça-feira";
            case WEDNESDAY: return "quarta-feira";
            case THURSDAY: return "quinta-feira";
            case FRIDAY: return "sexta-feira";
            case SATURDAY: return "sábado";
            default: return "domingo";
        }
    }

    public static DayOfWeek converterDia(String texto) {
        if (texto == null) return null;
        String s = java.text.Normalizer.normalize(texto.toLowerCase(Locale.ROOT),
                java.text.Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        if (s.startsWith("segunda")) return DayOfWeek.MONDAY;
        if (s.startsWith("terca")) return DayOfWeek.TUESDAY;
        if (s.startsWith("quarta")) return DayOfWeek.WEDNESDAY;
        if (s.startsWith("quinta")) return DayOfWeek.THURSDAY;
        if (s.startsWith("sexta")) return DayOfWeek.FRIDAY;
        if (s.startsWith("sabado")) return DayOfWeek.SATURDAY;
        if (s.startsWith("domingo")) return DayOfWeek.SUNDAY;
        return null;
    }
}
