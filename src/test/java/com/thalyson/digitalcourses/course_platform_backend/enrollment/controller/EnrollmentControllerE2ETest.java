package com.thalyson.digitalcourses.course_platform_backend.enrollment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thalyson.digitalcourses.course_platform_backend.category.jpa.CategoryJPA;
import com.thalyson.digitalcourses.course_platform_backend.category.repository.CategoryRepository;
import com.thalyson.digitalcourses.course_platform_backend.course.jpa.CourseJPA;
import com.thalyson.digitalcourses.course_platform_backend.course.repository.CourseRepository;
import com.thalyson.digitalcourses.course_platform_backend.enrollment.jpa.EnrollmentJPA;
import com.thalyson.digitalcourses.course_platform_backend.enrollment.repository.EnrollmentRepository;
import com.thalyson.digitalcourses.course_platform_backend.lesson.jpa.LessonJPA;
import com.thalyson.digitalcourses.course_platform_backend.lesson.repository.LessonRepository;
import com.thalyson.digitalcourses.course_platform_backend.module.jpa.ModuleJPA;
import com.thalyson.digitalcourses.course_platform_backend.module.repository.ModuleRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ContextConfiguration(initializers = EnrollmentControllerE2ETest.DataSourceInitializer.class)
@ActiveProfiles("test")
@DisplayName("Testes E2E do Controller de Matrículas")
class EnrollmentControllerE2ETest {

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
    @Autowired private CourseRepository courseRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private ModuleRepository moduleRepository;
    @Autowired private LessonRepository lessonRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private EnrollmentRepository enrollmentRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private UserJPA studentUser1, studentUser2, instructorUser, adminUser;
    private CourseJPA course;
    private LessonJPA lesson;
    private String studentToken1, studentToken2, adminToken;
    private record DadosTokenJWT(String token) {}

    @BeforeEach
    void setup() throws Exception {
        enrollmentRepository.deleteAllInBatch();
        lessonRepository.deleteAllInBatch();
        moduleRepository.deleteAllInBatch();
        courseRepository.deleteAllInBatch();
        categoryRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
        roleRepository.deleteAllInBatch();

        RoleJPA studentRole = roleRepository.saveAndFlush(new RoleJPA("ROLE_STUDENT"));
        RoleJPA instructorRole = roleRepository.saveAndFlush(new RoleJPA("ROLE_INSTRUCTOR"));
        RoleJPA adminRole = roleRepository.saveAndFlush(new RoleJPA("ROLE_ADMIN"));

        studentUser1 = userRepository.saveAndFlush(new UserJPA("aluno1@test.com", passwordEncoder.encode("123"), "Aluno", "Um", Set.of(studentRole)));
        studentUser2 = userRepository.saveAndFlush(new UserJPA("aluno2@test.com", passwordEncoder.encode("123"), "Aluno", "Dois", Set.of(studentRole)));
        instructorUser = userRepository.saveAndFlush(new UserJPA("instrutor@test.com", passwordEncoder.encode("123"), "Instrutor", "Teste", Set.of(instructorRole)));
        adminUser = userRepository.saveAndFlush(new UserJPA("admin@test.com", passwordEncoder.encode("123"), "Admin", "User", Set.of(adminRole)));

        CategoryJPA category = categoryRepository.saveAndFlush(new CategoryJPA("Programação"));
        course = courseRepository.saveAndFlush(new CourseJPA("Curso de Teste", "Desc", "url", true, instructorUser, category));
        ModuleJPA module = moduleRepository.saveAndFlush(new ModuleJPA(course, "Módulo 1", "Desc", 1));
        lesson = lessonRepository.saveAndFlush(new LessonJPA(module, "Aula 1", "Desc", "url", 300, 1));

        studentToken1 = getJwtToken("aluno1@test.com", "123");
        studentToken2 = getJwtToken("aluno2@test.com", "123");
        adminToken = getJwtToken("admin@test.com", "123");
    }

