package org.example;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

enum ProductionType {
    Movie,
    Series
}

public abstract class Production implements Comparable<Object> {
    String title;
    List<String> directorList;
    List<String> actorList;
    List<Genre> genreList;
    List<Rating> ratingList;
    String description;
    double avgRating;

    public Production(String title, List<String> directorList, List<String> actorList, List<Genre> genreList,
                      List<Rating> ratingList, String description, double avgRating) {
        this.title = title;
        this.directorList = directorList;
        this.actorList = actorList;
        this.genreList = genreList;
        this.ratingList = ratingList;
        this.description = description;
        this.avgRating = avgRating;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void addDirector(String director) {
        if (this.directorList.contains(director)) {
            System.out.println("Production already has this director.");
        } else {
            this.directorList.add(director);
        }
    }

    public boolean deleteDirector(String director) {
        return this.directorList.remove(director);
    }

    public void addActor(String actor) {
        if (this.actorList.contains(actor)) {
            System.out.println("Production already has this actor.");
        } else {
            this.actorList.add(actor);
        }
    }

    public boolean deleteActor(String actor) {
        return this.actorList.remove(actor);
    }

    public void addGenre(String genre) {
        try {
            if (this.genreList.contains(Genre.valueOf(genre))) {
                System.out.println("Production already has this genre.");
            } else {
                this.genreList.add(Genre.valueOf(genre));
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid genre.");
        }
    }

    public boolean deleteGenre(String genre) {
        try {
            return this.genreList.add(Genre.valueOf(genre));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean hasUserRated(String username) {
        for (Rating rating : this.ratingList) {
            if (rating.userName.equals(username))
                return true;
        }

        return false;
    }

    public Rating findUserRating(String username) {
        for (Rating rating : this.ratingList) {
            if (rating.userName.equals(username))
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

    public abstract void displayInfo();



    @Override
    public int compareTo(@NotNull Object o) {
        if (o instanceof Production) {
            return this.title.compareTo(((Production) o).title);
        } else if (o instanceof Actor) {
            return this.title.compareTo(((Actor) o).name);
        } else return 0;
    }
}