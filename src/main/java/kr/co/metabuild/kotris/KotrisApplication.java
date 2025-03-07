package kr.co.metabuild.kotris;

import kr.co.metabuild.kotris.config.BeanContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ApplicationContext;

import javax.annotation.PostConstruct;

@SpringBootApplication
@ConfigurationPropertiesScan("kr.co.metabuild.kotris")
public class KotrisApplication {

    private final ApplicationContext context;

    public KotrisApplication(ApplicationContext context) {
        this.context = context;
    }

    public static void main(String[] args) {
        SpringApplication.run(KotrisApplication.class, args);
    }
    @PostConstruct
    public void init(){
        BeanContext.init(context); // 3. 객체의 생명주기를 이용해 스태틱 참조변수에 주입한다.
    }				   // 스프링 앱이 정상적으로 기동된 후에 주입된다.

}
