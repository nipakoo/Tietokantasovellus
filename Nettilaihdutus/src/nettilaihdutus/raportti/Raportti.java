package nettilaihdutus.raportti;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import nettilaihdutus.aktiviteetit.Aktiviteetti;
import nettilaihdutus.ruoka.Ruoka;

/**
 *
 * @author albis
 */
public class Raportti {
    private Date pvm;
    private List<Ruoka> ruuat;
    private List<Aktiviteetti> aktiviteetit;
    
    public Raportti(Date pvm) {
        this.pvm = pvm;
        ruuat = new ArrayList<Ruoka>();
        aktiviteetit = new ArrayList<Aktiviteetti>();
    }
    
    public void lisaaRuoka(Ruoka ruoka) {
        ruuat.add(ruoka);
    }
    
    public void lisaaAktiviteetti(Aktiviteetti aktiviteetti) {
        aktiviteetit.add(aktiviteetti);
    }
    
    public List<Ruoka> getRuuat() {
        return ruuat;
    }
    
    public List<Aktiviteetti> getAktiviteetit() {
        return aktiviteetit;
    }
}
