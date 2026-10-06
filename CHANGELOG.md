# Changelog

Todas as mudanças notáveis deste projeto estão documentadas aqui, na ordem em que foram desenvolvidas.

## [1.0.0] - 2026-10-06

### Estrutura inicial
- Projeto Maven com Spring Boot, Thymeleaf e validação, com o Maven Wrapper.
- Página inicial simples.

### Conexão com o MongoDB Atlas
- Conexão configurada só por variável de ambiente (`MONGODB_URI`, `MONGODB_DATABASE`), carregada de um `.env` local nunca versionado.
- Falha clara na subida se `MONGODB_URI` não estiver definida, antes de qualquer tentativa de conexão.
- Confirmação da conexão no log, sem nunca expor a string de conexão.
- Criação automática dos índices declarados nos documentos.

### Layout e temas
- Layout comum com Thymeleaf Layout Dialect e fragmentos reutilizáveis (cabeçalho, navegação, rodapé, mensagens).
- Dois temas visuais (`padrao` e `escuro`), com nome e tema configuráveis por variável de ambiente.

### Usuário e perfis
- Documento `Usuario`, com e-mail normalizado e índice único.
- Três perfis: ADMINISTRADOR, SECRETARIA e ALUNO.

### Login e logout
- Autenticação com Spring Security e senha com BCrypt, custo 12.
- Mensagem genérica em qualquer falha de login, para não revelar se o e-mail existe.
- Logout por POST, com CSRF e troca do cookie de sessão.
- Criação do primeiro administrador a partir de variáveis de ambiente, só se nenhum administrador existir ainda.

### Cadastro com validação
- Cadastro público em `/cadastro`, sempre com perfil ALUNO.
- Validação de nome, e-mail (com domínio configurável), senha e aceite dos termos.
- E-mail duplicado tratado como erro no campo, mantendo os dados digitados.

### Controle de acesso por perfil
- Rotas `/admin/**`, `/secretaria/**` e `/aluno/**` protegidas por perfil, reforçadas com `@PreAuthorize` nos services.
- Telas de administração de usuários, lista de alunos para a secretaria e dados do próprio aluno.
- Páginas próprias para erro 403 e 404.

### Sessão no MongoDB
- Sessão HTTP persistida na coleção `sessoes`, com tempo de expiração configurável.
- Novo identificador de sessão a cada login, para evitar fixação de sessão.
- Cookie de sessão HttpOnly e SameSite sempre ligados, e Secure configurável.
- Encerramento das sessões ativas de um usuário ao desativá-lo ou mudar seu perfil.

### Proteção contra força bruta e auditoria
- Bloqueio de 5 tentativas em 15 minutos, por conta (identificada por hash) e por IP.
- Mensagem de bloqueio diferente da de senha errada, mas igual para conta existente ou não.
- Registro de auditoria dos eventos de acesso, sem nunca gravar senha ou e-mail digitado numa tentativa que falhou.
- Página de auditoria com os 100 eventos mais recentes.

### Redesign visual
- Interface redesenhada com inspiração no PFC do autor: barra superior, navegação, cartões, tabelas e etiquetas de perfil e status.
- Botão de alternância entre tema claro e escuro, persistido em cookie.
- Cartões de resumo no painel do administrador.

### Documentação
- `README.md` com instruções de configuração, execução e adaptação do projeto.
- `docs/documentacao-tecnica.md`, com a documentação acadêmica do sistema.

### Versão 1.0.0
- Versão do projeto fixada em `1.0.0` no `pom.xml`.
- Revisão final: nenhum segredo em arquivo versionado, `.env` fora do controle de versão, e subida do sistema validada seguindo só o `README.md`.
