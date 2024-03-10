package org.example;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

enum RequestStatus {
    PENDING,
    COMPLETED,
    REJECTED
}

interface RequestManager {
    void createRequest(Request r);

    void removeRequest(Request r);
}

public class Request implements RequestManager, Subject {
    public RequestType requestType;
    public LocalDateTime createdDate;
    public String name, description, userCreator, userResolver;
    private List<Observer> resolvers = new ArrayList<>();
    private List<Observer> creator = new ArrayList<>();
    private RequestStatus status;

    public Request(RequestType requestType, LocalDateTime createdDate, String description, String userCreator,
                   String userResolver) {
        this.requestType = requestType;
        this.createdDate = createdDate;
        this.description = description;
        this.userCreator = userCreator;
        this.userResolver = userResolver;
        this.status = RequestStatus.PENDING;
    }

    public Request(RequestType requestType, LocalDateTime createdDate, String description, String userCreator,
                   String userResolver, String name) {
        this.requestType = requestType;
        this.createdDate = createdDate;
        this.description = description;
        this.userCreator = userCreator;
        this.userResolver = userResolver;
        this.name = name;
        this.status = RequestStatus.PENDING;
    }

    public static Request createRegularRequest(RequestType type, Comparable<Object> problemObject, String description, String userCreator) {
        String problemName;
        if (problemObject instanceof Production) {
            problemName = ((Production) problemObject).title;
        } else {
            problemName = ((Actor) problemObject).name;
        }

        if (Admin.adminContributions.contains(problemObject)) {
            return new Request(type, LocalDateTime.now(), description, userCreator, "ADMIN", problemName);
        }

        Staff<Comparable<Object>> staff = IMDB.getInstance().findUserAfterContribution(problemObject);
        return new Request(type, LocalDateTime.now(), description, userCreator, staff.username, problemName);
    }

    public static Request createAdminRequest(RequestType type, String description, String userCreator) {
        return new Request(type, LocalDateTime.now(), description, userCreator, "ADMIN");
    }

    public void setRequestCompleted() {
        this.status = RequestStatus.COMPLETED;
    }

    public void setRequestRejected() {
        this.status = RequestStatus.REJECTED;
    }

    public void addRequest() {
        subscribe(creator, IMDB.getInstance().findUserAfterUsername(this.userCreator));

        if (this.userResolver.equals("ADMIN")) {
            RequestHolder.addRequest(this);
            for (User<Comparable<Object>> user : IMDB.getInstance().accountList) {
                if (user instanceof Admin)
                    subscribe(resolvers, user);
            }
        } else {
            Staff<Comparable<Object>> staff = ((Staff<Comparable<Object>>) IMDB.getInstance().findUserAfterUsername(this.userResolver));
            staff.individualRequests.add(this);
            subscribe(resolvers, staff);
        }

        notifyUsers(resolvers, "New " + getMiniInfo() + "!");
    }

    public void removeRequest() {
        if (this.userResolver.equals("ADMIN")) {
            RequestHolder.removeRequest(this);
            for (User<Comparable<Object>> user : IMDB.getInstance().accountList) {
                if (user instanceof Admin)
                    unsubscribe(resolvers, user);
            }
        } else {
            User<Comparable<Object>> user = IMDB.getInstance().findUserAfterUsername(this.userResolver);
            ((Staff<Comparable<Object>>) user).individualRequests.remove(this);
            this.unsubscribe(resolvers, user);
        }

        if (this.status == RequestStatus.COMPLETED) {
            notifyUsers(creator, "Completed " + getMiniInfo() + "!");

            User<Comparable<Object>> user = (User<Comparable<Object>>) creator.get(0);
            if (user instanceof Regular)
                user.experience += ((Regular<Comparable<Object>>) user).expGain.giveRequestSolvedExp();
            else
                user.experience += ((Contributor<Comparable<Object>>) user).expGain.giveRequestSolvedExp();
        } else if (this.status == RequestStatus.REJECTED) {
            notifyUsers(creator, "Rejected " + getMiniInfo() + "!");
        }
    }

    public void refactorRequestToAdmins() {
        this.userResolver = "ADMIN";
        this.addRequest();
    }

    public String getMiniInfo() {
        if (this.requestType == RequestType.MOVIE_ISSUE || this.requestType == RequestType.ACTOR_ISSUE)
            return "request about: " + this.name;
        else
            return "admin request";
    }

    public void printLesserDetails() {
        if (this.requestType == RequestType.ACTOR_ISSUE || this.requestType == RequestType.MOVIE_ISSUE)
            System.out.println("user: " + this.userCreator +
                    "\nrequest type: " + this.requestType +
                    "\nname: " + this.name +
                    "\ndescription: " + this.description);
        else
            System.out.println("user: " + this.userCreator +
                    "\nrequest type: " + this.requestType +
                    "\ndescription: " + this.description);
    }

    public String toString() {
        if (this.requestType == RequestType.ACTOR_ISSUE || this.requestType == RequestType.MOVIE_ISSUE)
            return "request type: " + this.requestType +
                    "\nname: " + this.name +
                    "\ncreated date: " + this.createdDate +
                    "\nstatus: " + this.status.toString().toLowerCase() +
                    "\ndescription: " + this.description +
                    "\ncreator: " + this.userCreator +
                    "\nresolver: " + this.userResolver;
        else
            return "request type: " + this.requestType +
                    "\ncreated date: " + this.createdDate +
                    "\nstatus: " + this.status.toString().toLowerCase() +
                    "\ndescription: " + this.description +
                    "\ncreator: " + this.userCreator +
                    "\nresolver: " + this.userResolver;
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
        for (Observer observer : observers)
            observer.update(message);
    }

    @Override
    public void createRequest(Request r) {

    }

    @Override
    public void removeRequest(Request r) {

    }

    public static class RequestHolder {
        static List<Request> requestList = new ArrayList<>();

        public static void addRequest(Request r) {
            requestList.add(r);
        }

        public static void removeRequest(Request r) {
            requestList.remove(r);
        }
    }
}