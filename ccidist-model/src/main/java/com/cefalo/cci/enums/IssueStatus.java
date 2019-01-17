package com.cefalo.cci.enums;

public enum IssueStatus {
    DRAFT(1, "Draft"), PUBLISHED(2, "Published");

    public static IssueStatus valueOf(int value) {
        switch (value) {
        case 1:
            return DRAFT;
        case 2:
            return PUBLISHED;
        default:
            throw new IllegalArgumentException(String.format("%s is not a valid status value.", value));
        }
    }

    /**
     * The numeric value of the status. This is stored in the data store also.
     */
    private final int value;
    /**
     * FIXME: A hack. This property is used on the adminIssueList.jsp file. Fix this.
     */
    private final String statusName;

    private IssueStatus(int value, String statusName) {
        this.value = value;
        this.statusName = statusName;
    }

    public int getValue() {
        return value;
    }

    public String getStatusName() {
        return statusName;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
