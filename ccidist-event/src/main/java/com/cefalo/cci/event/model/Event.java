package com.cefalo.cci.event.model;

import static com.google.common.base.Preconditions.checkNotNull;

public class Event {
    private final EventType type;
    private final Class<?> sourceClass;
    private final Object sourceId;
    /**
     * This is basically for the DELETE events where we will send out the parent object ID.
     */
    private final Object extraInfo;

    public Event(EventType type, Class<?> sourceClass, Object sourceId, Object extraInfo) {
        checkNotNull(type);
        checkNotNull(sourceClass);
        checkNotNull(sourceId);

        this.type = type;
        this.sourceClass = sourceClass;
        this.sourceId = sourceId;
        this.extraInfo = extraInfo;
    }

    public EventType getType() {
        return type;
    }

    public Class<?> getSourceClass() {
        return sourceClass;
    }

    public Object getSourceId() {
        return sourceId;
    }

    public Object getExtraInfo() {
        return extraInfo;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((extraInfo == null) ? 0 : extraInfo.hashCode());
        result = prime * result + ((sourceClass == null) ? 0 : sourceClass.hashCode());
        result = prime * result + ((sourceId == null) ? 0 : sourceId.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
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
        Event other = (Event) obj;
        if (extraInfo == null) {
            if (other.extraInfo != null) {
                return false;
            }
        } else if (!extraInfo.equals(other.extraInfo)) {
            return false;
        }
        if (sourceClass == null) {
            if (other.sourceClass != null) {
                return false;
            }
        } else if (!sourceClass.equals(other.sourceClass)) {
            return false;
        }
        if (sourceId == null) {
            if (other.sourceId != null) {
                return false;
            }
        } else if (!sourceId.equals(other.sourceId)) {
            return false;
        }
        if (type != other.type) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Event ["
                + "operationType=" + type
                + ", sourceClass=" + sourceClass
                + ", sourceId=" + sourceId
                + "]";
    }

}
