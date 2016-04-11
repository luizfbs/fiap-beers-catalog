package br.com.fiap.beerscatalog.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

@Table(name = "Users")
public class User extends Model {

    @Column(name = "Name")
    public String name;

    @Column(name = "Username")
    public String username;

    @Column(name = "Password")
    public String password;

    @Column(name = "RegistrationDate")
    public long registrationDate;

    public User() {
        super();
    }

    public User(String name, String username, String password,long registrationDate) {
        super();
        this.name = name;
        this.username = username;
        this.password = password;
        this.registrationDate = registrationDate;
    }

}