
import dal.AdminsDAO;
import dal.BookingsDAO;
import java.util.ArrayList;
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
        Bookings data2 = new Bookings("15", "Hi")
        List<Bookings> bookingsList = bookingsDAO.getAllBookings();
        for (Bookings a: bookingsList){
            System.out.println(a);
        }
        
        String searchAccount = "Hai";
        Admins adminByAccount = adminsDAO.getAdminByAccount(searchAccount);
        if (adminByAccount != null){
            System.out.println(adminByAccount);
        }
        
        int searchId = 1;
        Admins adminById = adminsDAO.getAdminById(searchId);
        if (adminById != null){
            System.out.println(adminById);
        }
        
        Admins adminToUpdate = new Admins("Haha", "Ho", "1111", "0000", "lulu");
        if (adminToUpdate != null){
            adminToUpdate.setAdminID(1);
            System.out.println(adminsDAO.updateAdmin(adminToUpdate));
            System.out.println(adminsDAO.getAdminById(1));
        }
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }
    }
    
    
}
