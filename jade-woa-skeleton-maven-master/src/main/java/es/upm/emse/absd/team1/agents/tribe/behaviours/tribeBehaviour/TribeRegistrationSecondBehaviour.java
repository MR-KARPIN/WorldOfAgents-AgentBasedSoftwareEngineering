package es.upm.emse.absd.team1.agents.tribe.behaviours.tribeBehaviour;

import es.upm.emse.absd.ontology.woa.actions.AssignNewUnit;
import es.upm.emse.absd.ontology.woa.actions.DistributeMap;
import es.upm.emse.absd.ontology.woa.actions.InformCurrentResources;
import es.upm.emse.absd.ontology.woa.concepts.Coordinate;
import es.upm.emse.absd.ontology.woa.concepts.NewPhase;
import es.upm.emse.absd.team1.agents.Utils;
import es.upm.emse.absd.ontology.woa.*;
import es.upm.emse.absd.team1.agents.tribe.AgTribe;
import es.upm.emse.absd.team1.agents.tribe.GameMap;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.basic.Action;
import jade.core.behaviours.ParallelBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import lombok.extern.java.Log;
import org.glassfish.pfl.basic.contain.Pair;

import java.util.LinkedList;

import static es.upm.emse.absd.team1.agents.Utils.ANSI_CYAN;
import static es.upm.emse.absd.team1.agents.Utils.ANSI_RED;

/*  Behaviour una vez se registra la tribu:

    1.1 Recibir info una vez registrado:
            1.1.1 Recursos iniciales del TribeAccountant
            1.1.2 Dimensiones del mapa del MapManager
            1.1.3 Unidades iniciales del UnitManager

    1.2 Recibir cambios de fase del PhaseManager
*/
@Log
public class TribeRegistrationSecondBehaviour extends ParallelBehaviour {

    private final Codec codec = new SLCodec();
    private final Ontology ontology = WoaOntology.getInstance();
    private final AgTribe agent;


    public TribeRegistrationSecondBehaviour(AgTribe a) {
        super();
        this.agent = a;

        //Recibe los recursos iniciales del TribeAccountant y recibe las dimensiones del mapa
        this.addSubBehaviour(new SimpleBehaviour() {
            private boolean receiveInitialResources = false;
            private boolean receiveDimensions = false;
            private int receiveInitialUnits = 0;

            @Override
            public void action() {
                if(!this.receiveInitialResources) this.receiveInitialResources = receiveInitialResources();
                if(!this.receiveDimensions) this.receiveDimensions = receiveDimensions();
                if (receiveInitialUnits<3 && receiveNewUnit()) this.receiveInitialUnits++; //y unidades
            }

            @Override
            public boolean done() {
                return this.receiveInitialResources && this.receiveDimensions && this.receiveInitialUnits==3;
            }
        });

        //Escucha y recibe cambios de fase del PhaseManager
        this.addSubBehaviour(new SimpleBehaviour() {

            @Override
            public void action() {receiveNotifyChangePhase();}

            @Override
            public boolean done() {return agent.getPhase()==1;}
        });
    }

    private boolean receiveNewUnit() {
        ACLMessage msg = this.getAgent().receive(
                MessageTemplate.and(MessageTemplate.and(MessageTemplate.MatchLanguage(codec.getName()),
                                MessageTemplate.MatchOntology(ontology.getName())),
                        MessageTemplate.MatchProtocol(WoaOntologyVocabulary.ASSIGN_NEW_UNIT))
        );
        if(msg != null) {
            log.info(ANSI_CYAN + this.getAgent().getLocalName() + " Message of new unit received from UnitManager");
            Action actionMsg = Utils.extractMsgOntoContent(this.getAgent(), msg);
            if (actionMsg != null) {
                AssignNewUnit newUnit = (AssignNewUnit) actionMsg.getAction();
                log.info(ANSI_CYAN + this.getAgent().getLocalName() + " gets the Unit AID: " + newUnit.getUnitID());
                //Envío mensaje a la unidad diciéndole que soy el padre y esperando su posición inicial
                ACLMessage soyTuPadre = Utils.newMsgWithObject(this.agent, ACLMessage.INFORM, null, newUnit.getUnitID(), "soyTuPadre");
                this.agent.send(soyTuPadre);
                ACLMessage respuesta = this.agent.blockingReceive(MessageTemplate.MatchProtocol("soyTuPadre"));
                try {
                    Coordinate posicion = (Coordinate) respuesta.getContentObject();
                    log.info(ANSI_CYAN + this.getAgent().getLocalName() +" Unit  " + newUnit.getUnitID().getLocalName() +
                            " está en la posición: " + posicion.getXValue() + " "+ posicion.getYValue());
                    agent.getPosiciones().put(newUnit.getUnitID(), posicion);
                    agent.getUnidades().add(newUnit.getUnitID());
                    agent.getInstrucciones().put(newUnit.getUnitID(), new LinkedList<>());
                } catch (UnreadableException e) { throw new RuntimeException(e); }
                return true;
            } else log.warning(ANSI_RED + this.getAgent().getLocalName() + " Error obtaining the content of the unit aid");
        }
        return false;
    }


