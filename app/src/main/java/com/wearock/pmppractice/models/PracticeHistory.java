package com.wearock.pmppractice.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;

public class PracticeHistory implements Serializable {

    private int id;
    private int totalScore;
    private PracticeConfiguration configuration;
    private ArrayList<Answer> answers;
    private String creationTime;
    private String completionTime;

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setTotalScore(int totalScore) {
        this.totalScore = totalScore;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public void setConfiguration(PracticeConfiguration configuration) {
        this.configuration = configuration;
    }

    public PracticeConfiguration getConfiguration() {
        return configuration;
    }

    public void setAnswers(Answer[] answers) {
        this.answers = new ArrayList<>();
        if (answers != null) {
            Collections.addAll(this.answers, answers);
        }
    }

    public Answer[] getAnswers() {
        if (this.answers == null) {
            return null;
        } else {
            Answer[] arrAnswers = new Answer[this.answers.size()];
            answers.toArray(arrAnswers);
            return arrAnswers;
        }
    }

    public void setCreationTime(String creationTime) {
        this.creationTime = creationTime;
    }

    public String getCreationTime() {
        return creationTime;
    }

    public void setCompletionTime(String completionTime) {
        this.completionTime = completionTime;
    }

    public String getCompletionTime() {
        return completionTime;
    }

    public double getCorrectRate() {
        if (configuration.getQuestionCount() == 0)
            return 0;

        return (double)totalScore / (double)configuration.getQuestionCount();
    }

    public String getCorrectRateString() {
        StringBuilder builder = new StringBuilder();
        builder.append(100 * getCorrectRate());
        int idxDot = builder.indexOf(".");
        if (idxDot < 0) {
            builder.append(".00");
        } else if (idxDot == builder.length() - 2) {
            builder.append("0");
        } else if (idxDot <= builder.length() - 3) {
            builder.substring(0, idxDot + 2);
        }
        builder.append("%");
        return builder.toString();
    }

    public void attachAnswer(Answer answer) {
        if (answers == null) {
            answers = new ArrayList<>();
        }

        Answer exitAnswer = getAnswerByQuestionId(answer.getQuestionId());
        if (exitAnswer != null) {
            exitAnswer.setAnswer(answer.getAnswer());
        } else {
            answers.add(answer);
        }
    }

    public Answer getAnswerByQuestionId(int qid) {
        if (null != answers) {
            for (Answer answer : answers) {
                if (answer.getQuestionId() == qid) {
                    return answer;
                }
            }
        }
        return null;
    }

}
