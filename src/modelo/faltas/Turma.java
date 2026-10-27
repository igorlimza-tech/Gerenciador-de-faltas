package modelo.faltas;

public class Turma {
    private int id;
    private int professorId;
    private String emailInstitucional;
    private String instituicao;
    private String nomeTurma;
    private String nomeMateria;
    private String codigoAcesso;
    private String periodo;
    
    public Turma() {
    }

    public Turma(int id, int professorId, String emailInstitucional, String instituicao, String nomeTurma, String nomeMateria, String codigoAcesso) {
        this.id = id;
        this.professorId = professorId;
        this.emailInstitucional = emailInstitucional;
        this.instituicao = instituicao;
        this.nomeTurma = nomeTurma;
        this.nomeMateria = nomeMateria;
        this.codigoAcesso = codigoAcesso;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getProfessorId() { return professorId; }
    public void setProfessorId(int professorId) { this.professorId = professorId; }

    public String getEmailInstitucional() { return emailInstitucional; }
    public void setEmailInstitucional(String emailInstitucional) { this.emailInstitucional = emailInstitucional; }

    public String getInstituicao() { return instituicao; }
    public void setInstituicao(String instituicao) { this.instituicao = instituicao; }

    public String getNomeTurma() { return nomeTurma; }
    public void setNomeTurma(String nomeTurma) { this.nomeTurma = nomeTurma; }

    public String getNomeMateria() { return nomeMateria; }
    public void setNomeMateria(String nomeMateria) { this.nomeMateria = nomeMateria; }

    public String getCodigoAcesso() { return codigoAcesso; }
    public void setCodigoAcesso(String codigoAcesso) { this.codigoAcesso = codigoAcesso; }
    
    public String getPeriodo() { return periodo; }
    public void setPeriodo(String periodo) {this.periodo = periodo; }
}