package nettilaihdutus.aktiviteetit;

import java.util.List;

/**
 *
 * @author albis
 */
public class Aktiviteetti {
    private String nimi;
    private int kulutusTunnissa;
    private List<Aktiviteettiluokka> luokat;
    
    public Aktiviteetti(String nimi, int kulutusTunnissa, List<Aktiviteettiluokka> luokat) {
        this.nimi = nimi;
        this.kulutusTunnissa = kulutusTunnissa;
        this.luokat = luokat;
    }
}
