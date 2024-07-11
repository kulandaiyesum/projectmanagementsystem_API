package com.mk.controller;

import com.mk.config.JwtProvider;
import com.mk.model.User;
import com.mk.repository.UserRepository;
import com.mk.response.AuthResponse;
import com.mk.request.LoginRequest;
import com.mk.service.CustomUserDetailsImpl;
import com.mk.service.SubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private CustomUserDetailsImpl customUserDetails;
    @Autowired
    private SubscriptionService subscriptionService;


    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> createUserHandler(@RequestBody User user) throws Exception {
        User isUserExist = userRepository.findByEmail(user.getEmail());
        if (isUserExist != null) {
            throw new Exception("Email already exist with another account");
        }
        User createUser = new User();
        createUser.setPassword(passwordEncoder.encode(user.getPassword()));
        createUser.setEmail(user.getEmail());
        createUser.setFullName(user.getFullName());

        User savedUser = userRepository.save(createUser);
        subscriptionService.createSubscription(savedUser);
        Authentication authentication = new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = JwtProvider.generateToken(authentication);
        AuthResponse res = new AuthResponse();
        res.setJwt(jwt);
        res.setMessage("signup success");
        return new ResponseEntity<>(res, HttpStatus.CREATED);

    }


    @PostMapping("/signin")
    public ResponseEntity<AuthResponse> signin(@RequestBody LoginRequest loginRequest) {
        String username = loginRequest.getEmail();
        String password = loginRequest.getPassword();

        Authentication authentication = authenticate(username, password);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = JwtProvider.generateToken(authentication);
        AuthResponse res = new AuthResponse();
        res.setJwt(jwt);
        res.setMessage("signing success");
        return new ResponseEntity<>(res, HttpStatus.CREATED);
    }

    private Authentication authenticate(String username, String password) {
        UserDetails userDetails = customUserDetails.loadUserByUsername(username);
        if(userDetails == null) {
            throw new BadCredentialsException("Invalid username");
        }
        if(!passwordEncoder.matches(password, userDetails.getPassword())){
            throw new BadCredentialsException("Invalid password");
        }

        return new UsernamePasswordAuthenticationToken(userDetails,null, userDetails.getAuthorities());
    }

}
