package es.upm.emse.absd.team1.agents.tribe.behaviours.unitBehaviour;

import es.upm.emse.absd.team1.agents.tribe.AgUnitController;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

import java.util.ArrayList;

import static java.lang.Thread.sleep;

public class WaitBehaviour extends CyclicBehaviour {

    AgUnitController agente;
    public WaitBehaviour(AgUnitController a) {
        super(a);
        this.agente = a;
    }

    @Override
    public void action() {
        //Recibir mensaje de la tribu de que explore toodo el mapa
        ACLMessage msg = this.getAgent().receive(MessageTemplate.MatchProtocol("esperate"));
        if (msg != null) {
            try { sleep(500); }
            catch (InterruptedException e) { throw new RuntimeException(e); }

            //Enviar libre
            this.agente.soyLibre(msg.getSender());
        }
    }
}
