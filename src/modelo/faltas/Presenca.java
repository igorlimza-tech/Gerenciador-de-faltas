package modelo.faltas;

public class Presenca {
    private int id;
    private int registroId;
    private int alunoId;
    private String statusPresenca;

    public Presenca() {
    }

    public Presenca(int id, int registroId, int alunoId, String statusPresenca) {
        this.id = id;
        this.registroId = registroId;
        this.alunoId = alunoId;
        this.statusPresenca = statusPresenca;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getRegistroId() { return registroId; }
    public void setRegistroId(int registroId) { this.registroId = registroId; }

    public int getAlunoId() { return alunoId; }
    public void setAlunoId(int alunoId) { this.alunoId = alunoId; }

    public String getStatusPresenca() { return statusPresenca; }
    public void setStatusPresenca(String statusPresenca) { this.statusPresenca = statusPresenca; }
}
