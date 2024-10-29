package avtobuks;

import org.json.JSONObject;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.util.concurrent.CompletableFuture;

@SpringBootApplication
@RestController
@EnableAsync
@EnableScheduling
@EnableRetry
@ComponentScan(basePackages = {"avtobuks"})
@EnableJpaRepositories(basePackages = {"avtobuks"})
public class JsonServerApplication {

    @Autowired
    private RobotService robotService;
    @Autowired
    private ResourceLoader resourceLoader;

    public static void main(String[] args) {
        SpringApplication.run(JsonServerApplication.class, args);
    }
    //TODO перед созданием авторегера нужно еще создать класс для работы с сервисами смс подтверждения
    // и работу с сервисом будет делать сам робот

    //TODO создать систему при которой первой бы создавался профиль браузера под surf-earner,
    // потом авторегистратор surf-earner первым создавал почту, а потом уже сам букс,
    // при этом работа с почтой реализовать через отдельный бот. А может в этот же бот запихнуть
    // и регистрацию самой почты?




    @PostMapping("/process-json")
    @Async
    public CompletableFuture<ResponseEntity<String>> processJson(@RequestBody String json) {
        try {
            JSONObject jsonObj = new JSONObject(json);

            // Оборачиваем результат в CompletableFuture для асинхронной обработки
            return CompletableFuture.completedFuture(ResponseEntity.ok(robotService.execute(jsonObj)));
        } catch (Exception e) {
            // Логируем ошибку и возвращаем ResponseEntity с ошибкой
            e.printStackTrace();
            return CompletableFuture.completedFuture(ResponseEntity.status(500).body("Произошла ошибка"));
        }
    }


    @GetMapping("/hello")
    public String hello() {
        return "Hello, World!";
    }

}

