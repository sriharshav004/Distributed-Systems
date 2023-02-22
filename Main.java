public class Main {
    public static void main(String[] args) {
        try {
            ReadConfig.read();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
