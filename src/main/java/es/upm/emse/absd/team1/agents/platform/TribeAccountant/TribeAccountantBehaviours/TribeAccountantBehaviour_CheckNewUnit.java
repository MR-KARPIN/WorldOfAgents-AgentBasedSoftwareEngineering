package es.upm.emse.absd.team1.agents.platform.TribeAccountant.TribeAccountantBehaviours;

import es.upm.emse.absd.GUIUtils;
import es.upm.emse.absd.team1.agents.platform.TribeResources;
import es.upm.emse.absd.team1.agents.platform.TribeAccountant.AgTribeAccountant;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import lombok.extern.java.Log;
import static es.upm.emse.absd.team1.agents.Utils.ANSI_YELLOW;

@Log
public class TribeAccountantBehaviour_CheckNewUnit extends CyclicBehaviour {
    AgTribeAccountant agente;
    private final int PRECIOUNIDAD=150;

    public TribeAccountantBehaviour_CheckNewUnit(AgTribeAccountant agente) {
        super(agente);
        this.agente=agente;
    }

    @Override
    public void action() {
        //recibir mensaje del unitManager para preguntar si tiene recursos para crear unidad.
        ACLMessage msg = this.getAgent().receive(MessageTemplate.MatchProtocol("tieneRecursosTribuParaUnidad"));
        if(msg!=null) {
            log.info(ANSI_YELLOW + this.getAgent().getLocalName() + " Recibe mensaje de UnitManager preguntando" +
                    "por los recursos para crear una unidad");
            Object[] resourcesInfoForBuilding;
            try { resourcesInfoForBuilding = (Object[]) msg.getContentObject(); }
            catch (UnreadableException e) { throw new RuntimeException(e); }
            AID tribu = (AID) resourcesInfoForBuilding[0];
            AID unidad = (AID) resourcesInfoForBuilding[1];
            String dineroDisponible="no";
            TribeResources resources = this.agente.getTribesResourcesMap().get(tribu);
            float oro= resources.getTribeGold();
            log.info(ANSI_YELLOW + this.getAgent().getLocalName() + " La tribu tiene: "+oro+" de oro, "+
                    resources.getTribeStone()+" de piedra, "+resources.getTribeWood()+" de madera");
            if( oro >= PRECIOUNIDAD){
                //Dinero disponible
                TribeResources recursos = this.agente.getTribesResourcesMap().get(tribu);
                recursos.setTribeGold(-PRECIOUNIDAD);
                this.agente.getTribesResourcesMap().put(tribu, recursos);
                GUIUtils.loseResource(tribu.getLocalName(), unidad.getLocalName(), "gold", (int) PRECIOUNIDAD);
                dineroDisponible="si";
                this.agente.sendCurrentResources(tribu,recursos.getTribeGold(),recursos.getTribeStone(),recursos.getTribeWood(),recursos.getTribeStorageCapacity());
            }
            ACLMessage response = msg.createReply();
            response.setProtocol("tieneRecursosTribuParaUnidad");
            response.setContent(dineroDisponible);
            this.agente.send(response);
        }

        ACLMessage msg2 = this.getAgent().receive(MessageTemplate.MatchProtocol("devolverOro"));
        if(msg2!=null) {
            log.info(ANSI_YELLOW + this.getAgent().getLocalName() + " Recibe mensaje de UnitManager pidiendo" +
                    "devolver dinero de la creación de una unidad");
            Object[] aidTribu;
            try { aidTribu = (Object[]) msg2.getContentObject(); }
            catch (UnreadableException e) { throw new RuntimeException(e); }
            AID tribu =  (AID) aidTribu[0];
            float oro= this.agente.getTribesResourcesMap().get(tribu).getTribeGold();
            TribeResources recursos = this.agente.getTribesResourcesMap().get(tribu);
            recursos.setTribeGold(+PRECIOUNIDAD);
            this.agente.getTribesResourcesMap().put(tribu, recursos);
        }
    }
}
