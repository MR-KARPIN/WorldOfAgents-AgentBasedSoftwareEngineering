package es.upm.emse.absd.team1.agents.platform.TribeAccountant.TribeAccountantBehaviours;


import es.upm.emse.absd.team1.agents.platform.TribeResources;
import es.upm.emse.absd.ontology.woa.WoaOntology;
import es.upm.emse.absd.ontology.woa.WoaOntologyVocabulary;
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
                1.2. Recibir mensaje del UnitManager para sumar 1 unidad a una tribu.

 */

@Log
@Data
public class TribeAccountantBehaviour_AddUnit extends CyclicBehaviour {

    private final Codec codec = new SLCodec();
    private final Ontology ontology = WoaOntology.getInstance();
    private AgTribeAccountant agente;
    private AID UnitManagerAID;
    private final String UNITMANAGER = "UnitManager";

    public TribeAccountantBehaviour_AddUnit(AgTribeAccountant agente) {
        super(agente);
        this.agente = agente;
        UnitManagerAID = untilFindDFS(this.getAgent(), UNITMANAGER);
    }

    @Override
    public void action() {
        ACLMessage msg = this.getAgent().receive(MessageTemplate.MatchProtocol("AddUnit"));
        if(msg!=null) {
            try { //Obtain initial units and tribe AID
                AID tribeAID = (AID) msg.getContentObject();
                log.info(ANSI_YELLOW + this.getAgent().getLocalName() +
                        " Recibe mensaje del UnitManager, vamos a sumar una unidad a la tribu " + tribeAID.getLocalName());

                if (!this.agente.getTribesResourcesMap().containsKey(tribeAID))
                    addTribe(tribeAID, tribeAID.getLocalName());

                TribeResources tribeResources = this.agente.getTribesResourcesMap().get(tribeAID);
                tribeResources.setNUnits(tribeResources.getNUnits() + 1);
                this.agente.getTribesResourcesMap().put(tribeAID, tribeResources);
            } catch (UnreadableException e) { throw new RuntimeException(e); }
        }
    }


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
