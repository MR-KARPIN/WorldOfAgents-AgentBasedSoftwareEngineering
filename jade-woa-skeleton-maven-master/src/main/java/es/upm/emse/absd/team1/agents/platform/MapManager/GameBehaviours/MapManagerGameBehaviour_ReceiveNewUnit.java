package es.upm.emse.absd.team1.agents.platform.MapManager.GameBehaviours;

import es.upm.emse.absd.team1.agents.platform.Unidad;
import es.upm.emse.absd.team1.agents.platform.MapManager.AgMapManager;
import jade.core.AID;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import lombok.extern.java.Log;

import java.util.ArrayList;

import static es.upm.emse.absd.team1.agents.Utils.ANSI_YELLOW;

@Log
public class MapManagerGameBehaviour_ReceiveNewUnit extends SimpleBehaviour {

    AgMapManager agente;

    public MapManagerGameBehaviour_ReceiveNewUnit(AgMapManager a) {
        super(a);
        this.agente=a;
    }

    @Override
    public void action() { receiveNewUnit(); }

    @Override
    public boolean done() { return this.agente.getPhase()>1; }

    private void receiveNewUnit(){
        ACLMessage msg = this.getAgent().receive(MessageTemplate.MatchProtocol("newUnitInfo"));
        if(msg!=null) {
            Object[] newUnitInfo;
            try { newUnitInfo = (Object[]) msg.getContentObject(); }
            catch (UnreadableException e) { throw new RuntimeException(e); }

            AID unitAID=(AID)newUnitInfo[1];
            String tribe=(String)newUnitInfo[0];
            int positionX = (int) newUnitInfo[2];
            int positionY = (int) newUnitInfo[3];

            log.info(ANSI_YELLOW + this.getAgent().getLocalName() +
                    " Recibe mensaje de nueva unidad (" + unitAID.getLocalName() +
                    ") para la tribu " + tribe + " en " + positionX + ", " + positionY);

            if(this.agente.getMapaUnidades().get(tribe)==null)
                this.agente.getMapaUnidades().put(tribe, new ArrayList<>());
            this.agente.getMapaUnidades().get(tribe).add(new Unidad(unitAID, positionX, positionY));
        }
    }
}
