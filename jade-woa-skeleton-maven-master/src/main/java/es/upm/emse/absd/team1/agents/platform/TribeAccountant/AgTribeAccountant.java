package es.upm.emse.absd.team1.agents.platform.TribeAccountant;

import es.upm.emse.absd.team1.agents.platform.TribeResources;
import es.upm.emse.absd.ontology.woa.actions.InformCurrentResources;
import es.upm.emse.absd.ontology.woa.concepts.CurrentResources;
import es.upm.emse.absd.ontology.woa.concepts.Resource;
import es.upm.emse.absd.ontology.woa.concepts.StorageCapacity;
import es.upm.emse.absd.team1.agents.Utils;
import es.upm.emse.absd.ontology.woa.*;
import es.upm.emse.absd.team1.agents.platform.TribeAccountant.TribeAccountantBehaviours.*;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.ParallelBehaviour;
import jade.lang.acl.ACLMessage;
import lombok.Data;
import lombok.extern.java.Log;
import org.json.simple.JSONObject;
import org.json.simple.parser.*;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import static es.upm.emse.absd.team1.agents.Utils.*;

@Log
@Data
public class AgTribeAccountant extends Agent {
    public final String TRIBEACCOUNTANT = "TribeAccountant";
    private final Codec codec = new SLCodec();
    private final Ontology ontology = WoaOntology.getInstance();


    private Float initialCapacity;
    private Float initialWood;
    private Float initialStone;
    private Float initialGold;

    private Map<AID, TribeResources> tribesResourcesMap = new HashMap<>();


    protected void setup() {
        log.info(ANSI_YELLOW + this.getLocalName() + " Se crea agente TribeAccountant");

        // Register of the codec and the ontology to be used in the ContentManager
        getContentManager().registerLanguage(codec);
        getContentManager().registerOntology(ontology);

        Utils.register(this, TRIBEACCOUNTANT); //Registers in the yellow pages
        log.info(ANSI_YELLOW + this.getLocalName() + " registra servicio TribeAccountant en DFS ");
        parseJSON(); // Read initial resources

        /*
        BEHAVIOUR TRIBE ACCOUNTANT:
            Paralelo:
                1.1. Recibir nueva tribu, guardar registro y enviar al TribeController sus recursos iniciales
                1.2. Recibir mensaje del UnitManager para sumar 1 unidad a una tribu.
                1.3 Recibir mensaje de Resource Manager con el recurso ganado
                1.4 Recibir mensaje de Building Manager para comprobar si hay los recursos necesarios para la
                construcción, en caso de haberlos, paga esos recursos, en caso de no haberlos failure.
         */

        ParallelBehaviour tribeAccountantBehaviour = new ParallelBehaviour();
        //1.1. Recibir nueva tribu, guardar registro y enviar al TribeController sus recursos iniciales
        tribeAccountantBehaviour.addSubBehaviour(new TribeAccountantBehaviour_NewTribe(this));
        //1.2. Recibir mensaje del UnitManager para sumar 1 unidad a una tribu.
        tribeAccountantBehaviour.addSubBehaviour(new TribeAccountantBehaviour_AddUnit(this));
        //1.3 Recibir mensaje de Resource Manager con el recurso ganado
        tribeAccountantBehaviour.addSubBehaviour(new TribeAccountantBehaviour_ResourceCollected(this));
        // Recibe mensaje de UnitManager para crear una unidad (checkea si es posible)
        tribeAccountantBehaviour.addSubBehaviour(new TribeAccountantBehaviour_CheckNewUnit(this));
        //1.4 Recibe mensaje de BuildingManager
        tribeAccountantBehaviour.addSubBehaviour(new TribeAccountantBehaviour_Building(this));

        this.addBehaviour(tribeAccountantBehaviour);

    }


    private void parseJSON(){
        JSONParser parser = new JSONParser();
        try {
            JSONObject jsonFile = (JSONObject)parser.parse(new FileReader("src/main/java/es/upm/emse/absd/newMap.json"));
            JSONObject jsonInfo = (JSONObject)jsonFile.get("initialResources");

            Long wood = (Long) jsonInfo.get("wood");
            Long stone = (Long) jsonInfo.get("stone");
            Long gold = (Long) jsonInfo.get("gold");

            initialWood = toFloat(wood);
            initialStone = toFloat(stone);
            initialGold = toFloat(gold);

            log.info(ANSI_YELLOW + this.getLocalName() + " Initial wood: " + initialWood);
            log.info(ANSI_YELLOW + this.getLocalName() + " Initial stone: " + initialStone);
            log.info(ANSI_YELLOW + this.getLocalName() + " Initial gold: " + initialGold);

        } catch (IOException | ParseException e) {
            log.warning(ANSI_RED + getLocalName()+": error lectura JSON initial configuration");
            log.warning(e.getMessage());
        }

        try {
            JSONObject jsonFile = (JSONObject)parser.parse(new FileReader(
                    "src/main/java/es/upm/emse/absd/configPlatformData.json"));
            Long capacity = (Long)jsonFile.get("initialStorage");
            initialCapacity = toFloat(capacity);
            log.info(ANSI_YELLOW + this.getLocalName() + " Initial capacity: " + initialCapacity);

        } catch (IOException | ParseException e) {
            log.warning(ANSI_RED + getLocalName()+": error lectura JSON initial configuration");
            log.warning(e.getMessage());
        }

    }


    private Float toFloat(Long value){
        if(value!=null) { return value.floatValue();}
        log.warning(ANSI_RED + this.getLocalName() + " Valor no encontrado en JSON initial configuration");
        return 0.0f;
    }


    public void sendCurrentResources(AID tribeAID, float gold, float stone, float wood, float capacity){
        ACLMessage sendCurrentResources = newMsgWithOnto(this, tribeAID,
                ACLMessage.INFORM, codec, ontology,
                buildUpdatedResources(tribeAID, gold, stone, wood, capacity),
                WoaOntologyVocabulary.INFORM_CURRENT_RESOURCES);
        this.send(sendCurrentResources);
        log.info(ANSI_BLUE + this.getLocalName() +"Mensaje enviado a la tribu " + tribeAID.getLocalName() +
                "con sus recursos actualizados");
    }


    private InformCurrentResources buildUpdatedResources(AID tribeAID, float gold, float stone, float wood, float capacity){
        Resource resourceGold = new Resource("gold",gold);
        Resource resourceStone = new Resource("stone",stone);
        Resource resourceWood = new Resource("wood",wood);
        CurrentResources currentResources = new CurrentResources(resourceGold, resourceStone, resourceWood);
        float updatedCapacity = capacity;
        if(capacity == 0) updatedCapacity = this.getInitialCapacity();
        StorageCapacity storageCapacity = new StorageCapacity(updatedCapacity);
        return new InformCurrentResources(currentResources, storageCapacity);
    }
}