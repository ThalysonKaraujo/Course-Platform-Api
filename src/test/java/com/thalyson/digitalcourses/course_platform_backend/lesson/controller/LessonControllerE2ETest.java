package com.thalyson.digitalcourses.course_platform_backend.lesson.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thalyson.digitalcourses.course_platform_backend.category.jpa.CategoryJPA;
import com.thalyson.digitalcourses.course_platform_backend.category.repository.CategoryRepository;
import com.thalyson.digitalcourses.course_platform_backend.course.jpa.CourseJPA;
import com.thalyson.digitalcourses.course_platform_backend.course.repository.CourseRepository;
import com.thalyson.digitalcourses.course_platform_backend.lesson.dto.DadosAtualizacaoLesson;
import com.thalyson.digitalcourses.course_platform_backend.lesson.dto.DadosCadastroLesson;
import com.thalyson.digitalcourses.course_platform_backend.lesson.jpa.LessonJPA;
import com.thalyson.digitalcourses.course_platform_backend.lesson.repository.LessonRepository;
import com.thalyson.digitalcourses.course_platform_backend.module.jpa.ModuleJPA;
import com.thalyson.digitalcourses.course_platform_backend.module.repository.ModuleRepository;
import com.thalyson.digitalcourses.course_platform_backend.role.jpa.RoleJPA;
import com.thalyson.digitalcourses.course_platform_backend.role.repository.RoleRepository;
import com.thalyson.digitalcourses.course_platform_backend.user.jpa.UserJPA;
import com.thalyson.digitalcourses.course_platform_backend.user.repository.UserRepository;
import org.junit.jupiter.api.*;
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
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Set;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
@ContextConfiguration(initializers = {LessonControllerE2ETest.DataSourceInitializer.class})
@ActiveProfiles("test")
@DisplayName("LessonControllerE2ETest")
class LessonControllerE2ETest {

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
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private CourseRepository courseRepository;
    @Autowired private ModuleRepository moduleRepository;
    @Autowired private LessonRepository lessonRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private UserJPA instructorUser, anotherInstructor, studentUser;
    private CourseJPA course;
    private ModuleJPA module;
    private String instructorToken, anotherInstructorToken, studentToken;
    private record DadosTokenJWT(String token) {}

    @BeforeEach
    void setUp() throws Exception {
        lessonRepository.deleteAllInBatch();
        moduleRepository.deleteAllInBatch();
        courseRepository.deleteAllInBatch();
        categoryRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
        roleRepository.deleteAllInBatch();

        RoleJPA instructorRole = roleRepository.saveAndFlush(new RoleJPA("ROLE_INSTRUCTOR"));
        RoleJPA studentRole = roleRepository.saveAndFlush(new RoleJPA("ROLE_STUDENT"));

        instructorUser = userRepository.saveAndFlush(new UserJPA("instructor@test.com", passwordEncoder.encode("123456"), "Instrutor", "Um", Set.of(instructorRole)));
        anotherInstructor = userRepository.saveAndFlush(new UserJPA("other@test.com", passwordEncoder.encode("123456"), "Instrutor", "Dois", Set.of(instructorRole)));
        studentUser = userRepository.saveAndFlush(new UserJPA("student@test.com", passwordEncoder.encode("123456"), "Estudante", "Teste", Set.of(studentRole)));

        instructorToken = login("instructor@test.com", "123456");
        anotherInstructorToken = login("other@test.com", "123456");
        studentToken = login("student@test.com", "123456");

        CategoryJPA category = categoryRepository.saveAndFlush(new CategoryJPA("Programação"));
        course = courseRepository.saveAndFlush(new CourseJPA("Curso de Java", "Desc", "url", true, instructorUser, category));
        module = moduleRepository.saveAndFlush(new ModuleJPA(course, "Módulo 1", "Desc", 1));
    }

