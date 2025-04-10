package es.upm.emse.absd.team1.agents.tribe;

import es.upm.emse.absd.ontology.woa.WoaOntology;
import es.upm.emse.absd.ontology.woa.WoaOntologyVocabulary;
import es.upm.emse.absd.ontology.woa.actions.CollectResource;
import es.upm.emse.absd.ontology.woa.actions.ConstructBuilding;
import es.upm.emse.absd.ontology.woa.actions.Move;
import es.upm.emse.absd.ontology.woa.concepts.Building;
import es.upm.emse.absd.ontology.woa.concepts.Coordinate;
import es.upm.emse.absd.ontology.woa.concepts.Destination;
import es.upm.emse.absd.ontology.woa.concepts.Direction;
import es.upm.emse.absd.team1.agents.Utils;
import es.upm.emse.absd.team1.agents.tribe.behaviours.unitBehaviour.ConstructorBehaviour;
import es.upm.emse.absd.team1.agents.tribe.behaviours.unitBehaviour.ExplorerBehaviour;
import es.upm.emse.absd.team1.agents.tribe.behaviours.unitBehaviour.RecolectorBehaviour;
import es.upm.emse.absd.team1.agents.tribe.behaviours.unitBehaviour.WaitBehaviour;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.ParallelBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import lombok.Data;
import lombok.extern.java.Log;

import java.io.IOException;
import java.util.Random;

import static es.upm.emse.absd.team1.agents.Utils.*;

@Log
@Data
public class AgUnitController extends Agent {
    private final Codec codec = new SLCodec();
    private final Ontology ontology = WoaOntology.getInstance();
    private final static String UNITMANAGER = "UnitManager";
    private AID unitManager;

    private Coordinate currentPos;

