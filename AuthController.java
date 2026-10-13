package com.ticketbooking.controller;
import com.ticketbooking.dto.AuthDtos;
import com.ticketbooking.model.User;
import com.ticketbooking.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.*;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService auth; private final SecurityContextRepository contexts;
    public AuthController(AuthService auth, SecurityContextRepository contexts) { this.auth=auth; this.contexts=contexts; }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody AuthDtos.RegisterRequest request) {
        try { return ResponseEntity.status(HttpStatus.CREATED).body(auth.register(request)); }
        catch(IllegalArgumentException ex) { return ResponseEntity.badRequest().body(java.util.Map.of("message",ex.getMessage())); }
    }
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthDtos.LoginRequest request, HttpServletRequest servletRequest, HttpServletResponse response) {
        try {
            User user=auth.authenticate(request);
            var authentication=new UsernamePasswordAuthenticationToken(user.getUsername(), null,
                java.util.List.of(new SimpleGrantedAuthority("ROLE_"+user.getRole())));
            SecurityContext context=SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication); SecurityContextHolder.setContext(context);
            HttpSession session=servletRequest.getSession(true);
            contexts.saveContext(context, servletRequest, response);
            session.setAttribute("USER_ID", user.getId());
            session.setAttribute("USER_NAME", user.getName());
            session.setAttribute("USER_ROLE", user.getRole());
            return ResponseEntity.ok(java.util.Map.of("id",user.getId(),"name",user.getName(),"username",user.getUsername(),"role",user.getRole()));
        } catch(IllegalArgumentException ex) { return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(java.util.Map.of("message",ex.getMessage())); }
    }
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        HttpSession session=request.getSession(false); if(session!=null) session.invalidate();
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(java.util.Map.of("message","Logged out"));
    }
    @GetMapping("/me")
    public ResponseEntity<?> me(HttpServletRequest request) {
        HttpSession session=request.getSession(false);
        if(session==null || session.getAttribute("USER_ID")==null) return ResponseEntity.status(401).body(java.util.Map.of("message","Not logged in"));
        return ResponseEntity.ok(java.util.Map.of("id",session.getAttribute("USER_ID"),"name",session.getAttribute("USER_NAME"),"role",session.getAttribute("USER_ROLE")));
    }
}
