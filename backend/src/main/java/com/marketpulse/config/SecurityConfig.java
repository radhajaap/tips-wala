package com.marketpulse.config;
import java.util.List;
import org.springframework.beans.factory.annotation.Value; import org.springframework.context.annotation.*;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.*;
@Configuration @EnableMethodSecurity
public class SecurityConfig {
  @Bean SecurityFilterChain chain(HttpSecurity http, JwtFilter jwt) throws Exception {
    http.csrf(c -> c.disable()) // stateless Bearer-token API: no cookie sessions
      .cors(Customizer.withDefaults())
      .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
      .authorizeHttpRequests(a -> a
        .requestMatchers("/api/auth/**","/api/market/**","/ws/**","/api/faqs","/api/reviews","/api/posts","/api/posts/*","/api/contact","/api/market/history","/actuator/health","/media/**","/api/payments/webhook","/api/calendar","/api/news").permitAll()
        .requestMatchers("/api/admin/**").hasAnyRole("SUPER_ADMIN","CONTENT_ADMIN","SUPPORT_ADMIN")
        .anyRequest().authenticated())
      .addFilterBefore(jwt, UsernamePasswordAuthenticationFilter.class);
    return http.build(); }
  @Bean PasswordEncoder encoder(){ return new BCryptPasswordEncoder(12); }
  @Bean CorsConfigurationSource cors(@Value("${app.cors.origins}") List<String> origins){
    var c = new CorsConfiguration(); c.setAllowedOrigins(origins); c.setAllowedMethods(List.of("GET","POST","PUT","DELETE"));
    c.setAllowedHeaders(List.of("Authorization","Content-Type"));
    var s = new UrlBasedCorsConfigurationSource(); s.registerCorsConfiguration("/api/**", c); return s; }
}
