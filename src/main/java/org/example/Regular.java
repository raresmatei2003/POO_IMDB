package org.example;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Regular<T extends Comparable<Object>> extends User<T> implements RequestManager {
    ExperienceStrategy expGain;

    List<Object> alreadyRated = new ArrayList<>();

    public Regular(String email, String password, String name, String country, long age, Gender gender, LocalDateTime birthDate,
                   AccountType accountType, String username) {
        super(email, password, name, country, age, gender, birthDate, accountType, username);
        this.expGain = new RegularExperience();
    }

    public List<Production> getPersonalRatedProductions() {
        List<Production> list = new ArrayList<>();
        for (Production production : IMDB.getInstance().productionList)
            if (production.hasUserRated(this.username))
                list.add(production);

        return list;
    }

    public List<Actor> getPersonalRatedActors() {
        List<Actor> list = new ArrayList<>();
        for (Actor actor : IMDB.getInstance().actorList)
            if (actor.hasUserRated(this.username))
                list.add(actor);

        return list;
    }

    public boolean hasNotAlreadyRatedObject(Object object) {
        return !this.alreadyRated.contains(object);
    }

    public void addToAlreadyRatedObjects(Object object) {
        this.alreadyRated.add(object);
    }

    public void createProductionRating(Production production, long userRating, String message) {
        Rating rating = new Rating(this.username, userRating, message);

        for (Rating r : production.ratingList) {
            rating.subscribe(rating.reviewers, IMDB.getInstance().findUserAfterUsername(r.userName));
        }

        rating.notifyUsers(rating.reviewers, "Someone rated a production you also rated.");
        production.ratingList.add(rating);
        production.sortRatings();
        production.recalculateAvgRating();

        if (Admin.adminContributions.contains(production)) {
            for (Observer user : IMDB.getInstance().accountList)
                if (user instanceof Admin) {
                    rating.subscribe(rating.contributors, user);
                }
        } else {
            for (Observer user : IMDB.getInstance().accountList)
                if (user instanceof Staff && ((Staff<?>) user).contributions.contains(production)) {
                    rating.subscribe(rating.contributors, user);
                }
        }

        if (hasNotAlreadyRatedObject(production)) {
            this.experience += this.expGain.giveRatingExp();
            this.addToAlreadyRatedObjects(production);
        }

        rating.notifyUsers(rating.contributors, "Someone rated a production you added.");
    }

    public void deleteProductionRating(Production production) {
        Rating rating = production.findUserRating(this.username);
        production.ratingList.remove(rating);
        production.recalculateAvgRating();
    }

    public void createActorRating(Actor actor, long userRating, String message) {
        Rating rating = new Rating(this.username, userRating, message);

        for (Rating r : actor.ratingList) {
            rating.subscribe(rating.reviewers, IMDB.getInstance().findUserAfterUsername(r.userName));
        }

        rating.notifyUsers(rating.reviewers, "Someone rated an actor you also rated.");
        actor.ratingList.add(rating);
        actor.sortRatings();
        actor.recalculateAvgRating();

        if (Admin.adminContributions.contains(actor)) {
            for (Observer user : IMDB.getInstance().accountList)
                if (user instanceof Admin) {
                    rating.subscribe(rating.contributors, user);
                }
        } else {
            for (Observer user : IMDB.getInstance().accountList)
                if (user instanceof Staff && ((Staff<?>) user).contributions.contains(actor)) {
                    rating.subscribe(rating.contributors, user);
                }
        }

        if (hasNotAlreadyRatedObject(actor)) {
            this.experience += this.expGain.giveRatingExp();
            this.addToAlreadyRatedObjects(actor);
        }

        rating.notifyUsers(rating.contributors, "Someone rated an actor you added.");
    }

    public void deleteActorRating(Actor actor) {
        Rating rating = actor.findUserRating(this.username);
        actor.ratingList.remove(rating);
        actor.recalculateAvgRating();
    }

    public void deleteAllProductionRatings() {
        List<Production> productionsRated = getPersonalRatedProductions();
        for (Production production : productionsRated)
            deleteProductionRating(production);
    }

    public void deleteAllActorRatings() {
        List<Actor> actorsRated = getPersonalRatedActors();
        for (Actor actor : actorsRated)
            deleteActorRating(actor);
    }

    public void deleteAllRatings() {
        deleteAllProductionRatings();
        deleteAllActorRatings();
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
