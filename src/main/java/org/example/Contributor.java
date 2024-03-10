package org.example;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;

public class Contributor<T extends Comparable<Object>> extends Staff<T> implements RequestManager{
    ExperienceStrategy expGain;
    public Contributor(String email, String password, String name, String country, long age, Gender gender, LocalDateTime birthDate,
                       AccountType accountType, String username){
        super(email, password, name, country, age, gender, birthDate, accountType, username);
        this.expGain = new ContributorExperience();
    }

    public void refactorRequestsToAdmins() {
        for(Request request : this.individualRequests)
            request.refactorRequestToAdmins();
    }

    public void refactorContributionsToAdmins() {
        Admin.adminContributions.addAll(this.contributions);
    }

    @Override
    public void createRequest(Request r) {

    }

    @Override
    public void removeRequest(Request r) {

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
