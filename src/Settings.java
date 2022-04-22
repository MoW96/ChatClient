
public class Settings {
    private String username;

    public Settings() {
        preloadUsername();
    }

    private void preloadUsername() {
        ReadXMLFile readXMLFile = new ReadXMLFile();
        username = readXMLFile.getUsername();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

}
