# Relatório Técnico — CresUp

**Disciplina:** Programação Mobile  
**Instituição:** UNASP — Centro Universitário Adventista de São Paulo  
**Tecnologia:** Android (Kotlin + Jetpack Compose)

**Integrantes:**

| Nome | RA |
|---|---|
| Felipe Guilherme Teodolino | 212392 |
| Carlos Eduardo Santos Silva | 213032 |
| Carlos Eduardo Brandão Souza | 202493 |
| Phelippe Oliveira Santos | 211268 |
| Vinicios Santos Silva | 212553 |
| Caio Santos Ferreira | 209281 |

---

## 1. Visão Geral do Projeto

CresUp é uma plataforma Android de evolução financeira pessoal gamificada com design premium. O objetivo central é transformar o controle financeiro em uma experiência motivadora para jovens adultos, combinando gestão de gastos, metas e desafios com um sistema completo de XP, níveis, conquistas, streaks e insights financeiros automáticos.

Funcionalidades principais:
- Registro de receitas e despesas com 11 categorias e ícones Material exclusivos
- Criação e acompanhamento de metas financeiras com presets
- Sistema de desafios com dificuldade (Fácil / Médio / Difícil) e progresso diário
- Gamificação: XP, 4 níveis, streaks, 15 conquistas com raridade, moeda virtual (Coins)
- Tela de Análises exclusiva: gráficos de categoria, métricas e insights financeiros automáticos
- Sistema social: pedidos de amizade (enviar/aceitar/recusar), ranking entre amigos, feed de atividades
- Frase motivacional diária via API externa com fallback local em português
- Autenticação com e-mail/senha e Google Sign-In

---

## 2. Arquitetura de Software

### 2.1 Clean Architecture + MVVM

O projeto adota Clean Architecture dividida em três camadas independentes:

**Camada Domain** (`domain/`)  
Núcleo da aplicação, sem dependência de frameworks Android:
- Modelos de domínio: `User`, `Transaction`, `Goal`, `Challenge`, `Achievement`, `PublicProfile`, `FriendRequest`, `FeedItem`
- Enums: `TransactionType`, `TransactionCategory`, `ChallengeDifficulty`, `AchievementRarity`, `FeedItemType`
- Interfaces de repositório: `UserRepository`, `TransactionRepository`, `GoalRepository`, `ChallengeRepository`, `QuoteRepository`, `SocialRepository`
- Regras de negócio: `computeLevel(xp)`, `levelProgress()`, detecção de conquistas

**Camada Data** (`data/`)  
Implementações concretas dos repositórios:
- Room: `AppDatabase`, 4 entities, 4 DAOs (persistência local SQLite)
- Firestore: 5 repositórios com sincronização em tempo real e cache offline (inclui `FirestoreSocialRepository`)
- Retrofit: `QuoteRepositoryImpl` chama a API ZenQuotes com fallback local

**Camada Presentation** (`presentation/`)  
Interface do usuário desacoplada da lógica de negócio:
- 8 ViewModels com `StateFlow` para estado reativo (incluindo `AnalyticsViewModel` e `SocialViewModel`)
- Composables observam estado e disparam eventos — sem lógica de negócio na UI
- `SnackbarHostState` para feedback não-intrusivo em todas as telas

### 2.2 Fluxo de Dados

```
Composable → ViewModel → Repository (interface) → Firestore / Room / Retrofit
                ↑                                         ↓
         StateFlow / Flow ←────────────── callbackFlow / suspend fun
```

---

## 3. Banco de Dados

### 3.1 Room — Persistência Local (SQLite)

`AppDatabase` gerencia 4 entidades com DAOs type-safe:

| Entidade | Tabela | Campos principais |
|---|---|---|
| `UserEntity` | `users` | uid, name, xp, level, streak, coins |
| `TransactionEntity` | `transactions` | id, title, amount, type, category, date |
| `GoalEntity` | `goals` | id, title, targetAmount, currentAmount, emoji |
| `ChallengeEntity` | `challenges` | id, title, type, status, currentProgress |

Todos os DAOs retornam `Flow<List<T>>` para observação reativa contínua, integrada nativamente com `collectAsStateWithLifecycle()` nos Composables.

### 3.2 Firebase Firestore — Banco em Nuvem

Banco NoSQL em nuvem com estrutura hierárquica por usuário autenticado:

