package com.thalyson.digitalcourses.course_platform_backend.module.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thalyson.digitalcourses.course_platform_backend.category.jpa.CategoryJPA;
import com.thalyson.digitalcourses.course_platform_backend.category.repository.CategoryRepository;
import com.thalyson.digitalcourses.course_platform_backend.course.jpa.CourseJPA;
import com.thalyson.digitalcourses.course_platform_backend.course.repository.CourseRepository;
import com.thalyson.digitalcourses.course_platform_backend.module.dto.DadosAtualizacaoModule;
import com.thalyson.digitalcourses.course_platform_backend.module.dto.DadosCadastroModule;
import com.thalyson.digitalcourses.course_platform_backend.module.repository.ModuleRepository;
import com.thalyson.digitalcourses.course_platform_backend.role.jpa.RoleJPA;
import com.thalyson.digitalcourses.course_platform_backend.role.repository.RoleRepository;
import com.thalyson.digitalcourses.course_platform_backend.user.jpa.UserJPA;
import com.thalyson.digitalcourses.course_platform_backend.user.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
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
@ContextConfiguration(initializers = {ModuleControllerE2ETest.DataSourceInitializer.class})
@DisplayName("Module Controller E2E Tests")
class ModuleControllerE2ETest {

    @Container
    public static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:15-alpine");

    static class DataSourceInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            TestPropertyValues.of(
                    "spring.datasource.url=" + postgreSQLContainer.getJdbcUrl(),
                    "spring.datasource.username=" + postgreSQLContainer.getUsername(),
                    "spring.datasource.password=" + postgreSQLContainer.getPassword()
            ).applyTo(configurableApplicationContext.getEnvironment());
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private ModuleRepository moduleRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private UserJPA adminUser, instructorUser, studentUser, anotherInstructor;
    private CategoryJPA defaultCategory;
    private String adminToken, instructorToken, studentToken, anotherInstructorToken;

    private record DadosTokenJWT(String token) {}

    @BeforeEach
    void setUp() throws Exception {
        moduleRepository.deleteAll();
        courseRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();

        RoleJPA adminRole = roleRepository.save(new RoleJPA("ROLE_ADMIN"));
        RoleJPA instructorRole = roleRepository.save(new RoleJPA("ROLE_INSTRUCTOR"));
        RoleJPA studentRole = roleRepository.save(new RoleJPA("ROLE_STUDENT"));

        adminUser = userRepository.save(new UserJPA("admin@test.com", passwordEncoder.encode("123456"), "Admin", "User", Set.of(adminRole)));
        instructorUser = userRepository.save(new UserJPA("instructor@test.com", passwordEncoder.encode("123456"), "Instructor", "User", Set.of(instructorRole)));
        studentUser = userRepository.save(new UserJPA("student@test.com", passwordEncoder.encode("123456"), "Student", "User", Set.of(studentRole)));
        anotherInstructor = userRepository.save(new UserJPA("other@test.com", passwordEncoder.encode("123456"), "Other", "Instructor", Set.of(instructorRole)));

        defaultCategory = categoryRepository.save(new CategoryJPA("Programming"));

        adminToken = login("admin@test.com", "123456");
        instructorToken = login("instructor@test.com", "123456");
        studentToken = login("student@test.com", "123456");
        anotherInstructorToken = login("other@test.com", "123456");
    }

