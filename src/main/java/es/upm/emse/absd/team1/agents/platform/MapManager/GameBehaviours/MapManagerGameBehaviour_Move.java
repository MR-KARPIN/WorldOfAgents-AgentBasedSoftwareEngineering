package es.upm.emse.absd.team1.agents.platform.MapManager.GameBehaviours;

import es.upm.emse.absd.GUIUtils;
import es.upm.emse.absd.ontology.woa.WoaOntology;
import es.upm.emse.absd.ontology.woa.WoaOntologyVocabulary;
import es.upm.emse.absd.ontology.woa.actions.RevealCell;
import es.upm.emse.absd.ontology.woa.concepts.Cell;
import es.upm.emse.absd.ontology.woa.concepts.Coordinate;
import es.upm.emse.absd.team1.agents.Utils;
import es.upm.emse.absd.team1.agents.platform.MapManager.AgMapManager;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import lombok.extern.java.Log;

import static es.upm.emse.absd.team1.agents.Utils.*;


/*
2. Se ubica dentro de MapManagerGameBehaviour que es paralelo.
         2.1 Este es el behaviour que hace Mover unidades, es cíclico hasta que

 */
@Log
public class MapManagerGameBehaviour_Move extends CyclicBehaviour {

    private final Codec codec = new SLCodec();
    private final Ontology ontology = WoaOntology.getInstance();
    private final AgMapManager agente;

    public MapManagerGameBehaviour_Move(AgMapManager a) {
        super(a);
        this.agente=a;
    }
    @Override
    public void action() {
        //recibir mensaje del unitmanager con el num(1..6) y el aid para mover una unidad
        ACLMessage msg;
        if((msg = this.getAgent().receive(MessageTemplate.MatchProtocol("moveUnit"))) != null) {
            log.info(ANSI_YELLOW + this.getAgent().getLocalName() + " Recibe mensaje de mover unidad");
            Object[] moveUnitInfo;
            try { moveUnitInfo = (Object[]) msg.getContentObject(); }
            catch (UnreadableException e) { throw new RuntimeException(e); }
            AID unitAID =  (AID) moveUnitInfo[0];
            AID tribeAID = (AID) moveUnitInfo[2];
            int positionX = (int) moveUnitInfo[3];
            int positionY = (int) moveUnitInfo[4];
            int direction = (int) moveUnitInfo[5];

            log.info(ANSI_YELLOW+ this.getAgent().getLocalName() + " Petición de mover la unidad "+
                    unitAID.getLocalName() + " de la tribu "+tribeAID.getLocalName()+ " que está en la posición "+
                    positionX + " "+ positionY + " en la dirección "+ direction);


            Coordinate newPos = assignNewPos(new Coordinate(positionX, positionY), direction);
            sendNewPos(newPos, msg.getSender(), tribeAID);
            //Mueve la unidad en la interfaz
            GUIUtils.moveAgent(tribeAID.getLocalName(),unitAID.getLocalName(), newPos.getXValue(), newPos.getYValue());


            //Mensaje del UnitManager cuando una tribu ha descubierto una nueva celda
        } else if ((msg = this.getAgent().receive(MessageTemplate.MatchProtocol("RevealCell"))) != null) {
            log.info(ANSI_YELLOW + this.getAgent().getLocalName() + " Recibe mensaje de que la tribu descubrió una nueva celda");
            Object[] newCell;
            try { newCell = (Object[]) msg.getContentObject(); }
            catch (UnreadableException e) { throw new RuntimeException(e); }
            AID tribeAID = (AID) newCell[0];
            Coordinate newPos = (Coordinate) newCell[1];
            log.info(ANSI_YELLOW + this.getAgent().getLocalName() + " La tribu " + tribeAID.getLocalName() +
                    " ha descubierto una nueva celda del mapa: "+newPos.getXValue()+ " , " + newPos.getYValue());
            this.informContentNewCellTribeController(tribeAID,newPos);
        }

    }

    private void sendNewPos(Coordinate movingCell, AID unitManager, AID tribeAID) {
        //mandar mensaje al unitmanager con la nueva posición
        Object[] newPositionInfo = {tribeAID,movingCell};
        ACLMessage newUnitManMsg = newMsgWithObject(this.getAgent(), ACLMessage.INFORM,
                newPositionInfo, unitManager,"newPosition");
        log.info(ANSI_YELLOW + this.getAgent().getLocalName() +
                " Mensaje enviado a " + unitManager.getLocalName() + " con la nueva posición: " +
                "(" + movingCell.getXValue() + ", " + movingCell.getYValue() + ")");
        this.getAgent().send(newUnitManMsg);
    }


    private Coordinate assignNewPos(Coordinate posActual, int direction){
        //Calcular la posición nueva de la unidad
        int filaActual = posActual.getXValue();
        int columnaActual = posActual.getYValue();
        //ver las dimensiones del mapa
        int filas = (int) agente.getMapWidth();
        int columnas = (int) agente.getMapHeight();

        switch (direction) {
            case 1 -> { // Arriba a la derecha
                filaActual--;
                columnaActual++;
            }

            case 2-> {// Abajo a la derecha
                filaActual++;
                columnaActual++;
            }
            case 3-> filaActual += 2; // Abajo
            case 4 -> {// Abajo a la izquierda
                filaActual++;
                columnaActual--;
            }
            case 5-> {// Arriba a la izquierda
                filaActual--;
                columnaActual--;
            }
            case 6-> filaActual -= 2; // Arriba
            default-> System.out.println("Dirección no válida.");
        }
        // Ajustar la posición si se sale del mapa
        if (filaActual < 1) { // se sale por debajo
            filaActual = filaActual==0 ? filas : filas-1;
        } else if (filaActual > filas) { // se sale por arriba
            filaActual = filaActual==filas+1 ? 1 : 2;
        }
        if (columnaActual < 1) columnaActual = columnas; // se sale por debajo
        else if (columnaActual > columnas) columnaActual = 1; // se sale por arriba

        log.info(ANSI_YELLOW + this.getAgent().getLocalName() + "Nueva posición: (" + filaActual + "," + columnaActual + ")");

        return new Coordinate(filaActual,columnaActual);
    }

    private void informContentNewCellTribeController(AID tribeAID, Coordinate movingCell) {
        //Obtener info de la celda
        String content = this.agente.getResourceGameMap().getValue(movingCell.getXValue(), movingCell.getYValue());
        log.info(ANSI_YELLOW + this.getAgent().getLocalName() + " The content of the new Position: "+ movingCell.getXValue() + "," + movingCell.getYValue()
                + " is: " + content + " and it is sent to: " + tribeAID.getLocalName());
        Cell cell= new Cell();
        cell.setCoord(movingCell);
        cell.setContent(content);
        RevealCell revealCell = new RevealCell();
        revealCell.setCellContent(cell);
        //Mensaje a tribe controller con la info de la celda nueva
        ACLMessage msg = Utils.newMsgWithOnto(this.agente, tribeAID, ACLMessage.INFORM,
                codec, ontology, revealCell, WoaOntologyVocabulary.REVEAL_CELL);
        log.info(ANSI_YELLOW + this.agente.getLocalName() + " Sending the content of the new position to the tribe controller");
        this.agente.send(msg);
    }


}
