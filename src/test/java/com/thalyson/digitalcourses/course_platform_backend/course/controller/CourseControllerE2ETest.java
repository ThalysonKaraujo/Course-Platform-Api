package com.thalyson.digitalcourses.course_platform_backend.course.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thalyson.digitalcourses.course_platform_backend.auth.dto.DadosLogin;
import com.thalyson.digitalcourses.course_platform_backend.category.jpa.CategoryJPA;
import com.thalyson.digitalcourses.course_platform_backend.category.repository.CategoryRepository;
import com.thalyson.digitalcourses.course_platform_backend.course.dto.DadosCadastroCourse;
import com.thalyson.digitalcourses.course_platform_backend.course.jpa.CourseJPA;
import com.thalyson.digitalcourses.course_platform_backend.course.repository.CourseRepository;
import com.thalyson.digitalcourses.course_platform_backend.role.jpa.RoleJPA;
import com.thalyson.digitalcourses.course_platform_backend.role.repository.RoleRepository;
import com.thalyson.digitalcourses.course_platform_backend.user.jpa.UserJPA;
import com.thalyson.digitalcourses.course_platform_backend.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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

import java.util.Collections;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers // Habilita o Testcontainers
@ContextConfiguration(initializers = CourseControllerE2ETest.DataSourceInitializer.class) // Aponta para o inicializador
@ActiveProfiles("test")
class CourseControllerE2ETest {

    @Container // Define o container do banco de dados
    private static final PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:16-alpine");

    // Classe interna para configurar a conexão com o banco do container
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

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String instructorToken;
    private Long instructorId;
    private Integer categoryId;
    private Long courseId;

    @BeforeEach
    void setup() throws Exception {
        courseRepository.deleteAllInBatch();
        categoryRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
        roleRepository.deleteAllInBatch();

        RoleJPA instructorRole = roleRepository.findByName("ROLE_INSTRUCTOR").orElseGet(() ->
                roleRepository.saveAndFlush(new RoleJPA("ROLE_INSTRUCTOR"))
        );

        UserJPA instructor = new UserJPA();
        instructor.setEmail("instrutor.teste@example.com");
        instructor.setPassword(passwordEncoder.encode("senha123"));
        instructor.setFirstName("Instrutor");
        instructor.setLastName("Teste");
        instructor.setRoles(Collections.singleton(instructorRole));
        userRepository.saveAndFlush(instructor);
        this.instructorId = instructor.getId();

        CategoryJPA category = new CategoryJPA("Desenvolvimento Web");
        categoryRepository.saveAndFlush(category);
        this.categoryId = category.getId();

        CourseJPA course = new CourseJPA();
        course.setTitle("Curso para Testes");
        course.setDescription("Descrição do curso de teste.");
        course.setInstructor(instructor);
        course.setCategory(category);
        courseRepository.saveAndFlush(course);
        this.courseId = course.getId();

        this.instructorToken = getJwtToken("instrutor.teste@example.com", "senha123");
    }

    private String getJwtToken(String email, String password) throws Exception {
        DadosLogin dadosLogin = new DadosLogin(email, password);
        MvcResult result = mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dadosLogin)))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        Map<String, String> tokenMap = objectMapper.readValue(responseBody, Map.class);
        return tokenMap.get("token");
    }

    @Test
    @DisplayName("Deve criar um novo curso e retornar 201 Created")
    void deveCriarCursoComSucesso() throws Exception {
        DadosCadastroCourse dadosCurso = new DadosCadastroCourse(
                "Curso de Spring Boot Avançado",
                "Domine os conceitos mais complexos do Spring Boot.",
                "http://thumbnail.com/advanced.png",
                true,
                this.instructorId,
                this.categoryId
        );
        String jsonCurso = objectMapper.writeValueAsString(dadosCurso);

        mockMvc.perform(post("/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + this.instructorToken)
                        .content(jsonCurso))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.title").value("Curso de Spring Boot Avançado"));
    }

    @Test
    @DisplayName("Deve retornar 200 OK e os detalhes de um curso existente")
    void deveRetornarDetalhesDoCurso() throws Exception {
        mockMvc.perform(get("/courses/{id}", this.courseId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(this.courseId))
                .andExpect(jsonPath("$.title").value("Curso para Testes"));
    }

    @Test
    @DisplayName("Deve retornar 404 Not Found para um curso que não existe")
    void deveRetornar404ParaCursoInexistente() throws Exception {
        Long idInexistente = 999L;
        mockMvc.perform(get("/courses/{id}", idInexistente))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve deletar um curso e retornar 204 No Content")
    void deveDeletarCursoComSucesso() throws Exception {
        mockMvc.perform(delete("/courses/{id}", this.courseId)
                        .header("Authorization", "Bearer " + this.instructorToken))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Deve retornar 404 Not Found ao tentar deletar um curso que não existe")
    void deveRetornar404AoDeletarCursoInexistente() throws Exception {
        Long idInexistente = 999L;
        mockMvc.perform(delete("/courses/{id}", idInexistente)
                        .header("Authorization", "Bearer " + this.instructorToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Não deve criar curso sem token e deve retornar 401 Unauthorized")
    void naoDeveCriarCursoSemTokenERetornar401() throws Exception {
        DadosCadastroCourse dadosCurso = new DadosCadastroCourse(
                "Curso Secreto",
                "Um curso que não deveria ser criado.",
                "http://thumbnail.com/secret.png",
                false,
                this.instructorId,
                this.categoryId
        );
        String jsonCurso = objectMapper.writeValueAsString(dadosCurso);

        mockMvc.perform(post("/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonCurso))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Não deve criar curso com token de STUDENT e deve retornar 403 Forbidden")
    void naoDeveCriarCursoComTokenDeStudentERetornar403() throws Exception {
        RoleJPA studentRole = roleRepository.findByName("ROLE_STUDENT").orElseGet(() ->
                roleRepository.saveAndFlush(new RoleJPA("ROLE_STUDENT"))
        );

        UserJPA studentUser = new UserJPA();
        studentUser.setEmail("student.teste@example.com");
        studentUser.setPassword(passwordEncoder.encode("senha123"));
        studentUser.setFirstName("Estudante");
        studentUser.setLastName("Teste");
        studentUser.setRoles(Collections.singleton(studentRole));
        userRepository.saveAndFlush(studentUser);

        String studentToken = getJwtToken("student.teste@example.com", "senha123");

        DadosCadastroCourse dadosCurso = new DadosCadastroCourse(
                "Curso do Estudante",
                "Um curso que um estudante não pode criar.",
                "http://thumbnail.com/student.png",
                false,
                this.instructorId,
                this.categoryId
        );
        String jsonCurso = objectMapper.writeValueAsString(dadosCurso);

        mockMvc.perform(post("/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + studentToken)
                        .content(jsonCurso))
                .andExpect(status().isForbidden());
    }
}