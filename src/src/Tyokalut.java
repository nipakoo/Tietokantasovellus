import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.*;

//Luokka tarjoaa yleishyödyllisiä metodeja joita useampi muu luokka käyttää.
public class Tyokalut extends HttpServlet {
    final static String ajuri = "org.postgresql.Driver";
    final static String serveri = "jdbc:postgresql:niko";
    final static String tunnus = "niko";
    final static String salasana = "a46f5f4142aaf274";
    
    //Yhdistetään tietokantaan
    public static Connection yhdista() {
        try {
            Class.forName(ajuri);
        } catch (ClassNotFoundException e) {
            System.out.println("Ajurin lataus epäonnistui!\n" + e.getMessage());
            return null;
        }
        
        Connection yhteys = null;
        
        try {
            yhteys = DriverManager.getConnection(serveri, tunnus, salasana);
        } catch (SQLException e) {
            System.out.println("Yhteyden muodostus epäonnistui!\n" + e.getMessage());
        }
        
        return yhteys;
    }
    
    //Tarkistetaan ovatko tunnukset oikein ja välitetään ne eteenpäin.
    public static boolean tarkistaJaValitaTunnukset(HttpServletRequest req, ServletOutputStream out, Connection yhteys) 
       throws ServletException, IOException {
        String account = req.getParameter("account");
        String password = req.getParameter("password");
        
        if (account == null || password == null) {
            return false;
        }
        
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            String sql = "SELECT salasana FROM kayttaja WHERE nimi = ?";
            stmt = yhteys.prepareStatement(sql);
            stmt.setString(1, account);
            
            rs = stmt.executeQuery();
            rs.next();
            
            if (!rs.getString("salasana").equals(password)) {
                return false;
            }
        } catch (SQLException ee) {
            out.println("Tietokantavirhe "+ee.getMessage());
        }
        
        out.println("<input type='hidden' name='account' value='" + account + "'>");
        out.println("<input type='hidden' name='password' value='" + password + "'>");
        return true;
    }
}
