package es.upm.emse.absd.team1.agents.platform.MapManager.GameBehaviours;

import es.upm.emse.absd.ontology.woa.concepts.Coordinate;
import es.upm.emse.absd.team1.agents.platform.MapManager.AgMapManager;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

import lombok.extern.java.Log;

import static es.upm.emse.absd.team1.agents.Utils.ANSI_YELLOW;

@Log
public class MapManagerGameBehaviour_CheckCell extends CyclicBehaviour {
    private AgMapManager agente;
    public MapManagerGameBehaviour_CheckCell(AgMapManager a) {
        super(a);
        this.agente=a;
    }
    public void action() {
        ACLMessage msg = this.getAgent().receive(MessageTemplate.MatchProtocol("CheckCell"));
        if(msg != null) {
            Object[] checkCellInfo;
            try { checkCellInfo = (Object[]) msg.getContentObject(); }
            catch (UnreadableException e) { throw new RuntimeException(e); }
            Coordinate coordenadas = (Coordinate) checkCellInfo[0];
            String type = (String) checkCellInfo[1];
            String tribeName = (String) checkCellInfo[2];
            log.info(ANSI_YELLOW + this.getAgent().getLocalName() + " Recibe mensaje de BuildingManager preguntando" +
                    "por la disponibilidad de la casilla "+ coordenadas.getXValue() + "," + coordenadas.getYValue() + " para crear un building");
            String resultadoCheckeo = agente.getResourceGameMap().checkBuilding(coordenadas.getXValue(), coordenadas.getYValue(),type,tribeName);
            ACLMessage respuesta = msg.createReply();
            respuesta.setPerformative(ACLMessage.INFORM);
            respuesta.setContent(resultadoCheckeo);
            respuesta.setProtocol("CheckCell");
            this.agente.send(respuesta);
            log.info(ANSI_YELLOW + this.getAgent().getLocalName() + " Se ha enviado la respuesta a buildingManager");

        }


    }
}
