import org.sql2o.Sql2o;

public class Main {

    public static void main(String[] args) {
        try {
            // Assuming Employee is in the same package, otherwise, import it too
            Employee e = new Employee();

            // Try to get the Sql2o instance, which implies a connection attempt
            Sql2o sql2o = e.getSql2o();

            if (sql2o != null) {
                System.out.println("Connected to the database...");
            } else {
                System.out.println("Failed to connect to the database.");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}