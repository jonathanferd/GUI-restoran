import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import org.sql2o.Connection;
import org.sql2o.Sql2o;
import org.sql2o.Sql2oException;

public class RestaurantGUI extends JFrame {
    private final JPanel panelUtama;
    private final Sql2o sql2o;

    public RestaurantGUI(Sql2o sql2o) {
        super("Aplikasi Restoran GUI");

        this.sql2o = sql2o;

        panelUtama = buatPanelUtama();
        getContentPane().add(panelUtama);

        // Set properti frame
        setSize(400, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    private double getHargaFromDatabase(String makananTerpilih) {
        String sql = "SELECT harga FROM daftarmenu WHERE menumakanan = :makananTerpilih";

        try (Connection connection = sql2o.open()) {
            return connection.createQuery(sql)
                    .addParameter("makananTerpilih", makananTerpilih)
                    .executeAndFetchFirst(Double.class);
        }
    }

    //buat panel main/utama
    private JPanel buatPanelUtama() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();

        JLabel judulLabel = new JLabel("Menu Utama");
        judulLabel.setFont(new Font("Arial", Font.BOLD, 20));

        JButton tombolBookingMeja = new JButton("Booking Meja");
        JButton tombolPesanMakanan = new JButton("Pesan Makanan");

        // Penanganan peristiwa untuk tombol-tombol
        tombolBookingMeja.addActionListener(e -> tampilkanPanel(buatPanelBookingMeja()));
        tombolPesanMakanan.addActionListener(e -> tampilkanPanel(buatPanelPesanMakanan()));

        // Tambahkan komponen ke panel menggunakan grid bag layout
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weighty = 1.0;
        panel.add(judulLabel, constraints);

        constraints.gridy = 1;
        constraints.weighty = 0.0;
        panel.add(tombolBookingMeja, constraints);

        constraints.gridy = 2;
        panel.add(tombolPesanMakanan, constraints);

        // Set warna latar belakang
        panel.setBackground(Color.lightGray);

        return panel;
    }
    // Fungsi untuk mendapatkan menu items dari database
    private String[] getMenuItemsFromDatabase() {
        String query = "SELECT menumakanan FROM daftarmenu";

        try (Connection connection = sql2o.open()) {
            List<String> menuItems = connection.createQuery(query)
                    .executeAndFetch(String.class);

            return menuItems.toArray(new String[0]);
        } catch (Sql2oException ex) {
            ex.printStackTrace();
            return new String[0]; // Return an empty array in case of an error
        }
    }

    private JPanel buatPanelBookingMeja() {
        JPanel panel = new JPanel(new BorderLayout());

        JButton tombolKembali = new JButton("Menu Utama");
        tombolKembali.addActionListener(e -> tampilkanPanel(buatPanelUtama()));

        JPanel panelAtas = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelAtas.add(tombolKembali);

        // Panel bagian tengah dengan komponen untuk booking meja
        JPanel panelTengah = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(5, 5, 5, 5);

        // Retrieve available tables from the database
        String[] availableTables = getAvailableTablesFromDatabase();
        JComboBox<String> comboBoxMejaNumber = new JComboBox<>(availableTables);

        // Fields for booking
        JLabel labelBookingID = new JLabel("ID Booking:");
        JTextField textFieldBookingID = new JTextField(10);
        JLabel labelCustomerID = new JLabel("ID Customer:");
        JTextField textFieldCustomerID = new JTextField(10);
        JLabel labelCustomerNama = new JLabel("Nama Customer:");
        JTextField textFieldCustomerNama = new JTextField(10);
        JLabel labelBookingDate = new JLabel("Tanggal Booking:");
        JTextField textFieldBookingDate = new JTextField(10);

        // Button to save booking
        JButton tombolSimpanBooking = new JButton("Simpan Booking");
        tombolSimpanBooking.addActionListener(e -> {
            String mejaNumber = (String) comboBoxMejaNumber.getSelectedItem();
            String bookingID = textFieldBookingID.getText();
            String customerID = textFieldCustomerID.getText();
            String customerNama = textFieldCustomerNama.getText();
            String bookingDate = textFieldBookingDate.getText();

            simpanDataBooking(mejaNumber, bookingID, customerID, customerNama, bookingDate);
            updateMejaStatusInDatabase(mejaNumber, "n"); // Updatestatus tabel ke  "n" setelah booking
        });

        // menambah komponen ke panel ini dengan grid layout
        constraints.gridx = 0;
        constraints.gridy = 0;
        panelTengah.add(new JLabel("Nomor Meja:"), constraints);
        constraints.gridx = 1;
        panelTengah.add(comboBoxMejaNumber, constraints);

        constraints.gridx = 0;
        constraints.gridy = 1;
        panelTengah.add(labelBookingID, constraints);
        constraints.gridx = 1;
        panelTengah.add(textFieldBookingID, constraints);

        constraints.gridx = 0;
        constraints.gridy = 2;
        panelTengah.add(labelCustomerID, constraints);
        constraints.gridx = 1;
        panelTengah.add(textFieldCustomerID, constraints);

        constraints.gridx = 0;
        constraints.gridy = 3;
        panelTengah.add(labelCustomerNama, constraints);
        constraints.gridx = 1;
        panelTengah.add(textFieldCustomerNama, constraints);

        constraints.gridx = 0;
        constraints.gridy = 4;
        panelTengah.add(labelBookingDate, constraints);
        constraints.gridx = 1;
        panelTengah.add(textFieldBookingDate, constraints);

        constraints.gridx = 0;
        constraints.gridy = 5;
        constraints.gridwidth = 2;
        panelTengah.add(tombolSimpanBooking, constraints);

        // Set warna latar belakang
        panel.setBackground(Color.lightGray);

        // Tambahkan panel ke panel utama
        panel.add(panelAtas, BorderLayout.NORTH);
        panel.add(panelTengah, BorderLayout.CENTER);

        return panel;
    }

    // Fungsi untuk mendapatkan meja yang tersedia dari database
    private String[] getAvailableTablesFromDatabase() {
        String sql = "SELECT mejaNumber FROM daftarmeja WHERE tableStatus = 'y'";

        try (Connection connection = sql2o.open()) {
            return connection.createQuery(sql)
                    .executeAndFetch(String.class)
                    .toArray(new String[0]);
        }
    }

    private void simpanDataBooking(String mejaNumber, String bookingID, String customerID, String customerNama, String bookingDate) {
        String insertCustomerQuery = "INSERT INTO customer (customerID, nama_customer) VALUES (:customerID, :customerNama)";
        String insertBookingQuery = "INSERT INTO booking (bookingID, customerID, mejaNumber, bookingDate) VALUES (:bookingID, :customerID, :mejaNumber, :bookingDate)";

        try (Connection connection = sql2o.beginTransaction()) {
            connection.createQuery(insertCustomerQuery)
                    .addParameter("customerID", customerID)
                    .addParameter("customerNama", customerNama)
                    .executeUpdate();

            connection.createQuery(insertBookingQuery)
                    .addParameter("bookingID", bookingID)
                    .addParameter("customerID", customerID)
                    .addParameter("mejaNumber", mejaNumber)
                    .addParameter("bookingDate", bookingDate)
                    .executeUpdate();

            updateMejaStatusInDatabase(mejaNumber, "n");

            connection.commit();
            JOptionPane.showMessageDialog(null, "Booking berhasil!");
            tampilkanPanel(buatPanelUtama());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void updateMejaStatusInDatabase(String mejaNumber, String status) {
        String updateMejaQuery = "UPDATE daftarmeja SET tableStatus = :status WHERE mejaNumber = :mejaNumber";

        try (Connection connection = sql2o.beginTransaction()) {
            connection.createQuery(updateMejaQuery)
                    .addParameter("status", status)
                    .addParameter("mejaNumber", mejaNumber)
                    .executeUpdate();

            connection.commit();
        } catch (Sql2oException ex) {
            ex.printStackTrace();
        }
    }

    private JPanel buatPanelPesanMakanan() {
        JPanel panel = new JPanel(new BorderLayout());

        JButton tombolKembali = new JButton("Menu Utama");
        tombolKembali.addActionListener(e -> tampilkanPanel(buatPanelUtama()));

        JPanel panelAtas = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelAtas.add(tombolKembali);

        // Panel bagian tengah dengan komponen untuk pesan makanan
        JPanel panelTengah = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(5, 5, 5, 5);

        JLabel labelMakanan = new JLabel("Pilih makanan:");
        String[] menuItems = getMenuItemsFromDatabase();
        JComboBox<String> comboBoxMakanan = new JComboBox<>(menuItems);

        JLabel labelJumlah = new JLabel("Jumlah:");
        JTextField textFieldJumlah = new JTextField(10);

        JLabel labelCustomerID = new JLabel("ID Customer:");
        JTextField textFieldCustomerID = new JTextField(10);

        JLabel labelCustomerNama = new JLabel("Nama Customer:");
        JTextField textFieldCustomerNama = new JTextField(10);

        JLabel labelIDPesanan = new JLabel("ID Pesanan:");
        JTextField textFieldIDPesanan = new JTextField(10);

        JButton tombolOrder = new JButton("Order");

        tombolOrder.addActionListener(e -> {
            String makananTerpilih = (String) comboBoxMakanan.getSelectedItem();
            String jumlahText = textFieldJumlah.getText();
            String customerID = textFieldCustomerID.getText();
            String customerNama = textFieldCustomerNama.getText();
            String idPesanan = textFieldIDPesanan.getText();

            if (!jumlahText.isEmpty()) {
                int jumlah = Integer.parseInt(jumlahText);
                JOptionPane.showMessageDialog(null, "Order: " + jumlah + " " + makananTerpilih);
                simpanDataPesanMakanan(makananTerpilih, jumlah, customerID, customerNama, idPesanan);
                tampilkanPanel(buatPanelPembayaran(makananTerpilih, jumlah));
            } else {
                JOptionPane.showMessageDialog(null, "Masukkan order yang valid.");
            }
        });

        // nambahkan komponen ke panel utama
        constraints.gridx = 0;
        constraints.gridy = 0;
        panelTengah.add(labelMakanan, constraints);
        constraints.gridx = 1;
        panelTengah.add(comboBoxMakanan, constraints);

        constraints.gridx = 0;
        constraints.gridy = 1;
        panelTengah.add(labelJumlah, constraints);
        constraints.gridx = 1;
        panelTengah.add(textFieldJumlah, constraints);

        constraints.gridx = 0;
        constraints.gridy = 2;
        panelTengah.add(labelCustomerID, constraints);
        constraints.gridx = 1;
        panelTengah.add(textFieldCustomerID, constraints);

        constraints.gridx = 0;
        constraints.gridy = 3;
        panelTengah.add(labelCustomerNama, constraints);
        constraints.gridx = 1;
        panelTengah.add(textFieldCustomerNama, constraints);

        constraints.gridx = 0;
        constraints.gridy = 4;
        panelTengah.add(labelIDPesanan, constraints);
        constraints.gridx = 1;
        panelTengah.add(textFieldIDPesanan, constraints);

        constraints.gridx = 0;
        constraints.gridy = 5;
        constraints.gridwidth = 2;
        panelTengah.add(tombolOrder, constraints);

        // Set warna latar belakang
        panel.setBackground(Color.lightGray);

        // Tambahkan panel ke panel utama
        panel.add(panelAtas, BorderLayout.NORTH);
        panel.add(panelTengah, BorderLayout.CENTER);

        return panel;
    }

    private void simpanDataPesanMakanan(String makananTerpilih, int jumlah, String customerID, String customerNama, String idPesanan) {
        String menuID = getMenuIDFromDatabase(makananTerpilih);

        String insertCustomerQuery = "INSERT INTO customer (customerID, nama_customer) VALUES (:customerID, :customerNama)";
        String insertPesananQuery = "INSERT INTO pesanan (IDPesanan, MenuID, CustomerID, Jumlah) VALUES (:idPesanan, :menuID, :customerID, :jumlah)";

        try (Connection connection = sql2o.beginTransaction()) {
            connection.createQuery(insertCustomerQuery)
                    .addParameter("customerID", customerID)
                    .addParameter("customerNama", customerNama)
                    .executeUpdate();

            connection.createQuery(insertPesananQuery)
                    .addParameter("idPesanan", idPesanan)
                    .addParameter("menuID", menuID)
                    .addParameter("customerID", customerID)
                    .addParameter("jumlah", jumlah)
                    .executeUpdate();

            connection.commit();
            JOptionPane.showMessageDialog(null, "Pesanan makanan berhasil!");
            tampilkanPanel(buatPanelPembayaran(makananTerpilih, jumlah));
        } catch (Sql2oException ex) {
            ex.printStackTrace();
        }
    }

    // Fungsi untuk mendapatkan MenuID dari database berdasarkan makananTerpilih
    private String getMenuIDFromDatabase(String makananTerpilih) {
        String query = "SELECT MenuID FROM daftarmenu WHERE menumakanan = :makananTerpilih";

        try (Connection connection = sql2o.open()) {
            return connection.createQuery(query)
                    .addParameter("makananTerpilih", makananTerpilih)
                    .executeAndFetchFirst(String.class);
        } catch (Sql2oException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    // Fungsi untuk membuat panel pembayaran
    private JPanel buatPanelPembayaran(String makananTerpilih, int jumlah) {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel panelAtas = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton tombolKembali = new JButton("Kembali ke Pesan Makanan");
        tombolKembali.addActionListener(e -> tampilkanPanel(buatPanelPesanMakanan()));
        panelAtas.add(tombolKembali);

        JPanel panelTengah = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));


        JLabel labelTotal = new JLabel("Total Harga order : Rp" + hitungTotalHarga(makananTerpilih, jumlah));
        JLabel labelBayar = new JLabel("(cash only) bayar di register ");

        // Tambahkan komponen label  ke panel
        panelTengah.add(labelTotal);
        panelTengah.add(labelBayar);

        panel.setBackground(Color.lightGray);

        panel.add(panelAtas, BorderLayout.NORTH);
        panel.add(panelTengah, BorderLayout.CENTER);

        return panel;
    }

    // Fungsi untuk menghitung total harga
    private double hitungTotalHarga(String makananTerpilih, int jumlah) {
        // Logika untuk menghitung total harga berdasarkan jumlah dan jenis makanan
        double hargaPerItem = getHargaFromDatabase(makananTerpilih);
        return jumlah * hargaPerItem;
    }

    // Fungsi untuk menampilkan panel baru pada frame
    private void tampilkanPanel(JPanel panelBaru) {
        getContentPane().removeAll();
        getContentPane().add(panelBaru);
        revalidate();
    }

    // Metode utama untuk menjalankan aplikasi
    public static void main(String[] args) {
        // Adjust your database URL, username, and password
        String url = "jdbc:mysql://localhost:3306/obp_db";
        String user = "jferd";
        String password = "qwerty123";

        Sql2o sql2o = new Sql2o(url, user, password);

        SwingUtilities.invokeLater(() -> new RestaurantGUI(sql2o).setVisible(true));
    }
}