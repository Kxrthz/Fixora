package com.fixora;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class AuthControllerTest {
  @Mock UserRepository users;
  @Mock JwtService jwt;
  @Captor ArgumentCaptor<User> userCaptor;

  @Test void registersCustomerWithHashedPassword() {
    when(users.findByEmail("customer@fixora.test")).thenReturn(Optional.empty());
    when(users.save(any(User.class))).thenAnswer(invocation -> { User user=invocation.getArgument(0); user.id=42L; return user; });
    when(jwt.issue(any(User.class), anyLong())).thenReturn("token");
    var response=new AuthController(users,new BCryptPasswordEncoder(),jwt).register(new RegisterRequest("Customer","customer@fixora.test","correct-horse",Role.CUSTOMER));
    assertEquals("token",response.accessToken()); assertEquals(Role.CUSTOMER,response.user().role());
    verify(users).save(userCaptor.capture()); assertNotEquals("correct-horse",userCaptor.getValue().passwordHash);
  }

  @Test void rejectsDuplicateEmail() {
    when(users.findByEmail("exists@fixora.test")).thenReturn(Optional.of(new User()));
    var controller=new AuthController(users,new BCryptPasswordEncoder(),jwt);
    assertThrows(ResponseStatusException.class,()->controller.register(new RegisterRequest("Exists","exists@fixora.test","correct-horse",Role.CUSTOMER)));
  }
}
