package api.gemini;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GeminiService {

    private final HttpClient cliente = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(8))
            .build();

    public String chamarGemini(String pergunta) throws Exception {

        String chave = System.getenv("GEMINI_API_KEY");

        if (chave == null || chave.isBlank()) {
            throw new IllegalStateException("GEMINI_API_KEY não configurada");
        }

        String modelo = System.getenv("GEMINI_MODEL");

        if (modelo == null || modelo.isBlank()) {
            modelo = "gemini-2.5-flash";
        }

        String url = "https://generativelanguage.googleapis.com/v1beta/models/"
                + modelo + ":generateContent";

        String orientacao = "Você é o Assistente do sistema acadêmico Presente!. "
                + "Responda em português brasileiro de forma clara e breve. "
                + "Não invente registros de faltas, presenças, horários, matérias ou dados pessoais. "
                + "Você não tem acesso direto ao banco de dados. "
                + "Para perguntas que dependam de dados do aluno, explique que é necessário consultar o painel. "
                + "Pergunta do aluno: " + pergunta;

        String json = "{\"contents\":[{\"parts\":[{\"text\":\""
                + escaparJson(orientacao) + "\"}]}]}";

        HttpRequest requisicao = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(20))
                .header("Content-Type", "application/json; charset=UTF-8")
                .header("x-goog-api-key", chave)
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> resposta = cliente.send(
                requisicao,
                HttpResponse.BodyHandlers.ofString()
        );

        if (resposta.statusCode() != 200) {
            throw new IllegalStateException(
                    "API retornou HTTP " + resposta.statusCode()
            );
        }

        Matcher matcher = Pattern.compile(
                "\\\"text\\\"\\s*:\\s*\\\"((?:\\\\.|[^\\\"\\\\])*)\\\""
        ).matcher(resposta.body());

        StringBuilder texto = new StringBuilder();

        while (matcher.find()) {
            if (texto.length() > 0) {
                texto.append("\n");
            }

            texto.append(desescaparJson(matcher.group(1)));
        }

        if (texto.length() == 0) {
            throw new IllegalStateException("Resposta sem texto");
        }

        return texto.toString().trim();
    }

    private String escaparJson(String texto) {
        return texto.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private String desescaparJson(String texto) {

        StringBuilder resultado = new StringBuilder();

        for (int i = 0; i < texto.length(); i++) {

            char c = texto.charAt(i);

            if (c == '\\' && i + 1 < texto.length()) {

                char proximo = texto.charAt(++i);

                switch (proximo) {
                    case 'n':
                        resultado.append('\n');
                        break;

                    case 'r':
                        resultado.append('\r');
                        break;

                    case 't':
                        resultado.append('\t');
                        break;

                    case '"':
                        resultado.append('"');
                        break;

                    case '\\':
                        resultado.append('\\');
                        break;

                    case 'u':
                        if (i + 4 < texto.length()) {

                            String hex = texto.substring(i + 1, i + 5);

                            try {
                                resultado.append(
                                        (char) Integer.parseInt(hex, 16)
                                );
                                i += 4;

                            } catch (NumberFormatException e) {
                                resultado.append("\\u").append(hex);
                                i += 4;
                            }

                        } else {
                            resultado.append("\\u");
                        }
                        break;

                    default:
                        resultado.append(proximo);
                }

            } else {
                resultado.append(c);
            }
        }

        return resultado.toString();
    }
}