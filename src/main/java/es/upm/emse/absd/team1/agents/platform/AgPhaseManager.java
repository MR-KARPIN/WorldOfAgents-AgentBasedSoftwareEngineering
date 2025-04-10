package es.upm.emse.absd.team1.agents.platform;

import es.upm.emse.absd.GUIUtils;
import es.upm.emse.absd.ontology.woa.WoaOntologyVocabulary;
import es.upm.emse.absd.team1.agents.Utils;
import es.upm.emse.absd.ontology.woa.concepts.NewPhase;
import es.upm.emse.absd.ontology.woa.WoaOntology;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import lombok.Data;
import lombok.extern.java.Log;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import static es.upm.emse.absd.team1.agents.Utils.*;

@Log
@Data
public class AgPhaseManager extends Agent {

    public final static String PHASEMANAGER = "PhaseManager";
    private final Codec codec = new SLCodec();
    private final Ontology ontology = WoaOntology.getInstance();
    private int phase = 0; //not allow subscription in scoring phase
    private long duration_registration;
    private long duration_match ;
    private ArrayList<AID> interestedAgents = new ArrayList<>();
    private ArrayList<String> tribes = new ArrayList<>();
    private JSONObject mapInit = new JSONObject();

    protected void setup() {
        log.info(ANSI_YELLOW + this.getLocalName() + " Se crea PhaseManager");

        getContentManager().registerLanguage(codec);
        getContentManager().registerOntology(ontology);

        //Registra servicio en DFS
        Utils.register(this, PHASEMANAGER);
        log.info(ANSI_YELLOW + this.getLocalName() + "PhaseManager is added to the DFService");
        //Lee configuración inicial
        parseJSON();


        //El behaviour del PhaseManager va a ser parallel para hacer las siguientes tareas a la vez:
        //      1. Contar duración de la fase de registro y la fase de juego
        //      2. Recibir y contestar mensajes de suscripción.

        ParallelBehaviour phaseManagerBehaviour = new ParallelBehaviour(this, 0);

        SequentialBehaviour seq = new SequentialBehaviour(this);
        seq.addSubBehaviour(new WakerBehaviour(this, duration_registration) {
            @Override
            protected void onWake() {
                super.onWake();
                //1.3.3.b. As the phase manager, I want to inform the GUI about the start of the game phase
                //so that the map is initialized
                phase=1;
                log.info(ANSI_PURPLE + "EL JUGO HA COMENZADO, SE INICIA EL MAPA");
                GUIUtils.GameStart(tribes);
                //Envía mensaje de cambio de fase
                log.info(ANSI_YELLOW + this.getAgent().getLocalName() + " Fase de registro terminada");
                //Notificar a agentes interesados de que la fase de registro ha terminado
                NewPhase newPhase = new NewPhase();
                newPhase.setPhase(1);
                for (AID agent : interestedAgents) {
                    ACLMessage newPhaseMsg = Utils.newMsgWithOnto(this.getAgent(), agent, ACLMessage.INFORM,
                            codec, ontology, newPhase, WoaOntologyVocabulary.CHANGE_PHASE);
                    send(newPhaseMsg);
                    log.info(ANSI_YELLOW + this.getAgent().getLocalName() +" Mensaje de notificación de fin de fase enviado a "
                            + agent.getLocalName());
                }
            }
        });

        seq.addSubBehaviour(new WakerBehaviour(this, duration_match) {
            @Override
            protected void onWake() {
                super.onWake();
                log.info(ANSI_YELLOW + this.getAgent().getLocalName() + " Fase de juego terminada");

                //Notificar a agentes interesados de que la fase de juego ha terminado
                NewPhase newPhase = new NewPhase();
                newPhase.setPhase(2);
                for (AID agent : interestedAgents) {
                    ACLMessage newPhaseMsg = Utils.newMsgWithOnto(this.getAgent(), agent, ACLMessage.INFORM,
                            codec, ontology, newPhase, WoaOntologyVocabulary.CHANGE_PHASE);
                    send(newPhaseMsg);

                    log.info(ANSI_YELLOW + this.getAgent().getLocalName() +" Mensaje de notificación de fin de fase enviado a "
                            + agent.getLocalName());
                }
                //1.3.5.b. As the phase manager, I want to inform the GUI about the end of the game phase
                //so that it can publish the final results
                phase=2;
                log.info(ANSI_PURPLE + "EL JUGO HA TERMINADO");
                GUIUtils.GameEnd();
            }
        });

        phaseManagerBehaviour.addSubBehaviour(seq);

        phaseManagerBehaviour.addSubBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() {
                // Receives  subscription requests
                ACLMessage msg = this.getAgent().receive(MessageTemplate.and(MessageTemplate.and(
                                MessageTemplate.MatchLanguage(codec.getName()), MessageTemplate.MatchOntology(ontology.getName())),
                        MessageTemplate.MatchProtocol(WoaOntologyVocabulary.CHANGE_PHASE)));
                if(msg != null) phaseManagerLogic(msg);
            }

            private void phaseManagerLogic(ACLMessage msg) {
                log.info(ANSI_YELLOW + this.getAgent().getLocalName() + " Mensaje recibido");
                if (msg.getPerformative() == ACLMessage.SUBSCRIBE) {
                    log.info(ANSI_YELLOW + this.getAgent().getLocalName() + " Petición de subscripcion de " + msg.getSender().getLocalName());
                    ACLMessage response = msg.createReply();
                    if (phase < 2) { //enviar mensaje con agree
                        //1. enviar agree y añadirlo a lista de agentes interesados
                        response.setPerformative(ACLMessage.AGREE);
                        log.info(ANSI_YELLOW + this.getAgent().getLocalName() + "Respuesta AGREE enviada");
                        interestedAgents.add(msg.getSender());
                        if (msg.getSender().getLocalName().startsWith("Team"))
                            tribes.add(msg.getSender().getLocalName());

                    } else { //enviar mensaje refuse
                        response.setPerformative(ACLMessage.REFUSE);
                        log.info(ANSI_YELLOW + this.getAgent().getLocalName() + "Respuesta REFUSE enviada");
                    }
                    this.getAgent().send(response);
                } else {
                    ACLMessage response = msg.createReply();
                    System.out.println(this.getAgent().getLocalName() + ": Problemas");
                    response.setPerformative(ACLMessage.NOT_UNDERSTOOD);
                    this.getAgent().send(response);
                    log.info(ANSI_YELLOW + this.getAgent().getLocalName() + " Respuesta NOT_UNDERSTOOD enviada");
                }
            }
        });
        addBehaviour(phaseManagerBehaviour);
    }


    private void parseJSON(){
        JSONParser parser = new JSONParser();
        try {
            JSONObject jsonFile = (JSONObject)parser.parse(new FileReader("src/main/java/es/upm/emse/absd/configPlatformData.json"));
            JSONObject jsonInfo = (JSONObject)jsonFile.get("durationPhases");

            this.duration_registration = (Long) jsonInfo.get("duration_registration");
            this.duration_match =  (Long) jsonInfo.get("duration_match");

            log.info(ANSI_YELLOW + this.getLocalName() + " Duration of the registration phase: " + duration_registration);
            log.info(ANSI_YELLOW + this.getLocalName() + " Duration of the match phase: " + duration_match);

            //mapInit.put("map", jsonFile);

        } catch (IOException | ParseException e) {
            log.warning(ANSI_RED + getLocalName()+": error lectura JSON initial configuration");
            log.warning(e.getMessage());
        }
    }

}
