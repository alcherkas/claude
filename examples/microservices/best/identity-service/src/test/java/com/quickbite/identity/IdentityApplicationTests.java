package com.quickbite.identity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quickbite.identity.config.JwtService;
import com.quickbite.identity.domain.Role;
import com.quickbite.identity.dto.LoginRequest;
import com.quickbite.identity.dto.LoginResponse;
import com.quickbite.identity.dto.UserResponse;
import com.quickbite.identity.service.InvalidCredentialsException;
import com.quickbite.identity.service.UserService;
import com.quickbite.identity.web.AuthController;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Context-load smoke test plus a MockMvc slice test for the auth endpoint.
 */
class IdentityApplicationTests {

    @SpringBootTest
    static class ContextLoadTest {

        @Autowired
        private ApplicationContext context;

        @Test
        void contextLoads() {
            assertThat(context).isNotNull();
            assertThat(context.getBean(UserService.class)).isNotNull();
            assertThat(context.getBean(JwtService.class)).isNotNull();
        }
    }

    @WebMvcTest(AuthController.class)
    @Import(com.quickbite.identity.web.GlobalExceptionHandler.class)
    @WithMockUser
    static class AuthControllerSliceTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private UserService userService;

        @Test
        void loginReturnsTokenForValidCredentials() throws Exception {
            UserResponse user = new UserResponse(
                    UUID.randomUUID(), "ada@quickbite.dev", "Ada Lovelace",
                    Role.CUSTOMER, true, Instant.now());
            LoginResponse response = new LoginResponse("jwt-token", Instant.now().plusSeconds(3600), user);
            when(userService.login(new LoginRequest("ada@quickbite.dev", "password1")))
                    .thenReturn(response);

            mockMvc.perform(post("/api/auth/login")
                            .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    new LoginRequest("ada@quickbite.dev", "password1"))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").value("jwt-token"))
                    .andExpect(jsonPath("$.user.email").value("ada@quickbite.dev"))
                    .andExpect(jsonPath("$.user.role").value("CUSTOMER"));
        }

        @Test
        void loginReturnsStandardErrorBodyForBadCredentials() throws Exception {
            when(userService.login(new LoginRequest("nobody@quickbite.dev", "wrongpass")))
                    .thenThrow(new InvalidCredentialsException("Invalid email or password"));

            mockMvc.perform(post("/api/auth/login")
                            .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    new LoginRequest("nobody@quickbite.dev", "wrongpass"))))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value(401))
                    .andExpect(jsonPath("$.error").value("Unauthorized"))
                    .andExpect(jsonPath("$.path").value("/api/auth/login"));
        }
    }
}
