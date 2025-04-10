package es.upm.emse.absd.team1.agents.platform;
import es.upm.emse.absd.ontology.woa.concepts.Coordinate;
import jade.core.AID;
import lombok.Data;

import java.io.Serializable;

@Data
public class NewBuildingFinished implements Serializable {
    private AID TribeAID;
    private AID unitAID;
    private String tipo;
    private Coordinate coordenadas;

    public NewBuildingFinished(AID tribeAID, AID unitAID, String tipo, Coordinate coordenadas) {
        this.TribeAID = tribeAID;
        this.unitAID = unitAID;
        this.tipo = tipo;
        this.coordenadas = coordenadas;
    }
}
