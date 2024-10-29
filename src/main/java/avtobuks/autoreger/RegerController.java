package avtobuks.autoreger;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.util.concurrent.CompletableFuture;

@RestController
public class RegerController {
    @Autowired
    private AsyncRegerService asyncProcessingService;

    @Retryable(
            retryFor = { HttpServerErrorException.class, ResourceAccessException.class },
            maxAttempts = 5,
            backoff = @Backoff(delay = 2000))
    @PostMapping("/auto-reger")
    public CompletableFuture<ResponseEntity<String>> processJson(@RequestBody String json) {
        try {
            JSONObject jsonObj = new JSONObject(json);
            return asyncProcessingService.processJson(jsonObj);
        } catch (Exception e) {
            e.printStackTrace();
            return CompletableFuture.completedFuture(ResponseEntity.status(500).body("Произошла ошибка"));
        }
    }
}
