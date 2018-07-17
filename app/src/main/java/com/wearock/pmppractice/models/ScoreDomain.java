package com.wearock.pmppractice.models;

public class ScoreDomain {

    private String domain;
    private int totalCount;
    private int correctCount;

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getDomain() {
        return domain;
    }

    public void addTotalCount(int count) {
        this.totalCount += count;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void addCorrectCount(int count) {
        this.correctCount += count;
    }

    public int getCorrectCount() {
        return correctCount;
    }

    public double getCorrectRate() {
        if (totalCount == 0) {
            return 0;
        } else {
            return (double)correctCount / (double)totalCount;
        }
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
}
