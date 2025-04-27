package poc.java.oauth;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.client.registration.ClientRegistration;

import java.util.Map;

public record RegistrationsConfiguration(Map<User, ClientRegistration> registrations) {
}
