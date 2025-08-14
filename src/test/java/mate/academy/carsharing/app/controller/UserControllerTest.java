package mate.academy.carsharing.app.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import mate.academy.carsharing.app.dto.UpdateUserPasswordRequestDto;
import mate.academy.carsharing.app.dto.UpdateUserRequestDto;
import mate.academy.carsharing.app.dto.user.UpdateUserRoleRequestDto;
import mate.academy.carsharing.app.model.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureJsonTesters
@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Sql(scripts = {
        "/db/delete-all-data-db.sql",
        "/db/rentals/add-users-with-roles.sql"
}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "veronika333@gmail.com")
    @DisplayName("GetCurrentUser: should return current authenticated user.")
    void getCurrentUser_ShouldReturnCorrectUser() throws Exception {
        mockMvc.perform(get("/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("veronika333@gmail.com"));
    }

    @Test
    @WithMockUser(username = "veronika333@gmail.com")
    @DisplayName("UpdateUser: should update and return updated user data.")
    void updateUser_ShouldReturnUpdatedData() throws Exception {
        UpdateUserRequestDto request = new UpdateUserRequestDto(
                "veronika333@gmail.com",
                "Nika",
                "Test",
                "1234567893"
        );

        mockMvc.perform(put("/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("veronika333@gmail.com"))
                .andExpect(jsonPath("$.firstName").value("Nika"))
                .andExpect(jsonPath("$.lastName").value("Test"))
                .andExpect(jsonPath("$.telegramChatId").value("1234567893"));
    }

    @Test
    @WithMockUser(username = "veronika333@gmail.com")
    @DisplayName("UpdatePassword: should return 204 No Content on successful password change.")
    void updatePassword_ShouldReturnNoContent() throws Exception {
        UpdateUserPasswordRequestDto request = new UpdateUserPasswordRequestDto(
                "newpass123", "newpass123");

        mockMvc.perform(patch("/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "manager@gmail.com", authorities = {"ROLE_MANAGER"})
    @DisplayName("UpdateUserRole: should update user role when requested by manager.")
    void updateUserRole_ShouldUpdateRole() throws Exception {
        UpdateUserRoleRequestDto request = new UpdateUserRoleRequestDto(Role.RoleName.MANAGER);

        mockMvc.perform(put("/users/2/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles[0]").value("MANAGER"));
    }

    @Test
    @WithMockUser(username = "manager@gmail.com", authorities = {"ROLE_MANAGER"})
    @DisplayName("GetAllUsers: should return paginated list of all users for manager.")
    void getAllUsers_ShouldReturnUsersList() throws Exception {
        mockMvc.perform(get("/users/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2));
    }
}
