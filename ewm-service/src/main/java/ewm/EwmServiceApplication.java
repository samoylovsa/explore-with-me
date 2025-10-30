package ewm;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@Slf4j
@SpringBootApplication
@ComponentScan({
        "ewm",
        "stats.client"
})
public class EwmServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(EwmServiceApplication.class, args);
    }
}