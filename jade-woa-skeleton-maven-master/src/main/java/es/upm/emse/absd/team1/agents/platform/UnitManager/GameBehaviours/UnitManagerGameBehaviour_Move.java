package es.upm.emse.absd.team1.agents.platform.UnitManager.GameBehaviours;

import es.upm.emse.absd.ontology.woa.WoaOntology;
import es.upm.emse.absd.ontology.woa.WoaOntologyVocabulary;
import es.upm.emse.absd.ontology.woa.actions.Move;
import es.upm.emse.absd.ontology.woa.concepts.Coordinate;
import es.upm.emse.absd.ontology.woa.concepts.Destination;
import es.upm.emse.absd.team1.agents.Utils;
import es.upm.emse.absd.team1.agents.platform.UnitInfo;
import es.upm.emse.absd.team1.agents.platform.UnitManager.AgUnitManager;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.ParallelBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import lombok.extern.java.Log;

import java.util.ArrayList;

import static es.upm.emse.absd.team1.agents.Utils.*;

@Log
public class UnitManagerGameBehaviour_Move extends ParallelBehaviour {
    private final AgUnitManager agente;
    private final Codec codec = new SLCodec();
    private final Ontology ontology = WoaOntology.getInstance();
    private AID MapManagerAID;
    private final String MAPMANAGER = "MapManager";
    private final double DURATION_MOVEMENT = 6000 * secondsPerHour;

    /*ESTE BEHAVIOUR ES UN PARALLEL BEHAVIOUR QUE TIENE COMO SUBBEHAVIOURS:
      1. Un cyclic behaviour para controlar las peticiones de movimiento del UnitManager
      2. Wakers behaviours que se van creando para controlar el tiempo de bloqueo para moverse de las unidades
     */

