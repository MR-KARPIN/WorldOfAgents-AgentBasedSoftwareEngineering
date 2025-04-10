package es.upm.emse.absd.team1.agents.platform;

import jade.core.AID;
import lombok.Data;

@Data
public class UnitInfo {
    private AID tribeAID;
    private AID unitAID;
    private String tribeName;
    private String unitName;
    private int posActualX;
    private int posActualY;
    private boolean isBlocked;

    public UnitInfo(AID tribeAID, AID unitAID, String tribeName, String unitName, boolean isBlocked, int posActualX, int posActualY) {
        this.tribeAID = tribeAID;
        this.unitAID = unitAID;
        this.tribeName = tribeName;
        this.unitName = unitName;
        this.isBlocked = isBlocked;
        this.posActualX = posActualX;
        this.posActualY = posActualY;
    }
}
