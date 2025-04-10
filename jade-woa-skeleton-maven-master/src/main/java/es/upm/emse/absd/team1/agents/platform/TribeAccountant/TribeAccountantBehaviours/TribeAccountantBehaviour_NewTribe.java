package es.upm.emse.absd.team1.agents.platform.TribeAccountant.TribeAccountantBehaviours;


import es.upm.emse.absd.team1.agents.platform.TribeResources;
import es.upm.emse.absd.ontology.woa.*;
import es.upm.emse.absd.ontology.woa.actions.InformCurrentResources;
import es.upm.emse.absd.ontology.woa.concepts.CurrentResources;
import es.upm.emse.absd.ontology.woa.concepts.Resource;
import es.upm.emse.absd.ontology.woa.concepts.StorageCapacity;
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
        1.1. Recibir nueva tribu, guardar registro y enviar al TribeController sus recursos iniciales

 */

@Log
@Data
public class TribeAccountantBehaviour_NewTribe extends CyclicBehaviour {

    private final Codec codec = new SLCodec();
    private final Ontology ontology = WoaOntology.getInstance();
    private AgTribeAccountant agente;
    private AID RegistrationManagerAID;
    private final String REGISTRATIONMANAGER = "RegistrationManager";


    public TribeAccountantBehaviour_NewTribe(AgTribeAccountant agente) {
        super(agente);
        this.agente = agente;
        RegistrationManagerAID = untilFindDFS(this.getAgent(), REGISTRATIONMANAGER);
    }

    @Override
    public void action() {
        ACLMessage msg = this.getAgent().receive(MessageTemplate.MatchSender(RegistrationManagerAID));
        if(msg!=null) {
            log.info(ANSI_YELLOW + this.getAgent().getLocalName() + " Recibe mensaje del Registration Manager");
            Object[] newTribeInfo;
            try {
                newTribeInfo = (Object[]) msg.getContentObject(); }
            catch (UnreadableException e) {
                throw new RuntimeException(e); }
            log.info(ANSI_YELLOW + this.getAgent().getLocalName() + " New tribe AID: " + newTribeInfo[0]);
            log.info(ANSI_YELLOW + this.getAgent().getLocalName() + " New tribe name: " + newTribeInfo[1]);

            AID tribeAID = (AID) newTribeInfo[0];
            if (!this.agente.getTribesResourcesMap().containsKey(tribeAID)) {
                addTribe(tribeAID, tribeAID.getLocalName());
            }
        }
    }


    //Añade una nueva tribu a la estructura de datos del Accountant e informa a dicha tribu de sus recursos iniciales
    private void addTribe(AID tribeAID, String tribeName){
        log.info(ANSI_YELLOW + this.getAgent().getLocalName() +" " + tribeName +
                " no la tenía guardada, la creo e informo al TribeController");
        TribeResources initialTribeResources = new TribeResources(tribeAID, tribeName,
                this.agente.getInitialGold(), this.agente.getInitialStone(), this.agente.getInitialWood(),
                this.agente.getInitialCapacity(), 0, 0 ,0);
        this.agente.getTribesResourcesMap().put(tribeAID, initialTribeResources); // Register the resources of the new tribe
        ACLMessage informInitialResources = newMsgWithOnto(this.getAgent(), tribeAID,
                ACLMessage.INFORM, codec, ontology,
                buildInformInitialResources(),
                WoaOntologyVocabulary.INFORM_CURRENT_RESOURCES);
        this.agente.send(informInitialResources);
        log.info(ANSI_YELLOW + this.getAgent().getLocalName() +"Mensaje enviado a la tribu " + tribeName +
                "con sus recursos iniciales");
        //Contact the tribe controller the amount of resources they have
    }


    //Construye el objeto de la ontología para enviar los recursos iniciales a una tribu
    private InformCurrentResources buildInformInitialResources() {
        CurrentResources iResources = new CurrentResources();
        Resource iGold = new Resource();
        iGold.setTypeRes("gold");
        iGold.setAmount(this.agente.getInitialGold());
        iResources.setGold(iGold);

        Resource iWood = new Resource();
        iWood.setTypeRes("wood");
        iWood.setAmount(this.agente.getInitialWood());
        iResources.setWood(iWood);

        Resource iStone = new Resource();
        iStone.setTypeRes("stone");
        iStone.setAmount(this.agente.getInitialStone());
        iResources.setStone(iStone);

        StorageCapacity storageCapacity = new StorageCapacity();
        storageCapacity.setSize(this.agente.getInitialCapacity());

        InformCurrentResources iir = new InformCurrentResources();
        iir.setTribeResources(iResources);
        iir.setStorage(storageCapacity);
        return iir;
    }
}
