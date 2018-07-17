package com.wearock.pmppractice.models;

public class QuestionStatus {

    private int index;
    private boolean isAnswered;
    private boolean isCorrect;

    public void setIndex(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public void setAnswered(boolean answered) {
        isAnswered = answered;
    }

    public boolean isAnswered() {
        return isAnswered;
    }

    public void setCorrect(boolean correct) {
        isCorrect = correct;
    }

    public boolean isCorrect() {
        return isCorrect;
    }

}
