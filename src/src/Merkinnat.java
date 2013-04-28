import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.*;
import java.util.ArrayList;

//Luokka käyttäjän omien merkintöjen tarkastelemista varten.
public class Merkinnat extends HttpServlet {
    
    private double kulutus;
    private double kalorit;
    private double hiilarit;
    private double proteiini;
    private double rasva;
    
    private int liikuntapaivat;
    private int ruokapaivat;
    
    public void doGet(HttpServletRequest req, HttpServletResponse res) 
       throws ServletException, IOException {
        
       ServletOutputStream out;  
       res.setContentType("text/html");
       out = res.getOutputStream();
       
       out.println("<html><head>"
               + "<link rel='stylesheet' type='text/css' href='/nettilaihdutus/Tyylit.css'>"
               + "<title>Merkinnat</title></head>");
       
       Connection yhteys = null;
       yhteys = Tyokalut.yhdista();
       
       if (yhteys==null) {
          out.println("<body bgcolor=white><h1>Tietokantayhtteyden muodostus epäonnistui</h1>");
	  out.println("<a href='/nettilaihdutus/Etusivu.html'>Takaisin etusivulle</a>");
	  out.println("</body></html>");
          return;
       }

       PreparedStatement stmt = null;
       ResultSet rs = null;
       
       out.println("<body bgcolor=white>");
       
       
       //Tulostetaan käyttäjälle syötemahdollisuuden, joiden avulla hän voi määrittää
       //haettavaksi ajaksi joko tarkan päivämäärän tai kokonaisen kuukauden merkinnät.
       out.println("<p>Päivämäärä</p>");
       out.println("<form action='Merkinnat' method='get'>");
       
       out.println("<select name='pvm'>");
       out.println("<option value=''></option>");
       for (int i = 1; i < 32; i++) {
           out.print("<option value='");
           if (i < 10) {
               out.print("0");
           }
           out.println(i + "'>"+ i + "</option>");
       }
       out.println("</select>");
       out.println("<select name='kk'>");
       out.println("<option value=''></option>");
       for (int i = 1; i < 13; i++) {
           out.print("<option value='");
           if (i < 10) {
               out.print("0");
           }
           out.println(i + "'>"+ i + "</option>");
       }
       out.println("</select>");
       
       out.println("<select name='vuosi'>");
       out.println("<option value=''></option>");
       out.println("<option value='2013'>2013</option>");
       out.println("</select>");
       if (!Tyokalut.tarkistaJaValitaTunnukset(req, out, yhteys)) {
           out.println("<p>Et ole kirjautunut</p>");
           return;
       }
       out.println("<input type='submit' name='pvmHaku' value='Hae päivän merkinnät'>");
       out.println("<input type='submit' name='kkHaku' value='Hae kuukausiraportti'>");
       out.println("</form>");
       
       out.println("<a href='/nettilaihdutus/Etusivu.html'>Takaisin etusivulle</a>");

       //Haetaan tietokannasta käyttäjän haluamat merkinnät ja tulostetaan ne.
            if (req.getParameter("pvmHaku") != null) {
                String paiva;
                String kk;
                String vuosi;
                
                paiva = req.getParameter("pvm");
                kk = req.getParameter("kk");
                vuosi = req.getParameter("vuosi");
                if (paiva.isEmpty() || kk.isEmpty() || vuosi.isEmpty()) {
                    out.println("Valitse haluttu päivämäärä!");
                    return;
                }
                
                String pvm = "";
                pvm += paiva;
                pvm += kk;
                pvm += vuosi;
                
                haeMerkinnat(req, yhteys, out, pvm);
            } else if (req.getParameter("kkHaku") != null) {
                String kk;
                String vuosi;
                kk = req.getParameter("kk");
                vuosi = req.getParameter("vuosi");
                if (kk.isEmpty() || vuosi.isEmpty()) {
                    out.println("Valitse haluttu kuukausi ja vuosi!");
                    return;
                }

                String pvm = "";
                pvm += kk;
                pvm += vuosi;

                haeMerkinnat(req, yhteys, out, pvm);
                
                double keskikulutus = kulutus / liikuntapaivat;
                double keskikalorit = kalorit / ruokapaivat;
                double keskihiilarit = hiilarit / ruokapaivat;
                double keskiproteiini = proteiini / ruokapaivat;
                double keskirasva = rasva / ruokapaivat;
                
                
                out.println("<h1>Keskimäärin päivässä</h1><table>");
                out.println("<tr><td>Kulutus: </td><td>" + keskikulutus + " kcal</td></tr>");
                out.println("<tr><td>Kalorit: </td><td>" + keskikalorit + " kcal</td></tr>");
                out.println("<tr><td>Hiilihydraatit: </td><td>" + keskihiilarit + " </td></tr>");
                out.println("<tr><td>Proteiini: </td><td>" + keskiproteiini + " </td></tr>");
                out.println("<tr><td>Rasva: </td><td>" + keskirasva + " </td></tr></table>");
            }
       
       out.println("</body></html>");
    }
    
