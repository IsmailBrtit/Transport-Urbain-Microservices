package Transport_Urbain_Microservices.user_service.config;


import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("User Service API")
                        .version("1.0.0")
                        .description("Service de gestion des utilisateurs - Transport Urbain")
                        .contact(new Contact()
                                .name("Urbain Transport Team")
                                .email("contact@urbain-transport.ma")));
    }
}