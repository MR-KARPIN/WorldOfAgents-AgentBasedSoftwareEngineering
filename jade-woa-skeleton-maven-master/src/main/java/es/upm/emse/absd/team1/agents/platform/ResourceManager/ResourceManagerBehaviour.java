package es.upm.emse.absd.team1.agents.platform.ResourceManager;

import es.upm.emse.absd.team1.agents.Utils;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.ParallelBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import lombok.Data;
import lombok.extern.java.Log;
import static es.upm.emse.absd.team1.agents.Utils.ANSI_YELLOW;
import static es.upm.emse.absd.team1.agents.Utils.newMsgWithObject;


/*
    BEHAVIOUR DEL RESOURCE MANAGER:
        Es cíclico y escucha permanentemente los mensajes de petición de explotación del unit manager.
        A cada petición crea el agente ExplotateResource que se encarga de procesar dicha explotación.
            Gracias a este diseño se permite atender de forma concurrente todas las explotaciones de recursos del juego
 */
@Log
@Data
public class ResourceManagerBehaviour extends ParallelBehaviour {

    private AgResourceManager agente;
    private static final int EXTRACTED_AMOUNT = 10;
    private static final double TIME_EXPLOTATION_STONE =8000 * Utils.secondsPerHour;
    private static final double TIME_EXPLOTATION_WOOD = 6000 * Utils.secondsPerHour;

