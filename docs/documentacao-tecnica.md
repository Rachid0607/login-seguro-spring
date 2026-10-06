# Documentação técnica: Sistema de Login Seguro

## 1 Introdução

Este trabalho apresenta o desenvolvimento de um sistema de login seguro, elaborado para a disciplina de Aplicativos Web da Universidade de Mogi das Cruzes (UMC). O sistema trata do cadastro de usuários, da autenticação, do controle de acesso por perfil, da persistência de sessão e do registro de auditoria dos eventos de acesso.

O projeto foi concebido de forma genérica, sem nenhuma regra específica de um domínio de negócio, para que a lógica de autenticação e autorização construída aqui possa ser reaproveitada como base para o módulo de login de outro sistema, o Sistema de Gestão de Eventos Acadêmicos (PFC desenvolvido pelo autor em dupla), adaptando-se apenas o nome da aplicação, o tema visual, os perfis de usuário e o domínio de e-mail aceito no cadastro.

## 2 Objetivos

### 2.1 Objetivo geral

Desenvolver um sistema web de autenticação e autorização com Spring Boot, Spring Security, Thymeleaf e MongoDB Atlas, aplicando práticas de segurança reconhecidas para o armazenamento de credenciais, o controle de sessão e a proteção contra ataques comuns de autenticação.

### 2.2 Objetivos específicos

- Implementar cadastro público com validação de dados e hash de senha.
- Implementar login e logout com sessão persistida no banco de dados.
- Implementar controle de acesso baseado em três perfis de usuário.
- Implementar proteção contra ataques de força bruta no login.
- Implementar auditoria dos eventos de acesso ao sistema.
- Disponibilizar uma interface com suporte a múltiplos temas visuais, configuráveis sem alteração do código.

## 3 Tecnologias utilizadas

O sistema foi desenvolvido em Java 21, com o framework Spring Boot na versão 4.1.1. Para a camada de autenticação e autorização, utilizou-se o Spring Security, na versão 7.1.1. A persistência da sessão HTTP no MongoDB foi implementada com a biblioteca `mongodb-spring-session`, na versão 4.0.0. Essa biblioteca passou a ser mantida pela MongoDB Inc. a partir do Spring Session 4.0, quando o time do Spring transferiu as integrações com bancos de dados específicos para os respectivos parceiros. A camada de apresentação utiliza o motor de templates Thymeleaf, na versão 3.1.5, com as extensões Thymeleaf Layout Dialect, para composição de layouts, e `thymeleaf-extras-springsecurity6`, para exibição condicional de elementos conforme o perfil autenticado. A persistência dos dados é feita no MongoDB Atlas, serviço de banco de dados como serviço da MongoDB Inc., por meio do driver Java oficial na versão 5.8.1. A automação de build utiliza o Apache Maven, por meio do Maven Wrapper incluído no repositório.

## 4 Arquitetura e estrutura do sistema

O sistema segue o padrão MVC (Model-View-Controller) nativo do Spring Web MVC, com controllers responsáveis apenas pelo roteamento e pela montagem do modelo, e a regra de negócio concentrada em classes de serviço. Os dados submetidos por formulário são recebidos por DTOs (Data Transfer Objects) específicos, como `CadastroForm` e `NovoUsuarioAdminForm`, validados com Bean Validation (Jakarta Validation) e só então convertidos para os documentos persistidos no banco.

A organização dos pacotes, em `br.umc.loginseguro`, segue um critério funcional, não de camada técnica:

- `config`: inicialização e verificação da conexão com o MongoDB e registro do dialeto de layout do Thymeleaf.
- `seguranca`: configuração do Spring Security, o principal autenticado (`UsuarioAutenticado`), a proteção contra força bruta e a configuração da sessão no MongoDB.
- `usuario`: o documento `Usuario`, o repositório, os serviços de leitura e escrita, e os controllers das áreas de administrador, secretaria e aluno.
- `auth`: cadastro público e página de login.
- `tema`: seleção do tema visual.
- `auditoria`: registro e consulta dos eventos de auditoria.
- `comum`: página inicial e o controller que direciona cada usuário autenticado para a área do seu próprio perfil.

A interface utiliza o Thymeleaf Layout Dialect para separar a estrutura comum das páginas (`layouts/base.html`) do conteúdo específico de cada tela, e fragmentos reutilizáveis para o cabeçalho, a navegação, o rodapé e as mensagens de retorno. Nenhuma folha de estilo é declarada em linha nos templates: toda a aparência está centralizada em `static/css/layout.css`, que utiliza exclusivamente propriedades customizadas do CSS (`var(--...)`), e nos arquivos de tema, descritos na seção 7.

## 5 Integração com o MongoDB Atlas

