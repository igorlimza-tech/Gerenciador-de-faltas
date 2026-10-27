package modelo.faltas;

public class Matricula {
    private int id;
    private int alunoId;
    private int turmaId;
    private String raAluno;

    public Matricula() {
    }

    public Matricula(int id, int alunoId, int turmaId, String raAluno) {
        this.id = id;
        this.alunoId = alunoId;
        this.turmaId = turmaId;
        this.raAluno = raAluno;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getAlunoId() { return alunoId; }
    public void setAlunoId(int alunoId) { this.alunoId = alunoId; }

    public int getTurmaId() { return turmaId; }
    public void setTurmaId(int turmaId) { this.turmaId = turmaId; }

    public String getRaAluno() { return raAluno; }
    public void setRaAluno(String raAluno) { this.raAluno = raAluno; }
}
