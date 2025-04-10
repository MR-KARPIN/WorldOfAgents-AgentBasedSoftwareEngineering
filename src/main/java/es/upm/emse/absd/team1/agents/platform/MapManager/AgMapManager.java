package es.upm.emse.absd.team1.agents.platform.MapManager;

import es.upm.emse.absd.team1.agents.platform.Unidad;
import es.upm.emse.absd.ontology.woa.WoaOntology;
import es.upm.emse.absd.ontology.woa.concepts.Coordinate;
import es.upm.emse.absd.team1.agents.Utils;
import es.upm.emse.absd.team1.agents.platform.GameMap;
import es.upm.emse.absd.team1.agents.platform.MapManager.GameBehaviours.MapManagerGameBehaviour2;
import es.upm.emse.absd.team1.agents.platform.MapManager.GameBehaviours.MapManagerGameBehaviour_ReceiveNewUnit;
import es.upm.emse.absd.team1.agents.platform.MapManager.RegistrationBehaviours.MapManagerRegistrationBehaviour1;
import es.upm.emse.absd.team1.agents.platform.UnitManager.AgUnitManager;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.ParallelBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.lang.acl.ACLMessage;
import lombok.Data;
import lombok.extern.java.Log;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static es.upm.emse.absd.team1.agents.Utils.*;

@Log
@Data
public class AgMapManager extends Agent {

    //Puede que sea necesaria una estructura de datos que represente el mapa entero

    public final  String MAPMANAGER = "MapManager";
    private long mapWidth;
    private long mapHeight;
    private int phase=0;
    private GameMap resourceGameMap;


    private ArrayList<Coordinate> positions = new ArrayList<>();
    private Map<String, ArrayList<Unidad>> mapaUnidades = new HashMap<>();

    private final Codec codec = new SLCodec();
    private final Ontology ontology = WoaOntology.getInstance();

    protected void setup() {
        /*
        0.  Registra servicio en DFS.
	        Leer configuration file.
	        Enviar al UnitManager las posiciones iniciales
         */

        log.info(ANSI_YELLOW + this.getLocalName() + " Se crea agente MapManager");

        Utils.register(this, MAPMANAGER);
        log.info(ANSI_YELLOW + this.getLocalName() + " registra servicio MapManager en DFS ");

        // Register of the codec and the ontology to be used in the ContentManager
        getContentManager().registerLanguage(codec);
        getContentManager().registerOntology(ontology);

        Utils.suscribePhaseManager(this, codec, ontology);
        readDimensionsInitialPositions();
        sendInitialPositions();
        readInitialTilesResources();

        /*
        BEHAVIOUR MAPMANAGER:
            Paralelo:
                1.  Recibir nueva unidad creada (recibe las unidades iniciales de las tribus) (cíclicamente)

                2. Secuencial:
                    2.1. Behaviour paralelo que se encarga de las funciones en la fase de registro,
                        2.1.1 Recibir nueva tribu registrada (cíclicamente).
                        2.1.2 Escuchar cambio de fase (cíclicamente).
                        Este behaviour acaba al terminar la fase de registro.

                    2.2. Behaviour paralelo que se encarga de las funciones de la fase de juego:
                        2.2.1 Mover unidades
                        2.2.2 Eliminar recurso explotado agotado
                        2.2.3 Escuchar cambio de fase
                        2.2.4 (Sprint 4) construir edificio
                       Este behaviour acaba al terminar la fase de juego.
         */

        SequentialBehaviour mapManagerNormalBehaviour = new SequentialBehaviour();
        mapManagerNormalBehaviour.addSubBehaviour(new MapManagerRegistrationBehaviour1(this));
        mapManagerNormalBehaviour.addSubBehaviour(new MapManagerGameBehaviour2(this));

        ParallelBehaviour mapManagerGeneralBehaviour = new ParallelBehaviour();
        mapManagerGeneralBehaviour.addSubBehaviour(new MapManagerGameBehaviour_ReceiveNewUnit(this));
        mapManagerGeneralBehaviour.addSubBehaviour(mapManagerNormalBehaviour);
        this.addBehaviour(mapManagerGeneralBehaviour);


    }

    private void readDimensionsInitialPositions(){
        JSONParser parser = new JSONParser();
        try {
            JSONObject jsonObject = (JSONObject)parser.parse(new FileReader("src/main/java/es/upm/emse/absd/newMap.json"));
            //Obtención dimensiones del mapa
            this.mapWidth = (Long)jsonObject.get("mapWidth");
            this.mapHeight = (Long)jsonObject.get("mapHeight");

            //Log de las dimensiones del mapa leidas
            log.info(ANSI_YELLOW + this.getLocalName() + " Dimensiones del mapa: " + this.mapWidth + " x "+this.mapHeight);

            //Obtención initial positions
            JSONArray jsonIntialPositions = (JSONArray)jsonObject.get("initialPositions");
            JSONObject jsonInitialPos;
            for (Object jsonIntialPosition : jsonIntialPositions) {
                jsonInitialPos = (JSONObject) jsonIntialPosition;
                int x = Integer.parseInt(jsonInitialPos.get("x").toString());
                int y = Integer.parseInt(jsonInitialPos.get("y").toString());
                Coordinate position = new Coordinate(x, y);
                this.positions.add(position);
            }

            //Log de las posiciones iniciales leidas
            for (Coordinate position : this.positions) {
                log.info(ANSI_YELLOW + this.getLocalName() + " Posición inicial leida: (" +
                        position.getXValue() + "," + position.getYValue() + ")");
            }
        } catch (IOException | ParseException e) {
            log.warning(ANSI_RED + getLocalName()+": error lectura JSON newMap.json");
            log.warning(e.getMessage());
        }
    }


    private void sendInitialPositions(){
        AID unitManager = Utils.untilFindDFS(this, AgUnitManager.UNITMANAGER);
        ACLMessage newTribeAccMsg = newMsgWithObject(this, ACLMessage.INFORM,
                this.positions, unitManager, "PosicionesIniciales");
        log.info(ANSI_YELLOW + this.getLocalName() +
                " Mensaje enviado a UnitManager con las posiciones iniciales");
        this.send(newTribeAccMsg);
    }

    private void readInitialTilesResources(){
        JSONParser parser = new JSONParser();
        try {
            JSONObject jsonObject = (JSONObject)parser.parse(new FileReader("src/main/java/es/upm/emse/absd/newMap.json"));
            this.resourceGameMap = new GameMap(Math.toIntExact((Long) jsonObject.get("mapWidth")),
                    Math.toIntExact((Long) jsonObject.get("mapHeight")));

            //Obtención recursos de cada tile para guardarlo
            JSONArray jsonTileList = (JSONArray) jsonObject.get("tiles");
            JSONObject jsonTile;
            for (Object o : jsonTileList) {
                jsonTile = (JSONObject) o;
                int xCoord = Math.toIntExact((Long) jsonTile.get("x"));
                int yCoord = Math.toIntExact((Long) jsonTile.get("y"));
                String resourceCode = (String) jsonTile.get("resource");
                resourceGameMap.setValue( xCoord, yCoord, resourceCode, true);

                log.info(ANSI_YELLOW + this.getLocalName() + " Recurso inicial: (" + xCoord +","+ yCoord +")->"+ resourceCode);
            }
        } catch (IOException | ParseException e) {
            log.warning(ANSI_RED + getLocalName()+": error lectura JSON newMap.json");
            log.warning(e.getMessage());
        }
    }

}

