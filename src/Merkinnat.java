import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.*;
import java.util.Calendar;

public class Merkinnat extends HttpServlet {
    final String ajuri = "org.postgresql.Driver";
    final String serveri = "jdbc:postgresql:niko";
    final String tunnus = "niko";
    final String salasana = "a46f5f4142aaf274";
    
    public void doGet(HttpServletRequest req, HttpServletResponse res) 
       throws ServletException, IOException {
        
       ServletOutputStream out;  
       res.setContentType("text/html");
       out = res.getOutputStream();
       
       out.println("<html><head><title>Merkinnat</title></head>");
       
       Connection yhteys = null;
       yhteys = yhdista(ajuri, serveri, tunnus, salasana);
       
       if (yhteys==null) {
          out.println("<body bgcolor=white><h1>Tietokantayhtteyden muodostus epäonnistui</h1>");
	  out.println("<a href='/nettilaihdutus/Etusivu.html'>Takaisin etusivulle</a>");
	  out.println("</body></html>");
          return;
       }

       PreparedStatement stmt = null;
       ResultSet rs = null;
       
       out.println("<body bgcolor=white>");
       
       out.println("<p>Päivämäärä</p>");
       out.println("<form action='Merkinnat' method='get'>");
       
       out.println("<select name='pvm'>");
       out.println("<option value=''></option>");
       for (int i = 1; i < 32; i++) {
           out.println("<option value='" + i + "'>"+ i + "</option>");
       }
       out.println("</select>");
       out.println("<select name='kk'>");
       out.println("<option value=''></option>");
       for (int i = 1; i < 13; i++) {
           out.println("<option value='" + i + "'>"+ i + "</option>");
       }
       out.println("</select>");
       
       out.println("<select name='vuosi'>");
       out.println("<option value=''></option>");
       out.println("<option value='2013'>2013</option>");
       out.println("</select>");
       
       out.println("<input type='submit' name='pvmHaku' value='Hae päivän merkinnät'>");
       out.println("<input type='submit' name='kkHaku' value='Hae kuukausiraportti'");
       out.println("<input type='submit' name='vuosiHaku' value='Hae vuosiraportti'>");
       out.println("</form>");

            if (req.getParameter("pvmHaku") != null) {
                int pvm;
                int kk;
                int vuosi;
                
                try {
                    pvm = Integer.parseInt(req.getParameter("pvm"));
                    kk = Integer.parseInt(req.getParameter("kk"));
                    vuosi = Integer.parseInt(req.getParameter("vuosi"));
                } catch (NumberFormatException e) {
                    out.println("Valitse haluttu päivämäärä!");
                    return;
                }
                Calendar alku = Calendar.getInstance();
                alku.set(Calendar.DAY_OF_MONTH, pvm);
                alku.set(Calendar.MONTH, kk);
                alku.set(Calendar.YEAR, vuosi);
                alku.set(Calendar.HOUR_OF_DAY, 0);
                alku.set(Calendar.MINUTE, 0);
                alku.set(Calendar.SECOND, 0);
                
                Calendar loppu = Calendar.getInstance();
                loppu.set(Calendar.DAY_OF_MONTH, pvm);
                loppu.set(Calendar.MONTH, kk);
                loppu.set(Calendar.YEAR, vuosi);
                loppu.set(Calendar.HOUR_OF_DAY, 23);
                loppu.set(Calendar.MINUTE, 59);
                loppu.set(Calendar.SECOND, 59);
                
                haeMerkinnat(req, yhteys, out, alku, loppu);
            } else if (req.getParameter("kkHaku") != null) {
                int kk;
                int vuosi;
                try {
                    kk = Integer.parseInt(req.getParameter("kk"));
                    vuosi = Integer.parseInt(req.getParameter("vuosi"));
                } catch (NumberFormatException e) {
                    out.println("Valitse haluttu kuukausi ja vuosi!");
                    return;
                }

                Calendar alku = Calendar.getInstance();
                alku.set(Calendar.DAY_OF_MONTH, 1);
                alku.set(Calendar.MONTH, kk);
                alku.set(Calendar.YEAR, vuosi);
                alku.set(Calendar.HOUR_OF_DAY, 0);
                alku.set(Calendar.MINUTE, 0);
                alku.set(Calendar.SECOND, 0);
                
                Calendar loppu = Calendar.getInstance();
                loppu.set(Calendar.DAY_OF_MONTH, 31);
                loppu.set(Calendar.MONTH, kk);
                loppu.set(Calendar.YEAR, vuosi);
                loppu.set(Calendar.HOUR_OF_DAY, 23);
                loppu.set(Calendar.MINUTE, 59);
                loppu.set(Calendar.SECOND, 59);

                haeMerkinnat(req, yhteys, out, alku, loppu);
            }
       
       out.println("</body></html>");
    }
    
    public void doPost(HttpServletRequest req, HttpServletResponse res) 
        throws ServletException, IOException {
	
        doGet(req, res);
    }
    
