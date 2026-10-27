package modelo.faltas;
import java.sql.Date;

public class Registro {
    private int id;
    private int turmaId;
    private Date dataAula;
    private int quantidadeAula;

    public Registro() {
    }

    public Registro(int id, int turmaId, Date dataAula) {
        this.id = id;
        this.turmaId = turmaId;
        this.dataAula = dataAula;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getTurmaId() { return turmaId; }
    public void setTurmaId(int turmaId) { this.turmaId = turmaId; }

    public Date getDataAula() { return dataAula; }
    public void setDataAula(Date dataAula) { this.dataAula = dataAula; }
    
    public int getQuantidadeAula() {return quantidadeAula; }
    public void setQuantidadeAula (int quantidadeAula) { this.quantidadeAula = quantidadeAula; }
}
