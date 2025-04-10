package es.upm.emse.absd.team1.agents.platform.UnitManager.GameBehaviours;

import es.upm.emse.absd.ontology.woa.WoaOntology;
import es.upm.emse.absd.ontology.woa.WoaOntologyVocabulary;
import es.upm.emse.absd.ontology.woa.actions.ConstructBuilding;
import es.upm.emse.absd.ontology.woa.concepts.Coordinate;
import es.upm.emse.absd.team1.agents.Utils;
import es.upm.emse.absd.team1.agents.platform.UnitInfo;
import es.upm.emse.absd.team1.agents.platform.UnitManager.AgUnitManager;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import lombok.extern.java.Log;

import static es.upm.emse.absd.team1.agents.Utils.*;

@Log
public class UnitManagerGameBehaviour_Building extends CyclicBehaviour {
    private static final String BUILDINGMANAGER = "BuildingManager";
    private final Codec codec = new SLCodec();
    private final Ontology ontology = WoaOntology.getInstance();
    AgUnitManager agente;
    public UnitManagerGameBehaviour_Building(AgUnitManager a) {
        super(a);
        this.agente=a;
    }

    @Override
    public void action() {
        ACLMessage msg;
        //esperar mensaje de la tribu
        if ((msg = this.getAgent().receive(MessageTemplate.MatchProtocol(WoaOntologyVocabulary.CONSTRUCT_BUILDING))) != null) {
            log.info(ANSI_YELLOW + this.agente.getLocalName() + " Recibida petición de construccion de edificio");
            Action actionMsg = Utils.extractMsgOntoContent(this.getAgent(), msg);
            if (actionMsg != null) {
                ConstructBuilding constructBuilding = (ConstructBuilding) actionMsg.getAction();
                log.info(ANSI_YELLOW + this.getAgent().getLocalName() +
                        " con esto la unidad " + msg.getSender().getLocalName() +
                        " quiere crear un edificio de tipo: " + constructBuilding.getBuilding());

                UnitInfo requesterInfo = this.agente.findUnitInfo(msg.getSender());
                AID tribeAID = requesterInfo.getTribeAID();
                AID unitAID = requesterInfo.getUnitAID();
                String type = constructBuilding.getBuilding().getType();
                Coordinate buildingPosition = new Coordinate(requesterInfo.getPosActualX(), requesterInfo.getPosActualY());

                // 2.1.3. As the unit manager, I want to check the availability of a unit so that it can construct a new building
                if (this.agente.isBlocked(msg.getSender())) {
                    //SCENARIO 2: A request to construct a new building from a unit controller blocked is not allowed
                    log.info(ANSI_YELLOW + this.agente.getLocalName() + " La unidad que pide construir un edificio esta bloqueada, envío Refuse");
                    ACLMessage response = msg.createReply();
                    response.setPerformative(ACLMessage.REFUSE);
                    this.agente.send(response);
                } else {
                    //Se bloquea la unidad
                    agente.blockUnit(msg.getSender());
                    //Se informa a la unidad de que no esta bloqueada y puede comenzar la construccion del edificio
                    log.info(ANSI_YELLOW + this.agente.getLocalName() + " La unidad que pide crear una unidad no esta bloqueada, envío Agree");
                    ACLMessage response = msg.createReply();
                    response.setPerformative(ACLMessage.AGREE);
                    this.agente.send(response);
                    checkConditions(tribeAID, unitAID, type, buildingPosition);
                }
            } else
                log.warning(ANSI_RED + this.getAgent().getLocalName() + " Error in the direction of the unit");

            //Esperar respuesta del BuildingManager
        } else if ((msg = this.getAgent().receive(MessageTemplate.MatchProtocol("buildingResponse"))) != null) {
            Object[] buildingInfo;
            try {
                buildingInfo = (Object[]) msg.getContentObject();
            } catch (UnreadableException e) {
                throw new RuntimeException(e);
            }
            AID unitAID = (AID) buildingInfo[0];

            ACLMessage buildingConstructResult;

            switch (msg.getPerformative()) {
                //2.1.11. As the unit manager, I want to confirm the result of constructing a building so that the unit is unblocked
                case ACLMessage.FAILURE:
                    //SCENARIO 2: the construction was wrong, the unit is unblocked, and it is informed about the failure
                    //escribirle un mensaje de vuelta de que no puede
                    log.info(ANSI_YELLOW + this.getAgent().getLocalName() +
                            " Mensaje recibido de tipo FAILURE ");
                    log.info(ANSI_YELLOW + this.agente.getLocalName() + " La construccion del edificio no cumple los requisitos, envío Failure");

                    buildingConstructResult = Utils.newMsgWithOnto(this.agente, unitAID, ACLMessage.FAILURE,
                            codec, ontology, null, WoaOntologyVocabulary.CONSTRUCT_BUILDING);
                    this.agente.send(buildingConstructResult);
                    break;

                case ACLMessage.INFORM:
                    //SCENARIO 1: the construction was correct, the unit is unblocked, and it is informed positively
                    log.info(ANSI_YELLOW + this.getAgent().getLocalName() +
                            " Mensaje recibido de tipo INFORM ");
                    log.info(ANSI_YELLOW + this.agente.getLocalName() + " La construccion del edificio cumple los requisitos, envío Inform");

                    buildingConstructResult = Utils.newMsgWithOnto(this.agente, unitAID, ACLMessage.INFORM,
                            codec, ontology, null, WoaOntologyVocabulary.CONSTRUCT_BUILDING);
                    this.agente.send(buildingConstructResult);
                    break;
                default: {
                    break;
                }
            }
            //desbloquear la unidad
            agente.unblockUnit(unitAID);
        }
    }

    private void checkConditions(AID tribeAID, AID unitAID, String type, Coordinate buildingPosition) {
        Object[] buildingInfo = {buildingPosition.getXValue(),buildingPosition.getYValue(),unitAID,type, tribeAID};
        log.info(ANSI_YELLOW + this.getAgent().getLocalName() +
                " Enviando mensaje a BuildingManager preguntando si se puede construir un building tipo: "+ type + " en: " +
                buildingPosition.getXValue() + ", " + buildingPosition.getYValue());

        // 2.1.5. As the unit manager, I want to ask the building manager to construct a building so that it can control its construction
        //mandar mensaje al buildingmanager para construir
        ACLMessage newBuildingMsg = newMsgWithObject(this.getAgent(), ACLMessage.INFORM,
                buildingInfo, Utils.untilFindDFS(this.getAgent(), BUILDINGMANAGER),"NewBuilding");
        log.info(ANSI_YELLOW + this.getAgent().getLocalName() +
                " Mensaje enviado a BuildingManager preguntando si se puede construir un building tipo: "+ type + " en: " +
                buildingPosition.getXValue() + ", " + buildingPosition.getYValue());
        this.getAgent().send(newBuildingMsg);
    }
}
