package de.malte.moviesapp.entity;


import de.malte.moviesapp.data.MovieContract;

/**
 * Movie Object
 */
public class Movie {

    // used for cursor
    public static final String COLUMN_NAME_MOVIE_ID = MovieContract.MovieEntry.COLUMN_MOVIE_ID;
    public static final String COLUMN_NAME_TITLE = MovieContract.MovieEntry.COLUMN_TITLE;
    public static final String COLUMN_NAME_RELEASE_DATE = MovieContract.MovieEntry.COLUMN_RELEASE_DATE;
    public static final String COLUMN_NAME_POSTER_PATH = MovieContract.MovieEntry.COLUMN_POSTER_PATH;
    public static final String COLUMN_NAME_VOTE_AVERAGE = MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE;
    public static final String COLUMN_NAME_OVERVIEW = MovieContract.MovieEntry.COLUMN_OVERVIEW;

    public static final int COLUMN_MOVIE_ID = MovieContract.MovieEntry.COL_MOVIE_ID;
    public static final int COLUMN_TITLE = MovieContract.MovieEntry.COL_TITLE;
    public static final int COLUMN_RELEASE_DATE = MovieContract.MovieEntry.COL_RELEASE_DATE;
    public static final int COLUMN_POSTER_PATH = MovieContract.MovieEntry.COL_POSTER_PATH;
    public static final int COLUMN_VOTE_AVERAGE = MovieContract.MovieEntry.COL_VOTE_AVERAGE;
    public static final int COLUMN_OVERVIEW = MovieContract.MovieEntry.COL_OVERVIEW;


    // content
    private int id;

    private boolean adult;
    private String backdrop_path;
    private String original_language;
    private String original_title;
    private String overview;
    private String release_date;
    private String poster_path;
    private double popularity;
    private String title;
    private boolean video;
    private double vote_average;
    private int vote_count;

    public Movie(int id, boolean adult, String backdrop_path, String original_language,
                 String original_title, String overview, String release_date, String poster_path,
                 double popularity, String title, boolean video, double vote_average,
                 int vote_count) {
        this.id = id;
        this.adult = adult;
        this.backdrop_path = backdrop_path;
        this.original_language = original_language;
        this.original_title = original_title;
        this.overview = overview;
        this.release_date = release_date;
        this.poster_path = poster_path;
        this.popularity = popularity;
        this.title = title;
        this.video = video;
        this.vote_average = vote_average;
        this.vote_count = vote_count;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isAdult() {
        return adult;
    }

    public void setAdult(boolean adult) {
        this.adult = adult;
    }

    public String getBackdrop_path() {
        return backdrop_path;
    }

    public void setBackdrop_path(String backdrop_path) {
        this.backdrop_path = backdrop_path;
    }

    public String getOriginal_language() {
        return original_language;
    }

    public void setOriginal_language(String original_language) {
        this.original_language = original_language;
    }

    public String getOriginal_title() {
        return original_title;
    }

    public void setOriginal_title(String original_title) {
        this.original_title = original_title;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public String getRelease_date() {
        return release_date;
    }

    public void setRelease_date(String release_date) {
        this.release_date = release_date;
    }

    public String getPoster_path() {
        return poster_path;
    }

    public void setPoster_path(String poster_path) {
        this.poster_path = poster_path;
    }

    public double getPopularity() {
        return popularity;
    }

    public void setPopularity(double popularity) {
        this.popularity = popularity;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isVideo() {
        return video;
    }

    public void setVideo(boolean video) {
        this.video = video;
    }

    public double getVote_average() {
        return vote_average;
    }

    public void setVote_average(double vote_average) {
        this.vote_average = vote_average;
    }

    public int getVote_count() {
        return vote_count;
    }

    public void setVote_count(int vote_count) {
        this.vote_count = vote_count;
    }
}
