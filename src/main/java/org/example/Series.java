package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Series extends Production {
    long releaseYear, seasonNumber;
    Map<String, List<Episode>> seasons;

    public void setReleaseYear(int releaseYear) {
        this.releaseYear = releaseYear;
    }

    public void addSeason(String season) {
        if (this.seasons.get(season) != null) {
            System.out.println("Production already has this season.");
        } else {
            this.seasons.put(season, addSeasonEpisodes());
        }
    }

    public List<Episode> addSeasonEpisodes() {
        List<Episode> episodeList = new ArrayList<>();
        int episodeNumber = IMDB.getInstance().getIntFromUser("Episodes number: ");

        while(episodeNumber < 0){
            System.out.print("Invalid input.");
            episodeNumber = IMDB.getInstance().getIntFromUser("Episodes number: ");
        }

        for(int i = 0; i < episodeNumber; i++) {
            episodeList.add(getEpisode());
        }

        return episodeList;
    }

    public Episode getEpisode() {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Episode name: ");
        String title = scanner.nextLine();

        System.out.println("Episode length: ");
        String length = scanner.nextLine();

        return new Episode(title, length);
    }

    public boolean deleteSeason(String season) {
        return this.seasons.remove(season, this.seasons.get(season));
    }

    public Series(String title, List<String> directorList, List<String> actorList, List<Genre> genreList,
                  List<Rating> ratingList, String description, double avgRating, long releaseYear,
                  long seasonNumber, Map<String, List<Episode>> seasons) {
        super(title, directorList, actorList, genreList, ratingList, description, avgRating);

        this.releaseYear = releaseYear;
        this.seasonNumber = seasonNumber;
        this.seasons = seasons;
    }

    @Override
    public void displayInfo() {
        StringBuilder info = new StringBuilder();

        info.append("Title: ").append(title)
                .append("\n").append("Directors: ").append(directorList.toString())
                .append("\n").append("Actors: ").append(actorList.toString())
                .append("\n").append("Genres: ").append(genreList.toString())
                .append("\n").append("Reviews: ").append(ratingList.toString())
                .append("\n").append("Description: ").append(description)
                .append("\n").append("Rating: ").append(avgRating)
                .append("\n");

        if(releaseYear != 0)
            info.append("Released in ").append(releaseYear).append("\n");
        if(seasonNumber != 0)
            info.append(seasonNumber).append(" seasons").append("\n");
        if(seasons != null) {
            for (String name : seasons.keySet()) {
                info.append(name).append("\n");
                List<Episode> episodeList = seasons.get(name);

                for (Episode episode : episodeList) {
                    info.append(episode.name).append(": ").append(episode.length).append("\n");
                }
                info.append("\n");
            }
        }

        System.out.println(info);
    }
}
