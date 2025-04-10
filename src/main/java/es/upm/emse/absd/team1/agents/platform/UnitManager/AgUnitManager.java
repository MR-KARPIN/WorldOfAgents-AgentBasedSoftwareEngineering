package es.upm.emse.absd.team1.agents.platform.UnitManager;

import es.upm.emse.absd.ontology.woa.WoaOntologyVocabulary;
import es.upm.emse.absd.ontology.woa.actions.AssignNewUnit;
import es.upm.emse.absd.ontology.woa.concepts.Coordinate;
import es.upm.emse.absd.ontology.woa.concepts.NewPhase;
import es.upm.emse.absd.team1.agents.Utils;
import es.upm.emse.absd.ontology.woa.WoaOntology;
import es.upm.emse.absd.team1.agents.platform.TribeUnitsInfo;
import es.upm.emse.absd.team1.agents.platform.UnitInfo;
import es.upm.emse.absd.team1.agents.platform.UnitManager.GameBehaviours.UnitManagerGameBehaviour_BehaviourManager;
import es.upm.emse.absd.team1.agents.platform.UnitManager.RegistrationBehaviours.UnitManagerRegistrationBehaviour1;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.Agent;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.core.behaviours.SequentialBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;
import lombok.Data;
import lombok.Setter;
import lombok.extern.java.Log;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import static es.upm.emse.absd.team1.agents.Utils.*;

@Log
@Data
public class AgUnitManager extends Agent {
    public final static String UNITMANAGER = "UnitManager";

    private final  String MAPMANAGER = "MapManager";
    private final String TRIBEACCOUNTANT = "TribeAccountant";
    private AID mapManager;
    private AID tribeAccountant;
    private AID resourceManager;
    private final Codec codec = new SLCodec();
    private final Ontology ontology = WoaOntology.getInstance();
    private static AgentContainer cc;
    private static Runtime rt;

    //para poder crear agentes se necesitan el cc y el rt
    private ArrayList<UnitInfo> unitsInfoList = new ArrayList<>();
    private ArrayList<TribeUnitsInfo> tribesUnitsInfo = new ArrayList<>();
    private int phase = 0;
    @Setter
    private ArrayList<Coordinate> initialPositions = new ArrayList<>();
    private Map<AID, ArrayList<Coordinate>> tribesKnownCells = new HashMap<>();


    protected void setup() {
        /*
        0.  Registra servicio en DFS.
            Obtiene posiciones iniciales del MapManager.
            Suscribe a phaseManager
         */

        this.mapManager= untilFindDFS(this, MAPMANAGER);
        this.tribeAccountant= untilFindDFS(this, TRIBEACCOUNTANT);

        log.info(ANSI_YELLOW + this.getLocalName() + " Se crea UnitManager");
        getContentManager().registerLanguage(codec);
        getContentManager().registerOntology(ontology);
        register(this, UNITMANAGER); //Registra servicio en DFS
        log.info(ANSI_YELLOW + this.getLocalName() + " is added to the DFService");
        //preparación para crear agentes
        // Create a default profile
        rt = Runtime.instance();
        ProfileImpl agentContainerProfile = new ProfileImpl(null, 1200, null);
        agentContainerProfile.setParameter(Profile.CONTAINER_NAME, "Unit-Container");
        cc = rt.createAgentContainer(agentContainerProfile);

        //Suscripción PhaseManager
        suscribePhaseManager(this, this.codec, this.ontology);

        //Recibe posiciones iniciales del mapManager
        receiveInitialPositions();

        /*
        BEHAVIOUR UNITMANAGER:
            Secuencial:
                1. Behaviour paralelo que se encarga de las funciones en la fase de registro,
                    1.1 Crea las 3 unidades iniciales de una nueva tribu
                    1.2 Escuchar cambio de fase (cíclicamente).
                    Este behaviour acaba al terminar la fase de registro.

                2. Behaviour paralelo que se encarga de las funciones de la fase de juego:
                    2.1 Mover unidades
                    2.2 Explotar recurso
                    2.3 Escuchar cambio de fase
                    2.4 (Sprint 4) construir edificio
                    2.5 (Sprint 4) creación unidad (se podrá reutilizar el 1.2)
                   Este behaviour acaba al terminar la fase de juego.
         */
        SequentialBehaviour unitManagerBehaviour = new SequentialBehaviour();
        unitManagerBehaviour.addSubBehaviour(new UnitManagerRegistrationBehaviour1(this, tribesUnitsInfo));
        unitManagerBehaviour.addSubBehaviour(new UnitManagerGameBehaviour_BehaviourManager(this));
        this.addBehaviour(unitManagerBehaviour);

    }


