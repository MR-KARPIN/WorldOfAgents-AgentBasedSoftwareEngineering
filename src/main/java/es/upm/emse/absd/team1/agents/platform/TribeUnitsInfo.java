package es.upm.emse.absd.team1.agents.platform;

import jade.core.AID;
import lombok.Data;

import java.util.ArrayList;

@Data
public class TribeUnitsInfo {
    private AID tribe;
    private String tribeName;
    private int posIniX;
    private int posIniY;
    private int numberUnits;

    public TribeUnitsInfo(AID tribe, String tribeName, int x, int y) {
        this.tribe = tribe;
        this.tribeName = tribeName;
        this.posIniX = x;
        this.posIniY = y;
        this.numberUnits = 0;
    }

    public void addUnit(){
        this.numberUnits +=1;
    }
}
