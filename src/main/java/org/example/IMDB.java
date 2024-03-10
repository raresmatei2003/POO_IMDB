package org.example;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import kotlin.Pair;

public class IMDB {
    private static IMDB app = null;
    List<User<Comparable<Object>>> accountList;
    public List<Actor> actorList;
    List<Request> requestList;
    public List<Production> productionList;

    private IMDB() {
        this.accountList = new ArrayList<>();
        this.actorList = new ArrayList<>();
        this.requestList = new ArrayList<>();
        this.productionList = new ArrayList<>();
    }

    public static IMDB getInstance() {
        if (app == null)
            app = new IMDB();
        return app;
    }

    private void parseProductions() {
        try {
            String path = "src/main/resources/input/production.json";
            FileReader fileReader = new FileReader(path);
            JSONParser parser = new JSONParser();
            JSONArray array = (JSONArray) parser.parse(fileReader);
            for (Object object : array) {
                JSONObject jsonObject = (JSONObject) object;

                String title = (String) jsonObject.get("title");

                List<String> directorList = new ArrayList<>();
                JSONArray jsonList = (JSONArray) jsonObject.get("directors");
                for (Object obj : jsonList) {
                    directorList.add((String) obj);
                }

                List<String> actorList = new ArrayList<>();
                jsonList = (JSONArray) jsonObject.get("actors");
                for (Object obj : jsonList) {
                    actorList.add((String) obj);
                }

                List<Genre> genreList = new ArrayList<>();
                jsonList = (JSONArray) jsonObject.get("genres");
                for (Object obj : jsonList) {
                    genreList.add(Genre.valueOf((String) obj));
                }

                List<Rating> ratingList = new ArrayList<>();
                jsonList = (JSONArray) jsonObject.get("ratings");
                for (Object obj : jsonList) {
                    Rating rating = new Rating((String) ((JSONObject) obj).get("username"),
                            (long) ((JSONObject) obj).get("rating"), (String) ((JSONObject) obj).get("comment"));
                    ratingList.add(rating);
                }

                String description = (String) jsonObject.get("plot");
                double avgRating = (double) jsonObject.get("averageRating");

                ProductionType prodType = ProductionType.valueOf((String) jsonObject.get("type"));
                if (prodType == ProductionType.Movie) {
                    Movie movie;

                    Object jsonLength = jsonObject.get("duration");
                    Object jsonReleaseYear = jsonObject.get("releaseYear");

                    String length = null;
                    long releaseYear = 0;

                    if (jsonLength != null)
                        length = (String) jsonLength;
                    if (jsonReleaseYear != null)
                        releaseYear = (long) jsonReleaseYear;

                    movie = new Movie(title, directorList, actorList, genreList, ratingList, description,
                            avgRating, length, releaseYear);

                    this.productionList.add(movie);
                } else if (prodType == ProductionType.Series) {
                    Series series;

                    Object jsonReleaseYear = jsonObject.get("releaseYear");
                    Object jsonSeasonNumber = jsonObject.get("numSeasons");
                    Object jsonSeasons = jsonObject.get("seasons");

                    long releaseYear = 0, seasonNumber = 0;
                    Map<String, List<Episode>> seasons = new HashMap<>();

                    if (jsonReleaseYear != null)
                        releaseYear = (long) jsonReleaseYear;
                    if (jsonSeasonNumber != null)
                        seasonNumber = (long) jsonSeasonNumber;

                    if (jsonSeasons != null) {
                        for (Object jsonSeasonName : ((JSONObject) jsonSeasons).keySet()) {
                            JSONArray jsonSeasonEpisodes = (JSONArray) ((JSONObject) jsonSeasons).get(jsonSeasonName);
                            List<Episode> seasonEpisodes = new ArrayList<>();

                            for (Object jsonEpisode : jsonSeasonEpisodes) {
                                Episode episode = new Episode((String) ((JSONObject) jsonEpisode).get("episodeName"),
                                        (String) ((JSONObject) jsonEpisode).get("duration"));
                                seasonEpisodes.add(episode);
                            }
                            seasons.put((String) jsonSeasonName, seasonEpisodes);
                        }
                    }

                    series = new Series(title, directorList, actorList, genreList, ratingList, description,
                            avgRating, releaseYear, seasonNumber, seasons);

                    this.productionList.add(series);
                }
            }

            fileReader.close();
        } catch (IOException | ParseException e) {
            throw new RuntimeException(e);
        }
    }

    private void parseActors() {
        try {
            String path = "src/main/resources/input/actors.json";
            FileReader fileReader = new FileReader(path);
            JSONParser parser = new JSONParser();
            JSONArray array = (JSONArray) parser.parse(fileReader);

            for (Object object : array) {
                JSONObject jsonObject = (JSONObject) object;

                String name = (String) jsonObject.get("name");
                String biography = (String) jsonObject.get("biography");

                List<Pair<String, String>> performances = new ArrayList<>();
                JSONArray jsonPerformances = (JSONArray) jsonObject.get("performances");
                for (Object obj : jsonPerformances) {
                    Pair<String, String> pair = new Pair<>(((JSONObject) obj).get("title").toString(),
                            ((JSONObject) obj).get("type").toString());
                    performances.add(pair);
                }

                Actor actor = new Actor(name, performances, biography);
                this.actorList.add(actor);
            }

        } catch (IOException | ParseException e) {
            throw new RuntimeException(e);
        }
    }

    private void parseUsers() {
        try {
            String path = "src/main/resources/input/accounts.json";
            FileReader fileReader = new FileReader(path);
            JSONParser parser = new JSONParser();
            JSONArray array = (JSONArray) parser.parse(fileReader);

            for (Object object : array) {
                JSONObject jsonObject = (JSONObject) object;

                String username = (String) jsonObject.get("username");

                String email, password, name, country;
                long age;
                Gender gender;
                LocalDateTime birthDate;

                int experience = Integer.MAX_VALUE;
                if (jsonObject.get("experience") != null)
                    experience = Integer.parseInt((String) jsonObject.get("experience"));

                AccountType accountType = AccountType.valueOf(((String) jsonObject.get("userType")).toUpperCase());

                JSONObject jsonInfo = (JSONObject) jsonObject.get("information");
                JSONObject jsonCred = (JSONObject) jsonInfo.get("credentials");

                email = (String) jsonCred.get("email");
                password = (String) jsonCred.get("password");
                name = (String) jsonInfo.get("name");
                country = (String) jsonInfo.get("country");
                age = (long) jsonInfo.get("age");
                gender = Gender.valueOf(String.valueOf(((String) jsonInfo.get("gender")).charAt(0)));
                birthDate = LocalDate.parse((String) jsonInfo.get("birthDate"), DateTimeFormatter.ofPattern("yyyy-MM-dd")).atStartOfDay();

                SortedSet<Comparable<Object>> preferences = new TreeSet<>();
                JSONArray jsonFavProd = (JSONArray) jsonObject.get("favoriteProductions");
                if (jsonFavProd != null) {
                    for (Object title : jsonFavProd) {
                        Production production = searchProduction((String) title);
                        if (production != null)
                            preferences.add(production);
                    }
                }

                JSONArray jsonFavActors = (JSONArray) jsonObject.get("favoriteActors");
                if (jsonFavActors != null) {
                    for (Object actorName : jsonFavActors) {
                        Actor actor = searchActor((String) actorName);
                        if (actor != null)
                            preferences.add(actor);
                    }
                }

                List<String> notifications = new ArrayList<>();
                JSONArray jsonNotifications = (JSONArray) jsonObject.get("notifications");
                if (jsonNotifications != null) {
                    for (Object n : jsonNotifications) {
                        notifications.add((String) n);
                    }
                }

                User<Comparable<Object>> user = UserFactory.factory(email, password, name, country, age, gender,
                        birthDate, accountType, username);
                assert user != null;
                user.experience = experience;
                user.preferences = preferences;
                user.notifications = notifications;

                if (user instanceof Staff) {
                    SortedSet<Comparable<Object>> contributions = new TreeSet<>();

                    JSONArray jsonProdContr = (JSONArray) jsonObject.get("productionsContribution");
                    if (jsonProdContr != null) {
                        for (Object prodName : jsonProdContr) {
                            for (Production prod : this.productionList) {
                                if (prod.title.equals(prodName)) {
                                    contributions.add(prod);
                                }
                            }
                        }
                    }

                    JSONArray jsonActorContr = (JSONArray) jsonObject.get("actorsContribution");
                    if (jsonActorContr != null) {
                        for (Object actorName : jsonActorContr) {
                            for (Actor actor : this.actorList) {
                                if (actor.name.equals(actorName))
                                    contributions.add(actor);
                            }
                        }
                    }

                    ((Staff<Comparable<Object>>) user).contributions = contributions;
                }

                this.accountList.add(user);
            }

        } catch (IOException | ParseException e) {
            throw new RuntimeException(e);
        }
    }

