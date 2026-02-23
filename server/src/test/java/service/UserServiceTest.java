package service;

import dataaccess.MemoryDataAccess;
import dataaccess.DataAccessException;
import model.AuthData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest {

    private UserService userService;

    @BeforeEach
    void setup() {
        userService = new UserService(new MemoryDataAccess());
    }

    @Test
    void registerSuccess() throws Exception {
        AuthData auth = userService.register(
                "bob",
                "password",
                "bob@email.com"
        );

        assertNotNull(auth);
        assertEquals("bob", auth.username());
    }

    @Test
    void registerDuplicate() throws Exception {
        userService.register("bob", "pass", "email");

        assertThrows(DataAccessException.class, () ->
                userService.register("bob", "pass", "email")
        );
    }

    @Test
    void loginSuccess() throws Exception {
        userService.register("bob", "pass", "email");

        AuthData auth = userService.login("bob", "pass");

        assertNotNull(auth);
    }

    @Test
    void loginWrongPassword() throws Exception {
        userService.register("bob", "pass", "email");

        assertThrows(DataAccessException.class, () ->
                userService.login("bob", "wrong")
        );
    }

    @Test
    void logoutSuccess() throws Exception {
        AuthData auth = userService.register("bob", "pass", "email");

        assertDoesNotThrow(() ->
                userService.logout(auth.authToken())
        );
    }

    @Test
    void logoutInvalidToken() {
        assertThrows(DataAccessException.class, () ->
                userService.logout("invalid")
        );
    }
}
