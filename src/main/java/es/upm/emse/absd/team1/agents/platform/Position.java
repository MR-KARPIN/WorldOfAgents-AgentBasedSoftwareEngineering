package es.upm.emse.absd.team1.agents.platform;

import lombok.Data;
import java.io.Serializable;

@Data
public class Position implements Serializable {
    private int x;
    private int y;

    public Position(long x, long y) {
        this.x = (int) x;
        this.y = (int) y;
    }
}
