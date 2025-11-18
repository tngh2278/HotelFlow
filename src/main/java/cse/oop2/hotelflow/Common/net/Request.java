package cse.oop2.hotelflow.Common.net;

import java.util.Map;

public class Request {
    private String type; // "Login" 등
    private Map<String, String > data;

    public Request(String type, Map<String, String>data){
        this.type = type;
        this.data = data;
    }

    public String getType() {return type;}
    public Map<String,String> getData() { return data;}


}
