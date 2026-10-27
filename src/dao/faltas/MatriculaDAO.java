
package dao.faltas;

import conexao.ConexaoMySQL;
import modelo.faltas.Matricula;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class MatriculaDAO {

    private String ultimoErro = "";

    public String getUltimoErro() {
        return ultimoErro;
    }

    public boolean cadastrarMatricula(Matricula matricula) {
        ultimoErro = "";

        String sql = "INSERT INTO matricula "
                + "(aluno_id, turma_id, ra_aluno, "
                + "email_institucional, usuarios_idUSUARIOS, turma_idTURMA) "
                + "VALUES (?, ?, ?, "
                + "(SELECT email FROM usuarios WHERE idUSUARIOS = ?), ?, ?)";

        try (Connection conn = ConexaoMySQL.obterConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, matricula.getAlunoId());
            stmt.setInt(2, matricula.getTurmaId());
            stmt.setString(3, matricula.getRaAluno());
            stmt.setInt(4, matricula.getAlunoId());
            stmt.setInt(5, matricula.getAlunoId());
            stmt.setInt(6, matricula.getTurmaId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            ultimoErro = e.getMessage();
            System.err.println("Erro ao realizar matrícula: " + ultimoErro);
            e.printStackTrace();
            return false;
        }
    }
}
