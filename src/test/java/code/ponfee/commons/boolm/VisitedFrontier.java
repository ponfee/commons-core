package code.ponfee.commons.boolm;

import java.net.URL;

public interface VisitedFrontier {
    public void put(URL url);
    public void put(String value);
    
    public boolean contains(URL url);
    public boolean contains(String value);
}