    private void receiveInitialPositions(){
        ACLMessage msg = this.blockingReceive(MessageTemplate.MatchProtocol("PosicionesIniciales"));
        log.info(ANSI_YELLOW + this.getLocalName() + " Recibe mensaje de mapManager con posiciones iniciales");
        try { initialPositions = (ArrayList<Coordinate>) msg.getContentObject(); }
        catch (UnreadableException e) {
            log.warning(ANSI_YELLOW + this.getLocalName() + " Fallo recibiendo las posiciones iniciales");
            throw new RuntimeException(e);
        }
        for (Coordinate initialPosition : initialPositions)
            log.info(ANSI_YELLOW + this.getLocalName() + " Posición inicial: " + initialPosition);
    }

    public void receiveNotifyChangePhase() {
        ACLMessage msg = this.receive(MessageTemplate.and(MessageTemplate.and(
                        MessageTemplate.MatchLanguage(codec.getName()), MessageTemplate.MatchOntology(ontology.getName())),
                MessageTemplate.MatchProtocol(WoaOntologyVocabulary.CHANGE_PHASE)));
        if (msg != null) {
            log.info(ANSI_YELLOW + this.getLocalName() + " Recibe mensaje de cambio de fase");
            Action actionMsg = extractMsgOntoContent(this, msg);
            NewPhase nuevaFase;
            if (actionMsg != null) {
                nuevaFase = (NewPhase) actionMsg.getAction();
                setPhase(nuevaFase.getPhase());
                log.info(ANSI_YELLOW + this.getLocalName() + " Cambio a fase: " + getPhase());
            }
        }
    }


    // devuelve la UnitInfo de una unidad dado su AID
    public UnitInfo findUnitInfo(AID aidUnidad) {
        for (UnitInfo unidad : this.getUnitsInfoList()) if (unidad.getUnitAID().equals(aidUnidad)) return unidad;
        return null;
    }


    //comprueba si la unidad dada esta bloqueada
    public boolean isBlocked(AID unit){
        return findUnitInfo(unit).isBlocked();
    }


    // bloquea una unidad dada
    public void blockUnit(AID unit){
        //Bloquear unidad que se le ha enviado un agree
        findUnitInfo(unit).setBlocked(true);
        log.info(ANSI_YELLOW + this.getLocalName() + " bloqueo la unidad " + unit.getLocalName());
    }


    // desbloquea una unidad dada
    public void unblockUnit(AID unit){
        //Bloquear unidad que se le ha enviado un agree
        findUnitInfo(unit).setBlocked(false);
        log.info(ANSI_YELLOW + this.getLocalName() + " desbloqueo la unidad " + unit.getLocalName());
    }



    //Dada una tribu y unas coordenadas, le envía al map manager un mensaje de que revele esa casilla
    public void revelarCasilla(AID tribeAID, Coordinate position) {
        //Añadimos la celda al registro de celdas y en caso de que sea nueva le informamos al mapManager
        ArrayList<Coordinate> celdasConocidasTribu = this.tribesKnownCells.get(tribeAID);
        log.info(ANSI_YELLOW + this.getLocalName() +
                " Llamada a reveal cell");
        if (!celdasConocidasTribu.contains(position)) {
            log.info(ANSI_YELLOW + this.getLocalName() +
                    "Se informa al mapManager para que informe de una nueva casilla a la tribu: "+tribeAID.getLocalName());
            //mandar mensaje a map manager con el nombre de la tribu y la posición
            ACLMessage newPosMsg = newMsgWithObject(this, ACLMessage.INFORM,
                    new Object[]{tribeAID, position},
                    untilFindDFS(this, MAPMANAGER),
                    "RevealCell");
            log.info(ANSI_YELLOW + this.getLocalName() +
                    " Mensaje a MapManager: " + tribeAID.getLocalName() +
                    " ha encontrado " + position.getXValue() +","+ position.getYValue());
            this.send(newPosMsg);

            //Añadimos la celda a las que conoce esa tribu
            celdasConocidasTribu.add(position);
            this.tribesKnownCells.put(tribeAID,celdasConocidasTribu);

        } else log.info(ANSI_YELLOW + this.getLocalName() + " la tribu ya conoce la celda lol");
    }

    private TribeUnitsInfo findTribeUnitsInfo(String tribeName){
        for (TribeUnitsInfo tribe : tribesUnitsInfo) if (tribe.getTribeName().equals(tribeName)) return tribe;
        return null;
    }

