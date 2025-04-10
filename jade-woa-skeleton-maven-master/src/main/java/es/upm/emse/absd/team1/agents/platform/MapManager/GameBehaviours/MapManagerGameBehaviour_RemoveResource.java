package es.upm.emse.absd.team1.agents.platform.MapManager.GameBehaviours;

import es.upm.emse.absd.GUIUtils;
import es.upm.emse.absd.team1.agents.platform.MapManager.AgMapManager;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import lombok.extern.java.Log;

import static es.upm.emse.absd.team1.agents.Utils.ANSI_YELLOW;
@Log
public class MapManagerGameBehaviour_RemoveResource extends CyclicBehaviour {
    private AgMapManager agente;
    public MapManagerGameBehaviour_RemoveResource(AgMapManager a) {
        super(a);
        this.agente=a;
    }

    @Override
    public void action() {
        //recibir mensaje del resource manager para eliminar un recurso del mapa
        ACLMessage msg = this.getAgent().receive(MessageTemplate.MatchProtocol("depletedResource"));
        if(msg!=null) {
            log.info(ANSI_YELLOW + this.getAgent().getLocalName() + " Recibe mensaje de eliminar un recurso");
            Object[] depletedResource;
            try {
                depletedResource = (Object[]) msg.getContentObject();
            } catch (UnreadableException e) {
                throw new RuntimeException(e);
            }
            int positionX=(int)depletedResource[0];
            int positionY=(int) depletedResource[1];

            log.info(ANSI_YELLOW + this.getAgent().getLocalName() + " se elimina el recurso agotado de la posición: "
                    + positionX + " "+positionY);

            agente.getResourceGameMap().setValue(positionX, positionY, "Ground", false);
            //Llamar a gui para que elimine de la interfaz el recurso.
            GUIUtils.depleteResource(positionX, positionY);
            //No sé si será necesario actualizar alguna estructura de datos del mapa que represente todas las casillas,
            // por ahora no existe.
        }
    }
}
