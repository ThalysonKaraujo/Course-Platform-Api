package com.thalyson.digitalcourses.course_platform_backend.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thalyson.digitalcourses.course_platform_backend.model.RoleJPA;
import com.thalyson.digitalcourses.course_platform_backend.repository.RoleRepository;
import com.thalyson.digitalcourses.course_platform_backend.dto.DadosAtualizacaoUser;
import com.thalyson.digitalcourses.course_platform_backend.model.UserJPA;
import com.thalyson.digitalcourses.course_platform_backend.repository.UserRepository;
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

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ContextConfiguration(initializers = UserControllerE2ETest.DataSourceInitializer.class)
@ActiveProfiles("test")
@DisplayName("Testes E2E do Controller de Usuário")
class UserControllerE2ETest {

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
    @Autowired private PasswordEncoder passwordEncoder;

    private UserJPA testUser;
    private String userToken;
    private record DadosTokenJWT(String token) {}

    @BeforeEach
    void setup() throws Exception {
        userRepository.deleteAllInBatch();
        roleRepository.deleteAllInBatch();

        RoleJPA studentRole = roleRepository.saveAndFlush(new RoleJPA("ROLE_STUDENT"));
        testUser = userRepository.saveAndFlush(new UserJPA("usuario.teste@example.com", passwordEncoder.encode("senha123"), "Usuário", "Teste", Set.of(studentRole)));
        userToken = getJwtToken("usuario.teste@example.com", "senha123");
    }

    private String getJwtToken(String email, String password) throws Exception {
        String loginJson = String.format("{\"email\": \"%s\", \"password\": \"%s\"}", email, password);
        MvcResult result = mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andReturn();
        Map<String, String> tokenMap = objectMapper.readValue(result.getResponse().getContentAsString(), Map.class);
        return tokenMap.get("token");
    }

    @Nested
    @DisplayName("Endpoint GET /users/me")
    class GetMyProfileTests {

        @Test
        @DisplayName("Deve retornar 200 e os dados do perfil do usuário autenticado")
        void deveRetornarPerfilDoUsuarioAutenticado() throws Exception {
            mockMvc.perform(get("/users/me")
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(testUser.getId()))
                    .andExpect(jsonPath("$.email").value("usuario.teste@example.com"));
        }

        @Test
        @DisplayName("Deve retornar 401 ao tentar buscar perfil sem autenticação")
        void naoDeveRetornarPerfilSemAutenticacao() throws Exception {
            mockMvc.perform(get("/users/me"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("Endpoint PUT /users/me")
    class UpdateMyProfileTests {

        @Test
        @DisplayName("Deve retornar 200 e um novo token ao atualizar o perfil com sucesso")
        void deveAtualizarPerfilComSucesso() throws Exception {
            DadosAtualizacaoUser updateData = new DadosAtualizacaoUser("usuario.atualizado@example.com", "Usuário", "Atualizado");

            mockMvc.perform(put("/users/me")
                            .header("Authorization", "Bearer " + userToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateData)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token", notNullValue()));
        }

        @Test
        @DisplayName("Deve retornar 400 ao tentar atualizar com dados inválidos (ex: email mal formatado)")
        void naoDeveAtualizarPerfilComDadosInvalidos() throws Exception {
            DadosAtualizacaoUser invalidData = new DadosAtualizacaoUser("email-invalido", "Nome", "Válido");

            mockMvc.perform(put("/users/me")
                            .header("Authorization", "Bearer " + userToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidData)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Deve retornar 401 ao tentar atualizar perfil sem autenticação")
        void naoDeveAtualizarPerfilSemAutenticacao() throws Exception {
            DadosAtualizacaoUser updateData = new DadosAtualizacaoUser("usuario.atualizado@example.com", "Usuário", "Atualizado");

            mockMvc.perform(put("/users/me")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateData)))
                    .andExpect(status().isUnauthorized());
        }
    }
}