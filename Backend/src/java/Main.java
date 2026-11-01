import dal.AdminsDAO;
import dal.BookingsDAO;
import enums.BookingStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import models.Admins;
import models.Bookings;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author NGUYEN HOANG HIEU
 */
public class Main {
    public static void main(String[] args) {
        try {
            BookingsDAO bookingsDAO = new BookingsDAO();
            AdminsDAO adminsDAO = new AdminsDAO();

            Bookings data2 = new Bookings(LocalDateTime.now(), new BigDecimal("150000"), BookingStatus.PENDING, 1, 1);
            System.out.println("Sample booking: " + data2);

            List<Bookings> bookingsList = bookingsDAO.getAllBookings();
            for (Bookings a : bookingsList) {
                System.out.println(a);
            }

            String searchAccount = "Hai";
            Admins adminByAccount = adminsDAO.getAdminByAccount(searchAccount);
            if (adminByAccount != null) {
                System.out.println(adminByAccount);
            }

            int searchId = 1;
            Admins adminById = adminsDAO.getAdminById(searchId);
            if (adminById != null) {
                System.out.println(adminById);
            }

            Admins adminToUpdate = new Admins("Haha", "Ho", "1111", "0000", "lulu");
            adminToUpdate.setAdminID(1);
            System.out.println(adminsDAO.updateAdmin(adminToUpdate));
            System.out.println(adminsDAO.getAdminById(1));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
