package nettilaihdutus.kayttajat;

import java.util.ArrayList;
import java.util.List;
import nettilaihdutus.raportti.Raportti;

/**
 *
 * @author albis
 */
public class RekisteroitynytKayttaja extends Kayttaja {
    private List<Raportti> raportit;
    
    public RekisteroitynytKayttaja() {
        super();
        raportit = new ArrayList<Raportti>();
    }
}
