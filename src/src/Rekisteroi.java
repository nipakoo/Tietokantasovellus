import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.*;

//Uuden käyttäjätunnuksen rekisteröimiseen käytetty luokka.
public class Rekisteroi extends HttpServlet {
    
    public void doGet(HttpServletRequest req, HttpServletResponse res) 
       throws ServletException, IOException {
       
       ServletOutputStream out;  
       res.setContentType("text/html");
       out = res.getOutputStream();
       out.println("<html><head>"
               + "<link rel='stylesheet' type='text/css' href='/nettilaihdutus/Tyylit.css'>"
               + "<title>Rekisterointi</title></head>");
       
       Connection yhteys = null;
       yhteys = Tyokalut.yhdista();
       
       if (yhteys==null) {
          out.println("<body bgcolor=white><h1>Tietokantayhtteyden muodostus epäonnistui</h1>");
	  out.println("<a href='/nettilaihdutus/Rekisteroityminen.html'>Takaisin rekisteröintiin</a>");
	  out.println("</body></html>");
          return;
       }

       PreparedStatement stmt = null;
       ResultSet rs = null;
       
       //Tallennetaan tietokantaan uudet käyttäjätunnukset.
       try {
           String nimi = req.getParameter("tunnus");
	   
	   if (nimi.length() > 50) {
		out.println("<body bgcolor=white><h1>Käyttäjätunnus on liian pitkä! Valitse toinen!</h1>");
          	out.println("<a href='/nettilaihdutus/Rekisteroityminen.html'>Takaisin rekisteröintiin</a>");
          	out.println("</body></html>");
          	return;
	   } else if (nimi.isEmpty()) {
               out.println("<body bgcolor=white><h1>Syötä haluamasi käyttäjätunnus!</h1>");
          	out.println("<a href='/nettilaihdutus/Rekisteroityminen.html'>Takaisin rekisteröintiin</a>");
          	out.println("</body></html>");
          	return;
           }

	   String sql = "SELECT kayttajaID FROM kayttaja WHERE nimi = ?";
           stmt = yhteys.prepareStatement(sql);
	   stmt.setString(1, nimi);

           rs = stmt.executeQuery();
           if (rs.next()) {
               out.println("<body bgcolor=white><h1>Tunnus on jo käytössä! Valitse toinen!</h1>");
               out.println("<a href='/nettilaihdutus/Rekisteroityminen.html'>Takaisin rekisteröintiin</a>");
	       out.println("</body></html>");
               return;
           }

           String salasana = req.getParameter("salasana");

	   if (salasana.length() > 30) {
                out.println("<body bgcolor=white><h1>Salasana on liian pitkä! Valitse toinen!</h1>");
                out.println("<a href='/nettilaihdutus/Rekisteroityminen.html'>Takaisin rekisteröintiin</a>");
                out.println("</body></html>");
                return;
           }


	   sql = "SELECT max(kayttajaID) FROM kayttaja";
           stmt = yhteys.prepareStatement(sql);

	   rs = stmt.executeQuery();
	   rs.next();
	   int ID = rs.getInt(1) + 1;

	   sql = "INSERT INTO kayttaja VALUES(?, ?, ?)";
	   stmt = yhteys.prepareStatement(sql);
	   stmt.setInt(1, ID);
	   stmt.setString(2, nimi);
	   stmt.setString(3, salasana);

	   stmt.executeUpdate(); 
       } catch (SQLException ee) {
             out.println("Tietokantavirhe "+ee.getMessage());
       } finally {
             try {
                if (rs!=null) { 
                    rs.close();
                } 
                if (stmt!=null) {
                    stmt.close();
                } 
                yhteys.close();
             } catch(SQLException e) { 
                out.println("An SQL Exception was thrown."); 
             }
       }  
       out.println("<body bgcolor=white><h1>Rekisteröityminen onnistui!</h1>");
       out.println("<a href='/nettilaihdutus/Etusivu.html'>Takaisin etusivulle</a>");
       out.println("</body></html>");

    }

    public void doPost(HttpServletRequest req, HttpServletResponse res) 
        throws ServletException, IOException {
	
        doGet(req, res);
    }
}
