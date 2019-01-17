package com.cefalo.cci.service.cache;

import com.cefalo.cci.model.Content;
import com.cefalo.cci.model.Publication;

public class DeviceMatchCacheKey {
    private final Content content;
    private final String publicationId;

    private DeviceMatchCacheKey(Content content, String publicationId) {
        this.content = content;
        this.publicationId = publicationId;
    }

    public static DeviceMatchCacheKey from(Content content, Publication publication) {
        return new DeviceMatchCacheKey(content, publication.getId());
    }

    public Content getContent() {
        return content;
    }

    public String getPublicationId() {
        return publicationId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((content == null) ? 0 : content.hashCode());
        result = prime * result + ((publicationId == null) ? 0 : publicationId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        DeviceMatchCacheKey other = (DeviceMatchCacheKey) obj;
        if (content == null) {
            if (other.content != null) {
                return false;
            }
        } else if (!content.equals(other.content)) {
            return false;
        }
        if (publicationId == null) {
            if (other.publicationId != null) {
                return false;
            }
        } else if (!publicationId.equals(other.publicationId)) {
            return false;
        }
        return true;
    }
}