    public UnitManagerGameBehaviour_Move(AgUnitManager a) {
        super();
        this.agente=a;

        this.addSubBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                //Recibe mensaje de unitcontroller de solicitud de moverse
                ACLMessage msg = this.getAgent().receive(
                        MessageTemplate.and(MessageTemplate.and(MessageTemplate.MatchLanguage(codec.getName()),
                                        MessageTemplate.MatchOntology(ontology.getName())),
                                MessageTemplate.MatchProtocol(WoaOntologyVocabulary.MOVE)));
                if(msg != null) {
                    log.info(ANSI_YELLOW + this.getAgent().getLocalName() + " recibo mensaje de "+ msg.getSender().getLocalName());
                    Action actionMsg = Utils.extractMsgOntoContent(this.getAgent(), msg);
                    if (actionMsg != null) {
                        Move movement = (Move) actionMsg.getAction();
                        log.info(ANSI_YELLOW + this.getAgent().getLocalName() +
                                " con esto la unidad "+ msg.getSender().getLocalName() +
                                " va en la dirección: " + movement.getDest().getDir());

                        movementLogic(agente.findUnitInfo(msg.getSender()),movement.getDest().getDir(), msg);
                    } else
                        log.warning(ANSI_RED + this.getAgent().getLocalName() + " Error in the direction of the unit");
                }
            }
        });
    }


    private void movementLogic(UnitInfo unitInfo, int direction, ACLMessage msg) {
        // 3.2.2. As the unit manager I want to check the availability of a unit so that it can be moved
        // Comprobar si está bloqueado
        AID unit = unitInfo.getUnitAID();
        ACLMessage response = msg.createReply();
        if (!agente.isBlocked(unit)) { //mandar agree
            response.setPerformative(ACLMessage.AGREE);
            log.info(ANSI_YELLOW + this.getAgent().getLocalName() +
                    " La unidad " + unit.getLocalName() + " NO esta bloqueada mandamos agree");
            // y bloquear la unidad durante DURATION_MOVEMENT horas
            agente.blockUnit(unit);
            movingTime(unitInfo, direction, msg);
        } else { //mandar refuse, La unidad está bloqueada
            response.setPerformative(ACLMessage.REFUSE);
            log.info(ANSI_YELLOW + this.getAgent().getLocalName() +
                    " La unidad " + unit.getLocalName() + " esta bloqueada mandamos refuse");
        }
        this.getAgent().send(response);

    }

    private void movingTime(UnitInfo unitInfo,int direction, ACLMessage msg) {
        //crear un waker que cuente las 6h y al despertar:
        //if game phase is over we send a cancel
        //else llame al unblockUnit(unit) y mandar al mapmanager una posición
        AID unit=unitInfo.getUnitAID();

        //Y mandamos la información de la posición de la unit al MapManager
        Object[] moveUnitInfo = {unitInfo.getUnitAID(), unitInfo.getTribeName(),unitInfo.getTribeAID(),unitInfo.getPosActualX(),unitInfo.getPosActualY(),direction};
        MapManagerAID = untilFindDFS(this.getAgent(), MAPMANAGER);
        //mandar mensaje a map manager con el nombre de la tribu
        ACLMessage newMapManMsg = newMsgWithObject(this.getAgent(), ACLMessage.INFORM,
                moveUnitInfo, MapManagerAID,"moveUnit");
        log.info(ANSI_YELLOW + this.getAgent().getLocalName() +
                " Mensaje a MapManager con el nombre de la unidad que se mueve: " + unit.getLocalName());
        this.getAgent().send(newMapManMsg);

        this.addSubBehaviour(new WakerBehaviour(this.getAgent(), (long) (DURATION_MOVEMENT)) {
            @Override
            protected void onWake() {
                super.onWake();
                //The time for moving the unit is up y desbloqueamos la unidad
                agente.unblockUnit(unit);

                //obtener la info del unit
                UnitInfo unitInfo=agente.findUnitInfo(unit);

                //Si el juego ha terminado, el movimiento falla
                if(agente.getPhase()!=1) {
                    ACLMessage response = msg.createReply();
                    response.setPerformative(ACLMessage.FAILURE);
                    this.getAgent().send(response);
                    log.info(ANSI_YELLOW + this.getAgent().getLocalName() +
                            " sends message to " + msg.getSender().getLocalName() +
                            ", the game time has finished, the movement has FAILED");
                } else {
                    //Esperar respuesta del MapManager con la información de la nueva posición
                    ACLMessage responseMapManager = this.getAgent().blockingReceive(MessageTemplate.MatchProtocol("newPosition"));
                    if (responseMapManager != null) {
                        Object[] newPositionInfo;
                        try { newPositionInfo = (Object[]) responseMapManager.getContentObject(); }
                        catch (UnreadableException e) { throw new RuntimeException(e); }

                        AID tribeAID = (AID) newPositionInfo[0];
                        Coordinate newPosition = (Coordinate) newPositionInfo[1];
                        log.info(ANSI_YELLOW + this.getAgent().getLocalName() + " recibe mensaje de " +
                                responseMapManager.getSender().getLocalName() + " con la nueva posición: (" +
                                newPosition.getXValue() + ", " + newPosition.getYValue() + ")");

                        //Actualizar posición de la unidad
                        unitInfo.setPosActualX(newPosition.getXValue());
                        unitInfo.setPosActualY(newPosition.getYValue());

                        //Enviamos la nueva posición a la unidad
                        Destination dest= new Destination(newPosition);
                        ACLMessage response = Utils.newMsgWithOnto(this.getAgent(), msg.getSender(), ACLMessage.INFORM,
                                codec, ontology, dest, WoaOntologyVocabulary.MOVE);
                        this.getAgent().send(response);
                        log.info(ANSI_YELLOW + this.getAgent().getLocalName() +
                                " sends message to " + msg.getSender().getLocalName() +
                                ", the movement of the unit has been completed.");

                        agente.revelarCasilla(tribeAID, newPosition);
                    }
                }
            }


        });
    }



}
