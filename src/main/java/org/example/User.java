package org.example;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

class UserFactory {
    static User<Comparable<Object>> factory(String email, String password, String name, String country, long age, Gender gender, LocalDateTime birthDate,
                                            AccountType accountType, String username) {
        if (accountType.equals(AccountType.ADMIN)) {
            return new Admin<>(email, password, name, country, age, gender, birthDate, accountType, username);
        } else if (accountType.equals(AccountType.CONTRIBUTOR)) {
            return new Contributor<>(email, password, name, country, age, gender, birthDate, accountType, username);
        } else if (accountType.equals(AccountType.REGULAR)) {
            return new Regular<>(email, password, name, country, age, gender, birthDate, accountType, username);
        }
        return null;
    }
}

public abstract class User<T extends Comparable<Object>> implements Observer, Comparable<Object> {
    Information information;
    AccountType accountType;
    String username;
    int experience;
    List<String> notifications;
    SortedSet<T> preferences;

    public User(String email, String password, String name, String country, long age, Gender gender, LocalDateTime birthDate,
                AccountType accountType, String username) {
        this.information = new Information.InformationBuilder(email, password)
                .name(name)
                .country(country)
                .age(age)
                .gender(gender)
                .birthDate(birthDate)
                .build();
        this.accountType = accountType;
        this.username = username;
        this.experience = 0;
        this.notifications = new ArrayList<>();
        this.preferences = new TreeSet<>();
    }

    public void addPreference(T obj) {
        preferences.add(obj);
    }

    public void deletePreference(T obj) {
        preferences.remove(obj);
    }

    public void deleteNotifications() {
        this.notifications.clear();
    }

    public List<Request> getPersonalCreatedRequests() {
        List<Request> createdRequests = new ArrayList<>();

        for (Request request : IMDB.getInstance().getAllCreatedRequests()) {
            if (request.userCreator.equals(this.username))
                createdRequests.add(request);
        }

        return createdRequests;
    }

    public void deleteRequests() {
        List<Request> createdRequests = getPersonalCreatedRequests();

        for (Request request : createdRequests) {
            request.removeRequest();
        }
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return this.username;
    }

    public String getExpString() {
        if (this.experience == Integer.MAX_VALUE)
            return "-";
        return "" + this.experience;
    }

    public void printUserInfo() {
        System.out.println("Username: " + getUsername() +
                        "\nAccount type: " + this.accountType.toString() +
                        "\nUser experience: " + this.getExpString() +
                        "\nName: " + this.information.getName() +
                        "\nCountry: " + this.information.getCountry() +
                        "\nBirthdate: " + this.information.getBirthDate().toLocalDate() +
                        "\nAge: " + this.information.getAge() +
                        "\nGender: " + this.information.getGender());
    }

    static class Information {
        private final Credentials credentials;
        private String name, country;
        private long age;
        private Gender gender;
        private LocalDateTime birthDate;

        private Information(InformationBuilder builder) {
            this.credentials = builder.credentials;
            this.name = builder.name;
            this.country = builder.country;
            this.age = builder.age;
            this.gender = builder.gender;
            this.birthDate = builder.birthDate;
        }

        public String getEmail() {
            return this.credentials.getEmail();
        }

        public String getPassword() {
            return this.credentials.getPassword();
        }

        public String getName() {
            return this.name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getCountry() {
            return this.country;
        }

        public void setCountry(String country) {
            this.country = country;
        }

        public LocalDateTime getBirthDate() {
            return this.birthDate;
        }

        public void setBirthDate(LocalDateTime birthDate) {
            this.birthDate = birthDate;
            this.age = Period.between(birthDate.toLocalDate(), LocalDate.now()).getYears();
        }

        public long getAge() {
            return this.age;
        }

        public void setGender(Gender gender) {
            this.gender = gender;
        }

        public Gender getGender() {
            return this.gender;
        }

        public static class InformationBuilder {
            private final Credentials credentials;
            private String name, country;
            private long age;
            private Gender gender;
            private LocalDateTime birthDate;

            public InformationBuilder(String email, String password) {
                this.credentials = new Credentials(email, password);
            }

            public InformationBuilder name(String name) {
                this.name = name;
                return this;
            }

            public InformationBuilder country(String country) {
                this.country = country;
                return this;
            }

            public InformationBuilder age(long age) {
                this.age = age;
                return this;
            }

            public InformationBuilder gender(Gender gender) {
                this.gender = gender;
                return this;
            }

            public InformationBuilder birthDate(LocalDateTime birthDate) {
                this.birthDate = birthDate;
                return this;
            }

            public Information build() {
                return new Information(this);
            }
        }

        private static class Credentials {
            private String email, password;

            public Credentials(String email, String password) {
                this.email = email;
                this.password = password;
            }

            public void setCredentials(String email, String password) {
                this.email = email;
                this.password = password;
            }

            public String getEmail() {
                return this.email;
            }

            public void setEmail(String email) {
                this.email = email;
            }

            public String getPassword() {
                return this.password;
            }

            public void setPassword(String password) {
                this.password = password;
            }
        }

    }

}