package NestNet.NestNetWebSite.config.swagger;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger 관련 설정 클래스
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI(){

        Info info = new Info()
                .title("네스트넷 동아리 웹사이트 API Document")
                .description("CBNU 학술동아리 네스트넷 웹사이트 프로젝트의 API 명세서");

        return new OpenAPI()
                .info(info);
    }
}


