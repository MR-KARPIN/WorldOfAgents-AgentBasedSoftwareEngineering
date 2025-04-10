package es.upm.emse.absd.team1.agents.platform.UnitManager.GameBehaviours;

import es.upm.emse.absd.GUIUtils;
import es.upm.emse.absd.ontology.woa.WoaOntologyVocabulary;
import es.upm.emse.absd.ontology.woa.concepts.Coordinate;
import es.upm.emse.absd.team1.agents.Utils;
import es.upm.emse.absd.team1.agents.platform.UnitInfo;
import es.upm.emse.absd.team1.agents.platform.UnitManager.AgUnitManager;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.ParallelBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import lombok.extern.java.Log;

import static es.upm.emse.absd.team1.agents.Utils.*;

@Log
public class UnitManagerGameBehaviour_CreateUnit extends ParallelBehaviour {

    private static final String MAPMANAGER = "MapManager";
    private static final String TRIBEACCOUNTANT = "TribeAccountant";
    AgUnitManager agente;
    private static final long NEWUNIT_TIME=8000;
    public UnitManagerGameBehaviour_CreateUnit(AgUnitManager a) {
        super();
        this.agente=a;
        this.addSubBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage msg;
                if((msg = this.getAgent().receive(MessageTemplate.MatchProtocol(WoaOntologyVocabulary.CREATE_UNIT))) != null){
                    log.info(ANSI_YELLOW + agente.getLocalName() + " Recibida petición de creación de una unidad");
                    UnitInfo requesterInfo = agente.findUnitInfo(msg.getSender());
                    Coordinate unitInitialCoords = new Coordinate(requesterInfo.getPosActualX(), requesterInfo.getPosActualY());

                    // 3.1.7. As the unit manager, I want to check the availability of a unit so that it can create a new unit
                    if (agente.isBlocked(msg.getSender())){
                        //SCENARIO 2: A request to create a new unit from a unit controller blocked is not allowed
                        //escribirle un mensaje de vuelta de que no puede (nanai de la china)
                        log.info(ANSI_YELLOW + agente.getLocalName() + " La unidad que pide crear una unidad esta bloqueada, envío Refuse");
                        ACLMessage response = msg.createReply();
                        response.setPerformative(ACLMessage.REFUSE);
                        agente.send(response);
                    }
                    else{
                        //Se informa a la unidad de que no esta bloqueada y puede comenzar la creación de una unidad
                        log.info(ANSI_YELLOW + agente.getLocalName() + " La unidad que pide crear una unidad no esta bloqueada, envío Agree");
                        ACLMessage response = msg.createReply();
                        response.setPerformative(ACLMessage.AGREE);
                        agente.send(response);

                        //SCENARIO 1: A request to create a new unit from a unit controller not blocked is allowed
                        if(checkConditions(msg)){
                            //3.1.10. As the unit manager, I want to control the creation of a unit so that I can decide when it is available for the tribe
                            //SCENARIO 1: The time for creating the unit is up, and the creation is completed
                            //SCENARIO 2: The time for creating the unit is up after the game phase is over, and the creation fails
                            //En este escenario hay que devolver los recursos a la tribu!
                            log.info(ANSI_YELLOW + agente.getLocalName() + " La creación de la unidad cumple los requisitos, comienza su creacion");
                            blockingTimeNewUnit(requesterInfo, unitInitialCoords, msg);
                        }
                        else {
                            //escribirle un mensaje de vuelta de que no puede (nanai de la china)
                            log.info(ANSI_YELLOW + agente.getLocalName() + " La creación de la unidad no cumple los requisitos, envío Failure");
                            ACLMessage unitCreationResult = msg.createReply();
                            unitCreationResult.setPerformative(ACLMessage.FAILURE);
                            agente.send(unitCreationResult);
                        }
                    }
                }
            }
        });
    }



    private boolean checkConditions(ACLMessage msg) {
        UnitInfo requesterInfo = this.agente.findUnitInfo(msg.getSender());
        Coordinate unitInitialCoords = new Coordinate(requesterInfo.getPosActualX(), requesterInfo.getPosActualY());

        // 3.1.9. As the unit manager, I want to ask the map manager about the content of a cell so that I can know whether it contains a town hall or not
        //mandar mensaje a map manager para saber que hay
        ACLMessage posContentMsg = newMsgWithObject(this.getAgent(), ACLMessage.INFORM,
                unitInitialCoords, Utils.untilFindDFS(this.getAgent(), MAPMANAGER),"queHayAqui");
        log.info(ANSI_YELLOW + this.getAgent().getLocalName() +
                " Mensaje enviado a MapManager preguntando que hay en: " +
                unitInitialCoords.getXValue() + ", " + unitInitialCoords.getYValue());
        this.getAgent().send(posContentMsg);

        ACLMessage posContentResponseMsg = this.getAgent().blockingReceive(MessageTemplate.MatchProtocol("aquiHay"));
        String posContent;
        posContent = (String) posContentResponseMsg.getContent();

        if (!posContent.equals(requesterInfo.getTribeName()+"Town Hall")){
            log.info(ANSI_YELLOW + this.getAgent().getLocalName() +
                    " La unidad no está en un ayuntamiento, no se puede crear unidad");
            return false; //La unidad que lo pide no esta un un townhall (pal lobby)
        }

        // 3.1.13. As the unit manager, I want to pay the price of a new unit so that I can authorize its creation
        Object[] resourcesNewUnit = {requesterInfo.getTribeAID(), requesterInfo.getUnitAID()};
        ACLMessage moneyMsg = newMsgWithObject(this.getAgent(), ACLMessage.INFORM,
                resourcesNewUnit, Utils.untilFindDFS(this.getAgent(), TRIBEACCOUNTANT),
                "tieneRecursosTribuParaUnidad");
        log.info(ANSI_YELLOW + this.getAgent().getLocalName() +
                " Mensaje enviado al Tribe Accountant a ver si tienen dinero los de: " + requesterInfo.getTribeName());
        this.getAgent().send(moneyMsg);

        ACLMessage moneyResponseMsg = this.getAgent().blockingReceive(MessageTemplate.MatchProtocol("tieneRecursosTribuParaUnidad"));
        String confirm;
        confirm = moneyResponseMsg.getContent();
        if (!confirm.equals("si")){
            log.info(ANSI_YELLOW + this.getAgent().getLocalName() +
                    " La tribu no tiene recursos suficientes, no se puede crear unidad");
            return false; // no tienen recursos suficientes

        }
        //SCENARIO 1: the unit manager asks the tribe accountant to pay for the new unit, and there are
        // enough resources to cover it

        // 3.1.8. As the unit manager, I want to check the conditions of creating a new unit so that I can proceed with or interrupt the creation process
        // esto es comprobar lo de antes (el 7 y 13) creo
        //SCENARIO 1: A request to create a new unit matching all the preconditions is accepted
        //SCENARIO 2: A request to create a new unit when the tribe does not have enough resources to pay for it fails
        //SCENARIO 3: A request to create a new unit in a cell without a town hall of its property fails
        return true;
    }


    //Espera tiempo de creacion de la unidad, crea unidad, informa a la unidad.
    private void blockingTimeNewUnit(UnitInfo unitInfo, Coordinate coordenadas, ACLMessage msg){
        this.addSubBehaviour(new WakerBehaviour(agente, NEWUNIT_TIME) {
            @Override
            protected void onWake() {
                super.onWake();
                AID tribeAID = unitInfo.getTribeAID();
                ACLMessage unitCreationResult = msg.createReply();
                if(agente.getPhase()!=1){
                    //Se acabó el tiempo de juego mientras se creaba unidad --> devolver pasta
                    ACLMessage moneyMsg = newMsgWithObject(this.getAgent(), ACLMessage.INFORM,
                            unitInfo.getTribeAID(), Utils.untilFindDFS(this.getAgent(), TRIBEACCOUNTANT),
                            "devolverOro");
                    log.info(ANSI_YELLOW + this.getAgent().getLocalName() +
                            " Mensaje enviado al Tribe Accountant para devolver oro a: " + unitInfo.getTribeName());
                    this.getAgent().send(moneyMsg);
                    //Enviar failure
                    log.info(ANSI_YELLOW + agente.getLocalName() + " La creación de la unidad cumple los requisitos" +
                            "pero se acabó el juego, envío Failure");
                    unitCreationResult.setPerformative(ACLMessage.FAILURE);
                }
                else{
                    //Ok
                    AID newUnit = agente.createUnit(tribeAID, coordenadas);
                    unitCreationResult.setPerformative(ACLMessage.INFORM);
                    log.info(ANSI_PURPLE + this.getAgent().getLocalName() + " Pinto unidad en: " +
                            coordenadas.getXValue()+" "+ coordenadas.getYValue());
                    GUIUtils.newUnit(tribeAID.getLocalName(), newUnit.getLocalName(), coordenadas.getXValue(), coordenadas.getYValue());
                }
                agente.send(unitCreationResult);
            }
        });
    }
}
