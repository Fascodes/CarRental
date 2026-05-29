package dev.fascodes.carRental.user.service;


import dev.fascodes.carRental.user.model.UserPrincipal;
import dev.fascodes.carRental.user.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    UserRepository repository;

    CustomUserDetailsService(UserRepository repository){
        this.repository = repository;
    }

    @Override
    public UserDetails loadUserByUsername(String email){
        return new UserPrincipal(repository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User not found " + email)));
    }
}
