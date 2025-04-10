package es.upm.emse.absd.team1.agents.tribe.behaviours.unitBehaviour;

import es.upm.emse.absd.ontology.woa.concepts.Building;
import es.upm.emse.absd.team1.agents.Utils;
import es.upm.emse.absd.team1.agents.tribe.AgUnitController;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import lombok.extern.java.Log;

import java.util.ArrayList;

@Log
public class ConstructorBehaviour extends CyclicBehaviour {

    AgUnitController agente;
    public ConstructorBehaviour(AgUnitController a) {
        super(a);
        this.agente = a;
    }

    @Override
    public void action() {
        //Recibir mensaje de la tribu de que explore
        ACLMessage msg = this.getAgent().receive(MessageTemplate.MatchProtocol("construye"));
        if (msg != null) {
            Object [] contenido;
            try {
                contenido = (Object []) msg.getContentObject();
            } catch (UnreadableException e) {
                throw new RuntimeException(e);
            }
            //Obtengo la lista de direcciones y realizo los movimientos
            ArrayList<Integer> direcciones = (ArrayList<Integer>) contenido[0];
            for (Integer direction : direcciones) {
                this.agente.setCurrentPos(this.agente.movement(direction));
            }

            //Obtengo tipo de edificio (o nulo para crear unidad) y ejecuto la acción
            //Puede ser building o puede ser nulo para hacer unidad
            boolean resultado;
            if(contenido[1]!=null){
                Building building = (Building) contenido [1];
                if(!this.agente.constructBuilding(building)){
                    ACLMessage msgFallo = Utils.newMsgWithObject(this.getAgent(), ACLMessage.INFORM, building.getType(), msg.getSender(), "problemas");
                    this.getAgent().send(msgFallo);
                }
            }
            else{
                if(!this.agente.createUnit()){
                    ACLMessage msgFallo = Utils.newMsgWithObject(this.getAgent(), ACLMessage.INFORM,"Unit", msg.getSender(), "problemas");
                    this.getAgent().send(msgFallo);
                }
            }
            //Enviar libre
            this.agente.soyLibre(msg.getSender());
        }
    }
}
