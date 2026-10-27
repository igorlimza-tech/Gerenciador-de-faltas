# Presente! — Gerenciador de Faltas

Sistema desktop em **Java (Swing)** com banco **MySQL** para controle de presença e faltas em instituições de ensino. O **professor** cria turmas e faz a chamada; o **aluno** entra na turma com um código, acompanha suas faltas por disciplina e conversa com um **assistente** que responde dúvidas sobre a própria situação acadêmica.

---

## Funcionalidades

### Autenticação
- Criação de conta com escolha de perfil (**Aluno** ou **Professor**).
- Login e redirecionamento para o painel correspondente ao perfil.

### Painel do Professor
- Criação de turmas (instituição, curso, matéria, turma, carga horária e percentual máximo de faltas).
- Definição da **grade semanal** (dias da semana e quantidade de aulas em cada dia).
- Geração de um **código de acesso** para compartilhar com os alunos.
- Realização da **chamada**, registrando presença ou falta de cada aluno por aula.

### Painel do Aluno
- Entrada em novas turmas usando o código fornecido pelo professor (matrícula).
- Visão geral de todas as disciplinas com faltas, presenças e aulas restantes.
- **Histórico detalhado** por disciplina, com cada chamada registrada.
- Indicação de quanto do limite de faltas já foi consumido.

### Assistente
- Chat integrado ao painel do aluno.
- **Assistente local** (`Inteligente`): responde perguntas como "quantas faltas eu tenho?", "tenho aula amanhã?", "qual minha próxima aula?" e "posso faltar?", consultando o banco de dados.
- **Integração com a API do Google Gemini** para perguntas gerais (opcional, ativada por variável de ambiente).

### Cálculo de carga horária
Implementado em `CalculoCargaHoraria`:
- 1 aula = **50 minutos**.
- Total de aulas = carga horária (em horas) convertida em minutos ÷ 50, arredondada para cima.
- Limite de faltas = total de aulas × percentual permitido (arredondado para baixo).
- Estimativa de semanas de curso com base nas aulas por semana.

---

## Tecnologias

| Item | Uso |
|---|---|
| Java 21 | Linguagem principal |
| Java Swing | Interface gráfica |
| MySQL | Banco de dados |
| MySQL Connector/J | Driver JDBC |
| Google Gemini API | Respostas gerais do assistente (opcional) |
| Eclipse IDE | Ambiente de desenvolvimento do projeto |

---

## Arquitetura

O projeto segue uma organização em camadas, separada por pacotes:

```
src/
├── executavel/faltas/   # Main — ponto de entrada (abre a TelaLogin)
├── view/faltas/         # Telas Swing (Login, Cadastro, Painéis, Chamada, Histórico, Assistente...)
├── modelo/faltas/       # Entidades: Usuario, Aluno, Professor, Turma, Matricula, Grade, Registro, Presenca
├── dao/faltas/          # Acesso a dados via JDBC: TurmaDAO, MatriculaDAO, AssistenteDAO
├── conexao/             # ConexaoMySQL — configuração da conexão
├── util/faltas/         # CalculoCargaHoraria — regras de cálculo
├── assistente/local/    # Inteligente — assistente que interpreta perguntas do aluno
└── api/gemini/          # GeminiService — cliente HTTP da API Gemini
```

### Tabelas utilizadas pelo sistema
`usuarios` (perfil ALUNO ou PROFESSOR), `turma`, `matricula`, `grade`, `registros` (chamadas por data) e `presença` (status de cada aluno em cada chamada). O script completo está em `database/schema.sql`.

---

## Como executar

### 1. Pré-requisitos
- JDK 21
- MySQL Server em execução
- [MySQL Connector/J](https://dev.mysql.com/downloads/connector/j/) (arquivo `.jar`)
- Eclipse IDE (recomendado)

### 2. Banco de dados
O script com a estrutura completa do banco está em [`database/schema.sql`](database/schema.sql) (somente estrutura, sem dados). Para criar tudo:

```bash
mysql -u root -p < database/schema.sql
```

Ou, no MySQL Workbench: `File > Open SQL Script`, selecione o `schema.sql` e execute. O script cria o banco `sistemadefaltas` e as tabelas. A conexão padrão do sistema é:

```
jdbc:mysql://localhost:3306/SISTEMADEFALTAS
usuário: root
```

### 3. Variáveis de ambiente

| Variável | Obrigatória | Descrição |
|---|---|---|
| `DB_SENHA` | Sim | Senha do usuário `root` do MySQL |
| `GEMINI_API_KEY` | Não | Chave da API do Gemini (habilita respostas gerais no assistente) |
| `GEMINI_MODEL` | Não | Modelo do Gemini (padrão: `gemini-2.5-flash`) |

> A senha do banco **não fica no código**. Nunca envie senhas ou chaves de API para o repositório.

### 4. Rodando no Eclipse
1. `File > Import > Existing Projects into Workspace` e selecione a pasta do projeto.
2. Em `Build Path > Configure Build Path > Libraries`, ajuste o caminho do `mysql-connector-j.jar` para o da sua máquina.
3. Configure as variáveis de ambiente em `Run Configurations > Environment`.
4. Execute a classe `executavel.faltas.Main`.

Para testar apenas a conexão com o banco, execute `conexao.ConexaoMySQL`.

---

## Fluxo de uso

1. O professor cria uma conta, cadastra a turma com a grade semanal e recebe o código de acesso.
2. O aluno cria uma conta e usa o código para se matricular na turma.
3. A cada aula, o professor faz a chamada.
4. O aluno acompanha faltas, presenças e o limite permitido no painel, e tira dúvidas com o assistente.

---

## Autor

**Igor** — [@igorlimza-tech](https://github.com/igorlimza-tech)
