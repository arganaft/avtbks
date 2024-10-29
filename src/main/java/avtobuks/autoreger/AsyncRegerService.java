package avtobuks.autoreger;

import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class AsyncRegerService {

    private final AutoReger autoReger;

    public AsyncRegerService(AutoReger autoReger) {
        this.autoReger = autoReger;
    }

    @Async
    public CompletableFuture<ResponseEntity<String>> processJson(JSONObject jsonObj) {
        String result = autoReger.execute(jsonObj);  // Получаем строку от autoReger
        return CompletableFuture.completedFuture(ResponseEntity.ok(result));  // Преобразуем в ResponseEntity
    }
}
