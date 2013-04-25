import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.*;
import java.util.Calendar;

public class Laskuri extends HttpServlet {
    final String ajuri = "org.postgresql.Driver";
    final String serveri = "jdbc:postgresql:niko";
    final String tunnus = "niko";
    final String salasana = "a46f5f4142aaf274";
    
    public void doGet(HttpServletRequest req, HttpServletResponse res) 
       throws ServletException, IOException {
        
       ServletOutputStream out;  
       res.setContentType("text/html");
       out = res.getOutputStream();

       out.println("<html><head>"
               + "<link rel='stylesheet' type='text/css' href='/nettilaihdutus/Tyylit.css'>"
               + "<title>Laske ravintosi ja kulutuksesi!</title></head>");
       
       Connection yhteys = null;
       yhteys = Tyokalut.yhdista(ajuri, serveri, tunnus, salasana);
       
       if (yhteys==null) {
          out.println("<body bgcolor=white><h1>Tietokantayhtteyden muodostus epäonnistui</h1>");
	  out.println("<a href='/nettilaihdutus/Etusivu.html'>Takaisin etusivulle</a>");
	  out.println("</body></html>");
          return;
       }
       
       out.println("<body bgcolor=white>");
       
       out.println("<form action='Lisaaja' method='get'>");
       if (Tyokalut.tarkistaJaValitaTunnukset(req, out, yhteys)) {
            out.println("<input type='submit' value='Lisaa ruokia ja aktiviteetteja'>");
       }
       out.println("</form>");
       
       out.println("<form action='Merkinnat' method='get'>");
       if (Tyokalut.tarkistaJaValitaTunnukset(req, out, yhteys)) {
           out.println("<input type='submit' value='Tarkastele merkintöjä'>");
       }
       out.println("</form>");
       
       out.println("<form action='Laskuri' method='get'>");
       out.println("<input type='submit' name='aktiviteetti' value='Hae aktiviteettejä'>");
       out.println("<input type='submit' name='ruoka' value='Hae ruokia'>");
       Tyokalut.tarkistaJaValitaTunnukset(req, out, yhteys);
       out.println("</form>");
       
       if (req.getParameter("aktiviteetti") != null) {
           out.println("<h1>Hae aktiviteettejä</h1>");
           out.println("<form action='Laskuri' method='get'>");
           out.println("Nimi: <input type='text' name='nimi'><br>");
           out.println("Kulutus(enemmän kuin):<input type='text' name='kulutus'><br>");
           
           out.println("<select name='luokka'>");
           out.println("<option></option>");
           out.println("<option value='100'>Ulkoliikunta</option>");
           out.println("<option value='101'>Sisäliikunta</option>");
           out.println("<option value='102'>Talviurheilu</option>");
           out.println("<option value='103'>Joukkuelaji</option>");
           out.println("</select>");
           Tyokalut.tarkistaJaValitaTunnukset(req, out, yhteys);
           out.println("<input type='submit' name='haeAktiviteetit' value='Hae'>");
           out.println("</form>");
           
       } else if (req.getParameter("ruoka") != null) {
           out.println("<h1>Hae ruokia</h1>");
           out.println("<form action='Laskuri' method='get'>");
           
           out.println("Nimi: <input type='text' name='nimi'><br>");
           out.println("Kalorit(vähemmän kuin): <input type='text' name='kalorit'><br>");
           out.println("Hiilihydraatit(vähemmän kuin): <input type='text' name='hiilarit'><br>");
           out.println("Proteiini(vähemmän kuin): <input type='text' name='proteiini'><br>");
           out.println("Rasva(vähemmän kuin): <input type='text' name='rasva'><br>");
           
           out.println("<select name='luokka'>");
           out.println("<option></option>");
           out.println("<option value='500'>Aamupala</option>");
           out.println("<option value='501'>Lounas</option>");
           out.println("<option value='502'>Päivällinen</option>");
           out.println("<option value='503'>Iltapala</option>");
           out.println("<option value='504'>Kasvisruoka</option>");
           out.println("<option value='505'>Gluteeniton</option>");
           out.println("<option value='506'>Laktoositon</option>");
           out.println("<option value='507'>HYLA</option>");
           out.println("</select>");
           Tyokalut.tarkistaJaValitaTunnukset(req, out, yhteys);
           out.println("<input type='submit' name='haeRuuat' value='Hae'>");
           out.println("</form>");
       }
       
       PreparedStatement stmt = null;
       ResultSet rs = null;
       
       if (req.getParameter("merkitseAktiviteetti") != null) {
           double lkm = 0.0;
           String kommentti = req.getParameter("kommentti");
           if (kommentti.length() > 50) {
               out.println("<p>Liian pitkä kommentti! Anna lyhyempi!</p>");
               out.println("</body></html>");
               return;
           }
           try {
               lkm = Double.parseDouble(req.getParameter("merkintaMaara"));
           } catch (NumberFormatException e) {
               out.println("<p>Lukumäärä ei ollut hyväksyttävä luku</p>");
               out.println("</body></html>");
               return;
           }
           lisaaMerkintaan(req, out, yhteys, req.getParameter("merkittavanNimi"), lkm,
                   kommentti, 1);
       } else if (req.getParameter("merkitseRaakaAine") != null) {
           double lkm = 0;
           String kommentti = req.getParameter("kommentti");
           if (kommentti.length() > 50) {
               out.println("<p>Liian pitkä kommentti! Anna lyhyempi!</p>");
               out.println("</body></html>");
               return;
           }
           try {
               lkm = Double.parseDouble(req.getParameter("merkintaMaara"));
           } catch (NumberFormatException e) {
               out.println("<p>Lukumäärä ei ollut hyväksyttävä luku</p>");
               out.println("</body></html>");
               return;
           }
           lisaaMerkintaan(req, out, yhteys, req.getParameter("merkittavanNimi"), lkm,
                   kommentti, 2);
       } else if (req.getParameter("merkitseRuokalaji") != null) {
           double lkm = 0;
           String kommentti = req.getParameter("kommentti");
           if (kommentti.length() > 50) {
               out.println("<p>Liian pitkä kommentti! Anna lyhyempi!</p>");
               out.println("</body></html>");
               return;
           }
           try {
               lkm = Double.parseDouble(req.getParameter("merkintaMaara"));
           } catch (NumberFormatException e) {
               out.println("<p>Lukumäärä ei ollut hyväksyttävä luku</p>");
               out.println("</body></html>");
               return;
           }
           lisaaMerkintaan(req, out, yhteys, req.getParameter("merkittavanNimi"), lkm,
                   kommentti, 3);
       }
       
       try {

            if (req.getParameter("haeAktiviteetit") != null) {

                String sql = "SELECT distinct aktiviteettiNimi, kulutus FROM aktiviteetti, kuuluuAktiviteettiluokkaan WHERE "
                        + "kulutus >= ? and "
                        + "aktiviteetti.aktiviteettiID = kuuluuAktiviteettiluokkaan.aktiviteettiID ";
                
                if (req.getParameter("nimi") != null && !req.getParameter("nimi").isEmpty()) {
                    sql += "and aktiviteettiNimi like ? ";
                }

                if (req.getParameter("luokka") != null && !req.getParameter("luokka").isEmpty()) {
                    sql += "and luokkaID = ?";          
                }

                stmt = yhteys.prepareStatement(sql);

                try {
                    stmt.setDouble(1, Double.parseDouble(req.getParameter("kulutus")));
                } catch (NumberFormatException e) {
                    stmt.setDouble(1, 0.0);
                }
                int i = 2;
                if (req.getParameter("nimi") != null && !req.getParameter("nimi").isEmpty()) {
                    stmt.setString(i, "%"+req.getParameter("nimi")+"%");
                    i++;
                }
                if (req.getParameter("luokka") != null && !req.getParameter("luokka").isEmpty()) {
                    stmt.setInt(i, Integer.parseInt(req.getParameter("luokka")));
                }

                rs = stmt.executeQuery();

                out.println("<table>");
                out.println("<tr><th>Nimi</th><th>Kulutus</th></tr>");

                while (rs.next()) {
                    out.println("<tr><td>" + rs.getString(1) + "</td><td>" + rs.getString(2) + "</td>"
                            + "<td><form action='Laskuri' method='get'>");
                            if (Tyokalut.tarkistaJaValitaTunnukset(req, out, yhteys)) {
                                out.println(" Paljonko(h): <input type='text' name='merkintaMaara'>"
                                + " Kommentti: <input type='text' name='kommentti'>"
                                + "<input type='hidden' name='merkittavanNimi' value='" + rs.getString(1) + "'>"
                                + "<input type='submit' name='merkitseAktiviteetti' value='Lisää päivän merkintään'>"
                                + "</form></td></tr>");
                            }
                            out.println("</td></form></tr>");
                }
                out.println("</table>");
            } else if (req.getParameter("haeRuuat") != null) {

                String sql = "SELECT distinct aineNimi, kalorit, hiilarit, proteiini, rasva FROM raakaAine, kuuluuRuokaluokkaan WHERE "
                        + "kalorit <= ? and "
                        + "hiilarit <= ? and "
                        + "proteiini <= ? and "
                        + "rasva <= ? and "
                        + "raakaAine.aineID = kuuluuRuokaluokkaan.aineID " ;

                if (req.getParameter("nimi") != null && !req.getParameter("nimi").isEmpty()) {
                    sql += "and aineNimi like ? ";
                }
                
                if (req.getParameter("luokka") != null && !req.getParameter("luokka").isEmpty()) {
                    sql += "and luokkaID = ? ";          
                }
                
                stmt = yhteys.prepareStatement(sql);

                try {
                    stmt.setDouble(1, Double.parseDouble(req.getParameter("kalorit")));
                } catch (NumberFormatException e) {
                    stmt.setDouble(1, 50000.0);
                }
                try {
                    stmt.setDouble(2, Double.parseDouble(req.getParameter("hiilarit")));
                } catch (NumberFormatException e) {
                    stmt.setDouble(2, 50000.0);
                }
                try {
                    stmt.setDouble(3, Double.parseDouble(req.getParameter("proteiini")));
                } catch (NumberFormatException e) {
                    stmt.setDouble(3, 50000.0);
                }
                try {
                    stmt.setDouble(4, Double.parseDouble(req.getParameter("rasva")));
                } catch (NumberFormatException e) {
                    stmt.setDouble(4, 50000.0);
                }
                int i = 5;
                if (req.getParameter("nimi") != null && !req.getParameter("nimi").isEmpty()) {
                    stmt.setString(i, "%"+req.getParameter("nimi")+"%");
                    i++;
                }
                if (req.getParameter("luokka") != null && !req.getParameter("luokka").isEmpty()) {
                    stmt.setInt(i, Integer.parseInt(req.getParameter("luokka")));
                }

                rs = stmt.executeQuery();

                out.println("<table>");
                out.println("<tr><th>Nimi</th><th>Kalorit</th><th>Hiilihydraatit</th><th>Proteiini</th><th>Rasva</th></tr>");

                while (rs.next()) {
                    out.println("<tr><td>" + rs.getString(1) + "</td><td>" + rs.getString(2) + "</td><td>" + rs.getString(3) + "</td>"
                            + "<td>" + rs.getString(4) + "</td><td>" + rs.getString(5) + "</td>"
                            + "<td><form action='Laskuri' method='get'>");
                            if (Tyokalut.tarkistaJaValitaTunnukset(req, out, yhteys)) {
                                out.println(" Paljonko(kpl): <input type='text' name='merkintaMaara'>"
                                + " Kommentti: <input type='text' name='kommentti'>"
                                + "<input type='hidden' name='merkittavanNimi' value='" + rs.getString(1) + "'>"
                                + "<input type='submit' name='merkitseRaakaAine' value='Lisää päivän merkintään'>"
                                + "</form></td></tr>");
                            }
                            out.println("</td></form></tr>");
                }
                sql = "SELECT lajiNimi, sum(kalorit * ainesOsa.lukumaara * ainesOsa.paino * ainesOsa.tilavuus), "
                        + "sum(hiilarit * ainesOsa.lukumaara * ainesOsa.paino * ainesOsa.tilavuus), "
                        + "sum(proteiini* ainesOsa.lukumaara * ainesOsa.paino * ainesOsa.tilavuus), "
                        + "sum(rasva * ainesOsa.lukumaara * ainesOsa.paino * ainesOsa.tilavuus) FROM raakaAine, ruokalaji, ainesOsa ";
                        if (req.getParameter("luokka") != null && !req.getParameter("luokka").isEmpty()) {
                            sql += ", kuuluuRuokaluokkaan ";
                        }
                        sql += "WHERE "
                        + "ruokalaji.lajiID = ainesOsa.lajiID and "
                        + "ainesOsa.aineID = raakaAine.aineID ";
                        if (req.getParameter("luokka") != null && !req.getParameter("luokka").isEmpty()) {
                            sql += "and ruokalaji.lajiID = kuuluuRuokaluokkaan.lajiID ";
                        }
                
                if (req.getParameter("nimi") != null && !req.getParameter("nimi").isEmpty()) {
                    sql += "and lajiNimi like ? ";
                }
                
                if (req.getParameter("luokka") != null && !req.getParameter("luokka").isEmpty()) {
                    sql += "and luokkaID = ? ";          
                }
                sql += "GROUP BY ruokalaji.lajiNimi";
                
                stmt = yhteys.prepareStatement(sql);
                
                i = 1;
                if (req.getParameter("nimi") != null && !req.getParameter("nimi").isEmpty()) {
                    stmt.setString(i, "%"+req.getParameter("nimi")+"%");
                    i++;
                }
                if (req.getParameter("luokka") != null && !req.getParameter("luokka").isEmpty()) {
                    stmt.setInt(i, Integer.parseInt(req.getParameter("luokka")));
                }

                rs = stmt.executeQuery();

                double kalorit = 50000.0;
                double hiilarit = 50000.0;
                double proteiini = 50000.0;
                double rasva = 50000.0;
                try {
                    kalorit = Double.parseDouble(req.getParameter("kalorit"));
                } catch (NumberFormatException e) {}
                try {
                    hiilarit = Double.parseDouble(req.getParameter("kalorit"));
                } catch (NumberFormatException e) {}
                try {
                    proteiini = Double.parseDouble(req.getParameter("proteiini"));
                } catch (NumberFormatException e) {}
                try {
                    rasva = Double.parseDouble(req.getParameter("rasva"));
                } catch (NumberFormatException e) {}

                while (rs.next()) {
                    if (rs.getDouble(2) <= kalorit && rs.getDouble(3) <= hiilarit &&
                            rs.getDouble(4) <= proteiini && rs.getDouble(5) <= rasva) {
                        out.println("<tr><td>" + rs.getString(1) + "</td><td>" + rs.getString(2) + "</td><td>" + rs.getString(3) + "</td>"
                            + "<td>" + rs.getString(4) + "</td><td>" + rs.getString(5) + "</td>"
                            + "<td><form action='Laskuri' method='get'>");
                            if (Tyokalut.tarkistaJaValitaTunnukset(req, out, yhteys)) {
                                out.println(" Paljonko(kpl): <input type='text' name='merkintaMaara'>"
                                + " Kommentti: <input type='text' name='kommentti'>"
                                + "<input type='hidden' name='merkittavanNimi' value='" + rs.getString(1) + "'>"
                                + "<input type='submit' name='merkitseRuokalaji' value='Lisää päivän merkintään'>"
                                + "</form></td></tr>");
                            }
                            out.println("</td></form></tr>");
                    }
                }
            }
            
                out.println("</table></body></html>");
                
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
    }
    
    public void doPost(HttpServletRequest req, HttpServletResponse res) 
        throws ServletException, IOException {
	
        doGet(req, res);
    }
    
    // tyypit 1=aktiviteetti 2=raaka-aine 3=ruokalaji
    private void lisaaMerkintaan(HttpServletRequest req, ServletOutputStream out, Connection yhteys,
            String lisattavaNimi, double lkm, String kommentti, int tyyppi) 
       throws ServletException, IOException {
        String account = req.getParameter("account");
        String password = req.getParameter("password");
        
        if (account == null || password == null) {
            return;
        }
        
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        int kayttajaID = 0;
        int merkintaID = 0;
        int lisattavaID = 0;
        int osaID = 0;
        String sql;
        try {
            
            sql = "SELECT max(osaID) FROM kuuluuMerkintaan";
            stmt = yhteys.prepareStatement(sql);
            rs = stmt.executeQuery();
            rs.next();
            osaID = rs.getInt(1) + 1;
            
            sql = "SELECT kayttajaID FROM kayttaja WHERE nimi = ?";
            stmt = yhteys.prepareStatement(sql);
            stmt.setString(1, account);
            
            rs = stmt.executeQuery();
            rs.next();
            
            kayttajaID = rs.getInt("kayttajaID");
            
            Calendar cal = Calendar.getInstance();
            String pvm = "";
            if (cal.get(Calendar.DAY_OF_MONTH) < 10) {
                pvm += "0";
            }
            pvm += (cal.get(Calendar.DAY_OF_MONTH));
            if (cal.get(Calendar.MONTH) < 9) {
                pvm += "0";
            }
            pvm += (cal.get(Calendar.MONTH) + 1);
            pvm += cal.get(Calendar.YEAR);
            
            sql = "SELECT merkintaID FROM merkinta WHERE kayttajaID = ? and pvm = ?";
            stmt = yhteys.prepareStatement(sql);
            stmt.setInt(1, kayttajaID);
            stmt.setString(2, pvm);
            
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                merkintaID = rs.getInt("merkintaID");
            } else {
                sql = "SELECT max(merkintaID) FROM merkinta";
                stmt = yhteys.prepareStatement(sql);
                rs = stmt.executeQuery();
                rs.next();
                
                merkintaID = rs.getInt(1) + 1;
                
                sql = "INSERT INTO merkinta values(?, ?, ?)";
                stmt = yhteys.prepareStatement(sql);
                stmt.setInt(1, merkintaID);
                stmt.setInt(2, kayttajaID);
                stmt.setString(3, pvm);
                stmt.executeUpdate();
            }
            
            if (tyyppi == 1) {
                sql = "SELECT aktiviteettiID FROM aktiviteetti WHERE aktiviteettiNimi = ?";
                stmt = yhteys.prepareStatement(sql);
                stmt.setString(1, lisattavaNimi);
                rs = stmt.executeQuery();
                
                rs.next();
                lisattavaID = rs.getInt(1);
                
                sql = "INSERT INTO kuuluuMerkintaan values(?, ?, null, null, ?, ?, ?)";
            } else if (tyyppi == 2) {
                sql = "SELECT aineID FROM raakaAine WHERE aineNimi = ?";
                stmt = yhteys.prepareStatement(sql);
                stmt.setString(1, lisattavaNimi);
                rs = stmt.executeQuery();
                    
                rs.next();
                lisattavaID = rs.getInt(1);
                    
                sql = "INSERT INTO kuuluuMerkintaan values(?, ?, null, ?, null, ?, ?)";
            } else {
                sql = "SELECT lajiID FROM ruokalaji WHERE lajiNimi = ?";
                stmt = yhteys.prepareStatement(sql);
                stmt.setString(1, lisattavaNimi);
                rs = stmt.executeQuery();
                    
                rs.next();
                lisattavaID = rs.getInt(1);
                    
                sql = "INSERT INTO kuuluuMerkintaan values(?, ?, ?, null, null, ?, ?)";
            }
            stmt = yhteys.prepareStatement(sql);
            stmt.setInt(1, osaID);
            stmt.setInt(2, merkintaID);
            stmt.setInt(3, lisattavaID);
            stmt.setDouble(4, lkm);
            stmt.setString(5, kommentti);
            
            stmt.executeUpdate();
        } catch (SQLException ee) {
            out.println("Tietokantavirhe "+ee.getMessage());
        }        
    }
}
