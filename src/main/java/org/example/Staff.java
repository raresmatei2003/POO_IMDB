package org.example;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

interface StaffInterface {
    void addProductionSystem(Production p);

    void addActorSystem(Actor a);

    void removeProductionSystem(String name);

    void removeActorSystem(String name);

    void updateProduction(Production p);

    void updateActor(Actor a);

    void resolveRequest(Request r);
}

public abstract class Staff<T extends Comparable<Object>> extends User<T> implements StaffInterface {
    List<Request> individualRequests;
    SortedSet<Comparable<Object>> contributions;

    public Staff(String email, String password, String name, String country, long age, Gender gender, LocalDateTime birthDate,
                 AccountType accountType, String username) {
        super(email, password, name, country, age, gender, birthDate, accountType, username);
        individualRequests = new ArrayList<>();
        contributions = new TreeSet<>();
    }

    @Override
    public void addProductionSystem(Production p) {
        IMDB.getInstance().productionList.add(p);
        this.contributions.add(p);
    }

    @Override
    public void addActorSystem(Actor a) {
        IMDB.getInstance().actorList.add(a);
        this.contributions.add(a);
    }

    @Override
    public void removeProductionSystem(String name) {
        IMDB.getInstance().productionList.removeIf(obj -> name.equals(obj.title));
        contributions.removeIf(obj -> obj instanceof Production && name.equals(((Production) obj).title));

    }

    @Override
    public void removeActorSystem(String name) {
        IMDB.getInstance().actorList.removeIf(obj -> name.equals(obj.name));
        contributions.removeIf(obj -> obj instanceof Actor && name.equals(((Actor) obj).name));
    }

    @Override
    public void updateProduction(Production p) {
        removeProductionSystem(p.title);
        addProductionSystem(p);
    }

    @Override
    public void updateActor(Actor a) {
        removeActorSystem(a.name);
        addActorSystem(a);
    }

    @Override
    public void resolveRequest(Request r) {

    }
}