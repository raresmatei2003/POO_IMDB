package org.example;

public interface ExperienceStrategy {
    int giveRatingExp();
    int giveRequestSolvedExp();
    int giveContributionExp();
}
class RegularExperience implements ExperienceStrategy{

    @Override
    public int giveRatingExp() {
        return 5;
    }

    @Override
    public int giveRequestSolvedExp() {
        return 10;
    }

    @Override
    public int giveContributionExp() {
        return 0;
    }
}

class ContributorExperience implements ExperienceStrategy{

    @Override
    public int giveRatingExp() {
        return 0;
    }

    @Override
    public int giveRequestSolvedExp() {
        return 10;
    }

    @Override
    public int giveContributionExp() {
        return 20;
    }
}