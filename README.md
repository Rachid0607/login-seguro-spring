# Login Seguro

Sistema de login com cadastro, perfis de acesso, sessão e auditoria, construído em Java com Spring Boot, Thymeleaf e MongoDB Atlas. Fiz este projeto para a disciplina de Aplicativos Web da UMC, mas ele não fala de nenhum assunto específico: a ideia desde o início foi deixar a parte de login pronta para eu reaproveitar como base do login do meu PFC, só trocando nome, tema e os perfis.

## O que dá para fazer

O cadastro público em `/cadastro` sempre cria uma conta ALUNO. Os outros dois perfis só existem se um administrador criar.

- **ADMINISTRADOR**: lista todos os usuários, cria contas SECRETARIA ou ADMINISTRADOR, muda o perfil de qualquer usuário e ativa ou desativa contas. Não pode alterar o próprio perfil nem desativar a própria conta. Também vê os 100 eventos mais recentes na tela de auditoria.
- **SECRETARIA**: vê a lista de alunos, só para consulta, sem poder editar nada.
- **ALUNO**: vê os próprios dados (nome, e-mail e perfil) na tela "Meus dados".

## Tecnologias

- Java 21
- Spring Boot 4.1.1
- Spring Security 7.1.1 (via `spring-boot-starter-security`)
- Spring Session no MongoDB, pela biblioteca `mongodb-spring-session` 4.0.0, mantida pela MongoDB Inc. desde que o time do Spring passou essa integração para o próprio parceiro, a partir do Spring Session 4.0
- Thymeleaf 3.1.5, com o Thymeleaf Layout Dialect e o `thymeleaf-extras-springsecurity6`
- MongoDB Atlas, pelo driver Java 5.8.1
- Maven, pelo wrapper incluso no projeto (`mvnw` / `mvnw.cmd`)

## Como rodar na sua máquina

### Pré-requisitos

- Java 21 instalado
- Uma conta no MongoDB Atlas (a camada gratuita serve)
- Git, se for clonar o repositório

### Criar o cluster no Atlas

1. Crie um cluster (a camada gratuita M0 já é suficiente).
2. Em **Database Access**, crie um usuário de banco com permissão **readWrite** só no banco `login_seguro`. Não use o usuário administrador do cluster para a aplicação.
3. Em **Network Access**, use **Add Current IP Address** para liberar só o IP de onde a aplicação vai rodar.
4. Em **Database > Connect > Drivers**, escolha Java e copie a string de conexão, que começa com `mongodb+srv://`.

### Criar o .env

Copie o arquivo `.env.example` para um arquivo novo chamado `.env`, na raiz do projeto. Abra o `.env` e, para cada variável que for usar, tire o `#` do começo da linha: com o `#`, a linha fica comentada e o Spring simplesmente ignora o valor, mesmo que ele esteja escrito ali. É o erro mais fácil de cometer ao copiar o arquivo de exemplo.

As variáveis são:

- `MONGODB_URI`: a string de conexão que você copiou do Atlas. Obrigatória; sem ela, a aplicação nem sobe.
- `MONGODB_DATABASE`: nome do banco. Opcional, o padrão já é `login_seguro`.
- `ADMIN_NOME`, `ADMIN_EMAIL`, `ADMIN_SENHA`: dados do primeiro administrador. Só têm efeito enquanto não existir nenhum ADMINISTRADOR no banco; depois que o primeiro é criado, pode até remover essas três linhas.

O `.env` nunca deve ser commitado. Ele já está no `.gitignore`.

### Rodar

No Windows, pelo PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

No Linux ou Mac:

```bash
./mvnw spring-boot:run
```

Depois abra `http://localhost:8080` no navegador.

### Primeiro acesso

Se você definiu `ADMIN_NOME`, `ADMIN_EMAIL` e `ADMIN_SENHA` no `.env`, a aplicação cria essa conta automaticamente na primeira subida (isso é feito pelo `BootstrapAdministradorRunner`, que só roda se ainda não existir nenhum ADMINISTRADOR). Entre em `/login` com esse e-mail e senha.

## Quem acessa o quê

| Rota | Quem acessa |
|---|---|
| `/`, `/login`, `/cadastro`, `/tema` | Qualquer pessoa |
| `/painel` | Qualquer pessoa logada (só redireciona para a área do próprio perfil) |
| `/admin/**` | ADMINISTRADOR |
| `/secretaria/**` | SECRETARIA e ADMINISTRADOR |
| `/aluno/**` | ALUNO |

