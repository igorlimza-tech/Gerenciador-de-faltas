package modelo.faltas;

public class Grade {
    private int id;
    private int turmaId;
    private String diaSemana;
    private int pesoAula;

    public Grade() {
    }

    public Grade(int id, int turmaId, String diaSemana, int pesoAula) {
        this.id = id;
        this.turmaId = turmaId;
        this.diaSemana = diaSemana;
        this.pesoAula = pesoAula;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getTurmaId() { return turmaId; }
    public void setTurmaId(int turmaId) { this.turmaId = turmaId; }

    public String getDiaSemana() { return diaSemana; }
    public void setDiaSemana(String diaSemana) { this.diaSemana = diaSemana; }

    public int getPesoAula() { return pesoAula; }
    public void setPesoAula(int pesoAula) { this.pesoAula = pesoAula; }
}