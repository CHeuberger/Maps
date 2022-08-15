package cfh.maps.gui;

import java.io.IOException;

public interface MapDAO {

    void save(Map map) throws IOException;
    
    Map read() throws IOException;
}
