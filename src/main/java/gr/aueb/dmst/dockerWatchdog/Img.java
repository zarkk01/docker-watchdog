package gr.aueb.dmst.dockerWatchdog;

public class Img {

    private final String id;
    private String name;
    private String status;
    private final Long size;

    public Img(String id, String name ,String status , Long size) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.size = size;
    }

    // Getter for id
    public String getId() {
        return id;
    }

    // Getter for name
    public String getName() {
        return name;
    }

    // Getter for status
    public String getStatus() {
        return status;
    }

    public void setStatus(String status){
        this.status = status;
    }

    // Getter for size
    public Long getSize() {
        return size;
    }

    public static int getUsedImages(){
        int c =0;
        for(Img ima : Main.imagesList){
            if(ima.getStatus() == "true"){
                c++;
            }
        }
        return c;
    }

}