    protected void setup() {

        this.currentPos= (Coordinate) this.getArguments()[0];
        log.info(ANSI_CYAN + "I am " + this.getLocalName() + " the new cog in the enemy-tribe-killing-machine!");
        log.info(ANSI_CYAN + this.getLocalName() +
                " My initial position is: "+this.currentPos.getXValue() + ","+this.currentPos.getYValue());


        unitManager = Utils.untilFindDFS(this, UNITMANAGER);
        getContentManager().registerLanguage(codec);
        getContentManager().registerOntology(ontology);

        //Recibe mensaje inicial de la tribu y le responde con sus coordenadas
        ACLMessage mensajeInicial = this.blockingReceive(MessageTemplate.MatchProtocol("soyTuPadre"));
        ACLMessage respuesta = mensajeInicial.createReply();
        try {
            respuesta.setContentObject(this.currentPos);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.send(respuesta);

        soyLibre(mensajeInicial.getSender());

        //Behaviours de las unidades:

        ParallelBehaviour ejecutadorInstrucciones = new ParallelBehaviour();
        ejecutadorInstrucciones.addSubBehaviour(new ConstructorBehaviour(this));
        ejecutadorInstrucciones.addSubBehaviour(new ExplorerBehaviour(this));
        ejecutadorInstrucciones.addSubBehaviour(new RecolectorBehaviour(this));
        ejecutadorInstrucciones.addSubBehaviour(new WaitBehaviour(this));

        this.addBehaviour(ejecutadorInstrucciones);



        //Comportamiento temporal que continuamente se mueve de forma aleatoria e intenta explotar la casilla
        // (da igual que tenga o no recurso).

        /*
        this.addBehaviour(new CyclicBehaviour(this) {
            //La dirección a la que se mueven las unidades se calcula aleatoriamente →
            // próximamente pensar estrategia de movimiento
            @Override
            public void action() {

                Random random = new Random();
                int nextDir;
                nextDir = random.nextInt(6) + 1;
                movement(nextDir);
                //Si hay recurso explotarlo hasta agotarlo
                while(exploitResource());

                Building building = new Building();
                String buildingType;
                buildingType="Town Hall";
                building.setType(buildingType);
                constructBuilding(building);
                buildingType="Store";
                building.setType(buildingType);
                constructBuilding(building);
                createUnit();
            }
        });

         */


    }



    public boolean exploitResource(){
        //2.2.2. As the unit controller I want to exploit the resource of my current position
        // cell so that I can increase the amount of this resource available in my repository
        // escribirle mensaje al unit manager para que este le deje explotar el recurso
        ACLMessage msg = Utils.newMsgWithOnto(this, unitManager, ACLMessage.REQUEST,
                codec, ontology, new CollectResource(), WoaOntologyVocabulary.COLLECT_RESOURCE);
        log.info(ANSI_CYAN + this.getLocalName() + " Sending exploit resource request");
        this.send(msg);

        ACLMessage response = this.blockingReceive(
                MessageTemplate.and(MessageTemplate.and(MessageTemplate.MatchLanguage(codec.getName()),
                                MessageTemplate.MatchOntology(ontology.getName())),
                        MessageTemplate.MatchProtocol(WoaOntologyVocabulary.COLLECT_RESOURCE)));
        switch (response.getPerformative()) {
            case ACLMessage.AGREE:
                log.info(ANSI_CYAN + this.getLocalName() + " Response received from " + response.getSender().getLocalName() + " about exploitation request: AGREE");
                //en el caso de que sea un agree, esperar a mensaje del UnitManager de que la explotación se ha completado o de si ha fallado
                ACLMessage responseAgree = this.blockingReceive(
                        MessageTemplate.and(MessageTemplate.and(MessageTemplate.MatchLanguage(codec.getName()),
                                        MessageTemplate.MatchOntology(ontology.getName())),
                                MessageTemplate.MatchProtocol(WoaOntologyVocabulary.COLLECT_RESOURCE)));
                if (responseAgree.getPerformative() == ACLMessage.FAILURE) {
                    log.info(ANSI_CYAN + this.getLocalName() + " Response received from " + response.getSender().getLocalName() +
                            " about exploitation request: FAILURE");
                } else if (responseAgree.getPerformative() == ACLMessage.INFORM) {
                    log.info(ANSI_CYAN + this.getLocalName() + " Response received from " + response.getSender().getLocalName() + " about exploitation request: INFORM");
                    return true;
                } else
                    log.warning(ANSI_RED + this.getLocalName() + " Error obtaining the second message form UnitManager during exploitation process");
                break;

            case ACLMessage.REFUSE:
                log.info(ANSI_CYAN + this.getLocalName() + " Response received from " + response.getSender().getLocalName() + " about exploitation request: REFUSE");
            default:
                log.info(ANSI_CYAN + this.getLocalName() + " Response received from " + response.getSender().getLocalName() + " about exploitation request: NOT_UNDERSTOOD");
        }
        return false;
    }


    public Coordinate movement(int direction) {
        Move move = new Move();
        Direction dir = new Direction(direction);
        move.setDest(dir);
        //3.2.1. As the unit controller I want to indicate a direction of movement (1..6) so that I can change my position
        ACLMessage msg = Utils.newMsgWithOnto(this, unitManager, ACLMessage.REQUEST,
                codec, ontology, move, WoaOntologyVocabulary.MOVE);
        log.info(ANSI_CYAN + this.getLocalName() + " Sending move request");
        this.send(msg);

        ACLMessage response = this.blockingReceive(
                MessageTemplate.and(MessageTemplate.and(MessageTemplate.MatchLanguage(codec.getName()),
                                MessageTemplate.MatchOntology(ontology.getName())),
                        MessageTemplate.MatchProtocol(WoaOntologyVocabulary.MOVE)));
        switch (response.getPerformative()) {
            case ACLMessage.AGREE:
                log.info(ANSI_CYAN + this.getLocalName() + " Response received from " + response.getSender().getLocalName() + " about move request: AGREE");
                //en el caso de que sea un agree, esperar a mensaje del UnitManager de que el movimiento se ha completado o de si ha fallado
                ACLMessage responseAgree = this.blockingReceive(
                        MessageTemplate.and(MessageTemplate.and(MessageTemplate.MatchLanguage(codec.getName()),
                                        MessageTemplate.MatchOntology(ontology.getName())),
                                MessageTemplate.MatchProtocol(WoaOntologyVocabulary.MOVE)));
                if (responseAgree.getPerformative() == ACLMessage.FAILURE) {
                    log.info(ANSI_CYAN + this.getLocalName() + " Response received from " + response.getSender().getLocalName() +
                            " about move request: FAILURE, the movement couldn't be completed");
                } else if (responseAgree.getPerformative() == ACLMessage.INFORM) {
                    Action actionMsg = Utils.extractMsgOntoContent(this, responseAgree);
                    Coordinate coord;
                    if (actionMsg != null) {
                        Destination dest = (Destination) actionMsg.getAction();
                        coord = dest.getNewDest();
                        log.info(ANSI_CYAN + this.getLocalName() + " Response received from " + response.getSender().getLocalName() +
                                " about move request: INFORM, the movement has been completed and the new position is (" +
                                coord.getXValue() + ", " + coord.getYValue() + ")");
                        return coord;
                    } else
                        log.warning(ANSI_RED + this.getLocalName() + " Error obtaining the new position of the unit");
                } else
                    log.warning(ANSI_RED + this.getLocalName() + " Error obtaining the second message from UnitManager during move request process");
                break;
            case ACLMessage.REFUSE:
                log.info(ANSI_CYAN + this.getLocalName() + " Response received from " + response.getSender().getLocalName() + " about move request: REFUSE");
            case ACLMessage.NOT_UNDERSTOOD:
                log.info(ANSI_CYAN + this.getLocalName() + " Response received from " + response.getSender().getLocalName() + " about move request: NOT_UNDERSTOOD");
            default: {
            }
        }
        return null;
    }


    public boolean createUnit() {
        // 3.1.6. As the unit controller, I want to create a new unit in my current position cell so that I can increase the number of units in my tribe
        ACLMessage msg = Utils.newMsgWithOnto(this, unitManager, ACLMessage.REQUEST,
                codec, ontology, null, WoaOntologyVocabulary.CREATE_UNIT);
        log.info(ANSI_CYAN + this.getLocalName() + " Sending create unit request");
        this.send(msg);
        //Recibir respuesta del unitManager:
        ACLMessage response = this.blockingReceive(
                MessageTemplate.and(MessageTemplate.and(MessageTemplate.MatchLanguage(codec.getName()),
                                MessageTemplate.MatchOntology(ontology.getName())),
                        MessageTemplate.MatchProtocol(WoaOntologyVocabulary.CREATE_UNIT)));
        switch (response.getPerformative()) {
            case ACLMessage.AGREE:
                log.info(ANSI_CYAN + this.getLocalName() + " Response received from " + response.getSender().getLocalName() + " about create unit request: AGREE");
                //en el caso de que sea un agree, esperar a mensaje del UnitManager de que la creación de la uinidad se ha completado o de si ha fallado
                ACLMessage responseAgree = this.blockingReceive(
                        MessageTemplate.and(MessageTemplate.and(MessageTemplate.MatchLanguage(codec.getName()),
                                        MessageTemplate.MatchOntology(ontology.getName())),
                                MessageTemplate.MatchProtocol(WoaOntologyVocabulary.CREATE_UNIT)));
                if (responseAgree.getPerformative() == ACLMessage.FAILURE) {
                    log.info(ANSI_CYAN + this.getLocalName() + " Response received from " + response.getSender().getLocalName() +
                            " about create unit request: FAILURE, the unit couldn't be created");
                } else if (responseAgree.getPerformative() == ACLMessage.INFORM) {
                    log.info(ANSI_CYAN + this.getLocalName() + " Response received from " + response.getSender().getLocalName() +
                            " about unit creation request: INFORM, a new unit has been created");
                    return true;
                } else {
                    log.warning(ANSI_RED + this.getLocalName() + " Error obtaining the second message from UnitManager during unit creation request process");
                }
                break;

            case ACLMessage.REFUSE:
                log.info(ANSI_CYAN + this.getLocalName() + " Response received from " + response.getSender().getLocalName() + " about unit creation request: REFUSE");
                break;

            case ACLMessage.NOT_UNDERSTOOD:
                log.info(ANSI_CYAN + this.getLocalName() + " Response received from " + response.getSender().getLocalName() + " about unit creation request: NOT_UNDERSTOOD");

                break;
            default: {}
        }
        return false;
    }


    public boolean constructBuilding(Building building) {
        ConstructBuilding constructBuilding = new ConstructBuilding();
        constructBuilding.setBuilding(building);
        // 3.1.6. As the unit controller, I want to create a new unit in my current position cell so that I can increase the number of units in my tribe
        ACLMessage msg = Utils.newMsgWithOnto(this, unitManager, ACLMessage.REQUEST,
                codec, ontology, constructBuilding, WoaOntologyVocabulary.CONSTRUCT_BUILDING);
        log.info(ANSI_CYAN + this.getLocalName() + " Sending construct building request");
        this.send(msg);
        //Recibir respuesta del unitManager:
        ACLMessage response = this.blockingReceive(
                MessageTemplate.and(MessageTemplate.and(MessageTemplate.MatchLanguage(codec.getName()),
                                MessageTemplate.MatchOntology(ontology.getName())),
                        MessageTemplate.MatchProtocol(WoaOntologyVocabulary.CONSTRUCT_BUILDING)));
        switch (response.getPerformative()) {
            case ACLMessage.AGREE:
                log.info(ANSI_CYAN + this.getLocalName() + " Response received from " + response.getSender().getLocalName() + " about create construct building request: AGREE");
                //en el caso de que sea un agree, esperar a mensaje del UnitManager de que la creación de la uinidad se ha completado o de si ha fallado
                ACLMessage responseAgree = this.blockingReceive(
                        MessageTemplate.and(MessageTemplate.and(MessageTemplate.MatchLanguage(codec.getName()),
                                        MessageTemplate.MatchOntology(ontology.getName())),
                                MessageTemplate.MatchProtocol(WoaOntologyVocabulary.CONSTRUCT_BUILDING)));
                if (responseAgree.getPerformative() == ACLMessage.FAILURE) {
                    log.info(ANSI_CYAN + this.getLocalName() + " Response received from " + response.getSender().getLocalName() +
                            " about construct building request: FAILURE, the building couldn't be constructed");
                } else if (responseAgree.getPerformative() == ACLMessage.INFORM) {
                    log.info(ANSI_CYAN + this.getLocalName() + " Response received from " + response.getSender().getLocalName() +
                            " about building construction request: INFORM, a new building has been constructed");
                    return true;
                } else {
                    log.warning(ANSI_RED + this.getLocalName() + " Error obtaining the second message from UnitManager during building construction request process");
                }
                break;

            case ACLMessage.REFUSE:
                log.info(ANSI_CYAN + this.getLocalName() + " Response received from " + response.getSender().getLocalName() + " about building construction request: REFUSE");
            case ACLMessage.NOT_UNDERSTOOD:
                log.info(ANSI_CYAN + this.getLocalName() + " Response received from " + response.getSender().getLocalName() + " about building construction request: NOT_UNDERSTOOD");
            default: {}
        }
        return false;
    }



    //metodo que envia mensaje de libre a la tribu
    public void soyLibre(AID tribu){
        ACLMessage msgLibertad = Utils.newMsgWithObject(this, ACLMessage.INFORM, this.currentPos, tribu, "libre");
        this.send(msgLibertad);
    }

}