A conexão com o MongoDB Atlas é configurada pelas propriedades `spring.mongodb.uri` e `spring.mongodb.database`, preenchidas a partir das variáveis de ambiente `MONGODB_URI` e `MONGODB_DATABASE`. A string de conexão nunca é escrita em arquivo versionado: ela é carregada de um arquivo `.env` local, não versionado, ou diretamente do ambiente de execução. Um inicializador de contexto (`MongoUriObrigatoriaInitializer`) interrompe a subida da aplicação com uma mensagem explícita caso a variável não esteja definida, e um `ApplicationRunner` (`VerificadorConexaoMongo`) confirma a conexão no log de inicialização, sem nunca registrar a string de conexão em si.

O sistema utiliza quatro coleções principais. A coleção `usuarios` armazena as contas de todos os perfis, com índice único no campo `email`, normalizado em minúsculas antes de qualquer gravação ou consulta. A coleção `sessoes` armazena a sessão HTTP de cada usuário autenticado, com os índices criados automaticamente pela biblioteca de sessão. A coleção `tentativas_login` armazena o contador de tentativas de autenticação malsucedidas, indexada por uma chave que identifica a conta (pelo hash SHA-256 do e-mail normalizado) ou o endereço IP de origem, com um índice TTL (time to live) no campo `expiraEm`, que expira o documento automaticamente após o período de inatividade. A coleção `auditoria` armazena o histórico dos eventos de acesso, sem índices além do identificador padrão.

A criação dos índices declarados nos documentos é automática, habilitada pela propriedade `spring.data.mongodb.auto-index-creation`.

## 6 Segurança e principais decisões de design

A senha é armazenada com a função de hash BCrypt, com fator de custo 12, por meio do `BCryptPasswordEncoder` do Spring Security. O fator de custo mais alto aumenta o tempo necessário para calcular cada hash, o que penaliza tentativas de quebra por força bruta sem comprometer de forma perceptível o tempo de resposta ao usuário legítimo.

Toda falha de autenticação, seja por senha incorreta, conta inexistente ou conta inativa, resulta na mesma mensagem genérica ("E-mail ou senha inválidos."), de modo a não revelar a um atacante se um determinado e-mail possui conta cadastrada no sistema. A proteção contra força bruta aplica essa mesma lógica: após cinco tentativas malsucedidas, a conta ou o endereço IP de origem (o que atingir o limite primeiro) é bloqueado por quinze minutos, com uma mensagem distinta ("Muitas tentativas. Tente novamente em alguns minutos."), também idêntica independentemente de a conta existir. A checagem do bloqueio ocorre antes de qualquer consulta ao usuário, por meio de um `AuthenticationProvider` dedicado (`BloqueioLoginAuthenticationProvider`), que é avaliado antes do provedor de autenticação principal numa cadeia de `ProviderManager` montada explicitamente. A chave utilizada para identificar a conta nessa verificação é o hash SHA-256 do e-mail normalizado, nunca o e-mail em texto legível.

A proteção contra Cross-Site Request Forgery (CSRF) está habilitada por padrão pelo Spring Security e é aplicada a todos os formulários da aplicação, que utilizam o atributo `th:action` do Thymeleaf para que o token seja inserido automaticamente pela integração entre Thymeleaf e Spring Security.

A sessão HTTP é persistida na coleção `sessoes` do MongoDB, em vez de permanecer apenas na memória da aplicação, o que permite que o estado de autenticação sobreviva a um reinício do processo. No momento em que a autenticação é concluída com sucesso, o identificador da sessão é substituído (estratégia `changeSessionId`), medida que mitiga ataques de fixação de sessão. O cookie de sessão é configurado com os atributos HttpOnly e SameSite=Lax de forma fixa, e o atributo Secure de forma configurável pela variável de ambiente `APP_SESSAO_COOKIE_SECURE`, destinada a ser ativada em ambiente de produção, onde há HTTPS.

O controle de acesso por perfil é aplicado em duas camadas. A primeira, centralizada na configuração do `SecurityFilterChain`, restringe o acesso aos prefixos de rota `/admin/**`, `/secretaria/**` e `/aluno/**` conforme o perfil do usuário autenticado. A segunda camada, aplicada diretamente sobre os métodos de serviço com a anotação `@PreAuthorize`, habilitada por `@EnableMethodSecurity`, reforça essa mesma restrição, de modo que uma eventual nova via de acesso a esses métodos não contorne a autorização.

Por fim, o registro de auditoria armazena o tipo do evento, o identificador do usuário quando aplicável e um motivo descrito em termos genéricos, nunca a senha submetida nem o e-mail digitado numa tentativa de autenticação malsucedida.

## 7 Interface e temas

A interface foi construída com o Thymeleaf e o Thymeleaf Layout Dialect, de modo que a estrutura visual comum (barra superior, navegação, rodapé) fique isolada do conteúdo específico de cada página. Toda a identidade visual, incluindo cor, tipografia, raio de borda e sombra, é definida como propriedade customizada do CSS, nunca como valor fixo nas folhas de estilo nem em atributo de estilo dentro dos templates.

