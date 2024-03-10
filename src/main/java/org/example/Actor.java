package org.example;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import kotlin.Pair;
import org.jetbrains.annotations.NotNull;

public class Actor implements Comparable<Object> {
    String name;
    List<Pair<String, String>> performances;
    List<Rating> ratingList;
    double avgRating;
    String personalBiography;

    public Actor(String name, List<Pair<String, String>> performances, String personalBiography) {
        this.name = name;
        this.performances = performances;
        this.personalBiography = personalBiography;
        this.ratingList = new ArrayList<>();
        this.avgRating = 0;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addPerformance(String title, String type) {
        if(this.performances.contains(new Pair<>(title, type))) {
            System.out.println("Actor already has this performance.");
        } else {
            this.performances.add(new Pair<>(title, type));
        }
    }

    public boolean deletePerformance(String title, String type) {
        return this.performances.remove(new Pair<>(title, type));
    }

    public void setPersonalBiography(String biography) {
        this.personalBiography = biography;
    }

    public boolean hasUserRated(String username) {
        for(Rating rating : this.ratingList) {
            if(rating.userName.equals(username))
                return true;
        }

        return false;
    }

    public Rating findUserRating(String username) {
        for(Rating rating : this.ratingList) {
            if(rating.userName.equals(username))
                return rating;
        }

        return null;
    }

    public void sortRatings() {
        Collections.sort(this.ratingList);
    }

    public void recalculateAvgRating() {
        if (this.ratingList.isEmpty()) {
            this.avgRating = 0.0;
            return;
        }

        double r = 0.0;
        for (Rating rate : ratingList) {
            r += rate.userRating;
        }
        this.avgRating = r / ratingList.size();
    }

    @Override
    public String toString() {
        return name + "\nPersonal Biography: " + personalBiography + "\nPerformances: \n" + performances
                + "\nratings: " + ratingList.toString() + "\naverage rating: " + avgRating;
    }

    @Override
    public int compareTo(@NotNull Object o) {
        if (o instanceof Production) {
            return this.name.compareTo(((Production) o).title);
        } else if (o instanceof Actor) {
            return this.name.compareTo(((Actor) o).name);
        } else return 0;
    }
}
