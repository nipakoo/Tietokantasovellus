package nettilaihdutus.ruoka;

import java.util.List;

/**
 *
 * @author albis
 */
public class Ruokalaji implements Ruoka {
    private String nimi;
    private List<RuokaAine> aineet;
    private List<Ruokaluokka> luokat;
    
    public Ruokalaji(String nimi, List<RuokaAine> aineet) {
        this.nimi = nimi;
        this.aineet = aineet;
        
        for (RuokaAine aine : aineet) {
            for (Ruokaluokka luokka : aine.getRuokaluokat()) {
                if (!luokat.contains(luokka)) {
                    luokat.add(luokka);
                }
            }
        }
        
        if (luokat.contains(Ruokaluokka.LIHARUOKA) && luokat.contains(Ruokaluokka.KASVISRUOKA)) {
            for (int i = 0; i < luokat.size(); i++) {
                if (luokat.get(i) == Ruokaluokka.KASVISRUOKA) {
                    luokat.remove(i);
                }
            }
        }
    }
    
    @Override
    public String getNimi() {
        return nimi;
    }
    
    @Override
    public int getRavintoarvo(Ravintoarvo arvo) {
        int arvotYhteensa = 0;
        
        for (RuokaAine aine : aineet) {
            arvotYhteensa += aine.getRavintoarvo(arvo);
        }
        
        return arvotYhteensa;
    }
    
    @Override
    public List<Ruokaluokka> getRuokaluokat() {
        return luokat;
    }
}
