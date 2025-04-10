package es.upm.emse.absd.team1.agents.tribe.behaviours.unitBehaviour;

import es.upm.emse.absd.ontology.woa.WoaOntologyVocabulary;
import es.upm.emse.absd.ontology.woa.actions.RevealCell;
import es.upm.emse.absd.ontology.woa.concepts.Cell;
import es.upm.emse.absd.team1.agents.Utils;
import es.upm.emse.absd.team1.agents.tribe.AgUnitController;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import lombok.extern.java.Log;

import java.util.ArrayList;

import static es.upm.emse.absd.team1.agents.Utils.*;

@Log
public class RecolectorBehaviour extends CyclicBehaviour {

    AgUnitController agente;

    public RecolectorBehaviour(AgUnitController a) {
        super(a);
        this.agente = a;
    }

    @Override
    public void action() {
        //Recibir mensaje de la tribu que me dice que haga x movimientos y mine el recurso x veces
        ACLMessage msg = this.getAgent().receive(MessageTemplate.MatchProtocol("recolecta"));
        if (msg!=null) {
            Object [] contenido;
            try {
                contenido = (Object []) msg.getContentObject();
            } catch (UnreadableException e) {
                throw new RuntimeException(e);
            }
            ArrayList<Integer> direcciones = (ArrayList<Integer>) contenido[0];
            int cantidad = (int) contenido [1];
            for (Integer direction : direcciones) {
                this.agente.setCurrentPos(this.agente.movement(direction));
            }

            boolean explotacionCorrecta = true;
            int iteraciones = 0;
            while(explotacionCorrecta && iteraciones<cantidad) {
                explotacionCorrecta = this.agente.exploitResource();
                iteraciones++;
            }

            //si la explotación falla es porque ya no hay recurso
            if(!explotacionCorrecta){
                log.info(ANSI_BLUE + this.getAgent().getLocalName() + " La casilla ya no se puede explotar, se agotó el recurso");
                Cell cell= new Cell();
                cell.setCoord(this.agente.getCurrentPos());
                cell.setContent("Ground");
                RevealCell revealCell = new RevealCell();
                revealCell.setCellContent(cell);
                //Mensaje a tribe controller con la info de la celda actualizada
                ACLMessage msgActualizarCell = Utils.newMsgWithOnto(this.agente, msg.getSender(), ACLMessage.INFORM,
                        this.agente.getCodec(), this.agente.getOntology(), revealCell, WoaOntologyVocabulary.REVEAL_CELL);
                log.info(ANSI_CYAN + this.agente.getLocalName() + " Sending the content of the new position to the tribe controller");
                this.agente.send(msgActualizarCell);
            }

            //Enviar libre
            this.agente.soyLibre(msg.getSender());
        }
    }
}
