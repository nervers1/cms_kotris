package kr.co.metabuild.kotris.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "cms")
public class ConfigProperties {
}
