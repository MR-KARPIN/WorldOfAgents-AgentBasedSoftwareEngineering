package es.upm.emse.absd.team1.agents.platform.UnitManager.RegistrationBehaviours;

import es.upm.emse.absd.ontology.woa.concepts.Coordinate;
import es.upm.emse.absd.team1.agents.platform.TribeUnitsInfo;
import es.upm.emse.absd.team1.agents.platform.UnitManager.AgUnitManager;
import jade.core.AID;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import lombok.extern.java.Log;
import java.util.ArrayList;

import static es.upm.emse.absd.team1.agents.Utils.*;

@Log

public class UnitManRegistrationBehaviour1_1 extends SimpleBehaviour {
    private final AgUnitManager agente;
    private final ArrayList<TribeUnitsInfo> tribesUnitsInfo;
    private int indexInitPos=0;

    public UnitManRegistrationBehaviour1_1(AgUnitManager a, ArrayList<TribeUnitsInfo> tribesUnitsInfo) {
        super(a);
        this.agente=a;
        this.tribesUnitsInfo = tribesUnitsInfo;
    }

    @Override
    public void action() {
        //Recibir mensaje de que se ha registrado una tribu nueva y se tienen que crear sus unidades
        ACLMessage msg = this.getAgent().receive(MessageTemplate.MatchProtocol("nuevaTribu"));
        if (msg!=null) {
            log.info(ANSI_YELLOW + this.getAgent().getLocalName() + " Mensaje recibido de crear una nueva tribu");
            newTribeCreation(msg);
        }
    }

    @Override
    public boolean done() {
        return this.agente.getPhase()>0;
    }


    private void newTribeCreation(ACLMessage msg) {
        try {
            Object[] newTribeInfo = (Object[]) msg.getContentObject();
            AID tribeAID = (AID) newTribeInfo[0];
            String tribeName = (String) newTribeInfo[1];

            log.info(ANSI_YELLOW + this.getAgent().getLocalName() + " New tribe name: " + tribeName);

            registrarInfoTribus( tribeAID, tribeName);

            //3.1.3. As the unit manager, I want to create 3 new units so that I can assign them to a new tribe
            for (int i = 0; i < 3 ; i++) this.agente.createUnit(tribeAID, null);
        } catch (UnreadableException e) { throw new RuntimeException(e); }
    }


    //asignar posiciones iniciales cada tribu tiene sus 3 unidades en la misma position inicial
    //cuando se usa una position inicial se pasa a la siguiente para que cada tribu tenga una distinta
    private void registrarInfoTribus(AID aidTribu, String nombreTribu) {
        Coordinate posicionInicial = this.agente.getInitialPositions().get(indexInitPos);
        int x=posicionInicial.getXValue();
        int y=posicionInicial.getYValue();
        indexInitPos++;
        tribesUnitsInfo.add(new TribeUnitsInfo(aidTribu, nombreTribu,x,y));

        //creamos lista de celdas conocidas de cada tribu vacía
        ArrayList<Coordinate> posiciones= new ArrayList<>();
        //posiciones.add(new Coordinate(x,y));
        agente.getTribesKnownCells().put(aidTribu,posiciones);
    }
}