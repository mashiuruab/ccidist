package com.cefalo.cci.model.token;

import static com.cefalo.cci.utils.StringUtils.isBlank;
import static com.google.common.base.Preconditions.checkArgument;

import java.io.Serializable;

public class ProductID implements Serializable {
    private static final long serialVersionUID = -621548175170780391L;

    public static final long WILDCARD_ISSUE_ID = 0;

    private final String publicationID;
    private final long issueID;

    public ProductID(String publicationID, long issueID) {
        checkArgument(!isBlank(publicationID));
        checkArgument(issueID > 0);

        this.publicationID = publicationID;
        this.issueID = issueID;
    }

    public ProductID(String publicationID) {
        checkArgument(!isBlank(publicationID));

        this.publicationID = publicationID;
        this.issueID = WILDCARD_ISSUE_ID; // Basically
    }

    public String getPublicationID() {
        return publicationID;
    }

    public long getIssueID() {
        return issueID;
    }

    public boolean matches(ProductID other) {
        if (this == other) {
            return true;
        }
        if (other == null) {
            return false;
        }

        // A -ve issueId denotes wildcard (*). So, that matches with anything.
        if (issueID > 0 && other.issueID > 0 && issueID != other.issueID) {
            return false;
        }

        if (!publicationID.equals(other.publicationID)) {
            return false;
        }

        return true;
    }

    public static final ProductID from(String productID) {
        checkArgument(!isBlank(productID));

        int lastIndexOfSep = productID.lastIndexOf(':');
        // TODO: Really?? Test with some unit test.
        if (lastIndexOfSep < 0 || lastIndexOfSep == productID.length() - 1) {
            throw new IllegalArgumentException("Invalid product id: ".concat(productID));
        }

        String publicationId = productID.substring(0, lastIndexOfSep);
        String issueId = productID.substring(lastIndexOfSep + 1);

        if ("*".equals(issueId)) {
            return new ProductID(publicationId);
        } else {
            return new ProductID(publicationId, Long.valueOf(issueId));
        }
    }

    @Override
    public String toString() {
        return String.format("%s:%s", publicationID, issueID == WILDCARD_ISSUE_ID ? "*" : issueID);
    }
}