```
users/{uid}/
├── (documento: perfil do usuário — inclui coins)
├── transactions/{transactionId}
├── goals/{goalId}
├── challenges/{challengeId}
├── friends/{friendUid}          # amigos aceitos (perfil público cacheado)
└── feed/{feedId}                # atividades do usuário

publicProfiles/{uid}             # perfil público para busca por código
friendRequests/{fromUid_toUid}   # pedidos de amizade pendentes
```

Cada repositório Firestore utiliza o padrão `callbackFlow { addSnapshotListener { ... } }` para expor as atualizações em tempo real como `Flow`. O Firestore tem cache offline habilitado por padrão. Os listeners usam `close()` (sem propagar exceção) para evitar crash por `PERMISSION_DENIED` ao fazer logout.

**Justificativa para uso do Firestore além do Room:**  
O Room atende ao requisito acadêmico de persistência local. O Firestore foi adicionado para oferecer sincronização entre dispositivos, login multi-usuário e dados persistentes após reinstalação.

---

## 4. Integração com APIs Externas

### 4.1 Frases Motivacionais (Retrofit + OkHttp)

A stack Retrofit + OkHttp está completamente implementada para integração futura com a ZenQuotes API (`GET https://zenquotes.io/api/random`):

1. `QuoteApi` — interface Retrofit com `@GET("random")` e retorno `suspend fun getRandomQuote(): List<QuoteDto>`
2. `QuoteDto` — data class com `@SerializedName` para mapeamento JSON → Kotlin
3. `QuoteRepositoryImpl` — realiza a requisição assíncrona à API em `try-catch`; independentemente do resultado (a API retorna frases em inglês), exibe sempre uma das 12 frases curadas em português para manter a consistência de idioma do app
4. `AppModule` — provê `OkHttpClient` (com `HttpLoggingInterceptor`), `Retrofit` (baseUrl ZenQuotes) e `QuoteApi`

### 4.2 Firebase Authentication

Dois métodos de autenticação implementados:

**E-mail e senha:**
```kotlin
auth.signInWithEmailAndPassword(email, password).await()
auth.createUserWithEmailAndPassword(email, password).await()
```

**Google Sign-In via Credential Manager:**
```kotlin
GetGoogleIdOption → GetCredentialRequest → credentialManager.getCredential()
→ GoogleIdTokenCredential → GoogleAuthProvider.getCredential() → auth.signInWithCredential()
```

Todos os erros Firebase são mapeados para mensagens em português em `mapFirebaseError()` no `AuthViewModel`.

---

## 5. Coroutines e Reatividade

| Padrão | Onde é usado |
|---|---|
| `viewModelScope.launch { }` | Operações assíncronas em todos os ViewModels |
| `callbackFlow { }` | Transformar listeners Firestore em `Flow` |
| `.await()` | Suspender Tasks do Firebase (via `kotlinx-coroutines-play-services`) |
| `collectAsStateWithLifecycle()` | Coletar `Flow` nos Composables respeitando ciclo de vida |
| `StateFlow` + `MutableStateFlow` | Estado único e imutável por ViewModel |
| `combine { }` | Mesclar múltiplos flows no AnalyticsViewModel e DashboardViewModel |
| `_state.update { }` | Atualização atômica e thread-safe do estado |
| `AuthStateListener` | Detectar troca de conta e reiniciar listeners no SocialViewModel |
| `Job.cancel()` | Cancelar coroutines de listeners antigos ao trocar de conta |

---

## 6. Interface do Usuário

### 6.1 Design System Premium

Paleta de cores baseada em Tailwind CSS, consistente em todo o app:

| Token | Valor | Uso |
|---|---|---|
| `Background` | `#050505` | Fundo principal |
| `CardBackground` | `#151515` | Fundo dos cards |
| `CardBorder` | `#262626` | Bordas e separadores |
| `NeonGreen` | `#A3E635` | Cor primária (lime-400) |
| `TextSecondary` | `#A1A1AA` | Texto secundário (zinc-400) |
| `Danger` | `#EF4444` | Despesas / erros |
| `Warning` | `#FACC15` | XP / conquistas |

Todos os emojis foram substituídos por **Material Icons** para consistência visual e acessibilidade.

### 6.2 Material Design 3

Tema completamente customizado em `Theme.kt`:
- Dark theme permanente com `darkColorScheme` do Material 3
- Componentes utilizados: `OutlinedTextField`, `AlertDialog`, `Button`, `FilterChip`, `LinearProgressIndicator`, `CircularProgressIndicator`, `SwipeToDismissBox`, `SnackbarHost`, `FloatingActionButton`

