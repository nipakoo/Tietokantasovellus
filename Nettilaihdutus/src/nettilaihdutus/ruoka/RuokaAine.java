package nettilaihdutus.ruoka;

import java.util.List;

/**
 *
 * @author albis
 */
public class RuokaAine implements Ruoka {
    private String nimi;
    private int kalorit;
    private int hiilarit;
    private int proteiini;
    private int rasva;
    private List<Ruokaluokka> luokat;
    
    public RuokaAine(String nimi, int kalorit, int hiilarit, int proteiini, int rasva, List<Ruokaluokka> luokat) {
        this.nimi = nimi;
        this.kalorit = kalorit;
        this.hiilarit = hiilarit;
        this.proteiini = proteiini;
        this.rasva = rasva;
        this.luokat = luokat;
    }
    
    @Override
    public String getNimi() {
        return nimi;
    }
    
    @Override
    public int getRavintoarvo(Ravintoarvo arvo) {
        switch (arvo) {
            case KALORIT:
                return kalorit;
            case HIILARIT:
                return hiilarit;
            case PROTEIINI:
                return proteiini;
            case RASVA:
                return rasva;
        }
        return 0;
    }
    
    @Override
    public List<Ruokaluokka> getRuokaluokat() {
        return luokat;
    }
}
