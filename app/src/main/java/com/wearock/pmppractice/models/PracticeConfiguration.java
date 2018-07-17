package com.wearock.pmppractice.models;

import java.io.Serializable;
import java.util.ArrayList;

public class PracticeConfiguration implements Serializable {

    private ArrayList<String> selectedDomains;
    private int questionCount;
    private int timeLimit;

    public PracticeConfiguration() {
        selectedDomains = new ArrayList<>();
    }

    public void setSelectedDomains(ArrayList<String> selectedDomains) {
        this.selectedDomains = selectedDomains;
    }

    public ArrayList<String> getSelectedDomains() {
        return selectedDomains;
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

}