### 6.3 Navegação

`NavGraph` com `NavHostController` e 7 rotas:

```
splash → onboarding → login
                        ↓
                   dashboard ←→ analytics
                        ↓
           gastos | metas | desafios | perfil
```

- `CresUpBottomNav` com 5 abas e animação de cor (ativo = lime / inativo = zinc)
- `Screen.Analytics` acessível via botão "Ver análise" na Dashboard (sem ocupar tab no BottomNav)

### 6.4 Formatação de Moeda Brasileira

`CurrencyVisualTransformation` (implementa `VisualTransformation` do Compose):
- Estado armazena apenas dígitos: `"200000"`
- Transformação exibe formatado: `"2.000,00"`
- Parsing nos ViewModels: `amountText.toLong() / 100.0`

---

## 7. Injeção de Dependência (Hilt)

`AppModule` (singleton) provê:
- `AppDatabase` — Room database
- `FirebaseAuth` e `FirebaseFirestore` — instâncias Firebase
- `OkHttpClient`, `Retrofit`, `QuoteApi` — stack Retrofit
- Bind de interfaces de repositório para suas implementações Firestore
- `SocialRepository` → `FirestoreSocialRepository` (amigos, pedidos, feed, perfis públicos)

ViewModels anotados com `@HiltViewModel` e injetados nas telas via `hiltViewModel()`. O `SocialViewModel` recebe `FirebaseAuth` diretamente para observar mudanças de conta via `AuthStateListener`.

---

## 8. Gamificação

### 8.1 Sistema de XP e Níveis

Implementado no modelo `User` e distribuído pelos ViewModels:

| Ação | XP |
|---|---|
| Registrar despesa | +10 XP |
| Registrar receita | +20 XP |
| Criar meta | +30 XP |
| Contribuir para meta | +25 XP |
| Concluir desafio | +XP variável |

**Níveis** calculados em `computeLevel(xp)`:

| Nível | Nome | XP |
|---|---|---|
| 1 | Poupador Iniciante | 0 – 499 |
| 2 | Construtor de Riqueza | 500 – 1.499 |
| 3 | Especialista Financeiro | 1.500 – 3.499 |
| 4 | Elite Financeira | 3.500+ |

### 8.2 Conquistas com Raridade

15 conquistas desbloqueáveis organizadas em 4 níveis de raridade:

| Raridade | Exemplos |
|---|---|
| Comum | Primeiro Passo, Economizador, Desafiador |
| Raro | Streak de Fogo, Meta Alcançada, Investidor, Sem Delivery, Construtor |
| Épico | Disciplinado, Milionário do XP, Centurião, Poupador de Elite |
| Lendário | Elite Financeiro, Conquistador, Maratonista |

Cada conquista exibe ícone Material exclusivo e badge de raridade colorido.

### 8.3 Desafios com Dificuldade

5 desafios classificados por dificuldade com badge visual:

| Desafio | Dificuldade | XP |
|---|---|---|
| Economize R$100 | Fácil | 150 |
| Semana de Economia | Fácil | 180 |
| 7 Dias Sem Delivery | Médio | 200 |
| Meta Semanal | Médio | 250 |
| 15 Dias Registrando | Difícil | 300 |

### 8.4 Streak e Coins

**Streak:** dias consecutivos ativos, com detecção automática de quebra.  
**Coins:** moeda virtual acumulada, exibida no Perfil com badge dourado. Base para futura Loja de Recompensas.

---

## 9. Tela de Análises (AnalyticsViewModel + AnalyticsScreen)

Tela exclusiva que processa e exibe dados financeiros avançados sem dependência de API externa:

**AnalyticsViewModel** combina `userRepository.getUser()` e `transactionRepository.getAllTransactions()` usando `combine { }` para computar:
- Receita e gastos do mês atual vs mês anterior
- Taxa de economia: `(receita - gastos) / receita × 100`
- Média diária de gastos: `gastos / dia_do_mês`
- Variação percentual de gastos vs mês anterior
- Breakdown de gastos por categoria (top 6, ordenado por valor)
- Lista de insights financeiros automáticos (rule-based)

**Insights automáticos gerados:**
- Alerta se gastos > receita no mês
- Parabéns se taxa de economia ≥ 30%
- Sugestão se taxa de economia está abaixo de 10%
- Alerta se uma categoria representa > 35% dos gastos
- Feedback de tendência: alta (+20%) ou queda (-10%) vs mês anterior
- Reconhecimento de streak longo (≥ 7 dias)
- Reconhecimento de nível avançado (≥ 3)

