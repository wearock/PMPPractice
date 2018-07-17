package com.wearock.pmppractice.models;

import java.io.Serializable;

public class Answer implements Serializable {

    private int questionId;
    private int answer;

    public void setQuestionId(int questionId) {
        this.questionId = questionId;
    }

    public int getQuestionId() {
        return questionId;
    }

    public void setAnswer(int answer) {
        this.answer = answer;
    }

    public int getAnswer() {
        return answer;
    }

}