    private void parseRequests() {
        try {
            String path = "src/main/resources/input/requests.json";
            FileReader fileReader = new FileReader(path);
            JSONParser parser = new JSONParser();
            JSONArray array = (JSONArray) parser.parse(fileReader);

            for (Object object : array) {
                JSONObject jsonObject = (JSONObject) object;

                RequestType requestType = RequestType.valueOf((String) jsonObject.get("type"));

                DateTimeFormatter formator = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
                LocalDateTime createdDate = LocalDateTime.parse((String) jsonObject.get("createdDate"), formator);

                String username = (String) jsonObject.get("username");
                String resolver = (String) jsonObject.get("to");
                String description = (String) jsonObject.get("description");

                Request request;
                if (requestType.equals(RequestType.ACTOR_ISSUE)) {
                    String actorName = (String) jsonObject.get("actorName");
                    request = new Request(requestType, createdDate, description, username, resolver, actorName);

                } else if (requestType.equals(RequestType.MOVIE_ISSUE)) {
                    String movieTitle = (String) jsonObject.get("movieTitle");
                    request = new Request(requestType, createdDate, description, username, resolver, movieTitle);

                } else {
                    request = new Request(requestType, createdDate, description, username, resolver);
                }

                request.addRequest();
                this.requestList.add(request);
            }

        } catch (IOException | ParseException e) {
            throw new RuntimeException(e);
        }
    }

    private void addProductionActors() {
        for (Production p : this.productionList) {
            for (String name : p.actorList) {
                if (searchActor(name) == null) {
                    List<Pair<String, String>> performances = new ArrayList<>();

                    Pair<String, String> pair;
                    if (p instanceof Movie)
                        pair = new Pair<>(p.title, "Movie");
                    else
                        pair = new Pair<>(p.title, "Series");

                    performances.add(pair);
                    Actor actor = new Actor(name, performances, "");

                    this.actorList.add(actor);
                    Admin.addAdminContribution(actor);
                }
            }
        }
    }

    private void sortObjectsRatings() {
        for (Production production : this.productionList)
            production.sortRatings();

        for (Actor actor : this.actorList)
            actor.sortRatings();
    }

    private void waitForEnter() {
        System.out.println("\nPress enter to continue.");
        (new Scanner(System.in)).nextLine();
    }

