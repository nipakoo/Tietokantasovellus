import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.*;
import java.util.ArrayList;

public class Lisaaja extends HttpServlet {
    final String ajuri = "org.postgresql.Driver";
    final String serveri = "jdbc:postgresql:niko";
    final String tunnus = "niko";
    final String salasana = "a46f5f4142aaf274";
    
    public void doGet(HttpServletRequest req, HttpServletResponse res) 
       throws ServletException, IOException {
       
       ServletOutputStream out;  
       res.setContentType("text/html");
       out = res.getOutputStream();
       
       out.println("<html><head><title></title></head>");
       
       Connection yhteys = null;
       yhteys = yhdista(ajuri, serveri, tunnus, salasana);
       
       PreparedStatement stmt = null;
       ResultSet rs = null;
       
       if (yhteys==null) {
          out.println("<body bgcolor=white><h1>Tietokantayhtteyden muodostus epäonnistui</h1>");
	  out.println("<a href='/nettilaihdutus/Etusivu.html'>Takaisin etusivulle</a>");
	  out.println("</body></html>");
          return;
       }
       
       out.println("<body bgcolor=white>");
       
       out.println("<form action='Lisaaja' method='get'>");
       out.println("<input type='submit' name='aktiviteetti' value='Lisää aktiviteettejä'>");
       out.println("<input type='submit' name='raakaAine' value='Lisää raaka-aineita'>");
       out.println("<input type='submit' name='ruokaLaji' value='Lisaa ruokalajeja'>");
       out.println("</form>");
       
       if (req.getParameter("aktiviteetti") != null) {
           out.println("<h1>Lisää aktiviteettejä</h1>");
           out.println("<form action='Lisaaja' method='get'>");
           out.println("Nimi: <input type='text' name='nimi'><br>");
           out.println("Kulutus(kcal/h): <input type='text' name='kulutus'><br>");
           
           out.println("<select multiple name='luokka'>");
           out.println("<option value='100'>Ulkoliikunta</option>");
           out.println("<option value='101'>Sisäliikunta</option>");
           out.println("<option value='102'>Talviurheilu</option>");
           out.println("<option value='103'>Joukkuelaji</option>");
           out.println("</select><br>");
           
           out.println("<input type='submit' name='lisaaAktiviteetteja' value='Lisää'>");
           out.println("</form>");
           
       } else if (req.getParameter("raakaAine") != null) {
           out.println("<h1>Lisää raaka-aineita</h1>");
           out.println("<form action='Lisaaja' method='get'>");
           
           out.println("Nimi: <input type='text' name='nimi'><br>");
           out.println("Kalorit: <input type='text' name='kalorit'><br>");
           out.println("Hiilarit: <input type='text' name='hiilarit'><br>");
           out.println("Proteiini: <input type='text' name='proteiini'><br>");
           out.println("Rasva: <input type='text' name='rasva'><br>");
           
           out.println("<select multiple name='luokka'>");
           out.println("<option value='500'>Aamupala</option>");
           out.println("<option value='501'>Lounas</option>");
           out.println("<option value='502'>Päivällinen</option>");
           out.println("<option value='503'>Iltapala</option>");
           out.println("<option value='504'>Kasvisruoka</option>");
           out.println("<option value='505'>Gluteeniton</option>");
           out.println("<option value='506'>Laktoositon</option>");
           out.println("<option value='507'>HYLA</option>");
           out.println("</select><br>");
           
           out.println("<input type='submit' name='lisaaRaakaAineita' value='Lisää'>");
           out.println("</form>");
       } else if (req.getParameter("ruokaLaji") != null) {
           out.println("<h1>Lisää ruokalajeja valitsemalla raaka-aineet</h1>");
           
           try { 
                String sql = "SELECT aineID, aineNimi, kalorit, hiilarit, proteiini, rasva FROM raakaAine";
           
                stmt = yhteys.prepareStatement(sql);
           
                rs = stmt.executeQuery();
           
           out.println("<form action='Lisaaja' method='get'>");
           out.println("Nimi: <input type='text' name='nimi'><br>");
           
           while (rs.next()) {
               int ID = rs.getInt("aineID");
               String nimi = rs.getString("aineNimi");
               double kalorit = rs.getDouble("kalorit");
               double hiilarit = rs.getDouble("hiilarit");
               double proteiini = rs.getDouble("proteiini");
               double rasva = rs.getDouble("rasva");
               
               out.println("<p>" + nimi + ", " + kalorit + "kcal " + hiilarit + "h " + proteiini + "p " + rasva + "r </p>");
               out.println("<select name='mittatyyppi'>");
               out.println("<option value='lukumaara'>Lukumäärä</option>");
               out.println("<option value='paino'>Paino(g)</option>");
               out.println("<option value='tilavuus'>Tilavuus(dl)</option>");
               out.println("</select>");
               out.println("Määrä: <input type='text' name='maara'>");
               out.println("<input type='hidden' name='id' value='" + ID + "'>");
           }
           
           out.println("<br><input type='submit' name='lisaaRuokalajeja' value='Lisää'>");
           out.println("</form>");
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
       
       try {
            if (req.getParameter("lisaaAktiviteetteja") != null) {
                String nimi = req.getParameter("nimi");
                
                if (nimi.isEmpty() || nimi == null) {
                    out.println("<p>Anna vähintään nimi!</p>");
                    out.println("</body></html>");
                    return;
                }
                
                String sql = "SELECT aktiviteettiID FROM aktiviteetti WHERE aktiviteettiNimi = ?";
                stmt = yhteys.prepareStatement(sql);
                stmt.setString(1, nimi);

                rs = stmt.executeQuery();
                if (rs.next()) {
                    out.println("<body bgcolor=white><h1>Annetulla nimellä on jo määritelty aktiviteetti! Valitse toinen!</h1>");
                    out.println("</body></html>");
                    return;
                }
                
                sql = "SELECT max(aktiviteettiID) FROM aktiviteetti";
                stmt = yhteys.prepareStatement(sql);

                rs = stmt.executeQuery();
                rs.next();
                int ID = rs.getInt(1) + 1;
                
                sql = "INSERT INTO aktiviteetti VALUES(?, ?, ?)";
                stmt = yhteys.prepareStatement(sql);
                stmt.setInt(1, ID);
                stmt.setString(2, nimi);

                try {
                    stmt.setDouble(3, Double.parseDouble(req.getParameter("kulutus")));
                } catch (NumberFormatException e) {
                    stmt.setDouble(3, 0.0);
                }

                stmt.executeUpdate();
                
                String[] luokat = req.getParameterValues("luokka");
                
                if (luokat != null) {
                    for (int i = 0; i < luokat.length; i++) {
                        sql = "SELECT max(osaID) FROM kuuluuAktiviteettiluokkaan";
                        stmt = yhteys.prepareStatement(sql);

                        rs = stmt.executeQuery();
                        rs.next();
                        int osaID = rs.getInt(1) + 1;
                        int luokkaID = Integer.parseInt(luokat[i]);

                        if (luokat[i] != null) {
                            sql = "INSERT INTO kuuluuAktiviteettiluokkaan VALUES(?, ?, ?)";
                            stmt = yhteys.prepareStatement(sql);
                            stmt.setInt(1, osaID);
                            stmt.setInt(2, luokkaID);
                            stmt.setInt(3, ID);

                            stmt.executeUpdate();
                        }
                    }
                }
            } else if (req.getParameter("lisaaRaakaAineita") != null) {
                String nimi = req.getParameter("nimi");
                
                if (nimi == null || nimi.isEmpty()) {
                    out.println("<p>Anna vähintään nimi!</p>");
                    out.println("</body></html>");
                    return;
                }
                
                String sql = "SELECT aineID FROM raakaAine WHERE aineNimi = ?";
                stmt = yhteys.prepareStatement(sql);
                stmt.setString(1, nimi);

                rs = stmt.executeQuery();
                if (rs.next()) {
                    out.println("<body bgcolor=white><h1>Annetulla nimellä on jo määritelty raaka-aine! Valitse toinen!</h1>");
                    out.println("</body></html>");
                    return;
                }
                
                sql = "SELECT max(aineID) FROM raakaAine";
                stmt = yhteys.prepareStatement(sql);

                rs = stmt.executeQuery();
                rs.next();
                int ID = rs.getInt(1) + 1;
                
                
                sql = "INSERT INTO raakaAine VALUES(?, ?, ?, ?, ?, ?)";
                stmt = yhteys.prepareStatement(sql);
                stmt.setInt(1, ID);
                stmt.setString(2, nimi);
                
                try {
                    stmt.setDouble(3, Double.parseDouble(req.getParameter("kalorit")));
                } catch (NumberFormatException e) {
                    stmt.setDouble(3, 0.0);
                }
                try {
                    stmt.setDouble(4, Double.parseDouble(req.getParameter("hiilarit")));
                } catch (NumberFormatException e) {
                    stmt.setDouble(4, 0.0);
                }
                try {
                    stmt.setDouble(5, Double.parseDouble(req.getParameter("proteiini")));
                } catch (NumberFormatException e) {
                    stmt.setDouble(5, 0.0);
                }
                try {
                    stmt.setDouble(6, Double.parseDouble(req.getParameter("rasva")));
                } catch (NumberFormatException e) {
                    stmt.setDouble(6, 0.0);
                }

                stmt.executeUpdate();
                
                String[] luokat = req.getParameterValues("luokka");
                
                if (luokat != null) {
                    for (int i = 0; i < luokat.length; i++) {
                        sql = "SELECT max(osaID) FROM kuuluuRuokaluokkaan";
                        stmt = yhteys.prepareStatement(sql);

                        rs = stmt.executeQuery();
                        rs.next();
                        int osaID = rs.getInt(1) + 1;

                        if (luokat[i] != null) {
                            sql = "INSERT INTO kuuluuRuokaluokkaan VALUES(?, ?, null, ?)";
                            stmt = yhteys.prepareStatement(sql);
                            stmt.setInt(1, osaID);
                            stmt.setInt(2, Integer.parseInt(luokat[i]));
                            stmt.setInt(3, ID);

                            stmt.executeUpdate();
                        }
                    }
                }
            } else if (req.getParameter("lisaaRuokalajeja") != null) {
                String nimi = req.getParameter("nimi");
                
                if (nimi == null || nimi.isEmpty()) {
                    out.println("<p>Anna vähintään nimi!</p>");
                    out.println("</body></html>");
                    return;
                }
                
                String sql = "SELECT lajiID FROM ruokalaji WHERE lajiNimi = ?";
                stmt = yhteys.prepareStatement(sql);
                stmt.setString(1, nimi);

                rs = stmt.executeQuery();
                if (rs.next()) {
                    out.println("<body bgcolor=white><h1>Annetulla nimellä on jo määritelty ruokalaji! Valitse toinen!</h1>");
                    out.println("</body></html>");
                    return;
                }
                
                sql = "SELECT max(lajiID) FROM ruokalaji";
                stmt = yhteys.prepareStatement(sql);

                rs = stmt.executeQuery();
                rs.next();
                int ID = rs.getInt(1) + 1;
                
                sql = "INSERT INTO ruokalaji VALUES(?, ?)";
                stmt = yhteys.prepareStatement(sql);
                stmt.setInt(1, ID);
                stmt.setString(2, nimi);
                
                stmt.executeUpdate();
                
                String[] aineID = req.getParameterValues("id");
                String[] maarat = req.getParameterValues("maara");
                String[] tyypit = req.getParameterValues("mittatyyppi");
                
                ArrayList<String> kaikkiLuokat = new ArrayList<String>();
                kaikkiLuokat.add("500");kaikkiLuokat.add("501");kaikkiLuokat.add("502");kaikkiLuokat.add("503");
                kaikkiLuokat.add("504");kaikkiLuokat.add("505");kaikkiLuokat.add("506");kaikkiLuokat.add("507");
                
                for (int i = 0; i < aineID.length; i++) {
                    double maara=0.0;
                    try {
                        maara = Double.parseDouble(maarat[i]);
                    } catch (NumberFormatException e) {
                        continue;
                    }
                    if (maara > 0.0 && tyypit[i] != null) {
                        sql = "SELECT max(osaID) FROM ainesOsa";
                        stmt = yhteys.prepareStatement(sql);

                        rs = stmt.executeQuery();
                        rs.next();
                        int osaID = rs.getInt(1) + 1;
                        
                        if (tyypit[i].equals("lukumaara")) {
                            sql = "INSERT INTO ainesOsa VALUES(?, ?, ?, ?, 1.0, 1.0)";
                            stmt = yhteys.prepareStatement(sql);
                            stmt.setInt(1, osaID);
                            stmt.setInt(2, ID);
                            stmt.setInt(3, Integer.parseInt(aineID[i]));
                            stmt.setDouble(4, maara);

                            stmt.executeUpdate();
                        } else if (tyypit[i].equals("paino")) {
                            sql = "INSERT INTO ainesOsa VALUES(?, ?, ?, 1.0, ?, 1.0)";
                            stmt = yhteys.prepareStatement(sql);
                            stmt.setInt(1, osaID);
                            stmt.setInt(2, ID);
                            stmt.setInt(3, Integer.parseInt(aineID[i]));
                            stmt.setDouble(4, maara);

                            stmt.executeUpdate();
                        } else if (tyypit[i].equals("tilavuus")) {
                            sql = "INSERT INTO ainesOsa VALUES(?, ?, ?, 1.0, 1.0, ?)";
                            stmt = yhteys.prepareStatement(sql);
                            stmt.setInt(1, osaID);
                            stmt.setInt(2, ID);
                            stmt.setInt(3, Integer.parseInt(aineID[i]));
                            stmt.setDouble(4, maara);

                            stmt.executeUpdate();
                        } else {
                            continue;
                        }
                        
                        sql = "SELECT luokkaID from kuuluuRuokaluokkaan WHERE aineID = ?";
                        stmt = yhteys.prepareStatement(sql);
                        stmt.setInt(1, Integer.parseInt(aineID[i]));
                        rs = stmt.executeQuery();
                        ArrayList<String> aineenLuokat = new ArrayList<String>();
                        while(rs.next()) {
                            aineenLuokat.add(""+rs.getInt(1));
                        }
                        
                        for (int j = 0; j < kaikkiLuokat.size(); j++) {
                            if (!aineenLuokat.contains(kaikkiLuokat.get(j))) {
                                kaikkiLuokat.remove(j);
                            }
                        }
                    }
                }
                
                for (int j = 0; j < kaikkiLuokat.size(); j++) {
                            sql = "SELECT max(osaID) FROM kuuluuRuokaluokkaan";
                            stmt = yhteys.prepareStatement(sql);
                            rs = stmt.executeQuery();
                            rs.next();
                            int osaID = rs.getInt(1) + 1;
                    
                            sql = "INSERT INTO kuuluuRuokaluokkaan VALUES(?, ?, ?, null)";
                            stmt = yhteys.prepareStatement(sql);
                            stmt.setInt(1, osaID);
                            stmt.setInt(2, Integer.parseInt(kaikkiLuokat.get(j)));
                            stmt.setInt(3, ID);

                            stmt.executeUpdate();
                }
            }
            
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
}