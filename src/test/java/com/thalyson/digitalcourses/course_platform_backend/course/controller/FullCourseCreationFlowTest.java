package com.thalyson.digitalcourses.course_platform_backend.course.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thalyson.digitalcourses.course_platform_backend.auth.dto.DadosLogin;
import com.thalyson.digitalcourses.course_platform_backend.category.dto.DadosCadastroCategory;
import com.thalyson.digitalcourses.course_platform_backend.course.dto.DadosCadastroCourse;
import com.thalyson.digitalcourses.course_platform_backend.exception.ResourceNotFoundException;
import com.thalyson.digitalcourses.course_platform_backend.lesson.dto.DadosCadastroLesson;
import com.thalyson.digitalcourses.course_platform_backend.module.dto.DadosCadastroModule;
import com.thalyson.digitalcourses.course_platform_backend.role.jpa.RoleJPA;
import com.thalyson.digitalcourses.course_platform_backend.role.repository.RoleRepository;
import com.thalyson.digitalcourses.course_platform_backend.user.jpa.UserJPA;
import com.thalyson.digitalcourses.course_platform_backend.user.repository.UserRepository;
import com.thalyson.digitalcourses.course_platform_backend.category.repository.CategoryRepository;
import com.thalyson.digitalcourses.course_platform_backend.course.repository.CourseRepository;
import com.thalyson.digitalcourses.course_platform_backend.module.repository.ModuleRepository;
import com.thalyson.digitalcourses.course_platform_backend.lesson.repository.LessonRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ContextConfiguration(initializers = FullCourseCreationFlowTest.DataSourceInitializer.class)
@ActiveProfiles("test")
class FullCourseCreationFlowTest {

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

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private ModuleRepository moduleRepository;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String jwtToken;
    private final String instructorEmail = "fullflow.instructor@example.com";
    private final String password = "SenhaSegura123";

    @BeforeEach
    void setup() throws Exception {

        lessonRepository.deleteAllInBatch();
        moduleRepository.deleteAllInBatch();
        courseRepository.deleteAllInBatch();
        categoryRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
        roleRepository.deleteAllInBatch();

        RoleJPA instructorRole = roleRepository.findByName("ROLE_INSTRUCTOR").orElseGet(() ->
                roleRepository.saveAndFlush(new RoleJPA("ROLE_INSTRUCTOR"))
        );

        UserJPA instructor = new UserJPA();
        instructor.setEmail(instructorEmail);
        instructor.setPassword(passwordEncoder.encode(password));
        instructor.setFirstName("Full");
        instructor.setLastName("Flow");
        instructor.setRoles(Collections.singleton(instructorRole));
        userRepository.saveAndFlush(instructor);

        DadosLogin dadosLogin = new DadosLogin(instructorEmail, password);
        String jsonLogin = objectMapper.writeValueAsString(dadosLogin);

        MvcResult result = mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonLogin))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        Map<String, String> tokenMap = objectMapper.readValue(responseBody, Map.class);
        this.jwtToken = tokenMap.get("token");
    }

    @Test
    @DisplayName("Deve completar o fluxo de criação de um curso, módulo e aula com sucesso")
    void deveCriarFluxoCompletoComSucesso() throws Exception {
        DadosCadastroCategory dadosCategoria = new DadosCadastroCategory("Desenvolvimento de Software");
        String jsonCategoria = objectMapper.writeValueAsString(dadosCategoria);

        MvcResult resultCategoria = mockMvc.perform(post("/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + this.jwtToken)
                        .content(jsonCategoria))
                .andExpect(status().isCreated())
                .andReturn();

        Integer categoriaId = objectMapper.readTree(resultCategoria.getResponse().getContentAsString()).get("id").asInt();


        UserJPA instructor = userRepository.findByEmail(instructorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário instrutor não encontrado."));

        DadosCadastroCourse dadosCurso = new DadosCadastroCourse(
                "Curso E2E Test", "Teste de fluxo completo.", "http://thumb.com/e2e.png", true,
                instructor.getId(), categoriaId
        );
        String jsonCurso = objectMapper.writeValueAsString(dadosCurso);

        MvcResult resultCurso = mockMvc.perform(post("/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + this.jwtToken)
                        .content(jsonCurso))
                .andExpect(status().isCreated())
                .andReturn();

        Long cursoId = objectMapper.readTree(resultCurso.getResponse().getContentAsString()).get("id").asLong();


        DadosCadastroModule dadosModulo = new DadosCadastroModule(
                "Módulo 1: Introdução", "Primeiro módulo do curso.", 1
        );
        String jsonModulo = objectMapper.writeValueAsString(dadosModulo);

        MvcResult resultModulo = mockMvc.perform(post("/courses/{courseId}/modules", cursoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + this.jwtToken)
                        .content(jsonModulo))
                .andExpect(status().isCreated())
                .andReturn();

        Long moduleId = objectMapper.readTree(resultModulo.getResponse().getContentAsString()).get("id").asLong();


        DadosCadastroLesson dadosAula = new DadosCadastroLesson(
                "Aula 1: Setup", "Instalação do ambiente.", "https://youtu.be/setup", 3600, 1
        );
        String jsonAula = objectMapper.writeValueAsString(dadosAula);

        mockMvc.perform(post("/modules/{moduleId}/lessons", moduleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + this.jwtToken)
                        .content(jsonAula))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Aula 1: Setup"));
    }
}