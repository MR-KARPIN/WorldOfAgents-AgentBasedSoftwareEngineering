package es.upm.emse.absd.team1.agents.platform.MapManager.GameBehaviours;

import es.upm.emse.absd.GUIUtils;
import es.upm.emse.absd.ontology.woa.concepts.Coordinate;
import es.upm.emse.absd.team1.agents.platform.MapManager.AgMapManager;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import lombok.extern.java.Log;

import static es.upm.emse.absd.team1.agents.Utils.ANSI_RED;
import static es.upm.emse.absd.team1.agents.Utils.ANSI_YELLOW;

@Log
public class MapManagerGameBehaviour_CheckNewUnit extends CyclicBehaviour {
    AgMapManager agente;
    public MapManagerGameBehaviour_CheckNewUnit(AgMapManager a) {
        super(a);
        this.agente=a;
    }

    @Override
    public void action() {
        //recibir mensaje del unitManager preguntando por el contenido de la casilla para crear una unidad
        ACLMessage msg = this.getAgent().receive(MessageTemplate.MatchProtocol("queHayAqui"));
        if(msg!=null) {
            log.info(ANSI_YELLOW + this.getAgent().getLocalName() + " Recibe mensaje de UnitManager preguntando" +
                    "por el contenido de la casilla para crear una unidad");
            Coordinate coordenadas;
            try {
                coordenadas = (Coordinate) msg.getContentObject();
            } catch (UnreadableException e) {
                log.warning(ANSI_RED+this.getAgent().getLocalName() + "ERROR obteniendo las coordenadas del mensaje");
                throw new RuntimeException(e);
            }
            String contenidoCasilla= this.agente.getResourceGameMap().getValue(coordenadas.getXValue(), coordenadas.getYValue());
            ACLMessage response = msg.createReply();
            response.setProtocol("aquiHay");
            response.setContent(contenidoCasilla);
            this.agente.send(response);
        }
    }
}
