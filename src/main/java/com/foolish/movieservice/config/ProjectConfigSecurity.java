package com.foolish.movieservice.config;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@AllArgsConstructor
public class ProjectConfigSecurity {
  private final Environment env;

  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.sessionManagement(config -> config.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
    http.csrf(AbstractHttpConfigurer::disable);
    http
            .authorizeHttpRequests(config -> config
                    .requestMatchers(
                            "/api/v1/movies/**").permitAll()
                    .anyRequest().denyAll());
    return http.build();
  }

  // CORS Configuration
  @Bean
  public WebMvcConfigurer corsConfigurer() {
    String url = env.getProperty("API_GATEWAY_URL");
    return new WebMvcConfigurer() {
      @Override
      public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**").allowedOrigins(url, "http://localhost:8080").allowCredentials(true).exposedHeaders("*").allowedMethods("*");
      }
    };
  }
}
