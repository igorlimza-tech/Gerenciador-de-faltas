package assistente.local;
import dao.faltas.AssistenteDAO;
import dao.faltas.AssistenteDAO.Chamada;
import dao.faltas.AssistenteDAO.DadosAluno;
import dao.faltas.AssistenteDAO.Materia;
import java.text.Normalizer;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Inteligente {

    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Locale BR = Locale.forLanguageTag("pt-BR");

    public static final class Resposta {
        public final boolean reconhecida;
        public final String mensagem;
        public final Integer idMateria;
        public final String perguntaPendente;
        public final String ultimaIntencao;

        private Resposta(boolean reconhecida, String mensagem, Integer idMateria,
                         String perguntaPendente, String ultimaIntencao) {
            this.reconhecida = reconhecida;
            this.mensagem = mensagem;
            this.idMateria = idMateria;
            this.perguntaPendente = perguntaPendente;
            this.ultimaIntencao = ultimaIntencao;
        }
    }

    private Resposta ok(String resposta, Integer id, String assunto) {
        return new Resposta(true, resposta, id, null, assunto);
    }

    private Resposta pedirMateria(String pergunta, DadosAluno dados, String assunto, Integer anterior) {
        StringBuilder s = new StringBuilder("De qual matéria você quer saber? Pode escrever só o nome:\n");
        for (Materia m : dados.materias) {
            s.append("• ").append(m.nome);
            if (m.turma != null && !m.turma.isBlank()) s.append(" (turma ").append(m.turma).append(")");
            s.append("\n");
        }
        return new Resposta(true, s.toString().trim(), anterior, pergunta, assunto);
    }

    public Resposta responderAcademico(String pergunta, DadosAluno dados,
                                      Integer materiaAnterior, String perguntaPendente,
                                      String ultimaIntencao) {
        String normal = normalizar(pergunta);
        if (dados.materias.isEmpty()) {
            if (ehPerguntaAcademica(normal)) {
                return ok("Ainda não encontrei matérias vinculadas à sua conta. "
                        + "Entre em uma turma usando o código fornecido pelo professor.", null, "MATERIAS");
            }
            return new Resposta(false, "", materiaAnterior, perguntaPendente, ultimaIntencao);
        }

        //se ficou uma pergunta aguardando, o aluno pode dizer apenas "banco de dados".
        if (perguntaPendente != null && !perguntaPendente.isBlank()
                && (!localizarMaterias(normal, dados).isEmpty() || contem(normal, "todas", "todas as materias"))) {
            pergunta = perguntaPendente + " " + pergunta;
            normal = normalizar(pergunta);
        }

        //permite "e em POO?" depois de "quantas faltas tenho em banco de dados?".
        if (comecaComoContinuacao(normal) && !ultimaVazia(ultimaIntencao)
                && !contem(normal, "falta", "frequencia", "presenca", "aula", "chamada", "limite", "risco")) {
            pergunta = prefixoDaIntencao(ultimaIntencao) + " " + pergunta;
            normal = normalizar(pergunta);
        }

        //regras e justificativas institucionais nao constam do banco.
        if (contem(normal, "atestado", "justificar", "justificativa", "abonar", "abonada",
                "abono", "compensacao de falta", "procedimento da faculdade", "regra academica",
                "regulamento", "documentos para falta", "falta justificada")) {
            return ok("A aceitação de atestado, justificativa ou abono depende das regras da sua "
                    + "instituição. O Presente! registra apenas as chamadas e o status de presença; "
                    + "ele não confirma solicitações ou abonos. Consulte a secretaria acadêmica "
                    + "ou a coordenação para saber o procedimento e os prazos.", materiaAnterior, "REGRAS");
        }

        List<Materia> citadas = localizarMaterias(normal, dados);
        if (citadas.size() > 1 && !contem(normal, "todas", "todas as", "minhas materias")) {
            return pedirMateria(pergunta, dados, ultimaIntencao, materiaAnterior);
        }
        Materia especifica = citadas.isEmpty() ? null : citadas.get(0);
        boolean geral = contem(normal, "todas", "todas as", "geral", "no total", "em cada", "minhas materias", "minhas turmas",
                "alguma materia", "qual materia", "quais materias", "mais faltas", "mais falta");

        if (especifica == null && materiaAnterior != null && !geral) {
            especifica = dados.buscarPorId(materiaAnterior);
        }

        Integer atual = especifica == null ? materiaAnterior : Integer.valueOf(especifica.id);
        boolean perguntaSoltaMateria = !citadas.isEmpty()
                && !contem(normal, "falta", "frequencia", "presenca", "aula", "chamada", "horario",
                "grade", "limite", "risco", "reprov", "seman", "mes", "dia", "status", "quanto", "professor");
        if (perguntaSoltaMateria) {
            return ok("Certo! Vamos falar de " + citadas.get(0).nome + ". "
                    + "Você quer consultar faltas, frequência, chamadas ou a grade de aulas?",
                    citadas.get(0).id, "SELECAO");
        }

        if (contem(normal, "meu nome", "como me chamo")) {
            return ok("Seu nome cadastrado é " + dados.nomeAluno + ".", atual, "NOME");
        }

        //especialidades primeiro: evita "tive falta hoje" cair na contagem geral.
        if (contem(normal, "resumo semanal", "resumo da semana", "essa semana", "esta semana",
                "semana passada", "resumo mensal", "resumo do mes", "este mes", "mes passado",
                "panorama semanal", "panorama mensal")) {
            boolean mes = contem(normal, "mes", "mensal");
            boolean passado = contem(normal, "passad");
            return ok(resumoPeriodo(dados, especifica, mes, passado), atual, "RESUMO");
        }

        if (contem(normal, "mais faltas", "maior numero de faltas", "mais faltei")) {
            return ok(maisFaltas(dados), atual, "MAIS_FALTAS");
        }

        if (contem(normal, "ultima falta", "ultima vez que faltei", "ultimo dia que faltei",
                "ultimo dia que levei falta", "quando faltei por ultimo")) {
            return ok(ultimaFalta(dados, especifica), atual, "ULTIMA_FALTA");
        }

        if (contem(normal, "quais dias eu faltei", "dias que faltei", "datas de faltas",
                "quando levei falta", "historico de faltas", "dias levei falta")
                || (contem(normal, "em quais dias") && contem(normal, "falta", "faltei"))) {
            if (especifica == null && dados.materias.size() > 1 && !geral)
                return pedirMateria(pergunta, dados, "DATAS", atual);
            return ok(datasDeFaltas(dados, especifica), atual, "DATAS");
        }

        if (contem(normal, "professor ja fez a chamada", "chamada de hoje", "chamada hoje",
                "chamada foi feita", "chamada foi realizada", "fez a chamada", "lancou a chamada",
                "lancou minha presenca", "registrou minha presenca", "lancou presenca")) {
            if (especifica == null && dados.materias.size() > 1 && !geral)
                return pedirMateria(pergunta, dados, "CHAMADA_HOJE", atual);
            return ok(situacaoDia(dados, especifica, LocalDate.now(), true), atual, "CHAMADA_HOJE");
        }

        if (contem(normal, "tive falta hoje", "faltei hoje", "falta hoje", "fiquei com falta hoje",
                "levei falta hoje", "estou com falta hoje", "falta ontem", "faltei ontem", "faltou hoje",
                "tive falta", "levei falta", "fiquei com falta")) {
            if (especifica == null && dados.materias.size() > 1 && !geral)
                return pedirMateria(pergunta, dados, "HOJE", atual);
            LocalDate data = contem(normal, "ontem") ? LocalDate.now().minusDays(1) : LocalDate.now();
            return ok(situacaoDia(dados, especifica, data, false), atual, "HOJE");
        }

        if (contem(normal, "quantas chamadas", "numero de chamadas", "chamadas realizadas",
                "chamadas ja foram", "aulas registradas pelo professor")) {
            if (especifica == null && dados.materias.size() > 1 && !geral)
                return pedirMateria(pergunta, dados, "CHAMADAS", atual);
            return ok(quantidadeChamadas(dados, especifica), atual, "CHAMADAS");
        }

        if (contem(normal, "reprovado por falta", "reprovada por falta", "reprovei por falta",
                "reprovacao por falta", "em risco", "situacao critica", "situacao academica",
                "ultrapassei o limite")) {
            return ok(situacaoLimites(dados, especifica), atual, "RISCO");
        }

        if (contem(normal, "posso faltar", "pode faltar", "da para faltar", "se eu faltar",
                "consigo faltar", "faltar hoje", "faltar amanha", "seguro faltar")) {
            if (especifica == null && dados.materias.size() > 1 && !geral)
                return pedirMateria(pergunta, dados, "PODE_FALTAR", atual);
            LocalDate data = extrairData(normal);
            if (data == null) {
                DayOfWeek dia = extrairDia(normal);
                if (dia != null) data = proximaOcorrencia(dia);
            }
            //sem dia informado, o aluno quer a margem geral, não uma previsão para hoje!!!
            if (data == null && extrairDia(normal) == null) {
                return ok(limites(dados, especifica), atual, "LIMITE");
            }
            if (data == null) data = LocalDate.now();
            return ok(possoFaltar(dados, especifica, data), atual, "PODE_FALTAR");
        }

        if (contem(normal, "faltas restantes", "faltas ainda posso", "quantas faltas ainda",
                "quanto ainda posso faltar", "faltam para o limite", "falta muito para",
                "restam de faltas", "margem de faltas", "limite de faltas", "limite dessa materia",
                "atingir o limite", "quanto posso faltar")) {
            if (especifica == null && dados.materias.size() > 1 && !geral)
                return pedirMateria(pergunta, dados, "LIMITE", atual);
            return ok(limites(dados, especifica), atual, "LIMITE");
        }

        if (contem(normal, "frequencia", "percentual de presenca", "porcentagem de presenca",
                "porcentagem de frequencia", "percentual de frequencia")) {
            if (especifica == null && dados.materias.size() > 1 && !geral)
                return pedirMateria(pergunta, dados, "FREQUENCIA", atual);
            return ok(frequencia(dados, especifica), atual, "FREQUENCIA");
        }

        DayOfWeek dia = extrairDia(normal);
        boolean grade = contem(normal, "aula", "grade", "horario", "materia de", "proxima", "quais dias",
                "tenho", "segunda", "terca", "quarta", "quinta", "sexta", "sabado", "domingo");
        if (grade && (dia != null || contem(normal, "hoje", "amanha", "proxima aula", "horario", "grade", "quais dias")
                || (especifica != null && contem(normal, "tenho aula", "aulas de", "aula de")))) {
            if (dia == null) {
                LocalDate data = extrairData(normal);
                if (data != null) dia = data.getDayOfWeek();
            }
            if (especifica == null && dados.materias.size() > 1 && !geral
                    && !contem(normal, "hoje", "amanha", "proxima aula", "semana", "quais aulas", "aula na")) {
                return pedirMateria(pergunta, dados, "GRADE", atual);
            }
            return ok(grade(dados, especifica, dia, normal), atual, "GRADE");
        }

        if (contem(normal, "quantas faltas", "minhas faltas", "falta em", "faltas tenho",
                "como estao minhas faltas", "quanto faltei", "falta(s)", "tenho falta", "tenho faltas", "faltas na")) {
            if (especifica == null && dados.materias.size() > 1 && !geral)
                return pedirMateria(pergunta, dados, "FALTAS", atual);
            return ok(faltas(dados, especifica), atual, "FALTAS");
        }

        if (contem(normal, "minhas materias", "minhas turmas", "materias cadastradas",
                "turmas cadastradas", "estou matricul", "quais disciplinas", "quais materias")) {
            return ok(materias(dados), atual, "MATERIAS");
        }

        if (normal.equals("oi") || normal.startsWith("oi ") || normal.equals("ola")
                || normal.startsWith("ola ") || contem(normal, "bom dia", "boa tarde", "boa noite")) {
            return ok("Oi, " + primeiroNome(dados.nomeAluno) + "! Posso verificar suas faltas, "
                    + "frequência, grade e chamadas. Se quiser, me diga uma matéria para começarmos. 💜",
                    atual, "SAUDACAO");
        }

        return new Resposta(false, "", atual, null, ultimaIntencao);
    }

    private String prefixoDaIntencao(String ultimaIntencao) {
        switch (ultimaIntencao) {
            case "FALTAS": return "Quantas faltas tenho";
            case "FREQUENCIA": return "Qual minha frequência";
            case "LIMITE": return "Quantas faltas ainda posso ter";
            case "RISCO": return "Estou em risco de ultrapassar o limite";
            case "GRADE": return "Tenho aula";
            case "CHAMADAS": return "Quantas chamadas foram realizadas";
            default: return "";
        }
    }

    private boolean comecaComoContinuacao(String s) {
        return s.startsWith("e em ") || s.startsWith("e na ") || s.startsWith("e de ")
                || s.startsWith("e pra ") || s.startsWith("e sobre ");
    }

    private boolean ultimaVazia(String s) { return s == null || s.isBlank(); }

    private List<Materia> localizarMaterias(String normal, DadosAluno dados) {
        List<Materia> matches = new ArrayList<>();
        for (Materia m : dados.materias) {
            String nome = normalizar(m.nome);
            String sigla = siglaMateria(nome);
            if (normal.contains(nome)
                    || (nome.length() >= 4 && contemTokens(nome, normal))
                    || (sigla.length() >= 2 && contemTokens(sigla, normal))) {
                matches.add(m);
            }
        }
        if (matches.size() > 1) {
            List<Materia> turmaExata = new ArrayList<>();
            for (Materia m : matches) {
                String etiqueta = "turma " + normalizar(m.turma);
                if (!m.turma.isBlank() && normal.contains(etiqueta)) turmaExata.add(m);
            }
            if (!turmaExata.isEmpty()) return turmaExata;
        }
        return matches;
    }

    private String siglaMateria(String nome) {
        StringBuilder sigla = new StringBuilder();
        for (String palavra : nome.split("\\s+")) {
            if (palavra.isBlank() || palavra.equals("de") || palavra.equals("da")
                    || palavra.equals("do") || palavra.equals("das") || palavra.equals("dos")
                    || palavra.equals("e") || palavra.equals("a") || palavra.equals("o")) continue;
            sigla.append(palavra.charAt(0));
        }
        return sigla.toString();
    }

    private boolean contemTokens(String nome, String frase) {
        //tolerancia para apelidos de matérias sem supor sig n cadastrados.
        return frase.matches(".*\\b" + Pattern.quote(nome) + "\\b.*");
    }

    private String materias(DadosAluno d) {
        StringBuilder s = new StringBuilder("Você está matriculado(a) em:\n");
        for (Materia m : d.materias) {
            s.append("• ").append(m.nome).append(" — turma ").append(m.turma);
            if (!m.instituicao.isBlank()) s.append(" (" ).append(m.instituicao).append(")");
            s.append("\n");
        }
        return s.toString().trim();
    }

    private List<Materia> alvo(DadosAluno d, Materia m) {
        return m == null ? d.materias : java.util.Collections.singletonList(m);
    }

    private String faltas(DadosAluno d, Materia m) {
        StringBuilder s = new StringBuilder();
        for (Materia x : alvo(d, m)) {
            s.append(x.nome).append(": ").append(x.faltas()).append(" falta(s) registrada(s)");
            if (x.limiteAulas >= 0) s.append(" de ").append(x.limiteAulas).append(" permitidas");
            if (x.chamadas.isEmpty()) s.append(" — ainda não há chamadas");
            else if (x.aulasSemMarcacao() > 0) s.append(" — algumas chamadas estão sem marcação");
            s.append(".\n");
        }
        return s.toString().trim();
    }

    private String frequencia(DadosAluno d, Materia m) {
        StringBuilder s = new StringBuilder();
        for (Materia x : alvo(d, m)) {
            if (x.aulasRegistradas() <= 0) {
                s.append(x.nome).append(": ainda não dá para calcular a frequência. "
                        + "Não há presença/falta registrada.\n");
            } else {
                s.append(x.nome).append(": ").append(porcentagem(x.frequencia()))
                        .append(" de frequência nas ").append(x.aulasRegistradas())
                        .append(" aulas com presença/falta marcada (")
                        .append(x.presencas()).append(" presentes e ")
                        .append(x.faltas()).append(" faltas).\n");
                if (x.aulasSemMarcacao() > 0) s.append("Existem aulas sem marcação; o percentual é parcial.\n");
            }
        }
        return s.toString().trim();
    }

    private String limites(DadosAluno d, Materia m) {
        StringBuilder s = new StringBuilder();
        for (Materia x : alvo(d, m)) {
            if (x.limiteAulas < 0) {
                s.append(x.nome).append(": a carga horária ou o percentual limite não está cadastrado.\n");
                continue;
            }
            s.append(x.nome).append(": o limite cadastrado é ")
                    .append(x.limiteAulas).append(" faltas (")
                    .append(x.percentualLimite.stripTrailingZeros().toPlainString())
                    .append("% de ").append(x.totalAulas).append(" aulas).\n")
                    .append("Você já tem ").append(x.faltas()).append(" e restam ")
                    .append(x.restantes()).append(" até atingir o limite.\n");
            if (x.percentualDoLimite() >= 70 && x.faltas() <= x.limiteAulas)
                s.append("Atenção: você já usou ")
                        .append(porcentagem(x.percentualDoLimite()))
                        .append(" desse limite.\n");
            if (x.faltas() > x.limiteAulas)
                s.append("Atenção: o limite cadastrado foi ultrapassado. Consulte a instituição sobre a situação acadêmica.\n");
        }
        return s.toString().trim();
    }

    private String situacaoLimites(DadosAluno d, Materia m) {
        StringBuilder s = new StringBuilder();
        for (Materia x : alvo(d, m)) {
            if (x.limiteAulas < 0) {
                s.append(x.nome).append(": não há limite válido cadastrado.\n");
            } else if (x.faltas() > x.limiteAulas) {
                s.append(x.nome).append(": acima do limite (").append(x.faltas())
                        .append(" de ").append(x.limiteAulas).append("). Procure a secretaria "
                                + "para verificar o resultado acadêmico oficial.\n");
            } else if (x.faltas() == x.limiteAulas) {
                s.append(x.nome).append(": exatamente no limite (").append(x.faltas())
                        .append(" de ").append(x.limiteAulas).append(").\n");
            } else if (x.percentualDoLimite() >= 70) {
                s.append(x.nome).append(": alerta — ").append(porcentagem(x.percentualDoLimite()))
                        .append(" do limite utilizado (").append(x.faltas()).append(" de ")
                        .append(x.limiteAulas).append(").\n");
            } else {
                s.append(x.nome).append(": abaixo do limite (").append(x.faltas())
                        .append(" de ").append(x.limiteAulas).append(").\n");
            }
            if (x.chamadas.isEmpty()) s.append("Ainda não existem chamadas registradas.\n");
        }
        return s.toString().trim() + "\nA confirmação de reprovação ou abono depende das regras da instituição.";
    }

    private String possoFaltar(DadosAluno d, Materia m, LocalDate data) {
        StringBuilder s = new StringBuilder();
        for (Materia x : alvo(d, m)) {
            int previstas = x.aulasNoDia(data.getDayOfWeek());
            if (previstas == 0) {
                s.append(x.nome).append(": sua grade não prevê aulas em ").append(DATA.format(data)).append(".\n");
                continue;
            }
            s.append(x.nome).append(": há ").append(previstas)
                    .append(" aula(s) prevista(s) para ").append(DATA.format(data)).append(".\n");
            if (x.limiteAulas < 0) {
                s.append("Não consigo calcular a margem porque não há limite cadastrado.\n");
                continue;
            }
            int hipotetico = x.faltas() + previstas;
            s.append("Você tem ").append(x.faltas()).append(" falta(s). Se faltar a todas, "
                    + "a simulação vai para ").append(hipotetico).append(" de ")
                    .append(x.limiteAulas).append(" permitidas.\n");
            if (hipotetico > x.limiteAulas)
                s.append("Atenção: ultrapassaria o limite cadastrado.\n");
            else if (hipotetico == x.limiteAulas)
                s.append("Atenção: chegaria exatamente ao limite.\n");
            else s.append("Pela contagem, ainda ficaria abaixo do limite.\n");
            if (x.chamadasNoDia(data) > 0)
                s.append("Já existem chamadas lançadas nessa data. A simulação não desconta "
                        + "possíveis presenças/faltas já registradas no mesmo dia.\n");
        }
        return s.toString().trim() + "\nIsso não autoriza a ausência nem confirma feriados, cancelamentos ou abonos.";
    }

    private String situacaoDia(DadosAluno d, Materia m, LocalDate data, boolean verificarChamada) {
        StringBuilder s = new StringBuilder();
        for (Materia x : alvo(d, m)) {
            int chamadas = x.chamadasNoDia(data);
            s.append(x.nome).append(" — ").append(DATA.format(data)).append(": ");
            if (chamadas == 0) {
                s.append("não há chamada registrada nessa data.\n");
                continue;
            }
            int faltas = x.faltasNoDia(data);
            int presencas = x.presencasNoDia(data);
            int pendentes = x.naoMarcadasNoDia(data);
            s.append(chamadas).append(chamadas == 1 ? " chamada lançada" : " chamadas lançadas")
                    .append("; ").append(presencas).append(" aula(s) como presente e ")
                    .append(faltas).append(" como falta");
            if (pendentes > 0) s.append("; ").append(pendentes).append(" sem status definido");
            s.append(".\n");
        }
        return s.toString().trim();
    }

    private String ultimaFalta(DadosAluno d, Materia m) {
        LocalDate data = null;
        String nome = null;
        for (Materia x : alvo(d, m)) {
            LocalDate ultima = x.ultimaFalta();
            if (ultima != null && (data == null || ultima.isAfter(data))) {
                data = ultima;
                nome = x.nome;
            }
        }
        return data == null ? "Não encontrei nenhuma falta registrada nas chamadas consultadas."
                : "Sua última falta registrada foi em " + DATA.format(data) + ", na matéria " + nome + ".";
    }

    private String datasDeFaltas(DadosAluno d, Materia m) {
        StringBuilder s = new StringBuilder();
        for (Materia x : alvo(d, m)) {
            Map<LocalDate, Integer> datas = new TreeMap<>(Comparator.reverseOrder());
            for (Chamada c : x.chamadas) if (c.falta()) datas.merge(c.data, c.quantidade, Integer::sum);
            s.append(x.nome).append(":\n");
            if (datas.isEmpty()) s.append("• Nenhuma falta registrada.\n");
            else for (Map.Entry<LocalDate, Integer> e : datas.entrySet())
                s.append("• ").append(DATA.format(e.getKey())).append(": ")
                        .append(e.getValue()).append(" aula(s) de falta\n");
        }
        return s.toString().trim();
    }

    private String maisFaltas(DadosAluno d) {
        int max = -1;
        List<String> nomes = new ArrayList<>();
        for (Materia m : d.materias) {
            int f = m.faltas();
            if (f > max) { max = f; nomes.clear(); }
            if (f == max) nomes.add(m.nome);
        }
        if (max <= 0) return "Não encontrei faltas registradas nas suas matérias até agora.";
        return "Maior número de faltas registrado: " + max + " aula(s), em "
                + String.join(" e ", nomes) + ".";
    }

    private String quantidadeChamadas(DadosAluno d, Materia m) {
        StringBuilder s = new StringBuilder();
        for (Materia x : alvo(d, m)) {
            s.append(x.nome).append(": ").append(x.chamadas.size())
                    .append(" chamada(s) lançada(s)");
            if (x.aulasSemMarcacao() > 0) s.append("; existem registros sem presença/falta marcada");
            s.append(".\n");
        }
        return s.toString().trim();
    }

    private String grade(DadosAluno d, Materia m, DayOfWeek dia, String pergunta) {
        boolean exato = contem(pergunta, "horario", "que horas", "hora comeca", "hora termina");
        if (contem(pergunta, "proxima aula")) {
            for (int i = 1; i <= 7; i++) {
                DayOfWeek prox = LocalDate.now().plusDays(i).getDayOfWeek();
                for (Materia x : alvo(d, m)) {
                    if (x.aulasNoDia(prox) > 0) {
                        dia = prox;
                        i = 8;
                        break;
                    }
                }
            }
        }
        if (dia == null) {
            StringBuilder s = new StringBuilder();
            for (Materia x : alvo(d, m)) {
                s.append(x.nome).append(": ");
                for (DayOfWeek diaSem : DayOfWeek.values())
                    if (x.aulasNoDia(diaSem) > 0)
                        s.append(AssistenteDAO.nomeDia(diaSem)).append(" (")
                                .append(x.aulasNoDia(diaSem)).append(" aulas), ");
                s.append("\n");
            }
            return s.toString().trim() + avisoHorario(exato);
        }
        StringBuilder s = new StringBuilder();
        for (Materia x : alvo(d, m)) {
            int q = x.aulasNoDia(dia);
            if (q > 0) s.append("• ").append(x.nome).append(": ").append(q)
                    .append(q == 1 ? " aula" : " aulas").append(" de 50 minutos\n");
        }
        if (s.length() == 0) return "Sua grade não possui aulas cadastradas para "
                + AssistenteDAO.nomeDia(dia) + "." + avisoHorario(exato);
        return "Para " + AssistenteDAO.nomeDia(dia) + ", a grade prevê:\n" + s.toString().trim()
                + avisoHorario(exato) + "\nA grade não confirma feriados ou cancelamentos.";
    }

    private String avisoHorario(boolean exato) {
        return exato ? "\nO banco só guarda dia e quantidade de aulas; não guarda os horários exatos. "
                + "Para saber a hora de início e fim, confirme com o professor ou a instituição.\n" : "\n";
    }

    private String resumoPeriodo(DadosAluno d, Materia m, boolean mes, boolean passado) {
        LocalDate agora = LocalDate.now();
        LocalDate inicio;
        LocalDate fim;
        if (mes) {
            LocalDate referencia = passado ? agora.minusMonths(1) : agora;
            inicio = referencia.withDayOfMonth(1);
            fim = passado ? referencia.withDayOfMonth(referencia.lengthOfMonth()) : agora;
        } else {
            LocalDate semana = passado ? agora.minusWeeks(1) : agora;
            inicio = semana.minusDays(semana.getDayOfWeek().getValue() - 1);
            fim = passado ? inicio.plusDays(6) : agora;
        }
        StringBuilder s = new StringBuilder("Resumo " + (mes ? "mensal" : "semanal") + " ("
                + DATA.format(inicio) + " a " + DATA.format(fim) + "):\n");
        for (Materia x : alvo(d, m)) {
            int p = 0, f = 0, n = 0;
            for (Chamada c : x.chamadas) {
                if (c.data.isBefore(inicio) || c.data.isAfter(fim)) continue;
                if (c.presente()) p += c.quantidade;
                else if (c.falta()) f += c.quantidade;
                else n += c.quantidade;
            }
            s.append("• ").append(x.nome).append(": ").append(p)
                    .append(" aula(s) presentes, ").append(f).append(" faltas");
            if (n > 0) s.append(", ").append(n).append(" sem marcação");
            if (p + f > 0) s.append(" (frequência ")
                    .append(porcentagem(p * 100.0 / (p + f))).append(")");
            else s.append(" (sem presença/falta lançada no período)");
            s.append(".\n");
        }
        return s.toString().trim();
    }

    public String alertasIniciais(DadosAluno d) {
        StringBuilder s = new StringBuilder();
        for (Materia m : d.materias) {
            if (m.limiteAulas >= 0 && (m.faltas() > m.limiteAulas || m.percentualDoLimite() >= 70)) {
                s.append("⚠ ").append(m.nome).append(": ").append(m.faltas())
                        .append(" de ").append(m.limiteAulas).append(" faltas permitidas (")
                        .append(porcentagem(m.percentualDoLimite())).append(" do limite).\n");
            }
            if (m.presencasNoDia(LocalDate.now()) > 0) {
                s.append("✓ ").append(m.nome).append(": o professor registrou ")
                        .append(m.presencasNoDia(LocalDate.now()))
                        .append(" aula(s) como presente hoje.\n");
            }
        }
        return s.toString().trim();
    }

    private boolean ehPerguntaAcademica(String s) {
        return contem(s, "aula", "materia", "turma", "falt", "presen", "chamada", "frequencia", "professor", "grade");
    }

    private String primeiroNome(String nome) {
        return nome == null || nome.isBlank() ? "aluno" : nome.trim().split("\\s+")[0];
    }

    private String porcentagem(double numero) {
        return String.format(BR, "%.1f%%", numero);
    }

    private LocalDate extrairData(String texto) {
        if (contem(texto, "amanha")) return LocalDate.now().plusDays(1);
        if (contem(texto, "ontem")) return LocalDate.now().minusDays(1);
        if (contem(texto, "hoje")) return LocalDate.now();
        Matcher m = Pattern.compile("\\b(\\d{1,2})/(\\d{1,2})/(\\d{4})\\b").matcher(texto);
        if (m.find()) {
            try { return LocalDate.of(Integer.parseInt(m.group(3)),
                    Integer.parseInt(m.group(2)), Integer.parseInt(m.group(1))); }
            catch (java.time.DateTimeException e) { return null; }
        }
        return null;
    }

    private LocalDate proximaOcorrencia(DayOfWeek dia) {
        LocalDate data = LocalDate.now();
        for (int i = 0; i < 7; i++) {
            if (data.plusDays(i).getDayOfWeek() == dia) return data.plusDays(i);
        }
        return data;
    }

    private DayOfWeek extrairDia(String s) {
        if (contem(s, "amanha")) return LocalDate.now().plusDays(1).getDayOfWeek();
        if (contem(s, "hoje")) return LocalDate.now().getDayOfWeek();
        if (contem(s, "segunda")) return DayOfWeek.MONDAY;
        if (contem(s, "terca")) return DayOfWeek.TUESDAY;
        if (contem(s, "quarta")) return DayOfWeek.WEDNESDAY;
        if (contem(s, "quinta")) return DayOfWeek.THURSDAY;
        if (contem(s, "sexta")) return DayOfWeek.FRIDAY;
        if (contem(s, "sabado")) return DayOfWeek.SATURDAY;
        if (contem(s, "domingo")) return DayOfWeek.SUNDAY;
        return null;
    }

    private static String normalizar(String s) {
        return Normalizer.normalize(s == null ? "" : s.toLowerCase(BR), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").trim();
    }

    private boolean contem(String s, String... termos) {
        for (String t : termos) if (s.contains(t)) return true;
        return false;
    }

    public String responderLocal(String perguntaAluno) {
        String texto = normalizar(perguntaAluno);
        if (contem(texto, "normalizacao")) {
            return "Normalização organiza as tabelas do banco para reduzir duplicidade e melhorar a consistência dos dados.";
        }
        if (contem(texto, "matricular", "codigo de acesso", "entrar na turma")) {
            return "No painel do aluno, escolha 'Entrar em nova turma' e informe o código de seis dígitos fornecido pelo professor.";
        }
        if (contem(texto, "atestado", "abono", "justificar", "regulamento")) {
            return "Para atestados, justificativas ou regras de presença, consulte a secretaria acadêmica ou a coordenação da instituição.";
        }
        if (contem(texto, "oi", "ola", "bom dia", "boa tarde", "boa noite")) {
            return "Olá! Posso ajudar com dúvidas sobre o Presente!, turmas e frequência. Pergunte por uma matéria!";
        }
        return "Posso te ajudar com consultas das suas matérias, faltas, chamadas e frequência. "
                + "Se a dúvida for geral, tente reformular; o serviço de IA pode estar indisponível.";
    }
}
