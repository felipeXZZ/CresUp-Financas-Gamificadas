# Projeto Final — Programação Mobile (UNASP)

> **Instituição:** Centro Universitário Adventista de São Paulo (UNASP)  
> **Disciplina:** Programação Mobile  
> **Professor:** Marnes Cassule  
> **Prazo:** Julho 2026

---

## Visão Geral

Desenvolver um aplicativo Android completo usando **Kotlin + Jetpack Compose**, simulando um cenário real de desenvolvimento — da concepção até a publicação na Google Play Store.

---

## Requisitos Técnicos

### Arquitetura (obrigatório)
- Implementar camadas organizadas com **MVVM** ou **Clean Architecture**

### Interface (UI/UX)
- Layout com **Jetpack Compose**
- Seguir diretrizes do **Material Design**
- Navegação fluida: Navigation Drawer, Bottom Navigation ou Tabs
- Responsivo a diferentes tamanhos de tela

### Persistência de Dados (obrigatório)
- **Room** (dados estruturados) ou **Firestore** (NoSQL com cache offline)
- ⚠️ SharedPreferences e DataStore **não são aceitos** como banco de dados principal

### Integração com APIs
- Requisições assíncronas com **Coroutines**
- Tratamento adequado de erros
- Serviços de rede organizados

### Tratamento de Erros
- Prevenir crashes
- Mensagens claras ao usuário em falhas de conexão ou entradas inválidas

---

## Entregáveis

1. **Código-Fonte**
   - Repositório no GitHub, organizado e modularizado

2. **Documentação**
   - `README.md` com: instruções de instalação, requisitos do sistema, screenshots e guia de execução
   - **Relatório Técnico** detalhando: decisões de projeto, bibliotecas usadas (especialmente a escolha do banco de dados), desafios e soluções

3. **Pacote de Distribuição**
   - APK ou AAB assinado, pronto para instalação
   - Ícone do aplicativo em alta resolução (padrões Android)

---

## Critérios de Avaliação — 10 pontos

### Desenvolvimento (5.0 pts)

| Critério                        | Pontos |
|---------------------------------|--------|
| Arquitetura (MVVM/Clean)        | 0.5    |
| Qualidade do Código Kotlin      | 1.0    |
| Interface UI/UX (Material Design)| 0.5   |
| **Banco de Dados e Persistência**| **1.0**|
| Integração com APIs             | 1.0    |
| Tratamento de Erros             | 0.5    |
| Fidelidade ao Protótipo         | 0.5    |

### Documentação (2.0 pts)

| Critério                    | Pontos |
|-----------------------------|--------|
| README.md completo          | 1.0    |
| Relatório Técnico Detalhado | 1.0    |

### Publicação (3.0 pts)

| Critério                          | Pontos |
|-----------------------------------|--------|
| APK/AAB Assinado                  | 1.0    |
| Ícone do Aplicativo               | 1.0    |
| Disponibilidade (Hospedagem/Loja) | 1.0    |

---

## Submissão

- **Formação de grupos:** conforme definido em sala
- **Formato:** link do repositório GitHub + arquivo `.apk`/`.aab` via plataforma oficial da instituição
