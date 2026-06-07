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

CresUp é uma plataforma Android de evolução financeira pessoal gamificada. O objetivo central é transformar o controle financeiro em uma experiência motivadora para jovens adultos, combinando gestão de gastos, metas e desafios com um sistema de XP e níveis.

Funcionalidades principais:
- Registro de receitas e despesas com categorias
- Criação e acompanhamento de metas financeiras
- Sistema de desafios com progresso diário
- Gamificação: XP, níveis, streaks e conquistas (badges)
- Frase motivacional diária via API externa com fallback local
- Autenticação com e-mail/senha e Google Sign-In

---

## 2. Arquitetura de Software

### 2.1 Clean Architecture + MVVM

O projeto adota Clean Architecture dividida em três camadas independentes:

**Camada Domain** (`domain/`)  
Núcleo da aplicação, sem dependência de frameworks Android:
- Modelos de domínio: `User`, `Transaction`, `Goal`, `Challenge`, `Achievement`
- Interfaces de repositório: `UserRepository`, `TransactionRepository`, `GoalRepository`, `ChallengeRepository`, `QuoteRepository`
- Regras de negócio: `computeLevel(xp)`, `levelProgress()`, detecção de conquistas

**Camada Data** (`data/`)  
Implementações concretas dos repositórios:
- Room: `AppDatabase`, 4 entities, 4 DAOs (persistência local SQLite)
- Firestore: 4 repositórios com sincronização em tempo real e cache offline
- Retrofit: `QuoteRepositoryImpl` chama a API ZenQuotes com fallback local

**Camada Presentation** (`presentation/`)  
Interface do usuário desacoplada da lógica de negócio:
- 6 ViewModels com `StateFlow` para estado reativo
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
├── (documento: perfil do usuário)
├── transactions/{transactionId}
├── goals/{goalId}
└── challenges/{challengeId}
```

Cada repositório Firestore utiliza o padrão `callbackFlow { addSnapshotListener { ... } }` para expor as atualizações em tempo real como `Flow`. O Firestore tem cache offline habilitado por padrão, garantindo que o app funcione sem internet.

**Justificativa para uso do Firestore além do Room:**  
O Room atende ao requisito acadêmico de persistência local. O Firestore foi adicionado para oferecer sincronização entre dispositivos, login multi-usuário e dados persistentes mesmo após reinstalação do app — cenários que o Room local não cobre.

---

## 4. Integração com APIs Externas

### 4.1 ZenQuotes API (Retrofit + OkHttp)

Endpoint consumido: `GET https://zenquotes.io/api/random`

Caminho da implementação:
1. `QuoteApi` — interface Retrofit com anotação `@GET("random")` e retorno `suspend fun getRandomQuote(): List<QuoteDto>`
2. `QuoteDto` — data class com `@SerializedName` para mapeamento JSON → Kotlin
3. `QuoteRepositoryImpl` — chama a API em `try-catch`; em caso de falha retorna uma frase local em português
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
| `_state.update { }` | Atualização atômica e thread-safe do estado |

---

## 6. Interface do Usuário

### 6.1 Material Design 3

Tema completamente customizado em `Theme.kt`:
- Dark theme permanente com paleta de cores premium
- Cores definidas em `Color.kt`: `NeonGreen`, `Background`, `CardBackground`, `AccentYellow`, `AccentRed`, `TextPrimary`, `TextMuted`
- Componentes Material 3 utilizados: `OutlinedTextField`, `AlertDialog`, `Button`, `FilterChip`, `LinearProgressIndicator`, `CircularProgressIndicator`, `SwipeToDismissBox`, `SnackbarHost`, `FloatingActionButton`

### 6.2 Navegação

`NavGraph` com `NavHostController` e 6 rotas:

```
splash → onboarding → login
                        ↓
                   dashboard (hub com BottomNav)
                        ↓
           gastos | metas | desafios | perfil
```

`CresUpBottomNav` implementa 5 abas com animação de cor nos ícones (ativo = verde neon / inativo = cinza).

### 6.3 Formatação de Moeda Brasileira

`CurrencyVisualTransformation` (implementa `VisualTransformation` do Compose):
- O estado armazena apenas dígitos: `"200000"`
- A transformação exibe formatado: `"2.000,00"`
- `KeyboardType.Number` impede entrada de vírgulas pelo teclado
- Parsing nos ViewModels: `amountText.toLong() / 100.0` converte centavos para reais

---

## 7. Injeção de Dependência (Hilt)

`AppModule` (singleton) provê:
- `AppDatabase` — Room database
- `FirebaseAuth` e `FirebaseFirestore` — instâncias Firebase
- `OkHttpClient`, `Retrofit`, `QuoteApi` — stack Retrofit
- Bind de interfaces de repositório para suas implementações Firestore

ViewModels anotados com `@HiltViewModel` e injetados nas telas via `hiltViewModel()`.

---

## 8. Gamificação

Sistema implementado no modelo `User` e distribuído pelos ViewModels:

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
| 2 | Construtor de Riqueza | 500 – 1499 |
| 3 | Especialista Financeiro | 1500 – 3499 |
| 4 | Elite Financeira | 3500+ |

**Streak:** dias consecutivos ativos, com detecção automática de quebra baseada na data da última atualização.

**Conquistas (badges):** desbloqueadas automaticamente por marcos (primeiro gasto registrado, primeira meta concluída, nível máximo atingido, etc.).

---

## 9. Tratamento de Erros

- Validação de formulários nos ViewModels antes de qualquer operação de I/O
- `try-catch` em todos os métodos que acessam repositórios
- Erros Firebase mapeados para português via `mapFirebaseError()`
- Snackbars para feedback imediato em todas as telas
- Fallback local para a API de frases quando sem conexão

---

## 10. Publicação e Distribuição

O aplicativo foi empacotado como **Android App Bundle (AAB) assinado** (`app-release.aab`, ~21 MB) seguindo as exigências da Google Play Store.

**Pipeline de release:**
1. Keystore gerada com `keytool` (RSA 2048, validade 10.000 dias)
2. `build.gradle.kts` configurado com `signingConfigs { release { ... } }` lendo credenciais de `keystore.properties` (excluído do git via `.gitignore`)
3. AAB gerado via **Android Studio → Build → Generate Signed Bundle**
4. App publicado na Google Play Store em **teste fechado** com 12 testadores internos

O ícone do aplicativo segue os padrões visuais do Android: fundo preto com logotipo "C" em verde neon, disponível em todas as densidades de tela (`mipmap-*`) e em formato adaptativo (`ic_launcher.xml`).

---

## 11. Desafios e Soluções

**UI consistente em dark mode permanente**  
Solução: `CresUpColorScheme` customizado definido em `Color.kt`, aplicado diretamente nos Composables sem depender das cores geradas pelo Material You (que variam com o tema do dispositivo).

**Swipe to delete em listas**  
Solução: `SwipeToDismissBox` do Material 3 com `rememberSwipeToDismissBoxState` e confirmação via `confirmValueChange`. Background vermelho com ícone de lixeira aparece durante o gesto.

**Formatação de moeda durante digitação**  
Solução: `VisualTransformation` do Compose — o estado real armazena apenas dígitos e a transformação aplica máscara de exibição sem alterar o valor subjacente. Evita problemas com o cursor que ocorrem em abordagens de máscara via `onValueChange`.

**Sincronização em tempo real sem polling**  
Solução: `callbackFlow` + `addSnapshotListener` do Firestore. O listener é registrado uma vez e emite novos valores automaticamente sempre que os dados mudam na nuvem, sem necessidade de requisições periódicas.
