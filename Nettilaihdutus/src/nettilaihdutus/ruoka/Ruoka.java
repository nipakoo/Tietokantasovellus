package nettilaihdutus.ruoka;

import java.util.List;

/**
 *
 * @author albis
 */
public interface Ruoka {
    public String getNimi();
    public int getRavintoarvo(Ravintoarvo arvo);
    public List<Ruokaluokka> getRuokaluokat();
}
