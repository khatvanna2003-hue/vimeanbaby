package com.vimeanbaby.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.vimeanbaby.exception.ResourceNotFoundException;
import com.vimeanbaby.user.entity.Role;
import com.vimeanbaby.user.entity.User;
import com.vimeanbaby.user.mapper.UserMapper;
import com.vimeanbaby.user.repository.AddressRepository;
import com.vimeanbaby.user.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdminCustomerServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private AdminCustomerService service;

    @Test
    void disablingCustomerRevokesTokens() {
        User user = customer(true);
        when(userRepository.findByIdAndRole(7L, Role.CUSTOMER)).thenReturn(Optional.of(user));
        when(addressRepository.findByUserIdOrderByDefaultAddressDescCreatedAtDesc(7L)).thenReturn(List.of());

        var result = service.setActive(7L, false);

        assertThat(user.getActive()).isFalse();
        assertThat(user.getTokenVersion()).isEqualTo(1);
        assertThat(result.active()).isFalse();
    }

    @Test
    void enablingCustomerKeepsTokenVersion() {
        User user = customer(false);
        when(userRepository.findByIdAndRole(7L, Role.CUSTOMER)).thenReturn(Optional.of(user));
        when(addressRepository.findByUserIdOrderByDefaultAddressDescCreatedAtDesc(7L)).thenReturn(List.of());

        service.setActive(7L, true);

        assertThat(user.getActive()).isTrue();
        assertThat(user.getTokenVersion()).isZero();
    }

    @Test
    void adminAccountsCannotBeManagedAsCustomers() {
        when(userRepository.findByIdAndRole(1L, Role.CUSTOMER)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.setActive(1L, false)).isInstanceOf(ResourceNotFoundException.class);
    }

    private User customer(boolean active) {
        User user = new User();
        user.setId(7L);
        user.setFullName("Sok Dara");
        user.setEmail("dara@example.com");
        user.setPhone("012345678");
        user.setRole(Role.CUSTOMER);
        user.setActive(active);
        return user;
    }
}