    private String login(String email, String password) throws Exception {
        String loginJson = String.format("{\"email\": \"%s\", \"password\": \"%s\"}", email, password);

        ResultActions result = mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson));

        String responseBody = result.andReturn().getResponse().getContentAsString();

        if (result.andReturn().getResponse().getStatus() != 200) {
            throw new RuntimeException("Falha no login durante o teste! Status: " + result.andReturn().getResponse().getStatus() + ", Body: " + responseBody);
        }

        return objectMapper.readValue(responseBody, DadosTokenJWT.class).token();
    }

    private CourseJPA createCourse(String title, UserJPA instructor, CategoryJPA category) {
        CourseJPA course = new CourseJPA(title, "Description for " + title, "http://thumbnail.url", true, instructor, category);
        return courseRepository.save(course);
    }

    @Nested
    @DisplayName("Criação de Módulo(POST)")
    class CreateModuleTests {

        @Test
        @DisplayName("Deve retornar 201 Created com dados válidos fornecidos por um instrutor")
        void deveCriarModuloComSucesso() throws Exception {
            CourseJPA course = createCourse("Java Course", instructorUser, defaultCategory);
            DadosCadastroModule newModuleDto = new DadosCadastroModule("Introduction", "Module description", 1);

            mockMvc.perform(post("/courses/{courseId}/modules", course.getId())
                            .header("Authorization", "Bearer " + instructorToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(newModuleDto)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.title", is("Introduction")))
                    .andExpect(jsonPath("$.orderIndex", is(1)));
        }

        @Test
        @DisplayName("Deve retornar 409 Conflict ao tentar criar módulo com título duplicado no mesmo curso")
        void naoDeveCriarModuloComTituloDuplicado() throws Exception {
            CourseJPA course = createCourse("Java Course", instructorUser, defaultCategory);
            DadosCadastroModule newModuleDto = new DadosCadastroModule("Introduction", "Module description", 1);

            mockMvc.perform(post("/courses/{courseId}/modules", course.getId())
                            .header("Authorization", "Bearer " + instructorToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(newModuleDto)))
                    .andExpect(status().isCreated());

            mockMvc.perform(post("/courses/{courseId}/modules", course.getId())
                            .header("Authorization", "Bearer " + instructorToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(newModuleDto)))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("Deve retornar 403 Forbidden ao tentar criar como um estudante")
        void naoDeveCriarModuloComoEstudante() throws Exception {
            CourseJPA course = createCourse("Java Course", instructorUser, defaultCategory);
            DadosCadastroModule newModuleDto = new DadosCadastroModule("Introduction", "Module description", 1);

            mockMvc.perform(post("/courses/{courseId}/modules", course.getId())
                            .header("Authorization", "Bearer " + studentToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(newModuleDto)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Deve retornar 401 Unauthorized quando nenhum token é fornecido")
        void naoDeveCriarModuloSemToken() throws Exception {
            CourseJPA course = createCourse("Java Course", instructorUser, defaultCategory);
            DadosCadastroModule newModuleDto = new DadosCadastroModule("Introduction", "Module description", 1);

            mockMvc.perform(post("/courses/{courseId}/modules", course.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(newModuleDto)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Deve retornar 404 Not Found ao tentar criar em um curso inexistente")
        void naoDeveCriarModuloEmCursoInexistente() throws Exception {
            long nonExistentCourseId = 999L;
            DadosCadastroModule newModuleDto = new DadosCadastroModule("Introduction", "Module description", 1);

            mockMvc.perform(post("/courses/{courseId}/modules", nonExistentCourseId)
                            .header("Authorization", "Bearer " + instructorToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(newModuleDto)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Deve retornar 400 Bad Request quando o título está em branco")
        void naoDeveCriarModuloComTituloEmBranco() throws Exception {
            CourseJPA course = createCourse("Java Course", instructorUser, defaultCategory);
            DadosCadastroModule newModuleDto = new DadosCadastroModule("", "Module description", 1);

            mockMvc.perform(post("/courses/{courseId}/modules", course.getId())
                            .header("Authorization", "Bearer " + instructorToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(newModuleDto)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Atualização de Módulo(PUT)")
    class UpdateModuleTests {

        @Test
        @DisplayName("Deve retornar 200 OK quando o instrutor do curso atualiza o módulo")
        void deveAtualizarModuloComoInstrutor() throws Exception {
            CourseJPA course = createCourse("Java Course", instructorUser, defaultCategory);
            mockMvc.perform(post("/courses/{courseId}/modules", course.getId())
                    .header("Authorization", "Bearer " + instructorToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(new DadosCadastroModule("Old Title", "Old Desc", 1))));
            Long moduleId = moduleRepository.findAll().get(0).getId();

            DadosAtualizacaoModule updateDto = new DadosAtualizacaoModule("New Title", null, 2);

            mockMvc.perform(put("/courses/{courseId}/modules/{moduleId}", course.getId(), moduleId)
                            .header("Authorization", "Bearer " + instructorToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title", is("New Title")))
                    .andExpect(jsonPath("$.description", is("Old Desc")))
                    .andExpect(jsonPath("$.orderIndex", is(2)));
        }

        @Test
        @DisplayName("Deve retornar 200 OK quando um administrador atualiza o módulo")
        void deveAtualizarModuloComoAdmin() throws Exception {
            CourseJPA course = createCourse("Java Course", instructorUser, defaultCategory);
            mockMvc.perform(post("/courses/{courseId}/modules", course.getId())
                    .header("Authorization", "Bearer " + instructorToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(new DadosCadastroModule("Old Title", "Old Desc", 1))));
            Long moduleId = moduleRepository.findAll().get(0).getId();

            DadosAtualizacaoModule updateDto = new DadosAtualizacaoModule("Updated by Admin", null, null);

            mockMvc.perform(put("/courses/{courseId}/modules/{moduleId}", course.getId(), moduleId)
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title", is("Updated by Admin")));
        }

        @Test
        @DisplayName("Deve retornar 403 Forbidden quando outro instrutor tenta atualizar")
        void naoDeveAtualizarModuloComoOutroInstrutor() throws Exception {
            CourseJPA course = createCourse("Java Course", instructorUser, defaultCategory);
            mockMvc.perform(post("/courses/{courseId}/modules", course.getId())
                    .header("Authorization", "Bearer " + instructorToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(new DadosCadastroModule("Old Title", "Old Desc", 1))));
            Long moduleId = moduleRepository.findAll().get(0).getId();
            DadosAtualizacaoModule updateDto = new DadosAtualizacaoModule("Attempted Update", null, null);

            mockMvc.perform(put("/courses/{courseId}/modules/{moduleId}", course.getId(), moduleId)
                            .header("Authorization", "Bearer " + anotherInstructorToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDto)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Deve retornar 404 Not Found ao tentar atualizar um módulo inexistente")
        void naoDeveAtualizarModuloInexistente() throws Exception {
            CourseJPA course = createCourse("Java Course", instructorUser, defaultCategory);
            long nonExistentModuleId = 999L;
            DadosAtualizacaoModule updateDto = new DadosAtualizacaoModule("Update", null, null);

            mockMvc.perform(put("/courses/{courseId}/modules/{moduleId}", course.getId(), nonExistentModuleId)
                            .header("Authorization", "Bearer " + instructorToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDto)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Exclusão de Módulo(DELETE)")
    class DeleteModuleTests {

        @Test
        @DisplayName("Deve retornar 204 No Content quando o instrutor do curso exclui o módulo")
        void deveExcluirModuloComoInstrutor() throws Exception {
            CourseJPA course = createCourse("Java Course", instructorUser, defaultCategory);
            mockMvc.perform(post("/courses/{courseId}/modules", course.getId())
                    .header("Authorization", "Bearer " + instructorToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(new DadosCadastroModule("To be deleted", "Desc", 1))));
            Long moduleId = moduleRepository.findAll().get(0).getId();

            mockMvc.perform(delete("/courses/{courseId}/modules/{moduleId}", course.getId(), moduleId)
                            .header("Authorization", "Bearer " + instructorToken))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("Deve retornar 403 Forbidden quando outro instrutor tenta excluir")
        void naoDeveExcluirModuloComoOutroInstrutor() throws Exception {
            CourseJPA course = createCourse("Java Course", instructorUser, defaultCategory);
            mockMvc.perform(post("/courses/{courseId}/modules", course.getId())
                    .header("Authorization", "Bearer " + instructorToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(new DadosCadastroModule("To be deleted", "Desc", 1))));
            Long moduleId = moduleRepository.findAll().get(0).getId();

            mockMvc.perform(delete("/courses/{courseId}/modules/{moduleId}", course.getId(), moduleId)
                            .header("Authorization", "Bearer " + anotherInstructorToken))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Deve retornar 404 Not Found ao tentar excluir um módulo inexistente")
        void naoDeveExcluirModuloInexistente() throws Exception {
            CourseJPA course = createCourse("Java Course", instructorUser, defaultCategory);
            long nonExistentModuleId = 999L;

            mockMvc.perform(delete("/courses/{courseId}/modules/{moduleId}", course.getId(), nonExistentModuleId)
                            .header("Authorization", "Bearer " + instructorToken))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Busca de Módulos (GET)")
    class GetModuleTests {

        @Test
        @DisplayName("Deve retornar 200 OK e a lista de módulos de um curso")
        void deveListarModulosDeUmCurso() throws Exception {
            CourseJPA course = createCourse("Java Course", instructorUser, defaultCategory);
            mockMvc.perform(post("/courses/{courseId}/modules", course.getId())
                    .header("Authorization", "Bearer " + instructorToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(new DadosCadastroModule("Module 1", "D1", 1))));
            mockMvc.perform(post("/courses/{courseId}/modules", course.getId())
                    .header("Authorization", "Bearer " + instructorToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(new DadosCadastroModule("Module 2", "D2", 2))));

            mockMvc.perform(get("/courses/{courseId}/modules", course.getId())
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(2)))
                    .andExpect(jsonPath("$[0].title", is("Module 1")));
        }

        @Test
        @DisplayName("Deve retornar 200 OK e os detalhes de um módulo específico")
        void deveBuscarModuloPorId() throws Exception {
            CourseJPA course = createCourse("Java Course", instructorUser, defaultCategory);
            mockMvc.perform(post("/courses/{courseId}/modules", course.getId())
                    .header("Authorization", "Bearer " + instructorToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(new DadosCadastroModule("Specific Module", "Desc", 1))));
            Long moduleId = moduleRepository.findAll().get(0).getId();

            mockMvc.perform(get("/courses/{courseId}/modules/{moduleId}", course.getId(), moduleId)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(moduleId.intValue())))
                    .andExpect(jsonPath("$.title", is("Specific Module")));
        }

        @Test
        @DisplayName("Deve retornar 404 Not Found quando o ID do módulo não pertence ao curso na URL")
        void naoDeveEncontrarModuloComCursoIncorreto() throws Exception {
            CourseJPA course1 = createCourse("Java Course", instructorUser, defaultCategory);
            CourseJPA course2 = createCourse("Python Course", anotherInstructor, defaultCategory);
            mockMvc.perform(post("/courses/{courseId}/modules", course2.getId())
                    .header("Authorization", "Bearer " + anotherInstructorToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(new DadosCadastroModule("Python Module", "Desc", 1))));
            Long ModuleId = moduleRepository.findByCourse(course2).get(0).getId();

            mockMvc.perform(get("/courses/{courseId}/modules/{moduleId}", course1.getId(), ModuleId)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }
    }
}