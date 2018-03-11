package com.frynd.debouncer.test;

import java.util.Objects;

/**
 * Test Record class.
 */
public class Record {
    public static class Users {
        public static final String WAIMARIE = "Waimarie";
        public static final String NIKAU = "Nikau";
    }

    public static class Records {
        public static final Record waimarie0 = new Record(Users.WAIMARIE, "Hello, World!");
        public static final Record nikau0 = new Record(Users.NIKAU, "Hello!");
        public static final Record waimarie1 = new Record(Users.WAIMARIE, "Hello, " + Users.NIKAU + "!");
    }

    private final String name;
    private final String message;

    private Record(String name, String message) {
        this.name = name;
        this.message = message;
    }

    public String getName() {
        return name;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Record record = (Record) o;
        return Objects.equals(name, record.name) &&
                Objects.equals(message, record.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, message);
    }
}
