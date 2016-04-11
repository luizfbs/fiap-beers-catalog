package br.com.fiap.beerscatalog.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import java.util.List;

@Table(name = "Beers")
public class Beer extends Model {
    public static final int DEFAULT_PAGE_SIZE = 5;

    @Column(name = "Name")
    public String name;

    @Column(name = "Brewery")
    public String brewery;

    @Column(name = "Style")
    public String style;

    @Column(name = "ABV")
    public Double abv;

    @Column(name = "ImageURL")
    public String imageURL;

    @Column(name = "Date")
    public long date;

    @Column(name = "User")
    public User user;

    public Beer() {
        super();
    }

    public Beer(String name, String brewery, String style, Double abv, String imageURL, long date, User user) {
        super();
        this.name = name;
        this.brewery = brewery;
        this.style = style;
        this.abv = abv;
        this.imageURL = imageURL;
        this.date = date;
        this.user = user;
    }

    public static List<Beer> retrieve(){
        return retrieve(0);
    }

    public static List<Beer> retrieve(int page) {
        return new Select()
            .from(Beer.class)
                .offset(DEFAULT_PAGE_SIZE * page)
                .limit(DEFAULT_PAGE_SIZE)
                .orderBy("Date DESC")
                .execute();
    }

    public static List<Beer> find(String keyword) {
        return new Select()
                .from(Beer.class)
                .where("Name LIKE ? ", new String[]{'%' + keyword + '%'})
                .or("Brewery LIKE ? ", new String[]{'%' + keyword + '%'})
                .or("Style LIKE ? ", new String[]{'%' + keyword + '%'})
                .orderBy("Date DESC")
                .execute();
    }

    public static Beer get(long id) {
        return new Select()
                .from(Beer.class)
                .where("Id = ? ", id)
                .executeSingle();
    }

    public static int count(){
        return new Select()
                .from(Beer.class)
                .count();
    }
}