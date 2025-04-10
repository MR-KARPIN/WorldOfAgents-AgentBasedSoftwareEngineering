package es.upm.emse.absd.team1.agents.tribe.behaviours.unitBehaviour;

import es.upm.emse.absd.ontology.woa.concepts.Coordinate;
import es.upm.emse.absd.team1.agents.Utils;
import es.upm.emse.absd.team1.agents.tribe.AgUnitController;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

import java.util.ArrayList;

public class ExplorerBehaviour extends CyclicBehaviour {

    AgUnitController agente;

    public ExplorerBehaviour(AgUnitController a) {
        super(a);
        this.agente = a;
    }

    @Override
    public void action() {
        //Recibir mensaje de la tribu de que explore toodo el mapa
        ACLMessage msg = this.getAgent().receive(MessageTemplate.MatchProtocol("explora"));
        if (msg != null) {
            try {
                ArrayList<Integer> direcciones = (ArrayList<Integer>) msg.getContentObject();
                for (Integer direction : direcciones){
                    this.agente.setCurrentPos( this.agente.movement(direction));
                }
            } catch (UnreadableException e) { throw new RuntimeException(e); }

            //Enviar libre
            this.agente.soyLibre(msg.getSender());
        }

    }


}
