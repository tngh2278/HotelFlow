package cse.oop2.hotelflow.Common.model;

public class User {
    private String id;
    private String name;
    private String password;
    private UserRole role;


    public User(String id, String name, String password, UserRole role){
        this.id = id;
        this.name = name;
        this.password = password;
        this.role = role;

    }
    

    // getter
    public String getId() {return id;}
    public String getName() {return name;}
    public String getPassword() {return password;}
    public UserRole getRole() {return role;}
    
    @Override
    public String toString(){
        return id + "," + name + "," + role;
    }


}