**AnalyticsScreen** exibe:
- Cards de métricas com ícones e cores semânticas
- Gráfico de barras horizontais animadas por categoria (`LinearProgressIndicator` com `tween(800)`)
- Lista de InsightCard com cor/ícone conforme tipo (POSITIVE / NEUTRAL / WARNING)
- Estatísticas gerais (total de transações, streak, maior categoria)

---

## 10. Sistema Social

### 10.1 Arquitetura

O módulo social é composto por `SocialRepository` (interface), `FirestoreSocialRepository` (implementação) e `SocialViewModel`, com três coleções Firestore:

- **`publicProfiles/{uid}`** — perfil público sincronizado automaticamente a cada login, contendo nome, código do usuário, nível e XP. Permite busca por código sem expor dados privados.
- **`friendRequests/{fromUid_toUid}`** — pedidos de amizade pendentes. O ID do documento é composto (`${remetenteUid}_${destinatarioUid}`) para evitar composite indexes e permitir verificação de existência com leitura direta.
- **`users/{uid}/friends/{friendUid}`** — subcoleção de amigos aceitos, armazenando o perfil público cacheado do amigo.

### 10.2 Fluxo de Pedidos de Amizade

```
Usuário A busca código → encontra Usuário B → envia pedido
    ↓
friendRequests/{A_B} criado com status "pending"
    ↓
Usuário B vê pedido em "Pedidos de Amizade" (query: toUid == B)
    ↓
Usuário B aceita:
  1. Adiciona A em B/friends (escrita própria)
  2. Adiciona B em A/friends (escrita cruzada — regra Firestore: auth.uid == friendId)
  3. Deleta friendRequests/{A_B}
  4. Deleta friendRequests/{B_A} se existir (limpeza de pedido reverso)
```

Validações no envio: já são amigos (`already_friends`), pedido já enviado (`already_sent`), outra pessoa já enviou pedido (`has_incoming`). Mensagens de erro em português exibidas via Snackbar.

### 10.3 Listeners em Tempo Real

O `SocialViewModel` coleta 5 flows simultâneos: `getUser`, `getFriends`, `getIncomingRequests`, `getSentRequests` e `getFeed`. Cada flow é um `callbackFlow` com `addSnapshotListener` do Firestore.

### 10.4 Troca de Conta (AuthStateListener)

Para evitar dados de outra conta persistindo na tela Social, o `SocialViewModel` registra um `FirebaseAuth.AuthStateListener`. Quando o UID muda (logout/login com outra conta):

1. Todos os `Job`s dos listeners são cancelados
2. O estado é resetado para `SocialState()` (vazio)
3. Novos listeners são criados com o UID da nova conta

Isso garante isolamento completo entre contas, mesmo que o ViewModel não seja destruído pela navegação.

### 10.5 Regras Firestore do Sistema Social

- `publicProfiles/{uid}`: leitura para qualquer autenticado; escrita apenas pelo dono
- `friendRequests/{requestId}`: leitura para qualquer autenticado; criação apenas se `fromUid == auth.uid`; deleção para qualquer autenticado
- `users/{userId}/friends/{friendId}`: leitura pelo dono; escrita pelo dono **ou** pelo amigo (`auth.uid == friendId`) — necessário para a escrita cruzada ao aceitar pedido

---

## 11. Tratamento de Erros

- Validação de formulários nos ViewModels antes de qualquer operação de I/O
- `try-catch` em todos os métodos que acessam repositórios
- Erros Firebase mapeados para português via `mapFirebaseError()`
- Snackbars para feedback imediato em todas as telas
- Frases motivacionais sempre disponíveis offline (12 frases curadas em português)
- Campos Firestore ausentes em documentos existentes recebem valores padrão (backward-compatible)
- Guarda de processamento em operações de desafio (`processingIds: MutableSet<Long>`) evita race conditions por double-tap

---

## 12. Publicação e Distribuição

O aplicativo foi empacotado como **Android App Bundle (AAB) assinado** (`app-release.aab`, ~21 MB) seguindo as exigências da Google Play Store.

**Pipeline de release:**
1. Keystore gerada com `keytool` (RSA 2048, validade 10.000 dias)
2. `build.gradle.kts` configurado com `signingConfigs { release { ... } }` lendo credenciais de `keystore.properties` (excluído do git via `.gitignore`)
3. AAB gerado via **Android Studio → Build → Generate Signed Bundle**
4. App publicado na Google Play Store em **teste fechado** com 12 testadores internos

