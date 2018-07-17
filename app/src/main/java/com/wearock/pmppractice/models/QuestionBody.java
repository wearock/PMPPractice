package com.wearock.pmppractice.models;

import java.io.Serializable;

public class QuestionBody implements Serializable {

    public enum Language {
        Chinese,
        English
    }

    private String description;
    private String choiceA;
    private String choiceB;
    private String choiceC;
    private String choiceD;

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setChoiceA(String choiceA) {
        this.choiceA = choiceA;
    }

    public String getChoiceA() {
        return choiceA;
    }

    public void setChoiceB(String choiceB) {
        this.choiceB = choiceB;
    }

    public String getChoiceB() {
        return choiceB;
    }

    public void setChoiceC(String choiceC) {
        this.choiceC = choiceC;
    }

    public String getChoiceC() {
        return choiceC;
    }

    public void setChoiceD(String choiceD) {
        this.choiceD = choiceD;
    }

    public String getChoiceD() {
        return choiceD;
    }
}