Essas regras estão centralizadas no `SecurityConfig`, e as ações mais sensíveis (criar usuário, mudar perfil, ativar ou desativar) são reforçadas de novo com `@PreAuthorize` nos services, caso alguém tente chamar o método direto por algum outro caminho.

## Temas

Tem dois temas: `padrao` (claro) e `escuro`. A troca é feita pelo botão ☀/☾, que aparece no topo das páginas internas e no canto do card de login e cadastro. Clicar nele manda um formulário POST para `/tema`, que grava um cookie chamado `tema` (válido por um ano) e volta para a página onde você estava. Sem esse cookie, o sistema usa a propriedade `app.tema`, que vem da variável de ambiente `APP_TEMA` (padrão: `padrao`).

Para criar um tema novo, copie a pasta `src/main/resources/static/temas/padrao` para uma pasta com o nome do novo tema e mude os valores das variáveis CSS lá dentro (cor, fonte, raio da borda, sombra). O `layout.css` não precisa mudar: ele só usa essas variáveis, nunca uma cor fixa. Se quiser que esse tema novo seja o padrão do sistema, defina `APP_TEMA` com o nome da pasta.

## Como o MongoDB é usado

| Coleção | Para que serve | Índices |
|---|---|---|
| `usuarios` | Conta de cada ADMINISTRADOR, SECRETARIA e ALUNO | único em `email` |
| `sessoes` | Sessão HTTP de quem está logado | criados pela própria biblioteca `mongodb-spring-session` |
| `tentativas_login` | Contador de tentativas de login erradas, por conta (em hash) ou por IP | TTL no campo `expiraEm`, renovado a cada tentativa errada |
| `auditoria` | Histórico dos eventos de acesso (login, logout, cadastro, mudanças de perfil) | nenhum além do `_id` |

A conexão usa as propriedades `spring.mongodb.uri` e `spring.mongodb.database`, preenchidas a partir de `MONGODB_URI` e `MONGODB_DATABASE`. Os índices declarados nos documentos (como o único em `usuarios.email`) são criados automaticamente na subida, porque `spring.data.mongodb.auto-index-creation` está ligado.

## Segurança, e por que fiz assim

**Senha com BCrypt custo 12.** É o algoritmo recomendado hoje para guardar senha, e custo 12 é um bom equilíbrio: alto o suficiente para ser caro de quebrar por força bruta, sem deixar o login lento para o usuário.

**Mensagem genérica no login.** Toda falha de login, seja senha errada, e-mail que não existe ou conta inativa, mostra a mesma frase: "E-mail ou senha inválidos.". Assim não dá para alguém descobrir, só tentando, quais e-mails estão cadastrados no sistema.

**Bloqueio por conta e por IP, com a chave em hash.** Depois de 5 tentativas erradas, a conta ou o IP (o que chegar primeiro a 5) fica bloqueado por 15 minutos, com a mensagem "Muitas tentativas. Tente novamente em alguns minutos.", também igual para conta existente ou não. A chave usada para contar as tentativas por conta é um hash SHA-256 do e-mail normalizado, nunca o e-mail em texto puro, porque essa coleção (`tentativas_login`) não precisa saber quem é a pessoa, só precisa saber que aquela conta está sendo tentada.

**CSRF ligado em todo formulário.** O Spring Security já vem com proteção contra CSRF ativada por padrão, e os formulários usam `th:action` do Thymeleaf, que injeta o token escondido automaticamente, sem eu precisar escrever esse campo à mão.

**Sessão no Mongo, com troca de ID no login.** A sessão fica guardada na coleção `sessoes`, não na memória da aplicação, então ela sobrevive se a aplicação reiniciar. No momento em que o login dá certo, o Spring Security troca o ID da sessão (`sessionFixation().changeSessionId()`), para evitar um ataque de fixação de sessão, onde alguém força a vítima a usar um ID de sessão que o atacante já conhece.

**Cookie de sessão com HttpOnly, SameSite e Secure configuráveis.** O cookie `SESSION` é sempre HttpOnly (JavaScript não consegue ler) e SameSite=Lax (não é enviado em requisições vindas de outro site). O `Secure` fica desligado por padrão, porque em desenvolvimento local não tem HTTPS, mas deve ser ligado em produção com a variável `APP_SESSAO_COOKIE_SECURE=true`.

