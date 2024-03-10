package org.example;

import java.util.List;

public interface Subject {
    void subscribe(List<Observer> observers, Observer newObserver);
    void unsubscribe(List<Observer> observers, Observer deleteObserver);
    void notifyUsers(List<Observer> observers, String message);
}
