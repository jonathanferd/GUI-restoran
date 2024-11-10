import org.sql2o.Sql2o;

public class Employee {
    private static Sql2o sql2o;

    static {
        try {
            sql2o = new Sql2o("jdbc:mysql://localhost:3306/obp_db", "jferd", "qwerty123");
        } catch (Exception e) {
            System.err.println("Error saat koneksi ke database");
            e.printStackTrace();
        }
    }

    public static Sql2o getSql2o() {
        return sql2o;
    }
}