    private Connection yhdista(String ajuri, String serveri, String tunnus, String salasana) {
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
    
    private void suljeYhteys(Connection yhteys) {
        try {
            yhteys.close();
        } catch (SQLException e) {
            System.out.println("Virhe suljettessa yhteyttä" + e.getMessage());
        }
    }
    
    private void haeMerkinnat(HttpServletRequest req, Connection yhteys, ServletOutputStream out,
            Calendar alku, Calendar loppu) throws ServletException, IOException {
        
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            String sql = "SELECT aktiviteettiNimi, kulutus * lukumaara, kommentti FROM aktiviteetti, "
                            + "kuuluuMerkintaan, merkinta, kayttaja WHERE nimi = ? and "
                            + "kayttaja.kayttajaID = merkinta.kayttajaID and "
                            + "pvm >= ? and pvm <= ? and merkinta.merkintaID = kuuluuMerkintaan.merkintaID and "
                            + "kuuluuMerkintaan.aktiviteettiID = aktiviteetti.aktiviteettiID";
            stmt = yhteys.prepareStatement(sql);
            stmt.setString(1, req.getParameter("account"));
            stmt.setDate(2, new Date(alku.getTime().getTime()));
            stmt.setDate(3, new Date(loppu.getTime().getTime()));
            rs = stmt.executeQuery();

            out.println("<h1>Kulutus</h1>");
            out.println("<table border='1'><tr><th>Aktiviteetti</th><th>Kuluttanut</th><th>Kommentti</th></tr>");
            
            while (rs.next()) {
                out.println("<tr><td>" + rs.getString(1) + "</td><td>" + rs.getInt(2) +
                    " kcal</td><td>" + rs.getString(3) + "</td></tr>");
            }
            out.println("</table>");

            sql = "SELECT aineNimi, kalorit * kuuluuMerkintaan.lukumaara, hiilarit * kuuluuMerkintaan.lukumaara, "
                            + "proteiini * kuuluuMerkintaan.lukumaara, rasva * kuuluuMerkintaan.lukumaara, kommentti FROM raakaAine, kuuluuMerkintaan, "
                            + "merkinta, kayttaja WHERE nimi = ? and kayttaja.kayttajaID = merkinta.kayttajaID and "
                            + "pvm >= ? and pvm <= ? and merkinta.merkintaID = kuuluuMerkintaan.merkintaID and "
                            + "kuuluuMerkintaan.aineID = raakaAine.aineID";
            stmt = yhteys.prepareStatement(sql);
            stmt.setString(1, req.getParameter("account"));
            stmt.setDate(2, new Date(alku.getTime().getTime()));
            stmt.setDate(3, new Date(loppu.getTime().getTime()));
            rs = stmt.executeQuery();

            out.println("<h1>Ravinto</h1>");
            out.println("<table border='1'><tr><th>Ruoka</th><th>Kaloreita</th><th>Hiilihydraatteja</th><th>"
                            + "Proteiinia</th><th>Rasvaa</th><th>Kommentti</th></tr>");
            
            while (rs.next()) {
                out.println("<tr><td>" + rs.getString(1) + "</td><td>" + rs.getInt(2)  + "</td>"
                                + "<td>" + rs.getInt(3) + "</td><td>" + rs.getInt(4) +
                                "</td><td>" + rs.getInt(5) + "</td><td>" + rs.getString(6) + "</td></tr>");
            }

            sql = "SELECT lajiNimi, sum(kalorit * ainesOsa.lukumaara * ainesOsa.paino * ainesOsa.tilavuus) * kuuluuMerkintaan.lukumaara, "
                            + "sum(hiilarit * ainesOsa.lukumaara * ainesOsa.paino * ainesOsa.tilavuus) * kuuluuMerkintaan.lukumaara, "
                            + "sum(proteiini * ainesOsa.lukumaara * ainesOsa.paino * ainesOsa.tilavuus) * kuuluuMerkintaan.lukumaara, "
                            + "sum(rasva * ainesOsa.lukumaara * ainesOsa.paino * ainesOsa.tilavuus) * kuuluuMerkintaan.lukumaara, "
                            + "kommentti FROM raakaAine, ainesOsa, ruokalaji, kuuluuMerkintaan, "
                            + "merkinta, kayttaja WHERE raakaAine.aineID = ainesOsa.aineID and "
                            + "ainesOsa.lajiID = ruokalaji.lajiID and "
                            + "ruokalaji.lajiID = kuuluuMerkintaan.lajiID and "
                            + "kuuluuMerkintaan.merkintaID = merkinta.merkintaID and "
                            + "pvm >= ? and pvm <= ? and merkinta.kayttajaID = kayttaja.kayttajaID and "
                            + "nimi = ? "
                            + "GROUP BY ruokalaji.lajiNimi, kuuluuMerkintaan.lukumaara, kuuluuMerkintaan.kommentti";

            stmt = yhteys.prepareStatement(sql);
            stmt.setDate(1, new Date(alku.getTime().getTime()));
            stmt.setDate(2, new Date(alku.getTime().getTime()));
            stmt.setString(3, req.getParameter("account"));
            rs = stmt.executeQuery();

            while (rs.next()) {
                out.println("<tr><td>" + rs.getString(1) + "</td><td>" + rs.getInt(2) + "</td><td>"
                                + rs.getInt(3)+ "</td><td>" + rs.getInt(4) + "</td><td>"
                                + rs.getInt(5)+ "</td><td>" + rs.getString(6) + "</td></tr>");
            }

            out.println("</table>");
        }  catch (SQLException ee) {
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
    }
}