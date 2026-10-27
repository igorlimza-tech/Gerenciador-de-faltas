
package conexao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexaoMySQL {

    // Configurações da conexão com MySQL
    private static final String URL =
            "jdbc:mysql://localhost:3306/SISTEMADEFALTAS"
            + "?useSSL=false"
            + "&allowPublicKeyRetrieval=true"
            + "&serverTimezone=UTC";

    private static final String USUARIO = "root";

    // A senha NÃO fica no código: defina a variável de ambiente DB_SENHA
    private static final String SENHA = System.getenv("DB_SENHA") != null
            ? System.getenv("DB_SENHA") : "";

    public static Connection obterConexao() {

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            Connection conexao = DriverManager.getConnection(
                    URL, USUARIO, SENHA
            );

            System.out.println(
                    "Conexão com o MySQL realizada com sucesso!"
            );

            return conexao;

        } catch (ClassNotFoundException e) {

            System.err.println(
                    "Driver do MySQL não encontrado!"
            );
            e.printStackTrace();
            return null;

        } catch (SQLException e) {

            System.err.println(
                    "Erro ao conectar ao banco de dados:"
            );
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {

        System.out.println("Testando a conexão com o MySQL...");

        try (Connection conexao = obterConexao()) {

            if (conexao != null) {
                System.out.println(
                    "Banco SISTEMADEFALTAS conectado com sucesso!"
                );
            } else {
                System.out.println(
                    "Falha na conexão com o MySQL."
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
