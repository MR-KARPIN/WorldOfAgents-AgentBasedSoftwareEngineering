package es.upm.emse.absd.team1.agents.platform.MapManager.RegistrationBehaviours;

import es.upm.emse.absd.GUIUtils;
import es.upm.emse.absd.team1.agents.platform.Unidad;
import es.upm.emse.absd.ontology.woa.WoaOntology;
import es.upm.emse.absd.ontology.woa.WoaOntologyVocabulary;
import es.upm.emse.absd.ontology.woa.actions.DistributeMap;
import es.upm.emse.absd.ontology.woa.concepts.Coordinate;
import es.upm.emse.absd.ontology.woa.concepts.NewPhase;
import es.upm.emse.absd.team1.agents.platform.MapManager.AgMapManager;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.behaviours.ParallelBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import lombok.extern.java.Log;
import java.util.ArrayList;
import java.util.Map;

import static es.upm.emse.absd.team1.agents.Utils.*;


/*
    Behaviour paralelo que se encarga de las funciones en la fase de registro,
       1.1 Recibir nueva tribu registrada.
       1.2 Escuchar cambio de fase. (Al cambiar de fase se llama a la GUI para pintar las unidades creadas)
       1.3 Recibir nueva unidad creada (recibe las unidades iniciales de las tribus)
       Este behaviour termina terminar la fase de registro.
 */
@Log
public class MapManagerRegistrationBehaviour1 extends ParallelBehaviour {

    private final Codec codec = new SLCodec();
    private final Ontology ontology = WoaOntology.getInstance();
    private AgMapManager agente;


    public MapManagerRegistrationBehaviour1(AgMapManager agente) {
        this.agente = agente;
        this.addSubBehaviour(nuevaTribuRegistrada);
        this.addSubBehaviour(cambioFase);
    }

    SimpleBehaviour nuevaTribuRegistrada = new SimpleBehaviour() {
        @Override
        public void action() {
            AID nuevaTribu = receiveNewTribe();
            if (nuevaTribu != null) distributeMap(nuevaTribu);
        }

        @Override
        public boolean done() {
            return agente.getPhase() > 0;
        }
    };

    SimpleBehaviour cambioFase = new SimpleBehaviour() {
        @Override
        public void action() {
            receiveNotifyChangePhase();
        }

        @Override
        public boolean done() {
            return agente.getPhase() > 0;
        }
    };


    //Recibe mensaje del registration manager con el nombre de una nueva tribu registrada en el sistema.
    private AID receiveNewTribe() {
        AID nuevaTribu = null;
        ACLMessage msg = this.getAgent().receive(MessageTemplate.MatchProtocol("nuevaTribu"));
        if (msg != null) {
            log.info(ANSI_YELLOW + this.getAgent().getLocalName() + " Recibe mensaje");
            Object[] newTribeInfo;
            try {
                newTribeInfo = (Object[]) msg.getContentObject();
            } catch (UnreadableException e) {
                throw new RuntimeException(e);
            }
            log.info(ANSI_YELLOW + this.getAgent().getLocalName() + " New tribe name: " + newTribeInfo[1]);

            nuevaTribu = (AID) newTribeInfo[0];
        }
        return nuevaTribu;
    }


    //Envia mensaje a tribeController con las dimensiones del mapa.
    private void distributeMap(AID tribeController) {
        Coordinate dimensions = new Coordinate();
        dimensions.setXValue((int) this.agente.getMapWidth());
        dimensions.setYValue((int) this.agente.getMapHeight());
        DistributeMap distributeDimensions = new DistributeMap();
        distributeDimensions.setMapSize(dimensions);
        ACLMessage msg = newMsgWithOnto(this.getAgent(), tribeController, ACLMessage.INFORM,
                this.codec, this.ontology, distributeDimensions, WoaOntologyVocabulary.DISTRIBUTE_MAP);
        this.agente.send(msg);
    }


    private void receiveNotifyChangePhase() {
        ACLMessage msg = this.getAgent().receive(MessageTemplate.and(MessageTemplate.and(
                        MessageTemplate.MatchLanguage(codec.getName()), MessageTemplate.MatchOntology(ontology.getName())),
                MessageTemplate.MatchProtocol(WoaOntologyVocabulary.CHANGE_PHASE)));
        if (msg != null) {
            log.info(ANSI_YELLOW + this.getAgent().getLocalName() + " Recibe mensaje de cambio de fase");
            Action actionMsg = extractMsgOntoContent(this.getAgent(), msg);
            NewPhase nuevaFase;
            if (actionMsg != null) {
                nuevaFase = (NewPhase) actionMsg.getAction();
                this.agente.setPhase(nuevaFase.getPhase());
                log.info(ANSI_YELLOW + this.getAgent().getLocalName() + " Cambio a fase: " + this.agente.getPhase());
                if (this.agente.getPhase() == 1) {
                    MakeWaitingCalls();
                }
            }
        }
    }

    //Llama al GUI para pintar las 3 unidades iniciales de una tribu registrada
    private void MakeWaitingCalls() {
        for (Map.Entry<String, ArrayList<Unidad>> entry : this.agente.getMapaUnidades().entrySet()) {
            String tribu = entry.getKey();
            ArrayList<Unidad> unidades = entry.getValue();
            // Iterar sobre la lista de unidades
            for (Unidad unidad : unidades) {
                String nombreUnidad = unidad.getUnidad().getLocalName();
                int x = unidad.getPosicion().getX();
                int y = unidad.getPosicion().getY();
                log.info(ANSI_PURPLE + this.getAgent().getLocalName() + " Pinto unidad en: " + x + " " + y);
                GUIUtils.newUnit(tribu, nombreUnidad, x, y);
            }
        }
    }
}