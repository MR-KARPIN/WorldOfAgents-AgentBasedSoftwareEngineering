package es.upm.emse.absd.team1.agents.tribe;

import static es.upm.emse.absd.team1.agents.Utils.ANSI_CYAN;

import es.upm.emse.absd.ontology.woa.*;
import es.upm.emse.absd.ontology.woa.concepts.Coordinate;
import es.upm.emse.absd.team1.agents.tribe.behaviours.tribeBehaviour.TribeRegistrationFirstBehaviour;
import es.upm.emse.absd.team1.agents.tribe.behaviours.tribeBehaviour.TribeRegistrationSecondBehaviour;
import es.upm.emse.absd.team1.agents.tribe.behaviours.tribeBehaviour.CerebroBehaviour;
import es.upm.emse.absd.team1.agents.tribe.instructions.Instruction;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.SequentialBehaviour;
import lombok.Data;
import lombok.extern.java.Log;
import org.glassfish.pfl.basic.contain.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Queue;


@Log
@Data
public class AgTribe extends Agent {

    private final Codec codec = new SLCodec();
    private final Ontology ontology = WoaOntology.getInstance();

    private int phase = 0;
    private Pair<Integer, Integer> mapDimensions;
    private GameMap mapa;
    private ArrayList<AID> unidades = new ArrayList<AID>();
    private HashMap<AID, Coordinate> posiciones = new HashMap<>();
    private HashMap<AID, Queue<Instruction>> instrucciones = new HashMap<>();
    private float wood;
    private float gold;
    private float stone;
    private float storage;

    protected void setup()
    {
        log.info(ANSI_CYAN + this.getLocalName() + " Now I am become "+this.getLocalName()+", the destroyer of tribes.");
        // Register of the codec and the ontology to be used in the ContentManager
        getContentManager().registerLanguage(codec);
        getContentManager().registerOntology(ontology);

        SequentialBehaviour tribeControllerBehaviour = new SequentialBehaviour(this);
        //Protocolo Registro y suscripción al PhaseManager
        tribeControllerBehaviour.addSubBehaviour(new TribeRegistrationFirstBehaviour(this));
        tribeControllerBehaviour.addSubBehaviour(new TribeRegistrationSecondBehaviour(this));
        tribeControllerBehaviour.addSubBehaviour(new CerebroBehaviour(this));

        addBehaviour(tribeControllerBehaviour);

    }

}