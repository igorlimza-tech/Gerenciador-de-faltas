
package dao.faltas;

import conexao.ConexaoMySQL;
import modelo.faltas.Turma;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TurmaDAO {

    public Turma buscarPorCodigo(String codigo) {

        String sql = "SELECT idTURMA, instituicao, "
                + "nome_turma, nome_materia, nome_curso, codigo_acesso "
                + "FROM turma WHERE codigo_acesso = ? LIMIT 1";

        try (Connection conn = ConexaoMySQL.obterConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, codigo.trim());

            try (ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {

                    Turma turma = new Turma();

                    turma.setId(rs.getInt("idTURMA"));
                    turma.setInstituicao(rs.getString("instituicao"));
                    turma.setNomeTurma(rs.getString("nome_turma"));
                    turma.setNomeMateria(rs.getString("nome_materia"));
                    turma.setPeriodo(rs.getString("nome_curso"));
                    turma.setCodigoAcesso(rs.getString("codigo_acesso"));

                    return turma;
                }
            }

        } catch (SQLException e) {
            System.err.println(
                "Erro ao buscar turma: " + e.getMessage()
            );
            e.printStackTrace();
        }

        return null;
    }
}
