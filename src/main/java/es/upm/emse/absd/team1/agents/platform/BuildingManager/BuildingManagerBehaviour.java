package es.upm.emse.absd.team1.agents.platform.BuildingManager;

import es.upm.emse.absd.ontology.woa.WoaOntology;
import es.upm.emse.absd.ontology.woa.WoaOntologyVocabulary;
import es.upm.emse.absd.ontology.woa.concepts.Coordinate;
import es.upm.emse.absd.ontology.woa.concepts.NewPhase;
import es.upm.emse.absd.team1.agents.platform.NewBuildingFinished;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.ParallelBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import lombok.Data;
import lombok.extern.java.Log;

import java.io.Serializable;
import java.util.ArrayList;

import static es.upm.emse.absd.team1.agents.Utils.*;


/*
    BEHAVIOUR DEL BUILDING MANAGER:
        Es cíclico y escucha permanentemente los mensajes de petición de construcción de un edificio del unit manager.
        Crea wakers paralelos para contar el tiempo de construcción de los edificios.

                comprobar las condiciones de construir un edificio
            Condiciones para construir un Town Hall:
                - it must contain just plain ground;
                - no one can be building in that cell already;
                - there must not be any building already built or being built (thus,
                  belonging to another city) in any of its contiguous cells;
                - the player must have enough resources to pay the price of building the Town Hall:
                    (250 Gold, 150 Stone, 200 Wood, 1 Unit, 240 Hours);
            Condiciones para construir un a Store:
                - the cell for the new building must contain just plain ground;
                - it must be connected in any of the surrounding cells directly with a
                single Town Hall or indirectly through other buildings connected to a single Town Hall;
                - the player must have enough resources to pay the price of building the new building:
                    (50 Gold, 50 Stone, 50 Wood, 1 Unit, 120 Hours);
*/


@Log
@Data
public class BuildingManagerBehaviour extends ParallelBehaviour {

    private AgBuildingManager agente;
    private AID MapManagerAID;
    private AID TribeAccountantAID;
    public final String MAPMANAGER = "MapManager";
    public final String TRIBEACCOUNTANT = "TribeAccountant";
    private final double DURATION_CONSTRUCTION_TOWNHALL = 240000 * secondsPerHour;
    private final double DURATION_CONSTRUCTION_STORE = 120000 * secondsPerHour;
    private double DURATION_CONSTRUCTION;
    //lista donde se van guardando las coordenadas donde se está construyendo un building
    private ArrayList<Coordinate> blockedCoordinates = new ArrayList<>();

    private final Codec codec = new SLCodec();
    private final Ontology ontology = WoaOntology.getInstance();

    public BuildingManagerBehaviour(AgBuildingManager agente) {
        super(agente, ParallelBehaviour.WHEN_ALL);
        this.agente = agente;
        MapManagerAID = untilFindDFS(this.getAgent(), MAPMANAGER);
        TribeAccountantAID = untilFindDFS(this.getAgent(), TRIBEACCOUNTANT);
    }

