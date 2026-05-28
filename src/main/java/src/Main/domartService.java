package src.Main;


import lombok.var;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;
import src.Main.Logic.dormancyEmailSender;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class domartService {

    public static void main(String[] args) {

        var context = SpringApplication.run(domartService.class, args);

        dormancyEmailSender sender =
                context.getBean(dormancyEmailSender.class);

        sender.sendDormancyEmails();  // 👈 THIS triggers it
    }
}