O sistema oferece dois temas visuais, "padrão" e "escuro", cada um definido em um arquivo próprio (`static/temas/<nome>/tema.css`) que declara os mesmos nomes de variável com valores diferentes. A folha de estilo principal (`static/css/layout.css`) consome exclusivamente essas variáveis, de modo que a troca de tema altera a aparência completa da aplicação sem qualquer modificação de template.

A seleção do tema é persistida em um cookie no navegador do usuário, por meio de um formulário HTTP POST enviado à rota `/tema`, protegido pelo mesmo mecanismo de CSRF empregado nos demais formulários. O controller responsável valida o tema recebido contra a lista de temas existentes e restringe o redirecionamento de retorno a caminhos internos da própria aplicação, de modo a impedir que esse mecanismo seja utilizado como redirecionamento aberto (open redirect) para um domínio externo. Na ausência do cookie, ou caso o seu valor não corresponda a um tema válido, a aplicação utiliza o tema definido pela propriedade `app.tema`, configurável pela variável de ambiente `APP_TEMA`.

## 8 Versionamento com Gitflow

O versionamento do projeto seguiu o modelo Gitflow, com a branch `main` reservada para versões de release, a branch `develop` concentrando a integração contínua do desenvolvimento, e uma branch de `feature` distinta para cada etapa do trabalho, posteriormente integrada à `develop`. As etapas de desenvolvimento, na ordem em que foram implementadas, foram: estrutura inicial do projeto, conexão com o MongoDB Atlas, layout e temas, modelagem de usuário e perfis, login e logout, cadastro com validação, controle de acesso por perfil, persistência de sessão no MongoDB, proteção contra força bruta com auditoria e, por fim, o redesign da interface, na branch `feature/ajustes-visuais`.

A versão final do trabalho foi preparada na branch `release/1.0.0`, integrada à `main` e marcada com a tag `v1.0.0`.

## 9 Como adaptar o sistema

A adaptação do sistema para um domínio de aplicação distinto, como o Sistema de Gestão de Eventos Acadêmicos, não exige alteração da lógica central de autenticação e autorização. São necessários os seguintes ajustes: a definição do nome da aplicação pela variável `APP_NOME`; a criação de um novo arquivo de tema em `static/temas/<nome>`, com os valores de cor e tipografia da nova identidade visual, selecionado pela variável `APP_TEMA`; a eventual alteração dos valores do enumerado `Perfil`, caso os perfis de usuário do novo domínio sejam distintos dos três perfis aqui definidos (ADMINISTRADOR, SECRETARIA, ALUNO), com o ajuste correspondente nas regras de autorização declaradas em `SecurityConfig`; e a restrição do domínio de e-mail aceito no cadastro público pela variável `APP_CADASTRO_DOMINIO_EMAIL`.

## 10 Considerações finais

O sistema desenvolvido atende aos objetivos propostos, com cadastro validado, autenticação e autorização por perfil, persistência de sessão no banco de dados, proteção contra tentativas repetidas de autenticação e registro de auditoria dos eventos de acesso. Ficaram fora do escopo deste trabalho, por definição prévia, a autenticação em duas etapas, a recuperação de senha por e-mail e o uso de tokens JWT, mecanismos que poderiam ser incorporados em trabalhos futuros sem alteração da arquitetura aqui estabelecida.

## Referências

MONGODB. MongoDB Atlas Documentation. Disponível em: https://www.mongodb.com/docs/atlas/. Acesso em: 6 out. 2026.

MONGODB. Spring Session MongoDB moves to new open source home. 14 out. 2025. Disponível em: https://mongodb.com/company/blog/product-release-announcements/spring-session-mongodb-moves-to-new-open-source-home. Acesso em: 6 out. 2026.

OWASP. Password Storage Cheat Sheet. OWASP Cheat Sheet Series. Disponível em: https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html. Acesso em: 6 out. 2026.

OWASP. Session Management Cheat Sheet. OWASP Cheat Sheet Series. Disponível em: https://cheatsheetseries.owasp.org/cheatsheets/Session_Management_Cheat_Sheet.html. Acesso em: 6 out. 2026.

SPRING. Spring Boot Reference Documentation. Disponível em: https://docs.spring.io/spring-boot/index.html. Acesso em: 6 out. 2026.

SPRING. Spring Security Reference Documentation. Disponível em: https://docs.spring.io/spring-security/reference/index.html. Acesso em: 6 out. 2026.

SPRING. Spring Session Reference Documentation. Disponível em: https://docs.spring.io/spring-session/reference/index.html. Acesso em: 6 out. 2026.

THYMELEAF. Thymeleaf Documentation. Disponível em: https://www.thymeleaf.org/documentation.html. Acesso em: 6 out. 2026.
