package shop.mtcoding.bank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class BankApplication {

	public static void main(String[] args) {
		SpringApplication.run(BankApplication.class, args);
		// ConfigurableApplicationContext context =  SpringApplication.run(BankApplication.class, args);
		// String[] iocNames = context.getBeanDefinitionNames();
		// for (String name : iocNames) {
		// 	System.out.println(name);
		// }
	}

}
