package es.upm.emse.absd.team1.agents.platform.MapManager.GameBehaviours;

import es.upm.emse.absd.GUIUtils;
import es.upm.emse.absd.ontology.woa.WoaOntology;
import es.upm.emse.absd.ontology.woa.WoaOntologyVocabulary;
import es.upm.emse.absd.ontology.woa.actions.InformNewBuilding;
import es.upm.emse.absd.ontology.woa.concepts.Cell;
import es.upm.emse.absd.ontology.woa.concepts.Coordinate;
import es.upm.emse.absd.team1.agents.platform.MapManager.AgMapManager;
import es.upm.emse.absd.team1.agents.platform.NewBuildingFinished;
import es.upm.emse.absd.team1.agents.platform.TribeAccountant.AgTribeAccountant;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

import static es.upm.emse.absd.team1.agents.Utils.ANSI_YELLOW;
import static es.upm.emse.absd.team1.agents.Utils.newMsgWithOnto;

public class MapManagerGameBehaviour_Build extends CyclicBehaviour {

    private final Codec codec = new SLCodec();
    private final Ontology ontology = WoaOntology.getInstance();
    private AgMapManager agente;
    public MapManagerGameBehaviour_Build(AgMapManager a) {
        super(a);
        this.agente=a;
    }
    @Override
    public void action() {
        ACLMessage msg = this.getAgent().receive(MessageTemplate.MatchProtocol("BuildingCreated"));
        if(msg!=null){
            try {
                //Saco la informacion
                NewBuildingFinished newBuildingFinished = (NewBuildingFinished) msg.getContentObject();
                Coordinate newBuildingCoordinates = newBuildingFinished.getCoordenadas();
                String newBuildingType = newBuildingFinished.getTipo();
                AID tribeAID = newBuildingFinished.getTribeAID();
                AID unitAID = newBuildingFinished.getUnitAID();

                //Construyo el contenido del mensaje
                Cell cell = new Cell(newBuildingCoordinates, newBuildingType);
                InformNewBuilding informNewBuilding = new InformNewBuilding(cell);

                agente.getResourceGameMap().setValue(newBuildingCoordinates.getXValue(), newBuildingCoordinates.getYValue(), tribeAID.getLocalName()+newBuildingType, false);

                //mando el mensaje al TribeController
                ACLMessage sendCurrentResources = newMsgWithOnto(this.getAgent(), tribeAID,
                        ACLMessage.INFORM, codec, ontology,
                        informNewBuilding,
                        WoaOntologyVocabulary.INFORM_NEW_BUILDING);
                this.agente.send(sendCurrentResources);

                //Llamo a la GUI
                GUIUtils.build(unitAID.getLocalName(), newBuildingType);

            } catch (UnreadableException e) {
                throw new RuntimeException(e);
            }
        }

    }
}
