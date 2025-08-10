package com.thalyson.digitalcourses.course_platform_backend.category.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thalyson.digitalcourses.course_platform_backend.category.dto.DadosCadastroCategory;
import com.thalyson.digitalcourses.course_platform_backend.category.jpa.CategoryJPA;
import com.thalyson.digitalcourses.course_platform_backend.category.repository.CategoryRepository;
import com.thalyson.digitalcourses.course_platform_backend.course.jpa.CourseJPA;
import com.thalyson.digitalcourses.course_platform_backend.course.repository.CourseRepository;
import com.thalyson.digitalcourses.course_platform_backend.role.jpa.RoleJPA;
import com.thalyson.digitalcourses.course_platform_backend.role.repository.RoleRepository;
import com.thalyson.digitalcourses.course_platform_backend.user.jpa.UserJPA;
import com.thalyson.digitalcourses.course_platform_backend.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;
import java.util.Set;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ContextConfiguration(initializers = CategoryControllerE2ETest.DataSourceInitializer.class)
@ActiveProfiles("test")
@DisplayName("Testes End-to-End para CategoryController")
class CategoryControllerE2ETest {

    @Container
    private static final PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:16-alpine");

    public static class DataSourceInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(
                    applicationContext,
                    "spring.datasource.url=" + postgresContainer.getJdbcUrl(),
                    "spring.datasource.username=" + postgresContainer.getUsername(),
                    "spring.datasource.password=" + postgresContainer.getPassword()
            );
        }
    }

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private CourseRepository courseRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private String adminToken, instructorToken, studentToken;
    private UserJPA instructorUser;
    private CategoryJPA existingCategory;
    private record DadosTokenJWT(String token) {}


    @BeforeEach
    void setup() throws Exception {
        courseRepository.deleteAllInBatch();
        categoryRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
        roleRepository.deleteAllInBatch();

        RoleJPA adminRole = roleRepository.saveAndFlush(new RoleJPA("ROLE_ADMIN"));
        RoleJPA instructorRole = roleRepository.saveAndFlush(new RoleJPA("ROLE_INSTRUCTOR"));
        RoleJPA studentRole = roleRepository.saveAndFlush(new RoleJPA("ROLE_STUDENT"));

        userRepository.saveAndFlush(new UserJPA("admin@test.com", passwordEncoder.encode("123"), "Admin", "User", Set.of(adminRole)));
        instructorUser = userRepository.saveAndFlush(new UserJPA("instructor@test.com", passwordEncoder.encode("123"), "Instrutor", "User", Set.of(instructorRole)));
        userRepository.saveAndFlush(new UserJPA("student@test.com", passwordEncoder.encode("123"), "Estudante", "User", Set.of(studentRole)));

        adminToken = getJwtToken("admin@test.com", "123");
        instructorToken = getJwtToken("instructor@test.com", "123");
        studentToken = getJwtToken("student@test.com", "123");

        existingCategory = categoryRepository.saveAndFlush(new CategoryJPA("Categoria Padrão"));
    }

    private String getJwtToken(String email, String password) throws Exception {
        String loginJson = String.format("{\"email\": \"%s\", \"password\": \"%s\"}", email, password);
        MvcResult result = mockMvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON).content(loginJson)).andReturn();
        return objectMapper.readValue(result.getResponse().getContentAsString(), DadosTokenJWT.class).token();
    }

    @Nested
    @DisplayName("Criação de Categoria (POST /categories)")
    class CreateCategoryTests {

        @Test
        @DisplayName("Deve criar categoria com sucesso como ADMIN e retornar 201")
        void deveCriarCategoriaComoAdmin() throws Exception {
            DadosCadastroCategory dto = new DadosCadastroCategory("Nova Categoria");
            mockMvc.perform(post("/categories")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.name", is("Nova Categoria")));
        }

        @Test
        @DisplayName("Deve criar categoria com sucesso como INSTRUCTOR e retornar 201")
        void deveCriarCategoriaComoInstrutor() throws Exception {
            DadosCadastroCategory dto = new DadosCadastroCategory("Nova Categoria 2");
            mockMvc.perform(post("/categories")
                            .header("Authorization", "Bearer " + instructorToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("Não deve criar categoria com nome duplicado e deve retornar 409")
        void naoDeveCriarCategoriaComNomeDuplicado() throws Exception {
            DadosCadastroCategory dto = new DadosCadastroCategory("Categoria Padrão");
            mockMvc.perform(post("/categories")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("Não deve criar categoria com dados inválidos (nome em branco) e deve retornar 400")
        void naoDeveCriarCategoriaComDadosInvalidos() throws Exception {
            DadosCadastroCategory dto = new DadosCadastroCategory("");
            mockMvc.perform(post("/categories")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Não deve permitir criar categoria como STUDENT e deve retornar 403")
        void naoDeveCriarCategoriaComoEstudante() throws Exception {
            DadosCadastroCategory dto = new DadosCadastroCategory("Categoria do Estudante");
            mockMvc.perform(post("/categories")
                            .header("Authorization", "Bearer " + studentToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("Busca de Categorias (GET)")
    class GetCategoryTests {

        @Test
        @DisplayName("Deve listar todas as categorias e retornar 200")
        void deveListarTodasCategorias() throws Exception {
            categoryRepository.saveAndFlush(new CategoryJPA("Outra Categoria"));
            mockMvc.perform(get("/categories")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].name", is("Categoria Padrão")))
                    .andExpect(jsonPath("$[1].name", is("Outra Categoria")));
        }

        @Test
        @DisplayName("Deve buscar uma categoria por ID e retornar 200")
        void deveBuscarCategoriaPorId() throws Exception {
            mockMvc.perform(get("/categories/{id}", existingCategory.getId())
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(existingCategory.getId())))
                    .andExpect(jsonPath("$.name", is("Categoria Padrão")));
        }

        @Test
        @DisplayName("Deve retornar 404 ao buscar uma categoria com ID inexistente")
        void deveRetornar404ParaCategoriaInexistente() throws Exception {
            int nonExistentId = 999;
            mockMvc.perform(get("/categories/{id}", nonExistentId)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Atualização de Categoria (PUT /categories/{id})")
    class UpdateCategoryTests {

        @Test
        @DisplayName("Deve atualizar categoria com sucesso como ADMIN e retornar 200")
        void deveAtualizarCategoriaComSucesso() throws Exception {
            DadosCadastroCategory dto = new DadosCadastroCategory("Nome Atualizado");
            mockMvc.perform(put("/categories/{id}", existingCategory.getId())
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name", is("Nome Atualizado")));
        }

        @Test
        @DisplayName("Não deve atualizar categoria com nome duplicado e deve retornar 409")
        void naoDeveAtualizarComNomeDuplicado() throws Exception {
            CategoryJPA outraCategoria = categoryRepository.saveAndFlush(new CategoryJPA("Nome Conflitante"));
            DadosCadastroCategory dto = new DadosCadastroCategory(outraCategoria.getName());
            mockMvc.perform(put("/categories/{id}", existingCategory.getId())
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("Não deve permitir atualização por INSTRUCTOR e deve retornar 403")
        void naoDevePermitirAtualizacaoPorInstrutor() throws Exception {
            DadosCadastroCategory dto = new DadosCadastroCategory("Update por Instrutor");
            mockMvc.perform(put("/categories/{id}", existingCategory.getId())
                            .header("Authorization", "Bearer " + instructorToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("Exclusão de Categoria (DELETE /categories/{id})")
    class DeleteCategoryTests {

        @Test
        @DisplayName("Deve excluir categoria sem cursos associados com sucesso e retornar 204")
        void deveExcluirCategoriaComSucesso() throws Exception {
            mockMvc.perform(delete("/categories/{id}", existingCategory.getId())
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("Não deve excluir categoria com cursos associados e deve retornar 409")
        void naoDeveExcluirCategoriaComCursosAssociados() throws Exception {
            CourseJPA course = new CourseJPA("Curso Teste", "Desc", "url", true, instructorUser, existingCategory);
            courseRepository.saveAndFlush(course);
            mockMvc.perform(delete("/categories/{id}", existingCategory.getId())
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("Não deve permitir exclusão por INSTRUCTOR e deve retornar 403")
        void naoDevePermitirExclusaoPorInstrutor() throws Exception {
            mockMvc.perform(delete("/categories/{id}", existingCategory.getId())
                            .header("Authorization", "Bearer " + instructorToken))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Deve retornar 404 ao tentar excluir categoria inexistente")
        void deveRetornar404ParaCategoriaInexistente() throws Exception {
            int nonExistentId = 999;
            mockMvc.perform(delete("/categories/{id}", nonExistentId)
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isNotFound());
        }
    }
}