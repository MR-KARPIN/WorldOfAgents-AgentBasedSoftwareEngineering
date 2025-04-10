package es.upm.emse.absd.team1.agents.platform.TribeAccountant.TribeAccountantBehaviours;


import es.upm.emse.absd.GUIUtils;
import es.upm.emse.absd.team1.agents.platform.TribeResources;
import es.upm.emse.absd.ontology.woa.WoaOntology;
import es.upm.emse.absd.team1.agents.platform.TribeAccountant.AgTribeAccountant;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import lombok.Data;
import lombok.extern.java.Log;

import static es.upm.emse.absd.team1.agents.Utils.*;


/*
                1.3 Recibir mensaje de Resource Manager con el recurso ganado

 */

@Log
@Data
public class TribeAccountantBehaviour_ResourceCollected extends CyclicBehaviour {

    private final Codec codec = new SLCodec();
    private final Ontology ontology = WoaOntology.getInstance();
    private AgTribeAccountant agente;

    public TribeAccountantBehaviour_ResourceCollected(AgTribeAccountant agente) {
        super(agente);
        this.agente = agente;
    }

    @Override
    public void action() {
        ACLMessage msg = this.getAgent().receive(MessageTemplate.MatchProtocol("resourceCollected"));
        if(msg!=null) {
            //Obtener contenido del mensaje --> necesita AID tribu, recurso y cantidad ganada
            Object[] resourceCollected;
            try { resourceCollected = (Object[]) msg.getContentObject(); }
            catch (UnreadableException e) { throw new RuntimeException(e); }
            AID tribeAID=(AID)resourceCollected[0];
            AID unitAID=(AID)resourceCollected[1];
            String resource=(String) resourceCollected[2];
            int cantidad = (int) resourceCollected[3];
            int percentage = (int) resourceCollected[4];
            int cantidadOro = (int) (cantidad*((float) percentage /100));
            int cantidadPiedra = cantidad - cantidadOro;

            TribeResources recursosTribu = this.agente.getTribesResourcesMap().get(tribeAID);
            int maxStorage = (int) recursosTribu.getTribeStorageCapacity();
            int usedCapacity = (int) recursosTribu.getUsedCapacity();
            log.info(ANSI_YELLOW + agente.getLocalName() + "La tribu "+tribeAID.getLocalName() +
                    " tiene "+ usedCapacity + " almacenado de una capacidad máxima de: "+maxStorage);

            if(resource.equals("Ore")){

                if((usedCapacity+cantidadOro) > maxStorage){
                    cantidadOro=maxStorage-usedCapacity;
                    log.info(ANSI_YELLOW + agente.getLocalName() + "La tribu "+tribeAID.getLocalName() +
                            " no tiene más espacio, solo recolecta: "+cantidadOro+" de oro");
                }
                recursosTribu.setTribeGold(cantidadOro);
                if(((usedCapacity+cantidadPiedra) > maxStorage)){
                    cantidadPiedra=maxStorage-usedCapacity;
                    log.info(ANSI_YELLOW + agente.getLocalName() + "La tribu "+tribeAID.getLocalName() +
                            " no tiene más espacio, solo recolecta: "+cantidadPiedra+" de piedra");
                }
                recursosTribu.setTribeStone(cantidadPiedra);

                log.info(ANSI_YELLOW + agente.getLocalName() + "La tribu "+tribeAID.getLocalName() +
                        " ha ganado "+ cantidadOro + " de oro y "+ cantidadPiedra+ " de piedra, almacenaje: "
                        +recursosTribu.getUsedCapacity()+"/"+recursosTribu.getTribeStorageCapacity());
                GUIUtils.gainResource(tribeAID.getLocalName(),unitAID.getLocalName(),"gold", cantidadOro);
                GUIUtils.gainResource(tribeAID.getLocalName(),unitAID.getLocalName(),"stone", cantidadPiedra);
            }
            if(resource.equals("Forest")){
                if((usedCapacity+cantidad) > maxStorage){
                    cantidad=maxStorage-usedCapacity;
                    log.info(ANSI_YELLOW + agente.getLocalName() + "La tribu "+tribeAID.getLocalName() +
                            " no tiene más espacio, solo recolecta: "+cantidad+" de madera");
                }
                recursosTribu.setTribeWood(cantidad);
                log.info(ANSI_YELLOW + agente.getLocalName() + "La tribu "+tribeAID.getLocalName() +
                        " ha ganado "+ cantidad + " de madera, almacenaje: "+recursosTribu.getUsedCapacity()+"/"+recursosTribu.getTribeStorageCapacity());
                GUIUtils.gainResource(tribeAID.getLocalName(),unitAID.getLocalName(),"wood", cantidad);
            }
            this.agente.getTribesResourcesMap().put(tribeAID, recursosTribu);
            //LLamar al tribeController para que actualice los recursos de la tribu
            this.agente.sendCurrentResources(tribeAID,recursosTribu.getTribeGold(),recursosTribu.getTribeStone(),recursosTribu.getTribeWood(),recursosTribu.getTribeStorageCapacity());
        }
    }

    /*
    private InformCurrentResources buildUpdatedResources(AID tribeAID, float gold, float stone, float wood, float capacity){
        Resource resourceGold = new Resource("gold",gold);
        Resource resourceStone = new Resource("stone",stone);
        Resource resourceWood = new Resource("wood",wood);
        CurrentResources currentResources = new CurrentResources(resourceGold, resourceStone, resourceWood);
        float updatedCapacity = capacity;
        if(capacity == 0) updatedCapacity = this.agente.getInitialCapacity();
        StorageCapacity storageCapacity = new StorageCapacity(updatedCapacity);
        return new InformCurrentResources(currentResources, storageCapacity);
    }

    private void sendCurrentResources1(AID tribeAID, float gold, float stone, float wood, float capacity){
        ACLMessage sendCurrentResources = newMsgWithOnto(this.getAgent(), tribeAID,
                ACLMessage.INFORM, codec, ontology,
                buildUpdatedResources(tribeAID, gold, stone, wood, capacity),
                WoaOntologyVocabulary.INFORM_CURRENT_RESOURCES);
        this.agente.send(sendCurrentResources);
        log.info(ANSI_BLUE + this.getAgent().getLocalName() +"Mensaje enviado a la tribu " + tribeAID.getLocalName() +
                "con sus recursos actualizados");
    }
     */
}
