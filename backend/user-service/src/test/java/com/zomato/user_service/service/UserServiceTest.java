package com.zomato.user_service.service;

import com.zomato.user_service.model.User;
import com.zomato.user_service.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateUser() {
        User user = new User();
        user.setName("Test User");
        when(userRepository.save(any(User.class))).thenReturn(user);

        User created = userService.createUser(user);

        assertNotNull(created);
        assertEquals("Test User", created.getName());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testGetUserById() {
        User user = new User();
        user.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        Optional<User> found = userService.getUserById(1L);

        assertTrue(found.isPresent());
        assertEquals(1L, found.get().getId());
    }

    @Test
    void testUpdateUser() {
        User existing = new User();
        existing.setId(1L);
        existing.setName("Old Name");

        User details = new User();
        details.setName("New Name");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);

        User updated = userService.updateUser(1L, details);

        assertEquals("New Name", updated.getName());
        verify(userRepository, times(1)).save(existing);
    }
}
