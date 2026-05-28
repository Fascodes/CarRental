package dev.fascodes.carRental.common.security;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.util.List;

public class WithMockAuthenticatedUserSecurityContextFactory
        implements WithSecurityContextFactory<WithMockAuthenticatedUser> {

    @Override
    public SecurityContext createSecurityContext(WithMockAuthenticatedUser annotation) {
        AuthenticatedUser principal = new AuthenticatedUser(annotation.email(), annotation.role());
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                principal, null,
                List.of(new SimpleGrantedAuthority("ROLE_" + annotation.role())));
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        return context;
    }
}