**Auditoria sem senha e sem e-mail digitado.** A coleção `auditoria` guarda o tipo do evento, o id do usuário (quando existe) e um motivo genérico, mas nunca a senha, nem o e-mail que foi digitado numa tentativa que falhou. Se alguém erra a senha, o evento `LOGIN_FALHA` fica sem nenhuma pista de qual e-mail foi usado.

## Estrutura de pastas

O código em `src/main/java/br/umc/loginseguro` está dividido por assunto, não por camada:

- `config`: conexão com o Mongo e o registro do dialeto de layout do Thymeleaf.
- `seguranca`: Spring Security, o principal autenticado, o bloqueio por força bruta e a sessão no Mongo.
- `usuario`: o documento `Usuario`, o repositório, os services e as telas de administrador, secretaria e aluno.
- `auth`: cadastro público e a página de login.
- `tema`: troca de tema visual.
- `auditoria`: registro e listagem dos eventos de acesso.
- `comum`: página inicial e o despachante do `/painel`.

## Como versionei

Usei gitflow: `main` só recebe release, `develop` é onde o trabalho se junta, e cada etapa do projeto foi uma branch de `feature` separada a partir do `develop`. Na ordem em que fiz: estrutura inicial, conexão com o MongoDB, layout e temas, usuário e perfis, login e logout, cadastro com validação, controle de acesso por perfil, sessão no Mongo, força bruta e auditoria e, por fim, `feature/ajustes-visuais`, com o redesign do visual inspirado no meu PFC.

## Como adaptar para o PFC

1. Troque o nome do sistema pela variável `APP_NOME` (ou o valor padrão em `app.nome`, no `application.properties`).
2. Crie um tema novo em `static/temas/<nome-do-pfc>` com as cores do PFC e aponte `APP_TEMA` para ele.
3. Se os perfis do PFC forem diferentes de ADMINISTRADOR, SECRETARIA e ALUNO, troque os valores do enum `Perfil` e ajuste as rotas protegidas no `SecurityConfig` e os links do menu.
4. Restrinja o cadastro ao domínio de e-mail da instituição definindo `APP_CADASTRO_DOMINIO_EMAIL` (por exemplo, `alunos.umc.br`).

## Problemas que encontrei no caminho

No Spring Boot 4, as propriedades de conexão com o Mongo mudaram de `spring.data.mongodb.*` para `spring.mongodb.*`. Eu tinha configurado com o nome antigo, e a aplicação subia sem erro nenhum, só que ignorando a URI e tentando conectar em `localhost:27017`. Só descobri abrindo o jar do Spring Boot e olhando o `spring-configuration-metadata.json`, que mostrava a propriedade antiga marcada como descontinuada.

Rotas sem nenhum controller, como `/cadastro` antes de eu criar a tela, estavam voltando redirecionamento para `/login` em vez de dar 404. O motivo é que o Spring Boot encaminha internamente para `/error` para montar a página de erro, e esse encaminhamento também passa pela segurança. Bastou liberar `/error` como rota pública.

As propriedades `server.servlet.session.cookie.*`, que configuram o cookie de sessão do servlet container, não têm efeito nenhum quando o Spring Session está ativo, porque ele usa seu próprio `CookieSerializer`. Levei um tempo para entender por que o cookie `Secure` não aparecia mesmo com a variável ligada, até perceber que precisava configurar esse serializer na mão.

O Thymeleaf 3.1, que vem com o Spring Boot 4.1, removeu por padrão os objetos de expressão `#request`, `#session` e `#servletContext`. Eu usava `#request.requestURI` para destacar o link ativo no menu, e toda página quebrava. Resolvi expondo esse caminho como um atributo de modelo, pelo `@ControllerAdvice`.

Por fim, o próprio `.env.example` vem com todas as linhas comentadas de propósito, para deixar claro que são só exemplos. É fácil esquecer de tirar o `#` ao copiar para o `.env` de verdade, e aí a aplicação não encontra a variável.

## Documentação

A documentação técnica completa, no formato acadêmico, está em [docs/documentacao-tecnica.md](docs/documentacao-tecnica.md) e, formatada em ABNT com os prints do sistema, em [docs/documentacao.pdf](docs/documentacao.pdf).

## Autor

João Pedro Rachid de Abreu
