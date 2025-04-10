package es.upm.emse.absd.team1.agents.platform.ResourceManager;

import es.upm.emse.absd.ontology.woa.WoaOntology;
import es.upm.emse.absd.team1.agents.Utils;
import es.upm.emse.absd.team1.agents.platform.GameMap;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.core.AID;
import jade.core.Agent;
import lombok.Data;
import lombok.extern.java.Log;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.io.FileReader;
import java.io.IOException;
import static es.upm.emse.absd.team1.agents.Utils.*;

@Log
@Data
public class AgResourceManager extends Agent {
    private final Codec codec = new SLCodec();
    private final Ontology ontology = WoaOntology.getInstance();
    private final String RESOURCEMANAGER = "ResourceManager";
    private GameMap resourceGameMap;
    private GameMap amountGameMap;
    private GameMap percentageOreMap;
    private AID mapManager;
    private AID tribeAccountant;

    @Override
    protected void setup() {
        // Register of the codec and the ontology to be used in the ContentManager
        getContentManager().registerLanguage(codec);
        getContentManager().registerOntology(ontology);

        log.info(ANSI_YELLOW + this.getLocalName() + " Se crea agente ResourceManager");

        //Registra servicio en DFS
        Utils.register(this, RESOURCEMANAGER);
        log.info(ANSI_YELLOW + this.getLocalName() + "ResourceManager is added to the DFService");
        this.mapManager= Utils.untilFindDFS(this, "MapManager");
        this.tribeAccountant= Utils.untilFindDFS(this, "TribeAccountant");
        readInitialTilesResources();
        Utils.suscribePhaseManager(this, codec, ontology);

        //BEHAVIOUR DEL RESOURCE MANAGER:
        //  Lanza el behaviour ResourceManagerBehaviour (paralelo)
        addBehaviour(new ResourceManagerBehaviour(this));
    }

    private void readInitialTilesResources(){
        JSONParser parser = new JSONParser();
        try {
            JSONObject jsonObject = (JSONObject)parser.parse(new FileReader("src/main/java/es/upm/emse/absd/newMap.json"));
            this.resourceGameMap = new GameMap(Math.toIntExact((Long) jsonObject.get("mapWidth")),
                                       Math.toIntExact((Long) jsonObject.get("mapHeight")));
            this.amountGameMap = new GameMap(Math.toIntExact((Long) jsonObject.get("mapWidth")),
                                     Math.toIntExact((Long) jsonObject.get("mapHeight")));
            this.percentageOreMap = new GameMap(Math.toIntExact((Long) jsonObject.get("mapWidth")),
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

                if (!resourceCode.equals("Ground")) {
                    int amount = Math.toIntExact((Long) jsonTile.get("resource_amount"));
                    amountGameMap.setValue( xCoord, yCoord, Integer.toString(amount), false);
                    if(resourceCode.equals("Ore")){
                        int percentage = Math.toIntExact((Long) jsonTile.get("gold_percentage"));
                        percentageOreMap.setValue( xCoord, yCoord, Integer.toString(percentage), false);
                    }
                }

                log.info(ANSI_YELLOW + this.getLocalName() + " Recurso inicial: (" + xCoord +","+ yCoord +")->"+ resourceCode);
            }
        } catch (IOException | ParseException e) {
            log.warning(ANSI_RED + getLocalName()+": error lectura JSON newMap.json");
            log.warning(e.getMessage());
        }
    }



}
