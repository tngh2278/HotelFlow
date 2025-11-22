package cse.oop2.hotelflow.Common.model;

public class User {
    private String id;
    private String name;
    private String password;
    private UserRole role;
    private final String phone;


    public User(String id,
            String name,
            String password,
            UserRole role,
            String phone) {
        this.id = id;
        this.name = name;
        this.password = password;
        this.role = role;
        this.phone = phone;
    }

    public User(String id,
            String name,
            String password,
            UserRole role) {
        this(id, name, password, role, "");
    }

    public User(String id,
            String password,
            UserRole role) {
        this(id, "", password, role, "");
    }
    

    // getter
    public String getId() {return id;}
    public String getName() {return name;}
    public String getPassword() {return password;}
    public UserRole getRole() {return role;}
    public String getPhone() {return phone;}
    
    @Override
    public String toString(){
        return id + "," + name + "," + role;
    }


}