    public int getIntFromUser(String message) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print(message);
            try {
                return Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input.");
            }
        }
    }

    public String chooseInterface() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Choose interface (CLI or GUI):");

        while (true) {
            String input = scanner.nextLine();

            if (input.equals("CLI") || input.equals("GUI")) {
                return input;
            } else {
                System.out.println("please choose one of the two");
                // throw new InvalidCommandException("Invalid choice.");
            }
        }
    }

    private User<Comparable<Object>> findUserAfterEmail(String email) {
        for (User<Comparable<Object>> u : IMDB.getInstance().accountList) {
            if (u.information.getEmail().equals(email))
                return u;
        }

        return null;
    }

    public User<Comparable<Object>> findUserAfterUsername(String username) {
        for (User<Comparable<Object>> u : IMDB.getInstance().accountList) {
            if (u.username.equals(username))
                return u;
        }

        return null;
    }

    public Staff<Comparable<Object>> findUserAfterContribution(Comparable<Object> contribution) {
        for (User<Comparable<Object>> u : this.accountList) {
            if (u instanceof Staff && ((Staff<Comparable<Object>>) u).contributions.contains(contribution))
                return (Staff<Comparable<Object>>) u;
        }

        return null;
    }


    private User<Comparable<Object>> tryLogin(String email, String password) {
        User<Comparable<Object>> user = findUserAfterEmail(email);

        if (user != null) {
            if (password.equals(user.information.getPassword())) {
                return user;
            } else {
                System.out.println("Wrong password! Retry logging in!");
            }
        } else {
            System.out.println("Email not found. Try again!");
        }

        waitForEnter();
        return null;
    }

    private User<Comparable<Object>> login() {
        System.out.println("Welcome back! Enter your credentials!");
        Scanner scanner = new Scanner(System.in);

        User<Comparable<Object>> user = null;
        while (user == null) {
            System.out.print("email: ");
            String email = scanner.nextLine();

            System.out.print("password: ");
            String password = scanner.nextLine();

            user = tryLogin(email, password);
        }

        return user;
    }

    private void printProductionsTitles() {
        for (Production production : this.productionList)
            System.out.println(production.title);
    }

    private void printActorsNames() {
        for (Actor actor : this.actorList)
            System.out.println(actor.name);
    }

    private void printNonAdminUsernames() {
        for (User<Comparable<Object>> user : this.accountList) {
            if (user instanceof Regular || user instanceof Contributor)
                System.out.println(user.username);
        }
    }

    private void printChoiceProductionsTitles(List<Production> productionList) {
        int index = 1;
        for (Production production : productionList) {
            System.out.println(index + ") " + production.title);
            index++;
        }
    }

    private void printChoiceActorsNames(List<Actor> actorList) {
        int index = 1;
        for (Actor actor : actorList) {
            System.out.println(index + ") " + actor.name);
            index++;
        }
    }

    private void viewProductionsDetails() {
        System.out.println("Type 'genre' to use the genre filter or 'reviews' to use the reviews filter. Leave blank" +
                " to display all productions");

        sortObjectsRatings();

        Scanner scanner = new Scanner(System.in);
        while (true) {
            String filter = scanner.nextLine();

            if (filter.isEmpty()) {
                for (Production p : this.productionList) {
                    p.displayInfo();
                }
                break;
            } else if (filter.equals("genre")) {
                System.out.println(Arrays.toString(Genre.values()));

                Genre genre;
                while (true) {
                    System.out.print("Choose a genre: ");
                    filter = scanner.nextLine();
                    try {
                        genre = Genre.valueOf(filter);
                        break;
                    } catch (IllegalArgumentException e) {
                        System.out.println("Enter a valid genre.");
                    }
                }

                boolean ok = false;

                for (Production p : this.productionList) {
                    if (p.genreList.contains(genre)) {
                        p.displayInfo();
                        ok = true;
                    }
                }

                if (!ok)
                    System.out.println("No production of this genre.");

                break;

            } else if (filter.equals("reviews")) {
                int reviewNumber;
                while (true) {
                    System.out.print("Type a number to represent the minimum number of reviews a production should have: ");
                    try {
                        filter = scanner.nextLine();
                        reviewNumber = Integer.parseInt(filter);
                        break;
                    } catch (NumberFormatException e) {
                        System.out.println("Enter a valid number.");
                    }
                }

                boolean ok = false;

                for (Production p : this.productionList) {
                    if (p.ratingList.size() >= reviewNumber) {
                        p.displayInfo();
                        ok = true;
                    }
                }

                if (!ok)
                    System.out.println("No production with this many reviews.");

                break;

            } else {
                System.out.println("Invalid input");
            }
        }

        waitForEnter();
    }

    private void viewActorsDetails() {
        System.out.println("Type 'sorted' if you want the actors to be sorted after their name, otherwise leave blank");
        Scanner scanner = new Scanner(System.in);

        sortObjectsRatings();

        while (true) {
            String input = scanner.nextLine();

            if (input.isEmpty()) {
                for (Actor a : this.actorList)
                    System.out.println(a.toString() + "\n");
                break;

            } else if (input.equals("sorted")) {
                List<Actor> actors = new ArrayList<>(this.actorList);
                actors.sort(Actor::compareTo);

                for (Actor a : actors)
                    System.out.println(a.toString() + "\n");
                break;

            } else {
                System.out.println("Invalid input.");
            }
        }

        waitForEnter();
    }

    private void viewNotifications(User<Comparable<Object>> user) {
        if (user.notifications.isEmpty()) {
            System.out.println("You have no notifications. :(");

        } else {
            System.out.println(user.notifications);

            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.println("Want to delete notifications? (yes/no)");
                String choice = scanner.nextLine();
                if (choice.equals("yes")) {
                    user.deleteNotifications();
                    System.out.println("Notifications deleted.");
                    break;
                } else if (choice.equals("no")) {
                    break;
                } else {
                    System.out.println("Invalid input.");
                }
            }
        }

        waitForEnter();
    }

    private Actor searchActor(String name) {
        for (Actor a : this.actorList) {
            if (a.name.equals(name))
                return a;
        }

        return null;
    }

    private Production searchProduction(String title) {
        for (Production p : this.productionList) {
            if (p.title.equals(title))
                return p;
        }

        return null;
    }

    private Comparable<Object> searchSortedSet(SortedSet<Comparable<Object>> sortedSet, String name) {
        for (Comparable<Object> object : sortedSet) {
            if ((object instanceof Actor && ((Actor) object).name.equals(name))
                    || (object instanceof Production && ((Production) object).title.equals(name))) {
                return object;
            }
        }

        return null;
    }

    private Comparable<Object> searchComparableSortedSet(SortedSet<Comparable<Object>> sortedSet, String name) {
        for (Comparable<Object> object : sortedSet) {
            if ((object instanceof Actor && ((Actor) object).name.equals(name))
                    || (object instanceof Production && ((Production) object).title.equals(name))) {
                return object;
            }
        }

        return null;
    }

    private void printSortedSet(SortedSet<Comparable<Object>> sortedSet) {
        for (Object object : sortedSet) {
            if (object instanceof Actor)
                System.out.println(((Actor) object).name);
            else
                System.out.println(((Production) object).title);
        }
    }

    private void printComparableSortedSet(SortedSet<Comparable<Object>> sortedSet) {
        for (Object object : sortedSet) {
            if (object instanceof Actor)
                System.out.println(((Actor) object).name);
            else
                System.out.println(((Production) object).title);
        }
    }

    private void search() {
        Scanner scanner = new Scanner(System.in);

        sortObjectsRatings();

        System.out.println("What do you want to search? (actor/movie/series)");
        label:
        while (true) {
            String input = scanner.nextLine();

            switch (input) {
                case "actor":
                    System.out.print("Type actor name: ");
                    Actor actor = searchActor(scanner.nextLine());
                    if (actor == null) {
                        System.out.println("Actor not found.");
                    } else {
                        System.out.println(actor);
                    }

                    break label;
                case "movie":
                    System.out.print("Type movie title: ");
                    Movie movie = (Movie) searchProduction(scanner.nextLine());
                    if (movie == null) {
                        System.out.println("Movie not found");
                    } else
                        movie.displayInfo();

                    break label;
                case "series":
                    System.out.print("Type series title: ");
                    Series series = (Series) searchProduction(scanner.nextLine());
                    if (series == null) {
                        System.out.println("Series not found");
                    } else
                        series.displayInfo();

                    break label;
                default:
                    System.out.println("Invalid input");
            }
        }

        waitForEnter();
    }

    private void addToFavourites(User<Comparable<Object>> user) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("What do you want to add? (actor/movie/series)");
        label:
        while (true) {
            String input = scanner.nextLine();

            switch (input) {
                case "actor":
                    System.out.print("Type actor name: ");
                    Actor actor = searchActor(scanner.nextLine());
                    if (actor == null) {
                        System.out.println("Actor not found.");
                    } else {
                        user.addPreference(actor);
                    }

                    break label;
                case "movie":
                    System.out.print("Type movie title: ");
                    Movie movie = (Movie) searchProduction(scanner.nextLine());
                    if (movie == null) {
                        System.out.println("Movie not found.");
                    } else
                        user.addPreference(movie);

                    break label;
                case "series":
                    System.out.print("Type series title: ");
                    Series series = (Series) searchProduction(scanner.nextLine());
                    if (series == null) {
                        System.out.println("Series not found.");
                    } else
                        user.addPreference(series);

                    break label;
                default:
                    System.out.println("Invalid input.");
            }
        }
    }

    private void deleteFromFavourites(User<Comparable<Object>> user) {
        if (user.preferences.isEmpty()) {
            System.out.println("Nothing to delete.");
            return;
        }

        printComparableSortedSet(user.preferences);

        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("\nChoose something to delete: ");
            Comparable<Object> toDelete = searchComparableSortedSet(user.preferences, scanner.nextLine());

            if (toDelete != null) {
                user.deletePreference(toDelete);
                break;
            }

            System.out.println("Invalid input. Choose something from the list to delete.");
        }

    }


    private void manageFavourites(User<Comparable<Object>> user) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Type 'add' to add to favorites or type 'delete' to delete from favorites");

        while (true) {
            String input = scanner.nextLine();

            if (input.equals("add")) {
                addToFavourites(user);
                break;
            } else if (input.equals("delete")) {
                deleteFromFavourites(user);
                break;
            } else {
                System.out.println("Invalid input");
            }
        }

        waitForEnter();
    }

    private Actor readActorDetails() {
        Scanner scanner = new Scanner(System.in);

        System.out.print("name: ");
        String name = scanner.nextLine();

        if (searchActor(name) != null) {
            System.out.println("Actor already exists.");
            return null;
        }

        int performanceNr = getIntFromUser("how many performances: ");

        List<Pair<String, String>> performances = new ArrayList<>();
        for (int i = 0; i < performanceNr; i++) {
            System.out.print("performance title: ");
            String title = scanner.nextLine();

            System.out.print("performance production type: ");
            String type = scanner.nextLine();

            Pair<String, String> pair = new Pair<>(title, type);
            performances.add(pair);
        }

        System.out.print("biography: ");
        String biography = scanner.nextLine();

        return new Actor(name, performances, biography);
    }

    private Movie readMovieDetails() {
        Scanner scanner = new Scanner(System.in);

        System.out.print("title: ");
        String title = scanner.nextLine();

        if (searchProduction(title) != null) {
            System.out.println("Movie already exists.");
            return null;
        }

        int nr = getIntFromUser("how many directors: ");

        List<String> directors = new ArrayList<>();
        for (int i = 0; i < nr; i++) {
            System.out.print("name: ");
            directors.add(scanner.nextLine());
        }

        nr = getIntFromUser("how many actors: ");

        List<String> actors = new ArrayList<>();
        for (int i = 0; i < nr; i++) {
            System.out.print("name: ");
            actors.add(scanner.nextLine());
        }

        nr = getIntFromUser("how many genres: ");

        List<Genre> genres = new ArrayList<>();
        for (int i = 0; i < nr; i++) {
            System.out.print("genre: ");

            String genre = scanner.nextLine();
            try {
                genres.add(Genre.valueOf(genre));
            } catch (IllegalArgumentException e) {
                System.out.println("Genre not known, try another");
                i--;
            }
        }

        List<Rating> ratings = new ArrayList<>();

        System.out.print("description: ");
        String description = scanner.nextLine();

        double avgRating = 0.0;

        System.out.print("length (minutes): ");
        String length = scanner.nextLine();

        long releaseYear = getIntFromUser("release year: ");

        return new Movie(title, directors, actors, genres, ratings, description, avgRating, length, releaseYear);
    }

    private Series readSeriesDetails() {
        Scanner scanner = new Scanner(System.in);

        System.out.print("title: ");
        String title = scanner.nextLine();

        if (searchProduction(title) != null) {
            System.out.println("Series already exists.");
            return null;
        }

        int nr = getIntFromUser("how many directors: ");

        List<String> directors = new ArrayList<>();
        for (int i = 0; i < nr; i++) {
            System.out.print("name: ");
            directors.add(scanner.nextLine());
        }

        nr = getIntFromUser("how many actors: ");

        List<String> actors = new ArrayList<>();
        for (int i = 0; i < nr; i++) {
            System.out.print("name: ");
            actors.add(scanner.nextLine());
        }

        nr = getIntFromUser("how many genres: ");

        List<Genre> genres = new ArrayList<>();
        for (int i = 0; i < nr; i++) {
            System.out.print("genre: ");

            String genre = scanner.nextLine();
            try {
                genres.add(Genre.valueOf(genre));
            } catch (IllegalArgumentException e) {
                System.out.println("Genre not known, try another");
                i--;
            }
        }

        List<Rating> ratings = new ArrayList<>();

        System.out.print("description: ");
        String description = scanner.nextLine();

        double avgRating = 0.0;

        long releaseYear = getIntFromUser("release year: ");

        long seasonsNumber = getIntFromUser("seasons number: ");

        Map<String, List<Episode>> seasons = new HashMap<>();
        for (int i = 0; i < seasonsNumber; i++) {
            System.out.print("season name: ");
            String seasonName = scanner.nextLine();

            nr = getIntFromUser("episodes number: ");

            List<Episode> episodes = new ArrayList<>();
            for (int j = 0; j < nr; j++) {
                System.out.print("name: ");
                String name = scanner.nextLine();

                System.out.print("length (minutes): ");
                String length = scanner.nextLine();

                episodes.add(new Episode(name, length));
            }

            seasons.put(seasonName, episodes);
        }

        return new Series(title, directors, actors, genres, ratings, description, avgRating, releaseYear, seasonsNumber,
                seasons);

    }

    private void addToSystem(Staff<Comparable<Object>> user) {
        System.out.print("Choose what to add to the system (actor/movie/series):");
        Scanner scanner = new Scanner(System.in);

        label:
        while (true) {
            String input = scanner.nextLine();
            switch (input) {
                case "actor":
                    Actor actor = readActorDetails();
                    if (actor != null)
                        user.addActorSystem(actor);
                    break label;
                case "movie":
                    Movie movie = readMovieDetails();
                    if (movie != null)
                        user.addProductionSystem(movie);
                    break label;
                case "series":
                    Series series = readSeriesDetails();
                    if (series != null)
                        user.addProductionSystem(series);
                    break label;
                default:
                    System.out.println("Invalid input");
            }
        }

        if (user instanceof Contributor)
            user.experience += ((Contributor<Comparable<Object>>) user).expGain.giveContributionExp();

    }

    private void deleteFromSystem(Staff<Comparable<Object>> user) {
        SortedSet<Comparable<Object>> contributions = new TreeSet<>(user.contributions);

        if (user.accountType == AccountType.ADMIN) {
            contributions.addAll(Admin.adminContributions);
        }

        for (Object object : contributions) {
            if (object instanceof Actor)
                System.out.println(((Actor) object).name);
            else
                System.out.println(((Production) object).title);
        }

        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("Choose something to delete from the system: ");
            Comparable<Object> toDelete = searchSortedSet(contributions, scanner.nextLine());

            if (toDelete == null) {
                System.out.println("Invalid input. Choose something from the list to delete.");
            } else {
                if (user.accountType == AccountType.ADMIN && Admin.adminContributions.contains(toDelete)) {
                    Admin.deleteAdminContribution(toDelete);
                } else {
                    if (toDelete instanceof Actor)
                        user.removeActorSystem(((Actor) toDelete).name);
                    else
                        user.removeProductionSystem(((Production) toDelete).title);
                }
                break;
            }
        }
    }

    private void manageSystem(User<Comparable<Object>> user) {
        System.out.println("Type 'add' or 'delete' to add/delete an actor/movie/series to/from the system.");
        Scanner scanner = new Scanner(System.in);

        while (true) {
            String input = scanner.nextLine();
            if (input.equals("add")) {
                addToSystem((Staff<Comparable<Object>>) user);
                break;
            } else if (input.equals("delete")) {
                deleteFromSystem((Staff<Comparable<Object>>) user);
                break;
            } else {
                System.out.println("Invalid input");
            }
        }

        waitForEnter();
    }

    private void updateActorInfo(Staff<Comparable<Object>> user) {
        SortedSet<Comparable<Object>> actors = new TreeSet<>();

        if (user.accountType == AccountType.ADMIN) {
            for (Comparable<Object> object : Admin.adminContributions)
                if (object instanceof Actor)
                    actors.add(object);
        }

        for (Comparable<Object> object : user.contributions)
            if (object instanceof Actor)
                actors.add(object);

        if (actors.isEmpty()) {
            System.out.println("You have access to no actor.");
            waitForEnter();
            return;
        }

        printSortedSet(actors);

        Scanner scanner = new Scanner(System.in);
        Actor actor;

        while (true) {
            System.out.print("Choose actor: ");
            actor = (Actor) searchSortedSet(actors, scanner.nextLine());

            if (actor == null) {
                System.out.println("Choose an existing actor.");
            } else {
                break;
            }
        }

        System.out.println(actor);

        label:
        while (true) {
            System.out.println("What to modify? (name, performances, biography)");
            String choice = scanner.nextLine();

            switch (choice) {
                case "name":
                    System.out.print("New name: ");
                    actor.setName(scanner.nextLine());
                    break label;
                case "performances":
                    System.out.print("Add or delete performance? ");

                    while (true) {
                        choice = scanner.nextLine();

                        if (choice.equals("add")) {
                            System.out.print("title: ");
                            String title = scanner.nextLine();

                            System.out.print("type: ");
                            String type = scanner.nextLine();

                            actor.addPerformance(title, type);
                            break;
                        } else if (choice.equals("delete")) {
                            if (actor.performances.isEmpty()) {
                                System.out.println("Actor has no performance");
                                break label;
                            }

                            System.out.print("title: ");
                            String title = scanner.nextLine();

                            System.out.print("type: ");
                            String type = scanner.nextLine();

                            if (!actor.deletePerformance(title, type))
                                System.out.println("Performance not found.");
                            break;
                        } else {
                            System.out.println("Invalid input.");
                        }
                    }

                    break label;
                case "biography":
                    System.out.print("New biography: ");
                    actor.setPersonalBiography(scanner.nextLine());
                    break label;
                default:
                    System.out.println("Invalid input.");
                    break;
            }
        }

        waitForEnter();
    }

    private void updateInfoProduction(Staff<Comparable<Object>> user, String type) {
        SortedSet<Comparable<Object>> productions = new TreeSet<>();

        if (user.accountType == AccountType.ADMIN) {
            for (Comparable<Object> object : Admin.adminContributions)
                if ((type.equals("movie") && object instanceof Movie) ||
                        (type.equals("series") && object instanceof Series))
                    productions.add(object);
        }

        for (Comparable<Object> object : user.contributions)
            if ((type.equals("movie") && object instanceof Movie) ||
                    (type.equals("series") && object instanceof Series))
                productions.add(object);

        if (productions.isEmpty()) {
            System.out.println("You have access to no " + type);
            waitForEnter();
            return;
        }

        printSortedSet(productions);

        Scanner scanner = new Scanner(System.in);
        Production production;

        while (true) {
            System.out.print("Choose production: ");

            production = (Production) searchSortedSet(productions, scanner.nextLine());

            if (production == null) {
                System.out.println("Choose an existing " + type + ".");
            } else {
                break;
            }
        }

        production.displayInfo();

        label:
        while (true) {
            System.out.print("What to modify? (title, directors, actors, genres, description, release year, ");
            if (type.equals("movie"))
                System.out.println("length)");
            else
                System.out.println("seasons)");

            String choice = scanner.nextLine();
            switch (choice) {
                case "title":
                    System.out.print("New title: ");
                    production.setTitle(scanner.nextLine());
                    break label;
                case "directors":
                    System.out.print("Add or delete director? ");

                    while (true) {
                        choice = scanner.nextLine();

                        if (choice.equals("add")) {
                            System.out.print("Director name: ");
                            production.addDirector(scanner.nextLine());
                            break;
                        } else if (choice.equals("delete")) {
                            if (production.directorList.isEmpty()) {
                                System.out.println("Production doesn't have directors.");
                                break label;
                            }

                            System.out.print("Director name: ");

                            if (!production.deleteDirector(scanner.nextLine()))
                                System.out.println("Director not found.");
                            break;
                        } else {
                            System.out.println("Invalid input.");
                        }
                    }
                    break label;
                case "actors":
                    System.out.print("Add or delete actor? ");

                    while (true) {
                        choice = scanner.nextLine();

                        if (choice.equals("add")) {
                            System.out.print("Actor name: ");
                            production.addActor(scanner.nextLine());
                            break;
                        } else if (choice.equals("delete")) {
                            if (production.actorList.isEmpty()) {
                                System.out.println("Production doesn't have actors.");
                                break label;
                            }

                            System.out.print("Actor name: ");

                            if (!production.deleteActor(scanner.nextLine()))
                                System.out.println("Actor not found.");
                            break;
                        } else {
                            System.out.println("Invalid input.");
                        }
                    }
                    break label;
                case "genres":
                    System.out.print("Add or delete genre? ");

                    while (true) {
                        choice = scanner.nextLine();

                        if (choice.equals("add")) {
                            System.out.print("Genre: ");
                            production.addGenre(scanner.nextLine());
                            break;
                        } else if (choice.equals("delete")) {
                            if (production.genreList.isEmpty()) {
                                System.out.println("Production doesn't have any genre.");
                                break label;
                            }

                            System.out.print("Genre: ");

                            if (!production.deleteGenre(scanner.nextLine()))
                                System.out.println("Genre not found.");
                            break;
                        } else {
                            System.out.println("Invalid input.");
                        }
                    }
                    break label;
                case "description":
                    System.out.print("New description: ");
                    production.setDescription(scanner.nextLine());
                    break label;
                case "release year":
                    if (type.equals("movie"))
                        ((Movie) production).setReleaseYear(getIntFromUser("New release year: "));
                    else
                        ((Series) production).setReleaseYear(getIntFromUser("New release year: "));
                    break label;
                case "length":
                    if (type.equals("movie")) {
                        ((Movie) production).setLength(getIntFromUser("New length: "));
                        break label;
                    } else {
                        System.out.println("Invalid input.");
                        break;
                    }
                case "seasons":
                    if (type.equals("series")) {
                        Series series = (Series) production;
                        System.out.println(series.seasons.keySet());

                        label1:
                        while (true) {
                            System.out.println("Select one: add/delete/modify");
                            choice = scanner.nextLine();

                            switch (choice) {
                                case "add":
                                    System.out.print("New season name: ");
                                    series.addSeason(scanner.nextLine());
                                    break label1;
                                case "delete":
                                    if (series.seasons.isEmpty()) {
                                        System.out.println("Series doesn't have any season.");
                                        break label;
                                    }

                                    System.out.print("Season name: ");

                                    if (!series.deleteSeason(scanner.nextLine()))
                                        System.out.println("Season not found.");
                                    break label1;
                                case "modify":
                                    String seasonName;

                                    while (true) {
                                        System.out.print("Choose season to modify: ");
                                        seasonName = scanner.nextLine();

                                        if (series.seasons.containsKey(seasonName))
                                            break;

                                        System.out.println("Season not found.");
                                    }

                                    while (true) {
                                        System.out.println("Modify season name? yes/no");
                                        choice = scanner.nextLine();
                                        if (choice.equals("yes")) {
                                            System.out.print("New season name: ");
                                            choice = scanner.nextLine();
                                            List<Episode> episodes = series.seasons.get(seasonName);
                                            series.deleteSeason(seasonName);
                                            series.seasons.put(choice, episodes);
                                            break;
                                        } else if (choice.equals("no")) {
                                            label2:
                                            while (true) {
                                                System.out.print("Add/delete/modify episode from season: ");
                                                choice = scanner.nextLine();
                                                String name, length;

                                                switch (choice) {
                                                    case "add":
                                                        System.out.print("name: ");
                                                        name = scanner.nextLine();

                                                        System.out.print("length (minutes): ");
                                                        length = scanner.nextLine();

                                                        series.seasons.get(seasonName).add(new Episode(name, length));
                                                        break label2;
                                                    case "delete":
                                                        for (Episode episode : series.seasons.get(seasonName))
                                                            System.out.println(episode.name);

                                                        boolean ok2 = true;
                                                        while (ok2) {
                                                            System.out.println("What episode to delete?");
                                                            String title = scanner.nextLine();

                                                            for (Episode episode : series.seasons.get(seasonName))
                                                                if (episode.name.equals(title)) {
                                                                    series.seasons.get(seasonName).remove(episode);
                                                                    ok2 = false;
                                                                    break;
                                                                }

                                                            if (ok2)
                                                                System.out.println("Invalid episode.");

                                                        }
                                                        break label2;
                                                    case "modify":
                                                        List<Episode> episodes = series.seasons.get(seasonName);
                                                        for (Episode episode : episodes)
                                                            System.out.println(episode.name);

                                                        boolean ok3 = true;
                                                        while (ok3) {
                                                            System.out.println("Which one?");
                                                            choice = scanner.nextLine();

                                                            for (Episode episode : episodes) {
                                                                if (episode.name.equals(choice)) {
                                                                    System.out.print("New episode name: ");
                                                                    name = scanner.nextLine();

                                                                    System.out.print("New episode length (minutes): ");
                                                                    length = scanner.nextLine();

                                                                    episode.name = name;
                                                                    episode.length = length;

                                                                    ok3 = false;
                                                                    break;
                                                                }
                                                            }
                                                            if (ok3)
                                                                System.out.println("Episode not found in season.");
                                                        }

                                                        break label2;
                                                    default:
                                                        System.out.println("Invalid choice.");
                                                }
                                            }

                                            break;
                                        } else {
                                            System.out.println("Invalid input.");
                                        }
                                    }


                                    break label1;
                                default:
                                    System.out.println("Invalid output.");
                                    break;
                            }
                        }

                        break label;
                    } else {
                        System.out.println("Invalid input.");
                        break;
                    }
                default:
                    System.out.println("Invalid input.");
                    break;
            }
        }

        waitForEnter();
    }

    private void updateInfo(User<Comparable<Object>> user) {
        System.out.println("Type 'actor', 'movie' or 'series' to update the info on an actor/movie/series.");
        Scanner scanner = new Scanner(System.in);

        while (true) {
            String input = scanner.nextLine();
            if (input.equals("actor")) {
                updateActorInfo((Staff<Comparable<Object>>) user);
                break;
            } else if (input.equals("movie") || input.equals("series")) {
                updateInfoProduction((Staff<Comparable<Object>>) user, input);
                break;
            } else {
                System.out.println("Invalid input.");
            }
        }
    }

    private List<Request> getRequestsToSolve(Staff<Comparable<Object>> user) {
        List<Request> requestsToSolve = new ArrayList<>();
        if (user.accountType == AccountType.ADMIN) {
            requestsToSolve.addAll(Request.RequestHolder.requestList);
        }

        requestsToSolve.addAll(user.individualRequests);

        return requestsToSolve;
    }

    private void viewPersonalRequests(Staff<Comparable<Object>> user) {
        List<Request> requestsToSolve = getRequestsToSolve(user);
        if (requestsToSolve.isEmpty()) {
            System.out.println("No requests avalible.");
        } else {
            printRequests(requestsToSolve);
        }

        waitForEnter();
    }

    private void solveRequests(Staff<Comparable<Object>> user) {
        Scanner scanner = new Scanner(System.in);
        List<Request> requestsToSolve = getRequestsToSolve(user);
        if (requestsToSolve.isEmpty()) {
            System.out.println("No requests avalible.");
            waitForEnter();
            return;
        }

        printRequests(requestsToSolve);

        while (true) {
            int index = getIntFromUser("Choose the request you want to view (type the index): ");
            if (index > requestsToSolve.size() || index <= 0)
                System.out.println("Invalid index.");
            else {
                Request request = requestsToSolve.get(index - 1);
                System.out.println(request);

                while (true) {
                    System.out.print("Complete or reject request? ");
                    String choice = scanner.nextLine();

                    if (choice.equals("complete")) {
                        request.setRequestCompleted();
                        break;
                    } else if (choice.equals("reject")) {
                        request.setRequestRejected();
                        break;
                    } else {
                        System.out.println("Invalid choice.");
                    }
                }

                request.removeRequest();
                break;
            }
        }

        waitForEnter();
    }

    public List<Request> getAllCreatedRequests() {
        List<Request> allRequests = new ArrayList<>(Request.RequestHolder.requestList);
        for (User<Comparable<Object>> u : this.accountList) {
            if (u instanceof Staff) {
                allRequests.addAll(((Staff<Comparable<Object>>) u).individualRequests);
            }
        }

        return allRequests;
    }

    private void printRequests(List<Request> requests) {
        int index = 1;
        for (Request request : requests) {
            System.out.println(index + ")");
            request.printLesserDetails();
            System.out.println();
            index++;
        }
    }

    private void managePersonalRequests(User<Comparable<Object>> user) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("Add or delete request: ");
            String input = scanner.nextLine();

            if (input.equals("add")) {
                System.out.println("Request types: delete account/actor issue/production issue/others");
                Request request;

                label:
                while (true) {
                    System.out.println("Choose request type");
                    input = scanner.nextLine();
                    switch (input) {
                        case "production issue":
                            printProductionsTitles();

                            while (true) {
                                System.out.print("Choose production: ");
                                Production production = searchProduction(scanner.nextLine());
                                if (production == null)
                                    System.out.println("Production not found.");
                                else if (user instanceof Contributor && ((Contributor<Comparable<Object>>) user).contributions.contains(production)) {
                                    System.out.println("A contributor can't place a request on a production he added into the system");
                                } else {
                                    System.out.print("Request description: ");
                                    String description = scanner.nextLine();

                                    request = Request.createRegularRequest(RequestType.MOVIE_ISSUE, production,
                                            description, user.username);

                                    break;
                                }
                            }

                            break label;
                        case "actor issue":
                            printActorsNames();

                            while (true) {
                                System.out.print("Choose actor: ");
                                Actor actor = searchActor(scanner.nextLine());
                                if (actor == null)
                                    System.out.println("Actor not found.");
                                else if (user instanceof Contributor && ((Contributor<Comparable<Object>>) user).contributions.contains(actor)) {
                                    System.out.println("A contributor can't place a request on an actor he added into the system");
                                } else {
                                    System.out.print("Request description: ");
                                    String description = scanner.nextLine();
                                    request = Request.createRegularRequest(RequestType.ACTOR_ISSUE, actor,
                                            description, user.username);

                                    break;
                                }
                            }

                            break label;
                        case "delete account": {
                            System.out.println("Request description: ");
                            String description = scanner.nextLine();
                            request = Request.createAdminRequest(RequestType.DELETE_ACCOUNT, description, user.username);

                            break label;
                        }
                        case "others": {
                            System.out.println("Request description: ");
                            String description = scanner.nextLine();
                            request = Request.createAdminRequest(RequestType.OTHERS, description, user.username);

                            break label;
                        }
                        default:
                            System.out.println("Invalid choice.");
                            break;
                    }
                }

                request.addRequest();

                break;
            } else if (input.equals("delete")) {
                List<Request> createdRequests = user.getPersonalCreatedRequests();
                if (createdRequests.isEmpty()) {
                    System.out.println("No requests avalible.");
                    waitForEnter();
                    return;
                }

                printRequests(createdRequests);

                while (true) {
                    int index = getIntFromUser("Choose the request you want to delete (type the index): ");
                    if (index > createdRequests.size() || index <= 0)
                        System.out.println("Invalid index.");
                    else {
                        Request request = createdRequests.get(index - 1);
                        request.removeRequest();
                        break;
                    }
                }
                break;
            } else {
                System.out.println("Invalid input.");
            }
        }

        waitForEnter();
    }

    private void manageReviews(Regular<Comparable<Object>> regular) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("Add or delete review: ");
            String input = scanner.nextLine();

            if (input.equals("add")) {

                label:
                while (true) {
                    System.out.println("What do you want to review? (production/actor)");
                    input = scanner.nextLine();
                    switch (input) {
                        case "production":
                            printProductionsTitles();

                            while (true) {
                                System.out.print("Choose production: ");
                                Production production = searchProduction(scanner.nextLine());
                                if (production == null)
                                    System.out.println("Production not found.");
                                else {
                                    if (production.hasUserRated(regular.username)) {
                                        System.out.println("User already rated this production.");
                                    }

                                    int note = getIntFromUser("Rating: ");

                                    while (note < 1 || note > 10) {
                                        System.out.println("Rate should be an integer between 1 and 10");
                                        note = getIntFromUser("Rating: ");
                                    }

                                    System.out.print("Review message: ");
                                    String message = scanner.nextLine();

                                    regular.createProductionRating(production, note, message);

                                    break;
                                }
                            }

                            break label;
                        case "actor":
                            printActorsNames();

                            while (true) {
                                System.out.print("Choose actor: ");
                                Actor actor = searchActor(scanner.nextLine());
                                if (actor == null)
                                    System.out.println("Actor not found.");
                                else {
                                    if (actor.hasUserRated(regular.username)) {
                                        System.out.println("User already rated this actor.");
                                        break;
                                    }

                                    int note = getIntFromUser("Rating: ");

                                    while (note < 1 || note > 10) {
                                        System.out.println("Rate should be an integer between 1 and 10");
                                        note = getIntFromUser("Rating: ");
                                    }

                                    System.out.print("Review message: ");
                                    String message = scanner.nextLine();

                                    regular.createActorRating(actor, note, message);

                                    break;
                                }
                            }

                            break label;
                        default:
                            System.out.println("Invalid choice.");
                            break;
                    }
                }

                break;
            } else if (input.equals("delete")) {
                label:
                while (true) {
                    System.out.println("What type of review do you want to delete? (production/actor)");
                    input = scanner.nextLine();
                    switch (input) {
                        case "production":
                            List<Production> ratedProductions = regular.getPersonalRatedProductions();
                            if (ratedProductions.isEmpty()) {
                                System.out.println("No productions reviewd.");
                                break label;
                            }
                            printChoiceProductionsTitles(ratedProductions);

                            while (true) {
                                int index = getIntFromUser("Choose production (type index): ");
                                if (index > ratedProductions.size() || index <= 0) {
                                    System.out.println("Invalid index.");
                                } else {
                                    Production production = ratedProductions.get(index - 1);
                                    Rating rating = production.findUserRating(regular.username);
                                    System.out.println(rating);

                                    while (true) {
                                        System.out.println("Delete? (yes/no) ");
                                        input = scanner.nextLine();
                                        if (input.equals("yes")) {
                                            regular.deleteProductionRating(production);
                                            System.out.println("Review deleted.");

                                            break;
                                        } else if (input.equals("no")) {
                                            System.out.println("Operation aborted.");
                                            break;
                                        } else {
                                            System.out.println("Invalid input.");
                                        }
                                    }

                                    break;
                                }
                            }

                            break label;
                        case "actor":
                            List<Actor> ratedActors = regular.getPersonalRatedActors();
                            if (ratedActors.isEmpty()) {
                                System.out.println("No actors reviewd.");
                                break label;
                            }
                            printChoiceActorsNames(ratedActors);

                            while (true) {
                                int index = getIntFromUser("Choose actor (type index): ");
                                if (index > ratedActors.size() || index <= 0)
                                    System.out.println("Invalid index");
                                else {
                                    Actor actor = ratedActors.get(index - 1);
                                    Rating rating = actor.findUserRating(regular.username);
                                    System.out.println(rating);

                                    while (true) {
                                        System.out.println("Delete? (yes/no) ");
                                        input = scanner.nextLine();
                                        if (input.equals("yes")) {
                                            regular.deleteActorRating(actor);
                                            System.out.println("Actor deleted.");

                                            break;
                                        } else if (input.equals("no")) {
                                            System.out.println("Operation aborted.");
                                            break;
                                        } else {
                                            System.out.println("Invalid input.");
                                        }
                                    }

                                    break;
                                }
                            }

                            break label;
                        default:
                            System.out.println("Invalid choice.");
                            break;
                    }
                }

                break;
            } else {
                System.out.println("Invalid input.");
            }
        }

        waitForEnter();

    }

    private String createUsername(String name) {
        String username;
        StringBuilder usernameBuilder = new StringBuilder();
        StringBuilder aux;


        for (String cuv : name.split("\\s+")) {
            usernameBuilder.append(cuv.toLowerCase()).append("_");
        }

        Random random = new Random();

        do {
            int randNr = random.nextInt(1000);
            aux = usernameBuilder;
            aux.append(randNr);
            username = aux.toString();

        } while (findUserAfterUsername(username) != null);

        return username;
    }

    private String createPassword(String name, LocalDateTime birthDate) {
        StringBuilder passwordBuilder = new StringBuilder();
        int count = 0;

        for (String cuv : name.split("\\s+")) {
            for (Character character : cuv.toCharArray()) {
                if (Character.isLowerCase(character)) {
                    switch (character) {
                        case 'a' -> character = '@';
                        case 'e' -> character = '3';
                        case 'i' -> character = '!';
                        case 'l' -> character = '1';
                        case 'b' -> character = '8';
                        case 'o', 'O' -> character = '0';
                        case 't' -> character = '+';
                    }
                }
                passwordBuilder.append(character);
            }
            passwordBuilder.append('#');
            switch (count) {
                case 0 -> {
                    int year = birthDate.getYear() % 100;
                    if (year < 10)
                        passwordBuilder.append("0").append(year);
                    else
                        passwordBuilder.append(year);
                }
                case 1 -> {
                    int month = birthDate.getMonthValue();
                    if (month < 10)
                        passwordBuilder.append("0").append(month);
                    else
                        passwordBuilder.append(month);
                }
                case 2 -> {
                    int day = birthDate.getDayOfMonth();
                    if (day < 10)
                        passwordBuilder.append("0").append(day);
                    else
                        passwordBuilder.append(day);
                }
            }
            count++;
        }

        return passwordBuilder.toString();
    }

    private void manageUsers() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Add, modify or delete user?");
        String choice = scanner.nextLine();

        while (!choice.equals("add") && !choice.equals("delete") && !choice.equals("modify")) {
            System.out.println("Invalid choice");
            choice = scanner.nextLine();
        }

        if (choice.equals("add")) {
            System.out.print("Name: ");
            String name = scanner.nextLine();

            String email;
            while (true) {
                System.out.print("Email: ");
                email = scanner.nextLine();

                String regex = "^([a-zA-Z0-9]+[.-_]*[a-zA-Z0-9]+)@([a-zA-Z0-9]+)\\.(.+){2,4}$";
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(email);

                if (!matcher.matches()) {
                    System.out.println("Email is invalid.");
                } else {
                    break;
                }
            }

            if (findUserAfterEmail(email) != null) {
                System.out.println("An user already exists using this email!");
                waitForEnter();
                return;
            }

            String username = createUsername(name);

            System.out.print("country: ");
            String country = scanner.nextLine();

            LocalDateTime birthdate;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            while (true) {
                try {
                    System.out.print("birthdate (Format: yyyy-MM-dd): ");
                    String input = scanner.nextLine();

                    birthdate = LocalDate.parse(input, formatter).atStartOfDay();
                    break;
                } catch (Exception e) {
                    System.out.println("Invalid format.");
                }
            }

            int age = Period.between(birthdate.toLocalDate(), LocalDate.now()).getYears();

            Gender gender;

            while (true) {
                System.out.print("gender (F/M/N): ");
                String input = scanner.nextLine();
                if (input.equals("F") || input.equals("M") || input.equals("N")) {
                    gender = Gender.valueOf(input);
                    break;
                } else {
                    System.out.println("Invalid input.");
                }
            }

            AccountType accountType;

            while (true) {
                System.out.print("account type (regular/contributor/admin): ");
                String input = scanner.nextLine();
                if (input.equalsIgnoreCase("REGULAR") || input.equalsIgnoreCase("CONTRIBUTOR")
                        || input.equalsIgnoreCase("ADMIN")) {
                    accountType = AccountType.valueOf(input.toUpperCase());
                    break;
                } else {
                    System.out.println("Invalid input.");
                }
            }

            String password = createPassword(name, birthdate);

            User<Comparable<Object>> user = UserFactory.factory(email, password, name, country, age, gender, birthdate,
                    accountType, username);
            this.accountList.add(user);
        } else if (choice.equals("delete")) {
            printNonAdminUsernames();
            User<Comparable<Object>> userToDelete = null;

            while (userToDelete == null) {
                System.out.print("Choose user to delete: ");
                choice = scanner.nextLine();

                userToDelete = findUserAfterUsername(choice);
                if (userToDelete == null)
                    System.out.println("User not found.");
            }

            if (userToDelete instanceof Regular) {
                ((Regular<Comparable<Object>>) userToDelete).deleteAllRatings();
            } else if (userToDelete instanceof Contributor) {
                ((Contributor<Comparable<Object>>) userToDelete).refactorRequestsToAdmins();
                ((Contributor<Comparable<Object>>) userToDelete).refactorContributionsToAdmins();
            }

            userToDelete.deleteRequests();

            this.accountList.remove(userToDelete);
        } else {
            printNonAdminUsernames();
            User<Comparable<Object>> userToModify = null;

            while (userToModify == null) {
                System.out.print("Choose user to modify: ");
                choice = scanner.nextLine();

                userToModify = findUserAfterUsername(choice);
                if (userToModify == null)
                    System.out.println("User not found.");
            }

            userToModify.printUserInfo();

            System.out.println("What to modify?");
            choice = scanner.nextLine().toLowerCase();

            while (!choice.equals("username") && !choice.equals("name") && !choice.equals("country")
                    && !choice.equals("birthdate") && !choice.equals("gender")) {

                if (choice.equals("age")) {
                    System.out.println("Age is changed automatically when birthdate is changed");
                } else {
                    System.out.println("Invalid choice");
                }
                choice = scanner.nextLine();
            }

            if (choice.equals("username")) {
                while (true) {
                    System.out.print("New username: ");
                    String username = scanner.nextLine();
                    if (!username.isEmpty()) {
                        userToModify.setUsername(username);
                        break;
                    } else {
                        System.out.println("Please enter a new username");
                    }
                }
            }

            if (choice.equals("name")) {
                while (true) {
                    System.out.print("New name: ");
                    String name = scanner.nextLine();
                    if (!name.isEmpty()) {
                        userToModify.information.setName(name);
                        break;
                    } else {
                        System.out.println("Please enter a new name");
                    }
                }
            }

            if (choice.equals("country")) {
                while (true) {
                    System.out.print("New country: ");
                    String country = scanner.nextLine();
                    if (!country.isEmpty()) {
                        userToModify.information.setCountry(country);
                        break;
                    } else {
                        System.out.println("Please enter a new country");
                    }
                }
            }

            if (choice.equals("birthdate")) {
                LocalDateTime birthdate;
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

                while (true) {
                    try {
                        System.out.print("New birthdate (Format: yyyy-MM-dd): ");
                        String input = scanner.nextLine();

                        birthdate = LocalDate.parse(input, formatter).atStartOfDay();
                        break;
                    } catch (Exception e) {
                        System.out.println("Invalid format.");
                    }
                }

                userToModify.information.setBirthDate(birthdate);
            }

            if (choice.equals("gender")) {
                Gender gender;

                while (true) {
                    System.out.print("New gender (F/M/N): ");
                    String input = scanner.nextLine();
                    if (input.equals("F") || input.equals("M") || input.equals("N")) {
                        gender = Gender.valueOf(input);
                        break;
                    } else {
                        System.out.println("Invalid input.");
                    }
                }

                userToModify.information.setGender(gender);
            }

        }

    }

    private void showUserInfo(User<Comparable<Object>> user) {
        user.printUserInfo();
        waitForEnter();
    }

    private void showUserPreferences(User<Comparable<Object>> user) {
        if(user.preferences.isEmpty()) {
            System.out.println("You have no preference saved.");
            waitForEnter();
            return;
        }

        for(Comparable<Object> object : user.preferences) {
            if(object instanceof Production)
                ((Production) object).displayInfo();
            else if (object instanceof Actor)
                System.out.println(object);
            System.out.println();
        }

        waitForEnter();
    }

    private boolean logout() {
        System.out.println("Type 'change account' to go back to login screen or 'exit' to exit the application");
        Scanner scanner = new Scanner(System.in);

        while (true) {
            String input = scanner.nextLine();

            if (input.equals("change account"))
                return false;
            else if (input.equals("exit"))
                return true;
            System.out.println("Invalid input!");
        }
    }

    private boolean displayAdminChoices(Admin<Comparable<Object>> admin) {
        while (true) {
            System.out.println("""
                    User experience: -
                    Choose action:
                       1) View productions details
                       2) View actors details
                       3) View notifications
                       4) Show favorites
                       5) Search for actor/movie/series
                       6) Add/Delete actor/movie/series to/from favorites
                       7) Add/Delete actor/movie/series from system
                       8) Update actor/production details
                       9) View requests
                       10) Solve a request
                       11) Manage user
                       12) Show account information
                       13) Logout""");
            System.out.print("Here: ");

            Scanner scanner = new Scanner(System.in);
            String action = scanner.nextLine();
            switch (action) {
                case "1" -> viewProductionsDetails();
                case "2" -> viewActorsDetails();
                case "3" -> viewNotifications(admin);
                case "4" -> showUserPreferences(admin);
                case "5" -> search();
                case "6" -> manageFavourites(admin);
                case "7" -> manageSystem(admin);
                case "8" -> updateInfo(admin);
                case "9" -> viewPersonalRequests(admin);
                case "10" -> solveRequests(admin);
                case "11" -> manageUsers();
                case "12" -> showUserInfo(admin);
                case "13" -> {
                    return logout();
                }
                default -> {
                    System.out.println("Invalid input.");
                    waitForEnter();
                }
            }
        }
    }

    private boolean displayContributorChoices(Contributor<Comparable<Object>> contributor) {
        while (true) {
            System.out.println("User experience: " + contributor.experience +
                    """
                            \nChoose action:
                            1) View productions details
                            2) View actors details
                            3) View notifications
                            4) Show favorites
                            5) Search for actor/movie/series
                            6) Add/Delete actor/movie/series to/from favorites
                            7) Add/Delete request
                            8) Add/Delete actor/movie/series from system
                            9) Update actor/production details
                            10) View requests
                            11) Solve a request
                            12) Show account information
                            13) Logout""");
            System.out.print("Here: ");

            Scanner scanner = new Scanner(System.in);
            String action = scanner.nextLine();
            switch (action) {
                case "1" -> viewProductionsDetails();
                case "2" -> viewActorsDetails();
                case "3" -> viewNotifications(contributor);
                case "4" -> showUserPreferences(contributor);
                case "5" -> search();
                case "6" -> manageFavourites(contributor);
                case "7" -> managePersonalRequests(contributor);
                case "8" -> manageSystem(contributor);
                case "9" -> updateInfo(contributor);
                case "10" -> viewPersonalRequests(contributor);
                case "11" -> solveRequests(contributor);
                case "12" -> showUserInfo(contributor);
                case "13" -> {
                    return logout();
                }
                default -> {
                    System.out.println("Invalid input.");
                    waitForEnter();
                }
            }
        }
    }

    private boolean displayRegularChoices(Regular<Comparable<Object>> regular) {
        while (true) {
            System.out.println("User experience: " + regular.experience +
                    """
                            \nChoose action:
                               1) View productions details
                               2) View actors details
                               3) View notifications
                               4) Show favorites
                               5) Search for actor/movie/series
                               6) Add/Delete actor/movie/series to/from favorites
                               7) Add/Delete request
                               8) Add/Delete review
                               9) Show account information
                               10) Logout""");
            System.out.print("Here: ");

            Scanner scanner = new Scanner(System.in);
            String action = scanner.nextLine();
            switch (action) {
                case "1" -> viewProductionsDetails();
                case "2" -> viewActorsDetails();
                case "3" -> viewNotifications(regular);
                case "4" -> showUserPreferences(regular);
                case "5" -> search();
                case "6" -> manageFavourites(regular);
                case "7" -> managePersonalRequests(regular);
                case "8" -> manageReviews(regular);
                case "9" -> showUserInfo(regular);
                case "10" -> {
                    return logout();
                }
                default -> {
                    System.out.println("Invalid input.");
                    waitForEnter();
                }
            }
        }
    }

    public void run() {
        parseProductions();
        parseActors();
        parseUsers();
        parseRequests();
        addProductionActors();
        sortObjectsRatings();

        String interfaceChosen = chooseInterface();

        if (interfaceChosen.equals("CLI")) {
            boolean exit = false;
            while (!exit) {
                User<Comparable<Object>> user = login();
                System.out.println("Welcome back user" + user.username
                        + "!\nUsername: " + user.username);

                switch (user.accountType) {
                    case ADMIN -> exit = displayAdminChoices((Admin<Comparable<Object>>) user);
                    case CONTRIBUTOR -> exit = displayContributorChoices((Contributor<Comparable<Object>>) user);
                    case REGULAR -> exit = displayRegularChoices((Regular<Comparable<Object>>) user);
                }
            }
        } else {
            System.out.println("Not done yet!");
        }
    }
}