    //Recibe mensaje de tribe accountant con los recursos iniciales de la tribu.
    private boolean receiveInitialResources() {
        boolean received = false;
        ACLMessage msg = this.getAgent().receive(
                MessageTemplate.and(MessageTemplate.and(MessageTemplate.MatchLanguage(codec.getName()),
                                MessageTemplate.MatchOntology(ontology.getName())),
                        MessageTemplate.MatchProtocol(WoaOntologyVocabulary.INFORM_CURRENT_RESOURCES))
        );
        if(msg != null) {
            log.info(ANSI_CYAN + this.getAgent().getLocalName() + " Message received from TribeAccountant");
            Action actionMsg = Utils.extractMsgOntoContent(this.getAgent(), msg);
            if (actionMsg != null) {
                InformCurrentResources initialResources = (InformCurrentResources) actionMsg.getAction();
                float gold= initialResources.getTribeResources().getGold().getAmount();
                float wood= initialResources.getTribeResources().getWood().getAmount() ;
                float stone=initialResources.getTribeResources().getStone().getAmount() ;
                float storage= initialResources.getStorage().getSize();
                log.info(ANSI_CYAN + this.getAgent().getLocalName() +
                        " With this initial resources I will conquer my enemies: " +
                        " Gold: " + gold +
                        " Wood: " + wood +
                        " Stone: " + stone +
                        " Storage: " + storage
                );
                this.agent.setGold(gold);
                this.agent.setStone(stone);
                this.agent.setWood(wood);
                this.agent.setStorage(storage);
                received = true;
            } else
                log.warning(ANSI_RED + this.getAgent().getLocalName() + " Error obtaining the content of the initial resources");
        }
        return received;
    }


    //Escucha mensaje del MapManager para recibir las dimensiones del mapa
    private boolean receiveDimensions(){
        boolean received=false;
        ACLMessage msg = this.getAgent().receive(
                MessageTemplate.and(MessageTemplate.and(MessageTemplate.MatchLanguage(codec.getName()),
                                MessageTemplate.MatchOntology(ontology.getName())),
                        MessageTemplate.MatchProtocol(WoaOntologyVocabulary.DISTRIBUTE_MAP))
        );
        if(msg != null) {
            log.info(ANSI_CYAN + this.getAgent().getLocalName() + " Message received from MapManager");
            Action actionMsg = Utils.extractMsgOntoContent(this.getAgent(), msg);
            if (actionMsg != null) {
                DistributeMap mapDimensions = (DistributeMap) actionMsg.getAction();
                int width =  mapDimensions.getMapSize().getXValue();
                int height = mapDimensions.getMapSize().getYValue();
                this.agent.setMapDimensions(new Pair<>(width, height));
                this.agent.setMapa(new GameMap(width, height));
                log.info(ANSI_CYAN + this.getAgent().getLocalName() +
                        " GameMap dimensions received: width "+ width+" height "+ height);
                received = true;
            } else
                log.warning(ANSI_RED + this.getAgent().getLocalName() + " Error obtaining the content of the initial resources");
        }
        return received;
    }


    //Escucha al PhaseManager para cambiar de fases.
    private void receiveNotifyChangePhase() {
        ACLMessage msg = this.getAgent().receive(MessageTemplate.and(MessageTemplate.and(
                        MessageTemplate.MatchLanguage(codec.getName()),
                        MessageTemplate.MatchOntology(ontology.getName())),
                            MessageTemplate.MatchProtocol(WoaOntologyVocabulary.CHANGE_PHASE)));
        if (msg != null) {
            log.info(ANSI_CYAN + this.getAgent().getLocalName() + " Message received of changing phase");
            Action actionMsg = Utils.extractMsgOntoContent(this.getAgent(), msg);
            if (actionMsg != null) {
                NewPhase nuevaFase = (NewPhase) actionMsg.getAction();
                log.info(ANSI_CYAN + this.getAgent().getLocalName() + " We have moved to phase: " + nuevaFase.getPhase());
                this.agent.setPhase(nuevaFase.getPhase());
            }
        }
    }
}