    public void doPost(HttpServletRequest req, HttpServletResponse res) 
        throws ServletException, IOException {
	
        doGet(req, res);
    }
    
    //Metodi, jolla haetaan ja tulostetaan halutut merkinnät.
    private void haeMerkinnat(HttpServletRequest req, Connection yhteys, ServletOutputStream out,
            String pvm) throws ServletException, IOException {
        
        kulutus = 0;
        kalorit = 0;
        hiilarit = 0;
        proteiini = 0;
        rasva = 0;

        liikuntapaivat = 0;
        ruokapaivat = 0;
        
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            String sql = "SELECT aktiviteettiNimi, kulutus * lukumaara, kommentti, pvm FROM aktiviteetti, "
                            + "kuuluuMerkintaan, merkinta, kayttaja WHERE nimi = ? and "
                            + "kayttaja.kayttajaID = merkinta.kayttajaID and ";
            if (pvm.length() < 8) {
                sql += "substring(pvm, 3, 8) = ?";
            } else {
                sql += "pvm = ?";
            }
            sql += " and merkinta.merkintaID = kuuluuMerkintaan.merkintaID and "
                            + "kuuluuMerkintaan.aktiviteettiID = aktiviteetti.aktiviteettiID";
            stmt = yhteys.prepareStatement(sql);
            stmt.setString(1, req.getParameter("account"));
            stmt.setString(2, pvm);
            rs = stmt.executeQuery();

            out.println("<h1>Kulutus</h1>");
            out.println("<table><tr><th>Aktiviteetti</th><th>Kuluttanut</th><th>Kommentti</th></tr>");
            
            ArrayList<String> paivat = new ArrayList<String>();
            while (rs.next()) {
                String paiva = rs.getString(4).substring(0, 2);
                
                if (!paivat.contains(paiva)) {
                    paivat.add(paiva);
                    liikuntapaivat++;
                }
                
                kulutus += rs.getDouble(2);
                
                out.println("<tr><td>" + rs.getString(1) + "</td><td>" + rs.getDouble(2) +
                    " kcal</td><td>" + rs.getString(3) + "</td></tr>");
            }
            out.println("</table>");

            sql = "SELECT aineNimi, kalorit * kuuluuMerkintaan.lukumaara, hiilarit * kuuluuMerkintaan.lukumaara, "
                            + "proteiini * kuuluuMerkintaan.lukumaara, rasva * kuuluuMerkintaan.lukumaara, kommentti, pvm FROM raakaAine, kuuluuMerkintaan, "
                            + "merkinta, kayttaja WHERE nimi = ? and kayttaja.kayttajaID = merkinta.kayttajaID and ";
            if (pvm.length() < 8) {
                sql += "substring(pvm, 3, 8) = ?";
            } else {
                sql += "pvm = ?";
            }
            sql += " and merkinta.merkintaID = kuuluuMerkintaan.merkintaID and "
                            + "kuuluuMerkintaan.aineID = raakaAine.aineID";
            stmt = yhteys.prepareStatement(sql);
            stmt.setString(1, req.getParameter("account"));
            stmt.setString(2, pvm);
            rs = stmt.executeQuery();

            out.println("<h1>Ravinto</h1>");
            out.println("<table><tr><th>Ruoka</th><th>Kaloreita</th><th>Hiilihydraatteja</th><th>"
                            + "Proteiinia</th><th>Rasvaa</th><th>Kommentti</th></tr>");
            
            paivat = new ArrayList<String>();
            while (rs.next()) {
                String paiva = rs.getString(7).substring(0, 2);
                
                if (!paivat.contains(paiva)) {
                    paivat.add(paiva);
                    ruokapaivat++;
                }
                
                kalorit += rs.getDouble(2);
                hiilarit += rs.getDouble(3);
                proteiini += rs.getDouble(4);
                rasva += rs.getDouble(5);
                
                out.println("<tr><td>" + rs.getString(1) + "</td><td>" + rs.getDouble(2) + "</td>"
                                + "<td>" + rs.getDouble(3) + "</td><td>" + rs.getDouble(4) +
                                "</td><td>" + rs.getDouble(5) + "</td><td>" + rs.getString(6) + "</td></tr>");
            }

            sql = "SELECT lajiNimi, sum(kalorit * ainesOsa.lukumaara * ainesOsa.paino * ainesOsa.tilavuus) * kuuluuMerkintaan.lukumaara, "
                            + "sum(hiilarit * ainesOsa.lukumaara * ainesOsa.paino * ainesOsa.tilavuus) * kuuluuMerkintaan.lukumaara, "
                            + "sum(proteiini * ainesOsa.lukumaara * ainesOsa.paino * ainesOsa.tilavuus) * kuuluuMerkintaan.lukumaara, "
                            + "sum(rasva * ainesOsa.lukumaara * ainesOsa.paino * ainesOsa.tilavuus) * kuuluuMerkintaan.lukumaara, "
                            + "kommentti, pvm FROM raakaAine, ainesOsa, ruokalaji, kuuluuMerkintaan, "
                            + "merkinta, kayttaja WHERE raakaAine.aineID = ainesOsa.aineID and "
                            + "ainesOsa.lajiID = ruokalaji.lajiID and "
                            + "ruokalaji.lajiID = kuuluuMerkintaan.lajiID and "
                            + "kuuluuMerkintaan.merkintaID = merkinta.merkintaID and ";
            if (pvm.length() < 8) {
                sql += "substring(pvm, 3, 8) = ?";
            } else {
                sql += "pvm = ?";
            }
            sql += " and merkinta.kayttajaID = kayttaja.kayttajaID and "
                            + "nimi = ? "
                            + "GROUP BY ruokalaji.lajiNimi, kuuluuMerkintaan.lukumaara, kuuluuMerkintaan.kommentti, merkinta.pvm";

            stmt = yhteys.prepareStatement(sql);
            stmt.setString(1, pvm);
            stmt.setString(2, req.getParameter("account"));
            rs = stmt.executeQuery();

            while (rs.next()) {
                String paiva = rs.getString(7).substring(0, 2);
                
                if (!paivat.contains(paiva)) {
                    paivat.add(paiva);
                    ruokapaivat++;
                }
                
                kalorit += rs.getDouble(2);
                hiilarit += rs.getDouble(3);
                proteiini += rs.getDouble(4);
                rasva += rs.getDouble(5);
                
                out.println("<tr><td>" + rs.getString(1) + "</td><td>" + rs.getDouble(2) + "</td><td>"
                                + rs.getDouble(3)+ "</td><td>" + rs.getDouble(4) + "</td><td>"
                                + rs.getDouble(5)+ "</td><td>" + rs.getString(6) + "</td></tr>");
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
