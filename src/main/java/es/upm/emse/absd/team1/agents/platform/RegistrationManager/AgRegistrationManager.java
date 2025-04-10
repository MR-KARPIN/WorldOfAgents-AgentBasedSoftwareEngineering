package es.upm.emse.absd.team1.agents.platform.RegistrationManager;

import static es.upm.emse.absd.team1.agents.Utils.*;
import es.upm.emse.absd.ontology.woa.WoaOntology;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.SequentialBehaviour;
import lombok.Data;
import lombok.extern.java.Log;

import java.util.HashMap;

@Log
@Data
public class AgRegistrationManager extends Agent {
    private final Codec codec = new SLCodec();
    private final Ontology ontology = WoaOntology.getInstance();
    private AID phaseManager;
    private AID tribeAccountant;
    private AID UnitManager;
    private AID MapManager;
    private int phase=0;
    private  HashMap<AID, String> registeredTribes = new HashMap<>();



    protected void setup() {
        log.info(ANSI_YELLOW + this.getLocalName() + " Se crea agente RegistrationManager");

        // Register of the codec and the ontology to be used in the ContentManager
        getContentManager().registerLanguage(codec);
        getContentManager().registerOntology(ontology);

        SequentialBehaviour registrationBehaviour = new SequentialBehaviour(this);
        registrationBehaviour.addSubBehaviour(new RegistrationInitialBehaviour(this));
        registrationBehaviour.addSubBehaviour(new RegistrationSecondBehaviour(this));
        addBehaviour(registrationBehaviour);
    }

}
