package org.route;


import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;


@Configuration
@ComponentScan
@PropertySource("/application.properties")
@EnableAspectJAutoProxy
public class AppConfig {

    // static: this bean must be created early so @Value placeholders can be resolved
    @Bean
    static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }
}