    private String login(String email, String password) throws Exception {
        String loginJson = String.format("{\"email\": \"%s\", \"password\": \"%s\"}", email, password);
        ResultActions result = mockMvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON).content(loginJson));
        String responseBody = result.andReturn().getResponse().getContentAsString();
        if (result.andReturn().getResponse().getStatus() != 200) { throw new RuntimeException("Falha no login durante o teste!"); }
        return objectMapper.readValue(responseBody, DadosTokenJWT.class).token();
    }

    @Nested
    @DisplayName("Criação de Aula (POST /modules/{moduleId}/lessons)")
    class CreateLessonTests {

        @Test
        @DisplayName("Deve retornar 201 Created ao criar aula com instrutor dono do curso")
        void deveCriarAulaComSucesso() throws Exception {
            DadosCadastroLesson newLessonDto = new DadosCadastroLesson("Aula de POO", "Desc", "http://video.url", 3600, 1);

            mockMvc.perform(post("/modules/{moduleId}/lessons", module.getId())
                            .header("Authorization", "Bearer " + instructorToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(newLessonDto)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.title", is("Aula de POO")));
        }

        @Test
        @DisplayName("Deve retornar 403 Forbidden ao tentar criar aula em curso de outro instrutor")
        void naoDeveCriarAulaEmCursoDeOutroInstrutor() throws Exception {
            DadosCadastroLesson newLessonDto = new DadosCadastroLesson("Aula de POO", "Desc", "http://video.url", 3600, 1);

            mockMvc.perform(post("/modules/{moduleId}/lessons", module.getId())
                            .header("Authorization", "Bearer " + anotherInstructorToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(newLessonDto)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Deve retornar 404 Not Found ao tentar criar aula em módulo inexistente")
        void naoDeveCriarAulaEmModuloInexistente() throws Exception {
            long nonExistentModuleId = 999L;
            DadosCadastroLesson newLessonDto = new DadosCadastroLesson("Aula de POO", "Desc", "http://video.url", 3600, 1);

            mockMvc.perform(post("/modules/{moduleId}/lessons", nonExistentModuleId)
                            .header("Authorization", "Bearer " + instructorToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(newLessonDto)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Deve retornar 400 Bad Request quando o título está em branco")
        void naoDeveCriarAulaComTituloEmBranco() throws Exception {
            DadosCadastroLesson newLessonDto = new DadosCadastroLesson("", "Desc", "http://video.url", 3600, 1);

            mockMvc.perform(post("/modules/{moduleId}/lessons", module.getId())
                            .header("Authorization", "Bearer " + instructorToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(newLessonDto)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Atualização de Aula (PUT /modules/{moduleId}/lessons/{lessonId})")
    class UpdateLessonTests {

        @Test
        @DisplayName("Deve retornar 200 OK ao atualizar aula pelo instrutor dono do curso")
        void deveAtualizarAulaComSucesso() throws Exception {
            LessonJPA lesson = lessonRepository.saveAndFlush(new LessonJPA(module, "Aula Antiga", "D", "v", 100, 1));
            DadosAtualizacaoLesson updateDto = new DadosAtualizacaoLesson("Aula Nova", "Nova Desc", "http://video.novo", 200, 2);

            mockMvc.perform(put("/modules/{moduleId}/lessons/{lessonId}", module.getId(), lesson.getId())
                            .header("Authorization", "Bearer " + instructorToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title", is("Aula Nova")))
                    .andExpect(jsonPath("$.description", is("Nova Desc")));
        }

        @Test
        @DisplayName("Deve retornar 403 Forbidden ao tentar atualizar aula de outro instrutor")
        void naoDeveAtualizarAulaDeOutroInstrutor() throws Exception {
            LessonJPA lesson = lessonRepository.saveAndFlush(new LessonJPA(module, "Aula Antiga", "D", "v", 100, 1));
            DadosAtualizacaoLesson updateDto = new DadosAtualizacaoLesson("Aula Nova", "Nova Desc", "http://video.novo", 200, 2);

            mockMvc.perform(put("/modules/{moduleId}/lessons/{lessonId}", module.getId(), lesson.getId())
                            .header("Authorization", "Bearer " + anotherInstructorToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDto)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("Busca de Aulas (GET)")
    class GetLessonTests {

        @Test
        @DisplayName("Deve retornar 200 OK e a lista de aulas de um módulo")
        void deveListarAulas() throws Exception {
            lessonRepository.saveAndFlush(new LessonJPA(module, "Aula 1", "D1", "v1", 100, 1));
            lessonRepository.saveAndFlush(new LessonJPA(module, "Aula 2", "D2", "v2", 200, 2));

            mockMvc.perform(get("/modules/{moduleId}/lessons", module.getId())
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(2)))
                    .andExpect(jsonPath("$[0].title", is("Aula 1")));
        }

        @Test
        @DisplayName("Deve retornar 404 Not Found quando o ID da aula não pertence ao módulo na URL")
        void naoDeveEncontrarAulaComModuloIncorreto() throws Exception {
            ModuleJPA anotherModule = moduleRepository.saveAndFlush(new ModuleJPA(course, "Módulo 2", "Desc", 2));
            LessonJPA lessonInAnotherModule = lessonRepository.saveAndFlush(new LessonJPA(anotherModule, "Outra Aula", "D", "v", 1, 1));

            mockMvc.perform(get("/modules/{moduleId}/lessons/{lessonId}", module.getId(), lessonInAnotherModule.getId())
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Exclusão de Aula (DELETE /modules/{moduleId}/lessons/{lessonId})")
    class DeleteLessonTests {

        @Test
        @DisplayName("Deve retornar 204 No Content ao excluir aula pelo instrutor dono do curso")
        void deveExcluirAulaComSucesso() throws Exception {
            LessonJPA lesson = lessonRepository.saveAndFlush(new LessonJPA(module, "Aula a deletar", "D", "v", 1, 1));

            mockMvc.perform(delete("/modules/{moduleId}/lessons/{lessonId}", module.getId(), lesson.getId())
                            .header("Authorization", "Bearer " + instructorToken))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("Deve retornar 404 Not Found ao tentar excluir aula inexistente")
        void naoDeveExcluirAulaInexistente() throws Exception {
            long nonExistentLessonId = 999L;

            mockMvc.perform(delete("/modules/{moduleId}/lessons/{lessonId}", module.getId(), nonExistentLessonId)
                            .header("Authorization", "Bearer " + instructorToken))
                    .andExpect(status().isNotFound());
        }
    }
}