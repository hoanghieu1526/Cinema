
import dal.UsersDAO;
import java.util.List;
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
        Users data2 = new Users("Hhieu", "Ew", "1122", "0909", "ngua", "C:\\Users\\NGUYEN HOANG HIEU\\Documents\\Cinema\\Backend\\src\\java\\images\\default_image\\haiz.png", "")
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
        }
    }    
}