    public void onStart() {
        //Behaviour cíclico que escucha permanentemente los mensajes de petición de construcción del UnitManager.
        this.addSubBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage msgUnitManager = this.getAgent().receive(MessageTemplate.MatchProtocol("NewBuilding"));
                if(msgUnitManager != null) {
                    log.info(ANSI_YELLOW + this.getAgent().getLocalName() +
                            " ha recibido el mensaje del UnitManager con el building request ");
                    checkBuildingConditions(msgUnitManager);
                }
            }
        });

        this.addSubBehaviour(new SimpleBehaviour() {
            @Override
            public void action() {
                receiveNotifyChangePhase();
            }

            @Override
            public boolean done() { return agente.getPhase()==2;
            }
        });
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
            }
        }
    }


    private void checkBuildingConditions(ACLMessage msgNewBuilding) {
        try {
            //Obtención del contenido del mensaje de construcción de un edificio
            Object[] buildingInfo = (Object[]) msgNewBuilding.getContentObject();
            int xCoord = (int) buildingInfo[0];
            int yCoord = (int) buildingInfo[1];
            AID unitAID = (AID) buildingInfo[2];
            String type = (String) buildingInfo[3];
            AID tribeAID = (AID) buildingInfo[4];

            //preguntar al MapManager sobre el contenido de la celda y sus celdas contiguas
            Coordinate checkCoord = new Coordinate(xCoord, yCoord);
            Object[] checkCellsInfo = {checkCoord,type,tribeAID.getLocalName()};
            ACLMessage msgCheckCell = newMsgWithObject(this.getAgent(), ACLMessage.REQUEST, checkCellsInfo, MapManagerAID,
                    "CheckCell");
            this.getAgent().send(msgCheckCell);
            ACLMessage responseMapManager = this.getAgent().blockingReceive(MessageTemplate.MatchProtocol("CheckCell"));
            String availability = responseMapManager.getContent();
            log.info(ANSI_YELLOW + this.getAgent().getLocalName() +
                    " ha recibido el mensaje del MapManager con la disponibilidad: "+ availability);
            //comprobar las condiciones para construir un Townhall
            if (type.equals("Town Hall")) {
                //it must contain just plain ground;
                //no one can be building in that cell already;
                //there must not be any building already built or being built (thus,
                //belonging to another city) in any of its contiguous cells;

                //comprobar que se puede construir un Townhall
                if (availability.equals("Available") && !checkBuildingsBeingBuiltInContiguousCells(checkCoord.getXValue(),checkCoord.getYValue())) {

                    //comprobar si se tienen todos los recursos
                    if(checkResources(tribeAID,unitAID, type)) {
                        NewBuildingFinished newBuildingFinished = new NewBuildingFinished(tribeAID, unitAID, type, checkCoord);
                        //si se cumplen todas las condiciones: reservar celda y construir building
                        log.info(ANSI_YELLOW + this.getAgent().getLocalName() +
                                " Se cumplen las condiciones para construir un Townhall, empieza la construcción");
                        blockedCoordinates.add(checkCoord);
                        buildBuilding(msgNewBuilding, newBuildingFinished);

                    } else {
                        //si no se cumplen todas las condiciones, mandar un failure al UnitManager
                        log.info(ANSI_YELLOW + this.getAgent().getLocalName() +
                                " No hay suficientes recursos para construir un Townhall");
                        sendFailureConstructionToUnitManager(msgNewBuilding, unitAID);
                    }

                } else {
                    //si no se cumplen todas las condiciones, mandar un failure al UnitManager
                    log.info(ANSI_YELLOW + this.getAgent().getLocalName() +
                            " No se cumplen las condiciones para construir un Townhall");
                    sendFailureConstructionToUnitManager(msgNewBuilding, unitAID);
                }

            } else if (type.equals("Store")) {
                // the cell for the new building must contain just plain ground;
                // it must be connected in any of the surrounding cells directly with a
                // single Town Hall or indirectly through other buildings connected to a single Town Hall;

                //comprobar que se puede construir una Store
                if(availability.equals("Available") && !blockedCoordinates.contains(checkCoord)) {

                    //comprobar que se tienen todos los recursos
                    if(checkResources(tribeAID,unitAID, type)) {

                        NewBuildingFinished newBuildingFinished = new NewBuildingFinished(tribeAID, unitAID, type, checkCoord);

                        //si se cumplen todas las condiciones: reservar celda y construir building
                        log.info(ANSI_YELLOW + this.getAgent().getLocalName() +
                                " Se cumplen las condiciones para construir una Store, empieza la construcción");
                        blockedCoordinates.add(checkCoord);
                        buildBuilding(msgNewBuilding, newBuildingFinished);

                    } else {
                        //si no se cumplen todas las condiciones, mandar un failure al UnitManager
                        log.info(ANSI_YELLOW + this.getAgent().getLocalName() +
                                " No hay suficientes recursos para construir una Store");
                        sendFailureConstructionToUnitManager(msgNewBuilding, unitAID);
                    }

                } else {
                    //si no se cumplen todas las condiciones, mandar un failure al UnitManager
                    log.info(ANSI_YELLOW + this.getAgent().getLocalName() +
                            " No se cumplen las condiciones para construir una Store");
                    sendFailureConstructionToUnitManager(msgNewBuilding, unitAID);
                }
            }
        } catch (UnreadableException e) {
            throw new RuntimeException(e);
        }
    }

    private void buildBuilding(ACLMessage msgNewBuilding, NewBuildingFinished newBuildingFinished){
        if (newBuildingFinished.getTipo().equals("Town Hall"))
            DURATION_CONSTRUCTION = DURATION_CONSTRUCTION_TOWNHALL;
        else
            DURATION_CONSTRUCTION = DURATION_CONSTRUCTION_STORE;

        this.addSubBehaviour(new WakerBehaviour(this.getAgent(), (long) DURATION_CONSTRUCTION) {
            @Override
            protected void onWake() {
                super.onWake();
                /*
                Comprobar primero si la fase de juego ha terminado o no y luego:
                3.1. Informar al UnitManager para que pueda desbloquear la unidad.
                3.2. Informar al tribeAccountant para actualizar el número de edificios de la tribu.
                3.3. Informar al MapManager para que pueda enseñarlo en el mapa.
                */
                log.info(ANSI_YELLOW + this.getAgent().getLocalName() +
                        " La construcción ha terminado");
                Object[] tribeAccountantInfo = {newBuildingFinished.getTribeAID(),
                        newBuildingFinished.getUnitAID(), newBuildingFinished.getTipo()};

                //Si el juego ha terminado, el movimiento falla
                if(agente.getPhase()!=1) {
                    //enviar mensaje a UnitManager de que ha fallado el movimiento
                    sendFailureConstructionToUnitManager(msgNewBuilding, newBuildingFinished.getUnitAID());

                    //Informar al tribeAccountant para que se devuelvan los recursos, ya que no han sido utilizados
                    //para construir el edificio porque el tiempo se ha terminado
                    ACLMessage returnResourcesMsg = newMsgWithObject(this.getAgent(), ACLMessage.INFORM,
                            tribeAccountantInfo, untilFindDFS(this.getAgent(), TRIBEACCOUNTANT),
                            "devolverRecursosBuilding");
                    log.info(ANSI_YELLOW + this.getAgent().getLocalName() +
                            " Mensaje enviado al Tribe Accountant para devolver recursos a: " +
                            newBuildingFinished.getTribeAID().getLocalName());
                    this.getAgent().send(returnResourcesMsg);

                } else{
                    //3.1. Informar al UnitManager para que pueda desbloquear la unidad.
                    sendInformConstructionToUnitManager(msgNewBuilding, newBuildingFinished.getUnitAID());

                    //3.2. Informar al tribeAccountant para actualizar el número de edificios de la tribu.
                    ACLMessage updateBuildingsMsg = newMsgWithObject(this.getAgent(), ACLMessage.INFORM,
                            tribeAccountantInfo, untilFindDFS(this.getAgent(), TRIBEACCOUNTANT),
                            "actualizarBuildings");
                    log.info(ANSI_YELLOW + this.getAgent().getLocalName() +
                            " Mensaje enviado al Tribe Accountant para actualizar número de buildings de " +
                            newBuildingFinished.getTribeAID().getLocalName());
                    this.getAgent().send(updateBuildingsMsg);

                    //3.3. Informar al MapManager para que pueda enseñarlo en el mapa.
                    ACLMessage msgBuildingCreated = newMsgWithObject(this.getAgent(), ACLMessage.INFORM,
                            (Serializable) newBuildingFinished, MapManagerAID, "BuildingCreated");

                    log.info(ANSI_YELLOW + this.getAgent().getLocalName() +
                            " Mensaje enviado a " + MapManagerAID.getLocalName() + " sobre construcción de edificio finalizada: " +
                            newBuildingFinished.getTribeAID() + newBuildingFinished.getUnitAID() + newBuildingFinished.getTipo()
                    + newBuildingFinished.getCoordenadas());
                    this.getAgent().send(msgBuildingCreated);

                }

                //liberar coordenadas de lista de coordenadas bloqueadas por construccion
                blockedCoordinates.remove(newBuildingFinished.getCoordenadas());
            }
        });

    }


    private boolean checkBuildingsBeingBuiltInContiguousCells (int xCoord, int yCoord) {
        int xCoord1a=adjustCoordinateX(xCoord+1, (int) agente.mapWidth);
        int xCoord1b=adjustCoordinateX(xCoord-1,(int) agente.mapWidth);
        int xCoord2a=adjustCoordinateX(xCoord+2,(int) agente.mapWidth);
        int xCoord2b=adjustCoordinateX(xCoord-2,(int) agente.mapWidth);

        int yCoord1a=adjustCoordinateY(yCoord+1,(int) agente.mapHeight);
        int yCoord1b=adjustCoordinateY(yCoord-1,(int) agente.mapHeight);

        if(!blockedCoordinates.contains(new Coordinate(xCoord1a, yCoord1b))
                && !blockedCoordinates.contains(new Coordinate(xCoord1a, yCoord1a))
            && !blockedCoordinates.contains(new Coordinate(xCoord1b, yCoord1b))
            && !blockedCoordinates.contains(new Coordinate(xCoord1b, yCoord1a))
            && !blockedCoordinates.contains(new Coordinate(xCoord2b, yCoord))
            && !blockedCoordinates.contains(new Coordinate(xCoord2a, yCoord))
            && !blockedCoordinates.contains(new Coordinate(xCoord, yCoord))) {
            log.info(ANSI_YELLOW + this.getAgent().getLocalName() +
                    " No hay otras tribus edificando cerca ni aqui");
            //return false if buildings are not being built in contiguous cells
            return false;

        } else {
            return true;
        }

    }
    private static int adjustCoordinateX(int coord, int max) {
        if (coord < 1) {
            return coord == 0 ? max : max - 1;
        } else if (coord > max) {
            return coord == max + 1 ? 1 : 2;
        }
        return coord;
    }
    private static int adjustCoordinateY(int coord, int max) {
        if (coord < 1) {
            return max;
        } else if (coord > max) {
            return 1;
        }
        return coord;
    }

    private boolean checkResources(AID tribeAID, AID unitAID, String type) {
        //comprobar preguntando al TribeAccountant si se tienen todos los recursos
        Object[] resourcesInfoForBuilding = {tribeAID, unitAID, type};
        ACLMessage msgCheckResources = newMsgWithObject(this.getAgent(), ACLMessage.REQUEST, resourcesInfoForBuilding,
                TribeAccountantAID, "tieneRecursosParaBuilding");
        log.info(ANSI_YELLOW + this.getAgent().getLocalName() +
                " Mensaje enviado a " + TribeAccountantAID.getLocalName() + " para comprobar si hay suficientes recursos " +
                "para la construcción");
        this.getAgent().send(msgCheckResources);

        //esperar mensaje del TribeAccountant sobre si tiene o no los recursos necesarios
        ACLMessage enoughResourcesForBuildingMsg = this.getAgent().blockingReceive(MessageTemplate.MatchProtocol("tieneRecursosParaBuilding"));
        log.info(ANSI_YELLOW + this.getAgent().getLocalName() +
                " Mensaje recibido de " + enoughResourcesForBuildingMsg.getSender().getLocalName() +
                " sobre si hay suficientes recursos para la construcción");

        //devolver true si hay suficientes recursos
        return enoughResourcesForBuildingMsg.getPerformative() == ACLMessage.AGREE;
    }

    private void sendFailureConstructionToUnitManager(ACLMessage msgUnitManager, AID unitAID) {
        Object[] unitAIDInfo = {unitAID};
        ACLMessage responseUnitManager = newMsgWithObject(this.getAgent(), ACLMessage.FAILURE, unitAIDInfo, msgUnitManager.getSender(),
                "buildingResponse");
        this.getAgent().send(responseUnitManager);
        log.info(ANSI_YELLOW + this.getAgent().getLocalName() +
                " Mensaje enviado a UnitManager: la construcción ha fallado");
    }
    private void sendInformConstructionToUnitManager(ACLMessage msgUnitManager, AID unitAID) {
        Object[] unitAIDInfo = {unitAID};
        ACLMessage responseUnitManager = newMsgWithObject(this.getAgent(), ACLMessage.INFORM, unitAIDInfo, msgUnitManager.getSender(),
                "buildingResponse");
        this.getAgent().send(responseUnitManager);
        log.info(ANSI_YELLOW + this.getAgent().getLocalName() +
                " Mensaje enviado a UnitManager: la construcción ha finalizado correctamente");
    }
}
