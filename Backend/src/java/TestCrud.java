import dal.AdminsDAO;
import dal.BookingsDAO;
import dal.UsersDAO;
import enums.UserGender;
import java.time.LocalDateTime;
import java.util.List;
import models.Admins;
import models.Bookings;
import models.Users;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author NGUYEN HOANG HIEU
 */
public class TestCrud {
    public static void main(String[] args) {
        try {
            UsersDAO usersDAO = new UsersDAO();
            BookingsDAO bookingsDAO = new BookingsDAO();
            AdminsDAO adminsDAO = new AdminsDAO();

            Users data2 = new Users(
                    "Hhieu",
                    "Ew",
                    "1122",
                    "0909",
                    "ngua@example.com",
                    "",
                    UserGender.MALE,
                    LocalDateTime.of(2000, 1, 1, 0, 0)
            );
            System.out.println("Sample user: " + data2);

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
