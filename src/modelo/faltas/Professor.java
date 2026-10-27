package modelo.faltas;

public class Professor {
    private int id;
    private String nome;
    private String registroFuncional;

    public Professor() {
    }

    public Professor(int id, String nome, String registroFuncional) {
        this.id = id;
        this.nome = nome;
        this.registroFuncional = registroFuncional;
    }
    
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getRegistroFuncional() { return registroFuncional; }
    public void setRegistroFuncional(String registroFuncional) { this.registroFuncional = registroFuncional; }
}