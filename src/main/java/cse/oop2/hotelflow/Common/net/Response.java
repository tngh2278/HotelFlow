package cse.oop2.hotelflow.Common.net;

import java.util.Map;


public class Response {
    private boolean success;
    private String message;
    private Map<String,String> data;

    public Response(boolean success, String message, Map<String, String> data){
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public boolean isSuccess() {return success;}
    public String getMessage() {return message;}
    public Map<String, String> getData() { return data;}

}
