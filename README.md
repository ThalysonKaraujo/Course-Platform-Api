# Plataforma de Cursos Online - API REST
API REST robusta e segura, desenvolvida em Java com Spring Boot, que serve como backend para uma plataforma de cursos online. O projeto foi construído seguindo as melhores práticas de desenvolvimento, com foco em segurança, testabilidade e manutenibilidade.

A API permite o gerenciamento completo de cursos, módulos, aulas, matrículas e perfis de usuário, com um sistema de permissões baseado em papéis (Aluno, Instrutor e Administrador).

## Funcionalidades Principais

* 🔐 **Autenticação e Autorização:** Sistema completo de registro e login utilizando Spring Security e tokens JWT. As permissões são controladas em nível de endpoint e de serviço, garantindo que os usuários só possam acessar os recursos que lhes são permitidos.

* 📚 **Gerenciamento de Cursos:** CRUD completo para Cursos, Módulos e Aulas, com regras de negócio que garantem a integridade dos dados (ex: um instrutor só pode modificar seus próprios cursos).

* 🎓 **Sistema de Matrículas:** Alunos podem se matricular em cursos, e a API gerencia o progresso individual em cada aula, calculando a porcentagem de conclusão.

* 👤 **Perfis de Usuário:** Usuários autenticados podem visualizar e atualizar seus próprios dados de perfil.

* 📄 **Documentação Interativa:** A API é 100% documentada com Swagger (OpenAPI), 

* ✅ **Suíte de Testes Completa:** O projeto conta com uma suíte de mais de 75 testes de integração (End-to-End) que validam todos os cenários de sucesso e de erro da aplicação, garantindo alta confiabilidade e facilitando a manutenção.

## Tecnologias Utilizadas

* **Linguagem e Framework:** Java 17, Spring Boot 3

* **Segurança:** Spring Security 6, JSON Web Tokens (JWT)

* **Banco de Dados:** Spring Data JPA / Hibernate, PostgreSQL

* **Migrações de Banco:** Flyway

* **Testes:** JUnit 5, MockMvc, Testcontainers (para testes de integração com um banco de dados real em ambiente Docker)

* **Documentação da API:** Springdoc OpenAPI (Swagger)

* **Build Tool:** Maven

## Como Executar o Projeto Localmente

### 1. Pré-requisitos
* Java 17 (ou superior)
* Maven 3.8 (ou superior)
* Docker

### 2. Clonar o Repositório

````
git clone https://github.com/ThalysonKaraujo/Course-Platform-Api.git
cd Course-Platform-Api
````

### 3. Configurar o Bando de Dados com Docker

````
docker run --name pg-cursos -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=sua_senha_aqui -e POSTGRES_DB=curso_platform_db -p 5433: 5432 -d postgres
````
Este comando cria um banco dcom uma senha de exemplo. Lembre-se de usar a mesma senha na configuração das variáveis de ambiente.

### 4. Configurar as variáveis de ambiente
Para rodar a aplicação, você precisa configurar as seguintes variáveis de ambiente. A forma mais fácil é através de sua IDE (ex: IntelliJ, na configuração de execução "Application").

* `DB_URL`: `jdbc:postgresql://localhost:5433/curso_platform_db`
* `DB_USER`: `postgres`
* `DB_PASS`: `sua_senha_aqui`
* `JWT_SECRET`: `281f3c5e-9a20-4058-ae57-eec1f618e984`

### 5. Executar a Aplicação
Com o banco de dados rodando e as variáveis configuradas, execute a aplicação usando o Maven.
``````
./mvnw spring-boot:run
``````
A API estará disponivel em http://localhost:8080.

## Acessando a Documentação da API
Após iniciar a aplicação, a documentação interativa do Swagger estará disponível no seu navegador. Use-a para explorar e testar todos os endpoints.

http://localhost:8080/swagger-ui.html