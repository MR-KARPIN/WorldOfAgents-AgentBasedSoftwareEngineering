package es.upm.emse.absd.team1.agents.platform;

import jade.core.AID;
import lombok.Data;

@Data
public class TribeResources {

    private AID tribeId;
    private String tribeName;
    private float tribeGold;
    private float tribeStone;
    private float tribeWood;
    private float tribeStorageCapacity;
    private float usedCapacity;
    private float nUnits;
    private float nCities;
    private float nStores;

    public TribeResources(AID tribeId, String tribeName,
                          float tribeGold, float tribeStone, float tribeWood,
                          float tribeStorageCapacity, float nUnits, float nCities, float nStores){
        this.tribeId = tribeId;
        this.tribeName = tribeName;
        this.tribeGold = tribeGold;
        this.tribeStone = tribeStone;
        this.tribeWood = tribeWood;
        this.tribeStorageCapacity = tribeStorageCapacity;
        this.nUnits = nUnits;
        this.nCities = nCities;
        this.nStores = nStores;
        this.usedCapacity=tribeGold+tribeStone+tribeWood;
    }

    public void setTribeGold(float tribeGold) {
        this.tribeGold += tribeGold;
        this.usedCapacity+=tribeGold;
    }

    public void setTribeStone(float tribeStone) {
        this.tribeStone += tribeStone;
        this.usedCapacity+=tribeStone;
    }

    public void setTribeWood(float tribeWood) {
        this.tribeWood += tribeWood;
        this.usedCapacity+=tribeWood;
    }
}


