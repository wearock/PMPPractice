package com.wearock.pmppractice.models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

public class PracticeConfiguration implements Serializable {

    private SourceEnum questionSource;
    private int questionCount;
    private int timeLimit;
    private ArrayList<String> selectedDomains;

    public PracticeConfiguration() {
        selectedDomains = new ArrayList<>();
    }

    private void addSelectedDomain(String domain) {
        if (!this.selectedDomains.contains(domain)) {
            this.selectedDomains.add(domain);
        }
    }

    public void setSelectedDomains(ArrayList<String> selectedDomains) {
        this.selectedDomains = selectedDomains;
    }

    public ArrayList<String> getSelectedDomains() {
        return selectedDomains;
    }

    public void setQuestionSource(SourceEnum questionSource) {
        this.questionSource = questionSource;
    }

    private SourceEnum getQuestionSource() {
        return questionSource;
    }

    public void setQuestionCount(int questionCount) {
        this.questionCount = questionCount;
    }

    public int getQuestionCount() {
        return questionCount;
    }

    public void setTimeLimit(int timeLimit) {
        this.timeLimit = timeLimit;
    }

    public int getTimeLimit() {
        return timeLimit;
    }

    public static String toJsonString(PracticeConfiguration config) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("source", config.getQuestionSource().getName());
        jsonObject.put("qcount", config.getQuestionCount());
        jsonObject.put("time", config.getTimeLimit());
        JSONArray array = new JSONArray();
        for (String domain : config.getSelectedDomains()) {
            array.put(domain);
        }
        jsonObject.put("domains", array);
        return jsonObject.toString();
    }

    public static PracticeConfiguration parseJsonString(String jsonString) throws JSONException {
        JSONObject jsonObject = new JSONObject(jsonString);

        PracticeConfiguration config = new PracticeConfiguration();
        String source = jsonObject.optString("source");
        if (source != null) {
            config.setQuestionSource(SourceEnum.fromName(source));
        } else {
            config.setQuestionSource(SourceEnum.EXAM1);
        }
        config.setQuestionCount(jsonObject.optInt("qcount"));
        config.setTimeLimit(jsonObject.getInt("time"));
        JSONArray array = jsonObject.optJSONArray("domains");
        for (int i=0; i<array.length(); i++) {
            config.addSelectedDomain(array.optString(i));
        }

        return config;
    }

    public enum SourceEnum {
        ALL("所有题目", -1),
        IMPORTED("导入题目", 0),
        EXAM1("第一次模拟考试", 1),
        EXAM2("第二次模拟考试", 2);

        private final String name;
        private final Integer value;
        SourceEnum(String name, Integer value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public Integer getValue() {
            return value;
        }

        public static SourceEnum fromName(String name) {
            for (SourceEnum se : SourceEnum.values()) {
                if (se.name.equalsIgnoreCase(name)) {
                    return se;
                }
            }
            throw new IllegalArgumentException(name);
        }

        public static SourceEnum fromValue(Integer value) {
            for (SourceEnum se : SourceEnum.values()) {
                if (se.value.equals(value)) {
                    return se;
                }
            }
            throw new IllegalArgumentException(String.valueOf(value));
        }
    }

}
