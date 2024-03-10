package org.example;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.SortedSet;
import java.util.TreeSet;

public class Admin<T extends Comparable<Object>> extends Staff<T> {
    public static SortedSet<Comparable<Object>> adminContributions = new TreeSet<>();

    public Admin(String email, String password, String name, String country, long age, Gender gender, LocalDateTime birthDate,
                 AccountType accountType, String username) {
        super(email, password, name, country, age, gender, birthDate, accountType, username);
        this.experience = Integer.MAX_VALUE;
    }

    public static void addAdminContribution(Comparable<Object> object) {
        adminContributions.add(object);
    }

    public static void deleteAdminContribution(Comparable<Object> object) {
        adminContributions.remove(object);
    }

    private void addUser() {

    }

    private void removeUser(User<T> user) {

    }

    @Override
    public void update(String message) {
        this.notifications.add(message);
    }

    @Override
    public int compareTo(@NotNull Object o) {
        return 0;
    }
}
