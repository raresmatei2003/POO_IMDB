package org.example;

import java.util.ArrayList;
import java.util.List;

public class Rating implements Subject, Comparable<Rating> {
    String userName;
    long userRating;
    String userComment;
    List<Observer> reviewers = new ArrayList<>();
    List<Observer> contributors = new ArrayList<>();

    public Rating(String userName, long userRating, String userComment) {
        this.userName = userName;
        this.userRating = userRating;
        this.userComment = userComment;
    }

    @Override
    public String toString() {
        return userName + ": " + userComment + " note: " + userRating + "\n";
    }

    @Override
    public void subscribe(List<Observer> observers, Observer newObserver) {
        if (!observers.contains(newObserver))
            observers.add(newObserver);
    }

    @Override
    public void unsubscribe(List<Observer> observers, Observer deleteObserver) {
        observers.remove(deleteObserver);
    }

    @Override
    public void notifyUsers(List<Observer> observers, String message) {
        for (Observer observer : observers) {
            observer.update(message);
        }
    }

    @Override
    public int compareTo(Rating rating) {
        User<Comparable<Object>> user1 = IMDB.getInstance().findUserAfterUsername(this.userName);
        User<Comparable<Object>> user2 = IMDB.getInstance().findUserAfterUsername(rating.userName);

        if(user1.experience > user2.experience)
            return -1;
        else if(user1.experience == user2.experience)
            return 0;
        return 1;
    }
}
