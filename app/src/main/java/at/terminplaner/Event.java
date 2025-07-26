package at.terminplaner;

public class Event {
    public String description;
    public String date;
    public String time;
    public int duration;
    public boolean isRepeating;
    public String repeatType;
    public String repeatUntil;

    public Event(String description, String date, String time, int duration, boolean isRepeating, String repeatType, String repeatUntil) {
        this.description = description;
        this.date = date;
        this.time = time;
        this.duration = duration;
        this.isRepeating = isRepeating;
        this.repeatType = repeatType;
        this.repeatUntil = repeatUntil;
    }

    public String getDescription() {
        return description;
    }

    public int getDuration() {
        return duration;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getRepeatUntil() {
        return repeatUntil;
    }

    public boolean isRepeating() {
        return isRepeating;
    }

    public String getRepeatType() {
        return repeatType;
    }
}
