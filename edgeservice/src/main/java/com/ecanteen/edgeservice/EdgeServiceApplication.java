package com.ecanteen.edgeservice;


@SpringBootApplication
@EnableWebFluxSecurity

public class EdgeServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(EdgeServiceApplication.class, args);
    }


    @Bean
    SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http, ReactiveClientRegistrationRepository clientRegistrationRepository) {
        return http
                .authorizeExchange(authorize -> authorize
                        .pathMatchers("/", "/*.css", "/*.js", "/favicon.ico").permitAll()
                        .anyExchange().authenticated())
                .oauth2Login(Customizer.withDefaults())
                .logout(logout -> logout.logoutSuccessHandler(oidcLogoutSuccessHandler(clientRegistrationRepository)))
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint(new HttpStatusServerEntryPoint(HttpStatus.UNAUTHORIZED)))
                .csrf(csrf -> csrf.csrfTokenRepository(CookieServerCsrfTokenRepository.withHttpOnlyFalse()))
                .build();
    }


    @Bean
    ServerOAuth2AuthorizedClientRepository oAuth2AuthorizedClientRepository() {
        return new WebSessionServerOAuth2AuthorizedClientRepository();
    }

//    @Bean
//    WebFilter csrfWebFilter() {
//        // Required because of https://github.com/spring-projects/spring-security/issues/5766
//        return (exchange, chain) -> {
//            exchange.getResponse().beforeCommit(() -> Mono.defer(() -> {
//                Mono<CsrfToken> csrfToken = exchange.getAttribute(CsrfToken.class.getName());
//                return csrfToken != null ? csrfToken.then() : Mono.empty();
//            }));
//            return chain.filter(exchange);
//        };
//    }

    private ServerLogoutSuccessHandler oidcLogoutSuccessHandler(ReactiveClientRegistrationRepository clientRegistrationRepository) {
        var oidcLogoutSuccessHandler = new OidcClientInitiatedServerLogoutSuccessHandler(clientRegistrationRepository);
        oidcLogoutSuccessHandler.setPostLogoutRedirectUri("{baseUrl}");
        return oidcLogoutSuccessHandler;
    }

    @RestController
    class WelcomeController {

        @GetMapping("welcome")
        Mono<Welcome> getWelcome() {
            return Mono.just(new Welcome("Welcome to school "));
        }

    }

    record Welcome(String message){}

}