    public ResourceManagerBehaviour(AgResourceManager agente) {
        this.agente = agente;

        //Behaviour cíclico que escucha permanentemente los mensajes de petición de explotación del unit manager.
        this.addSubBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage msg = this.getAgent().receive(MessageTemplate.MatchProtocol("ExplotarRecurso"));
                if(msg != null) { exploitationProcess(msg); }
            }
        });
    }

    private void exploitationProcess(ACLMessage msgExplotation){
        log.info(ANSI_YELLOW + agente.getLocalName() + " Petición de explotar recurso recibida");
        //2.2.6. As the resource manager, I want to check the conditions of exploiting
        //some resource so that I can proceed with or interrupt the exploitation process
        try {
            //Obtención del contenido del mensaje de explotación de recurso
            Object[] exploitationInfo = (Object[]) msgExplotation.getContentObject();
            int xCoord = (int) exploitationInfo[0];
            int yCoord = (int) exploitationInfo[1];
            AID unitAID = (AID) exploitationInfo[2];
            //Comprobación del contenido y la cantidad de la casilla a explotar
            String resource = agente.getResourceGameMap().getValue(xCoord, yCoord);
            int amount;
            String protocol = "ExploitationFail";
            //Check de si hay recurso
            if (resource.equals("Ground") || resource.equals("Building")){
                //SCENARIO 2: A request to exploit some resource from a cell without any resource fails
                log.info(ANSI_YELLOW + this.getAgent().getLocalName() +
                        " No hay recursos aqui en " + xCoord + "," + yCoord);
            //Check de si hay suficiente recurso
            } else if ((amount = Integer.parseInt(agente.getAmountGameMap().getValue(xCoord, yCoord))) >= EXTRACTED_AMOUNT){
                //SCENARIO 1: A request to exploit some resource matching all the preconditions is accepted, and
                //the future amount of resource to be collected is reserved for this tribe
                log.info(ANSI_YELLOW + this.getAgent().getLocalName() +
                        " Hay "+ amount +" cantidad de " + resource + " en " + xCoord + "," + yCoord);
                //Se reserva la cantidad a explotar
                agente.getAmountGameMap().setValue(xCoord, yCoord, Integer.toString(amount-10), false);
                protocol = "AcceptedResourceRequest";
                //Se elimina el recurso del mapa si con esta explotación ya no queda más recurso
                if(Integer.parseInt(agente.getAmountGameMap().getValue(xCoord, yCoord))<EXTRACTED_AMOUNT){
                    sendMapDepletedResource(xCoord, yCoord);
                }
            //Check no hay suficiente recurso
            } else{
                //SCENARIO 3: A request to exploit some resource from a cell with a resource source
                //but without that resource available (all remaining resource has already been reserved) fails
                log.info(ANSI_YELLOW + this.getAgent().getLocalName() +
                        " Queda "+ amount +" cantidad de " + resource + " en " + xCoord + "," + yCoord + "... Se ha acabado");
                //Actualizamos las estructuras de datos para quitar el recurso
                agente.getAmountGameMap().setValue(xCoord, yCoord, null, false);
                agente.getResourceGameMap().setValue(xCoord, yCoord, "Ground", false);
                //comunicar al map manager que ahi ya no hay nada
                sendMapDepletedResource(xCoord, yCoord);
            }

            //Explotatión exitosa Asi que se lanza subbehaviour que espera y luego informa al unitManager
            if (protocol.equals("AcceptedResourceRequest")){
                if(resource.equals("Ore")){ //esperar x segundos
                    sendFinishExploitation((int) TIME_EXPLOTATION_STONE, msgExplotation, resource,
                            Integer.parseInt(agente.getPercentageOreMap().getValue(xCoord,yCoord)));
                }
                if(resource.equals("Forest")){
                    sendFinishExploitation((int) TIME_EXPLOTATION_WOOD, msgExplotation, resource, 0);
                }
            } else { //No se cumplen las condiciones de explotar, se responde al unitManager directamente
                //Se envia el AID de la unidad
                //2.2.8. As the resource manager, I want to inform the unit manager about
                // the successful end of a resource exploitation so that the unit exploiting can be liberated
                ACLMessage unitManagerMsg = newMsgWithObject(this.getAgent(), ACLMessage.INFORM,
                        unitAID, msgExplotation.getSender(), protocol);
                log.info(ANSI_YELLOW + this.getAgent().getLocalName() +
                        " Se comunica al UnitManager que falla la explotación de recurso de la unidad: "
                        +unitAID.getLocalName() + " en la posición: "+xCoord+ " "+yCoord);
                this.getAgent().send(unitManagerMsg);
            }
        } catch (UnreadableException e) { throw new RuntimeException(e); }
    }

    private void sendFinishExploitation(int time, ACLMessage msg, String resource, int percentage){
        this.addSubBehaviour(new WakerBehaviour(this.getAgent(), time) {
            @Override
            protected void onWake() {
                super.onWake();
                //Se envia el AID de la unidad
                Object[] exploitationInfo;
                try {
                    exploitationInfo = (Object[]) msg.getContentObject();
                    AID unitAID = (AID) exploitationInfo[2];
                    AID tribeAID = (AID) exploitationInfo[3];
                    ACLMessage newTribeAccMsg = newMsgWithObject(this.getAgent(), ACLMessage.INFORM,
                            unitAID, msg.getSender(), "AcceptedResourceRequest");
                    log.info(ANSI_YELLOW + this.getAgent().getLocalName() +
                            " Mensaje enviado a UnitManager, explotación de recurso exitosa de la unidad: " +unitAID.getLocalName());
                    this.getAgent().send(newTribeAccMsg);
                    informTribeAccountant(tribeAID,unitAID, resource, percentage);
                } catch (UnreadableException e) { throw new RuntimeException(e); }
            }
        });
    }


    private void sendMapDepletedResource(int x, int y){
        Object[] depletedResource = {x, y};
        ACLMessage depletedMsg = newMsgWithObject(this.getAgent(), ACLMessage.INFORM,
                depletedResource, agente.getMapManager(), "depletedResource");
        log.info(ANSI_YELLOW + this.getAgent().getLocalName() +
                " Mensaje enviado a MapManager comunicando que el recurso de la posicion: "
                +x + ", "+y+" se ha consumido por completo");
        this.getAgent().send(depletedMsg);
    }


    private void informTribeAccountant(AID tribu, AID unit, String recurso, int percentage){
        Object[] resourceCollected = {tribu, unit, recurso, EXTRACTED_AMOUNT, percentage};
        ACLMessage collectedMsg = newMsgWithObject(this.getAgent(), ACLMessage.INFORM,
                resourceCollected, agente.getTribeAccountant(), "resourceCollected");
        log.info(ANSI_YELLOW + this.getAgent().getLocalName() +
                " Mensaje enviado a tribeAccountant comunicando que la tribu " + tribu.getLocalName()+
                " ha ganado "+ ResourceManagerBehaviour.EXTRACTED_AMOUNT + " de "+recurso);
        this.getAgent().send(collectedMsg);
    }


}
