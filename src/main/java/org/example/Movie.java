package org.example;

import java.util.List;

public class Movie extends Production {
    String length;
    long releaseYear;

    public void setReleaseYear(int releaseYear) {
        this.releaseYear = releaseYear;
    }

    public void setLength(int length) {
        this.length = length + " minutes";
    }

    public Movie(String title, List<String> directorList, List<String> actorList, List<Genre> genreList,
                 List<Rating> ratingList, String description, double avgRating, String length, long releaseYear) {
        super(title, directorList, actorList, genreList, ratingList, description, avgRating);

        this.length = length;
        this.releaseYear = releaseYear;
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
        if(length != null)
                info.append("Duration: ").append(length).append("\n");
        if(releaseYear != 0)
                info.append("Released in ").append(releaseYear);
        System.out.println(info + "\n");
    }
}
