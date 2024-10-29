package avtobuks.gmail_bot;


import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;


public class MailBox {
    private boolean isBlocked;
    private Map<String, Mail> buksMailMap;
    private int workCount;

    public MailBox() {
        this.isBlocked = false;
        this.buksMailMap = new HashMap<>();
        workCount = 0;
    }

    public boolean hasWork() {
        return workCount > 0 && !isBlocked;
    }

    public void block(boolean isBlocked) {
        this.isBlocked = isBlocked;
    }


    /**
     метод для воркера, если задачи для получения письма нет то добавляет новую задачу,
     если задача уже добавлена, то пытается получить письмо
     */
    public String getMail(String buks, String title, String patternText) {
        if (!buksMailMap.containsKey(buks)) {
            buksMailMap.put(buks, new Mail());
        }
        Mail mail = buksMailMap.get(buks);
        if (mail.patternText == null) {
            mail.patternText = patternText;
            mail.title = title;
            workCount = workCount + 1;
            return "задача добавлена";
        }
        if (mail.fullText == null ) {
            return "письмо еще не получено";
        }
        String fullText = mail.fullText;
        mail.title = null;
        mail.fullText = null;
        mail.patternText = null;
        return fullText;
    }

    /**
     метод для gmailBot для получения новой работы
     */
    public JSONArray getWork() {
        JSONArray jsonArray = new JSONArray();
        for (String buks : buksMailMap.keySet()) {
            Mail mail = buksMailMap.get(buks);
            if (mail.patternText != null) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("buks", buks);
                jsonObject.put("title", mail.title);
                jsonObject.put("patternText", mail.patternText);
                jsonArray.put(jsonObject);
            }
        }
        return jsonArray;
    }

    /**
     метод для gmailBot для публикации результатов работы
     */
    public void submitWork(JSONArray jsonArray) {
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            Mail mail = buksMailMap.get(jsonObject.getString("buks"));
            mail.fullText = jsonObject.getString("fullText");
        }
        workCount = Math.max(0, workCount - jsonArray.length());
    }



    /**
     Класс обьект которого представляет собой письмо:
     title = заголовок письма.
     patternText = примерный текст который должно содержать в себе искомое письмо.
     fullText = полный текст уже найденного письма
     */
    private static class Mail {
        String title;
        String patternText;
        String fullText;

        public Mail() {
        }
    }
}
