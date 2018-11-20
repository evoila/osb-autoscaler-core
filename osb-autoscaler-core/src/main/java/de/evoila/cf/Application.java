package de.evoila.cf;

import de.evoila.cf.autoscaler.core.properties.AutoscalerPropertiesBean;
import de.evoila.cf.autoscaler.core.properties.DefaultValueBean;
import de.evoila.cf.autoscaler.core.properties.ScalingEnginePropertiesBean;
import de.evoila.cf.autoscaler.kafka.KafkaPropertiesBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Main class of the project, which starts the Autoscaler.
 * @author Marius Berger
 *
 */
@SpringBootApplication
@EnableMongoRepositories
@EnableConfigurationProperties({ KafkaPropertiesBean.class, AutoscalerPropertiesBean.class,
        ScalingEnginePropertiesBean.class, DefaultValueBean.class })
public class Application implements WebMvcConfigurer {

    static Logger log = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {

        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedHeaders("*")
                .exposedHeaders("WWW-Authenticate",
                        "Access-Control-Allow-Origin",
                        "Access-Control-Allow-Headers"
                )
                .allowedMethods("OPTIONS", "HEAD",
                        "GET", "POST",
                        "PUT", "PATCH",
                        "DELETE", "HEAD")
                .allowCredentials(true);
    }

}
