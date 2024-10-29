package avtobuks;


import org.json.JSONObject;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
@Service
@Component
public class RobotService {

    public RobotService() throws SQLException {
    }

    public String execute(JSONObject jsonObj) throws InterruptedException {
        return "tru";

    }
}