    public AID createUnit(AID tribeAID, Coordinate unitInitialCoords){
        AID unitAID = null;
        try {
            String tribeName = tribeAID.getLocalName();
            TribeUnitsInfo tribeUnitsInfo = findTribeUnitsInfo(tribeName);
            int nunit = tribeUnitsInfo != null ? tribeUnitsInfo.getNumberUnits() : 0;
            String unitName = "Unit" + nunit + (tribeUnitsInfo != null ? tribeUnitsInfo.getTribeName() : "0");
            String unitContClass = getTribeController(tribeName);

            if (!unitContClass.isEmpty()) {
                log.info(ANSI_YELLOW + this.getLocalName() + " Creando unidad de " + tribeName);
                if (unitInitialCoords == null && tribeUnitsInfo != null)
                    unitInitialCoords = new Coordinate(tribeUnitsInfo.getPosIniX(), tribeUnitsInfo.getPosIniY());

                AgentController unitController = this.getContainerController().createNewAgent( unitName, unitContClass,
                        new Object[]{ unitInitialCoords });
                unitController.start();
                log.info(ANSI_YELLOW + this.getLocalName() +
                        " Se crea unidad " + nunit + "de la tribu " + tribeName + " con el unitName:" + unitName);

                //Creamos un AID para la unidad creada
                unitAID= new AID(unitName, AID.ISLOCALNAME);
                this.unitsInfoList.add(new UnitInfo(tribeAID, unitAID, tribeName,
                        unitName, false, tribeUnitsInfo.getPosIniX(), tribeUnitsInfo.getPosIniY()));
                tribeUnitsInfo.addUnit();

                sendUnitCreationMsgMapManager(unitInitialCoords, unitAID, tribeAID); //mandar mensaje a mapManager
                sendUnitCreationMsgTribeController(tribeUnitsInfo, unitAID); //mandar mensaje a tribeController
                sendUnitCreationMsgTribeAccountant(tribeAID); //mandar mensaje a tribeAccountant
                this.revelarCasilla(tribeAID, unitInitialCoords);
            } else log.warning(ANSI_YELLOW + this.getLocalName() + " No existe la tribu " + tribeName);
        } catch (StaleProxyException e) {
            System.err.println("Error creating my unit agents!!!");
            throw new RuntimeException(e);
        }
        return unitAID;
    }

    //Envío de mensaje al tribeAccountant para que sume el número de la unidad.
    private void sendUnitCreationMsgTribeAccountant(AID tribeAID) {
        AID tribeAccountant= untilFindDFS(this, TRIBEACCOUNTANT);
        ACLMessage newTribeAccMsg = newMsgWithObject(this, ACLMessage.INFORM,
                tribeAID, tribeAccountant,"AddUnit");
        log.info(ANSI_YELLOW + this.getLocalName() +
                " Mensaje enviado a TribeAccountant para que sume una unidad a la tribu: " + tribeAID.getLocalName());
        this.send(newTribeAccMsg);
    }


    //Envío de mensaje a tribeController con su unidad creada
    private void sendUnitCreationMsgTribeController(TribeUnitsInfo unidad, AID unitAID) {
        AssignNewUnit newUnitAID = new AssignNewUnit();
        newUnitAID.setUnitID(unitAID);
        log.info(ANSI_YELLOW + this.getLocalName() + " envía el AID de la unidad a: "+
                unidad.getTribe().getLocalName());
        this.send(newMsgWithOnto(this, unidad.getTribe(), ACLMessage.INFORM,
                codec, ontology, newUnitAID, WoaOntologyVocabulary.ASSIGN_NEW_UNIT));
    }


    //Envío de mensaje al mapManager con aid de la tribu, el aid de la unidad y su localization inicial
    private void sendUnitCreationMsgMapManager(Coordinate unitInitialCoords, AID unitAID, AID tribeAID) {
        AID mapManager = untilFindDFS(this, MAPMANAGER);
        int posX = unitInitialCoords.getXValue();
        int posY = unitInitialCoords.getYValue();
        String tribeName = tribeAID.getLocalName();

        //enviar mensaje al MapManager diciendo que se crea una nueva unidad
        Object[] newUnitInfo = {tribeName, unitAID, posX, posY};
        ACLMessage newMapManMsg = newMsgWithObject(this, ACLMessage.INFORM,
                newUnitInfo, mapManager,"newUnitInfo");
        log.info(ANSI_YELLOW + this.getLocalName() +
                " Mensaje enviado a MapManager con a la tribu " +tribeName +
                " de la nueva unidad: " + unitAID.getLocalName() + " con posiciones " + posX + " " + posY);
        this.send(newMapManMsg);


    }

}