O ícone do aplicativo segue os padrões visuais do Android: fundo preto com logotipo "C" em verde lime, disponível em todas as densidades de tela (`mipmap-*`) e em formato adaptativo (`ic_launcher.xml`).

---

## 13. Desafios e Soluções

**UI consistente em dark mode permanente**  
Solução: `CresUpColorScheme` customizado baseado em tokens Tailwind CSS (`#050505`, `#A3E635`, `#A1A1AA`), aplicado diretamente nos Composables sem depender das cores geradas pelo Material You.

**Substituição de emojis por ícones**  
Solução: função `categoryIcon(TransactionCategory)` mapeia enum para `ImageVector`, e funções análogas para conquistas e desafios. A string de emoji é mantida no modelo para backward-compatibility com Firestore, mas a UI usa exclusivamente Material Icons.

**Swipe to delete em listas**  
Solução: `SwipeToDismissBox` do Material 3 com `rememberSwipeToDismissBoxState` e `confirmValueChange`. Background vermelho com ícone de lixeira aparece durante o gesto.

**Formatação de moeda durante digitação**  
Solução: `VisualTransformation` do Compose — o estado real armazena apenas dígitos e a transformação aplica máscara de exibição sem alterar o valor subjacente.

**Sincronização em tempo real sem polling**  
Solução: `callbackFlow` + `addSnapshotListener` do Firestore. O listener é registrado uma vez e emite novos valores automaticamente.

**Insights financeiros sem IA externa**  
Solução: `AnalyticsViewModel.buildInsights()` computa regras determinísticas sobre os dados locais: comparação de mês atual vs anterior, proporção por categoria, taxa de economia. Resultado equivalente a um sistema de IA leve, sem custo de API.

**Backward-compatibility ao adicionar campos ao Firestore**  
Solução: todos os novos campos (`coins`, `difficulty`) usam `?: 0` / `?: MEDIUM` como fallback nos mappers, garantindo que documentos antigos funcionem sem migração.

**Crash ao fazer logout com listeners Firestore ativos**  
Todos os `callbackFlow` usavam `close(err)` ao receber erro do `SnapshotListener`, propagando `PERMISSION_DENIED` como exceção fatal quando o `signOut()` invalidava as credenciais. Solução: alterar para `close()` (sem exceção) em todos os 7 callbackFlows dos 5 repositórios Firestore, encerrando o flow silenciosamente.

**Dados de outra conta persistindo na aba Social após troca de conta**  
O `SocialViewModel` configurava listeners no `init {}` com o UID da primeira conta; ao fazer logout e login com outra conta, os listeners antigos permaneciam ativos com o UID anterior. Solução: `FirebaseAuth.AuthStateListener` detecta mudança de UID, cancela todos os `Job`s dos listeners anteriores, reseta o estado e cria novos listeners com o UID correto.

**Escrita cruzada entre usuários ao aceitar amizade**  
Ao aceitar um pedido, o aceitante precisa escrever na subcoleção `friends` do remetente (outro usuário). Solução: regra Firestore que permite escrita em `users/{userId}/friends/{friendId}` quando `auth.uid == friendId`, combinada com `runCatching` para falha não-fatal caso a escrita cruzada falhe.

**Integridade de XP nos desafios — off-by-one + race condition**  
Três bugs interagiam para permitir acúmulo indevido de XP: (1) off-by-one em `incrementProgress` — após `dao.incrementProgress(id)` o valor já estava incrementado, mas o código somava `+1` novamente na comparação, concluindo o desafio um dia antes do prazo; (2) leitura de estado stale — o ViewModel lia `_state.value` imediatamente após o suspend do repositório, antes do Flow emitir o novo dado, fazendo vários taps simultâneos lerem o mesmo `progressCurrent` e cada um conceder XP; (3) guarda insuficiente contra double-tap — a verificação `challenge.isCompleted` usava o objeto recebido do Composable (stale), permitindo múltiplos coroutines concorrentes no mesmo desafio. Solução: `incrementProgress` passa a retornar `Boolean` indicando conclusão; o XP é concedido com base nesse retorno direto (sem leitura de estado stale); e `processingIds: MutableSet<Long>` no ViewModel bloqueia chamadas concorrentes para o mesmo desafio via `MutableSet.add` atômico antes do `launch`.
