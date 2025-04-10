package es.upm.emse.absd.team1.agents.platform.BuildingManager;

import es.upm.emse.absd.ontology.woa.WoaOntology;
import es.upm.emse.absd.ontology.woa.WoaOntologyVocabulary;
import es.upm.emse.absd.ontology.woa.concepts.Building;
import es.upm.emse.absd.ontology.woa.concepts.NewPhase;
import es.upm.emse.absd.team1.agents.Utils;
import es.upm.emse.absd.team1.agents.platform.RegistrationManager.RegistrationInitialBehaviour;
import es.upm.emse.absd.team1.agents.platform.RegistrationManager.RegistrationSecondBehaviour;
import es.upm.emse.absd.team1.agents.platform.ResourceManager.ResourceManagerBehaviour;
import es.upm.emse.absd.team1.agents.platform.UnitManager.GameBehaviours.UnitManagerGameBehaviour_BehaviourManager;
import es.upm.emse.absd.team1.agents.platform.UnitManager.RegistrationBehaviours.UnitManagerRegistrationBehaviour1;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.basic.Action;
import jade.core.Agent;

import jade.core.behaviours.ParallelBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import lombok.Data;
import lombok.Setter;
import lombok.extern.java.Log;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;

import static es.upm.emse.absd.team1.agents.Utils.*;

@Log
@Data
public class AgBuildingManager extends Agent {
    private final Codec codec = new SLCodec();
    private final Ontology ontology = WoaOntology.getInstance();
    public final String BUILDINGMANAGER = "BuildingManager";
    private int phase = 0;
    public long mapWidth;
    public long mapHeight;
    private BuildingManagerBehaviour buildingManagerBehaviour;

    protected void setup() {
        log.info(ANSI_YELLOW + this.getLocalName() + " Se crea agente BuildingManager");

        // Register of the codec and the ontology to be used
        getContentManager().registerLanguage(codec);
        getContentManager().registerOntology(ontology);

        //Registra servicio en DFS
        Utils.register(this, BUILDINGMANAGER);
        log.info(ANSI_YELLOW + this.getLocalName() + " BuildingManager is added to the DFService");

        //Suscripción PhaseManager
        suscribePhaseManager(this, codec, ontology);
        JSONParser parser = new JSONParser();
        try {
            JSONObject jsonObject = (JSONObject) parser.parse(new FileReader("src/main/java/es/upm/emse/absd/newMap.json"));
            //Obtención dimensiones del mapa
            this.mapWidth = (Long) jsonObject.get("mapWidth");
            this.mapHeight = (Long) jsonObject.get("mapHeight");
        }catch (IOException | ParseException e) {
            log.warning(ANSI_RED + getLocalName()+": error lectura JSON newMap.json");
            log.warning(e.getMessage());
        }
        /*Comportamiento del BuildingManager
            1. Esperar peticiones del UnitManager sobre construir un edificio.
            2. Comprobar si se puede construir el edificio o no.
                2.1. Preguntar al MapManager sobre el contenido de la celda en la que
                   se quiere construir el edificio y las celdas contiguas.
                2.2. Preguntar al tribeAccountant si hay suficientes recursos.
        3. Contar tiempo de construcción del edificio y cuando termine:
                3.1. Informar al UnitManager para que pueda desbloquear la unidad.
                3.2. Informar al tribeAccountant para actualizar el número de edificios de la tribu.
                3.3. Informar al MapManager para que pueda enseñarlo en el mapa.
         */

        /*
            Tiene un ParallelBehaviour:
                1. Esperar peticiones y comprobar si se puede construir el edificio o no.
                2. Contar tiempo de construcción del edificio con wakers
                3. Escuchar cambio de fase
        */

        /*ParallelBehaviour parallelBehaviour=new ParallelBehaviour();
        parallelBehaviour.addSubBehaviour(new BuildingManagerBehaviour(this));
        parallelBehaviour.addSubBehaviour(cambioFase);
        addBehaviour(parallelBehaviour);*/

        //buildingManagerBehaviour = new BuildingManagerBehaviour(this);
        //addBehaviour(buildingManagerBehaviour);
        //buildingManagerBehaviour.addSubBehaviour(cambioFase);

        addBehaviour(new BuildingManagerBehaviour(this));

    }

}