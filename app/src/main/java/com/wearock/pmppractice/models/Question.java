package com.wearock.pmppractice.models;

import com.wearock.pmppractice.controllers.dao.QuestionDAO;

import java.io.Serializable;
import java.util.HashMap;

public class Question implements Serializable {

    private int id;
    private HashMap<QuestionBody.Language, QuestionBody> bodyMap;
    private String image;
    private int answer;
    private String domain;
    private String process;
    private String subProcess;
    private String knowledgePoint;
    private String explanation;

    public Question() {
        bodyMap = new HashMap<>();
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void putBody(QuestionBody.Language language, QuestionBody body) {
        bodyMap.put(language, body);
    }

    public QuestionBody getBodyByLanguage(QuestionBody.Language language) {
        if (!bodyMap.containsKey(language))
            return null;
        return bodyMap.get(language);
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getImage() {
        return image;
    }

    public void setAnswer(int answer) {
        this.answer = answer;
    }

    public int getAnswer() {
        return answer;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getDomain() {
        return domain;
    }

    public void setProcess(String process) {
        this.process = process;
    }

    public String getProcess() {
        return process;
    }

    public void setSubProcess(String subProcess) {
        this.subProcess = subProcess;
    }

    public String getSubProcess() {
        return subProcess;
    }

    public void setKnowledgePoint(String knowledgePoint) {
        this.knowledgePoint = knowledgePoint;
    }

    public String getKnowledgePoint() {
        return knowledgePoint;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public String getExplanation() {
        return explanation;
    }
}