    private String getJwtToken(String email, String password) throws Exception {
        String loginJson = String.format("{\"email\": \"%s\", \"password\": \"%s\"}", email, password);
        MvcResult result = mockMvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON).content(loginJson)).andExpect(status().isOk()).andReturn();
        Map<String, String> tokenMap = objectMapper.readValue(result.getResponse().getContentAsString(), Map.class);
        return tokenMap.get("token");
    }

    private EnrollmentJPA createEnrollment(UserJPA student, CourseJPA courseToEnroll) {
        return enrollmentRepository.saveAndFlush(new EnrollmentJPA(student, courseToEnroll));
    }

    @Nested
    @DisplayName("Criação de Matrícula (POST /enrollments)")
    class CreateEnrollmentTests {

        @Test
        @DisplayName("Deve permitir um usuário se matricular em um curso com sucesso")
        void deveCriarMatriculaComSucesso() throws Exception {
            mockMvc.perform(post("/enrollments")
                            .header("Authorization", "Bearer " + studentToken1)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(String.format("{\"userId\":%d,\"courseId\":%d}", studentUser1.getId(), course.getId())))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.user.id").value(studentUser1.getId()))
                    .andExpect(jsonPath("$.course.id").value(course.getId()));
        }

        @Test
        @DisplayName("Deve retornar 409 ao tentar matricular em um curso que já está matriculado")
        void naoDeveCriarMatriculaDuplicada() throws Exception {
            createEnrollment(studentUser1, course);

            mockMvc.perform(post("/enrollments")
                            .header("Authorization", "Bearer " + studentToken1)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(String.format("{\"userId\":%d,\"courseId\":%d}", studentUser1.getId(), course.getId())))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("Deve retornar 400 ao tentar matricular outro usuário")
        void naoDevePermitirMatricularOutroUsuario() throws Exception {
            mockMvc.perform(post("/enrollments")
                            .header("Authorization", "Bearer " + studentToken1)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(String.format("{\"userId\":%d,\"courseId\":%d}", studentUser2.getId(), course.getId())))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Deve retornar 404 ao tentar matricular em um curso inexistente")
        void naoDeveCriarMatriculaParaCursoInexistente() throws Exception {
            long nonExistentCourseId = 999L;
            mockMvc.perform(post("/enrollments")
                            .header("Authorization", "Bearer " + studentToken1)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(String.format("{\"userId\":%d,\"courseId\":%d}", studentUser1.getId(), nonExistentCourseId)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Atualização de Progresso (PUT /enrollments/{id}/progress)")
    class UpdateProgressTests {

        @Test
        @DisplayName("Deve permitir o dono da matrícula atualizar seu progresso em uma aula")
        void deveAtualizarProgressoComSucesso() throws Exception {
            EnrollmentJPA enrollment = createEnrollment(studentUser1, course);

            mockMvc.perform(put("/enrollments/{enrollmentId}/progress", enrollment.getId())
                            .header("Authorization", "Bearer " + studentToken1)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(String.format("{\"lessonId\":%d}", lesson.getId())))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Deve retornar 403 ao tentar atualizar progresso de outro aluno")
        void naoDeveAtualizarProgressoDeOutroAluno() throws Exception {
            EnrollmentJPA enrollment = createEnrollment(studentUser1, course);

            mockMvc.perform(put("/enrollments/{enrollmentId}/progress", enrollment.getId())
                            .header("Authorization", "Bearer " + studentToken2)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(String.format("{\"lessonId\":%d}", lesson.getId())))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Deve retornar 404 para uma matrícula inexistente")
        void naoDeveAtualizarProgressoDeMatriculaInexistente() throws Exception {
            long nonExistentEnrollmentId = 999L;
            mockMvc.perform(put("/enrollments/{enrollmentId}/progress", nonExistentEnrollmentId)
                            .header("Authorization", "Bearer " + studentToken1)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(String.format("{\"lessonId\":%d}", lesson.getId())))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Deve retornar 400 ao tentar registrar progresso com aula de outro curso")
        void naoDeveAtualizarProgressoComAulaDeOutroCurso() throws Exception {

            CourseJPA anotherCourse = courseRepository.saveAndFlush(new CourseJPA("Outro Curso", "Desc", "url", true, instructorUser, course.getCategory()));
            ModuleJPA anotherModule = moduleRepository.saveAndFlush(new ModuleJPA(anotherCourse, "Módulo Outro", "Desc", 1));
            LessonJPA lessonFromAnotherCourse = lessonRepository.saveAndFlush(new LessonJPA(anotherModule, "Aula Outra", "Desc", "url", 1, 1));

            EnrollmentJPA enrollment = createEnrollment(studentUser1, course);

            mockMvc.perform(put("/enrollments/{enrollmentId}/progress", enrollment.getId())
                            .header("Authorization", "Bearer " + studentToken1)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(String.format("{\"lessonId\":%d}", lessonFromAnotherCourse.getId())))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Busca de Matrículas (GET)")
    class GetEnrollmentTests {

        @Test
        @DisplayName("Deve listar matrículas por usuário")
        void deveListarMatriculasPorUsuario() throws Exception {
            createEnrollment(studentUser1, course);

            mockMvc.perform(get("/enrollments/user/{userId}", studentUser1.getId())
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].user.id").value(studentUser1.getId()));
        }

        @Test
        @DisplayName("Deve listar matrículas por curso")
        void deveListarMatriculasPorCurso() throws Exception {
            createEnrollment(studentUser1, course);
            createEnrollment(studentUser2, course);

            mockMvc.perform(get("/enrollments/course/{courseId}", course.getId())
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)));
        }

        @Test
        @DisplayName("Deve buscar matrícula por ID com sucesso pelo dono")
        void deveBuscarMatriculaPorIdComSucesso() throws Exception {
            EnrollmentJPA enrollment = createEnrollment(studentUser1, course);

            mockMvc.perform(get("/enrollments/{id}", enrollment.getId())
                            .header("Authorization", "Bearer " + studentToken1))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(enrollment.getId()));
        }

        @Test
        @DisplayName("Deve retornar 403 ao buscar matrícula de outro aluno")
        void naoDeveBuscarMatriculaDeOutroAluno() throws Exception {
            EnrollmentJPA enrollment = createEnrollment(studentUser1, course);

            mockMvc.perform(get("/enrollments/{id}", enrollment.getId())
                            .header("Authorization", "Bearer " + studentToken2))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("Cancelamento de Matrícula (DELETE /enrollments/{id})")
    class DeleteEnrollmentTests {

        @Test
        @DisplayName("Deve permitir um aluno cancelar a própria matrícula")
        void deveCancelarMatriculaComSucesso() throws Exception {
            EnrollmentJPA enrollment = createEnrollment(studentUser1, course);

            mockMvc.perform(delete("/enrollments/{enrollmentId}", enrollment.getId())
                            .header("Authorization", "Bearer " + studentToken1))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("Deve permitir um admin cancelar qualquer matrícula")
        void devePermitirAdminCancelarQualquerMatricula() throws Exception {
            EnrollmentJPA enrollment = createEnrollment(studentUser1, course);

            mockMvc.perform(delete("/enrollments/{enrollmentId}", enrollment.getId())
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("Deve retornar 403 ao tentar cancelar matrícula de outro aluno")
        void naoDeveCancelarMatriculaDeOutroAluno() throws Exception {
            EnrollmentJPA enrollment = createEnrollment(studentUser1, course);

            mockMvc.perform(delete("/enrollments/{enrollmentId}", enrollment.getId())
                            .header("Authorization", "Bearer " + studentToken2))
                    .andExpect(status().isForbidden());
        }


    }
}