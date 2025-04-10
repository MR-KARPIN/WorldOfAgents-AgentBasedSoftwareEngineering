package es.upm.emse.absd.team1.agents.platform.UnitManager.GameBehaviours;


import es.upm.emse.absd.team1.agents.platform.UnitManager.AgUnitManager;
import jade.core.behaviours.ParallelBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import lombok.extern.java.Log;

/*
    2. Behaviour paralelo que realiza todas las siguientes funciones de la fase de juego:
	        2.1 Mover unidades
            2.2 Explotar recurso
            2.3 Escuchar cambio de fase
            2.4 (en un futuro construir edificio)
            2.5 (en un futuro crear unidades).
       Este behaviour acaba al terminar la fase de juego.
 */
@Log
public class UnitManagerGameBehaviour_BehaviourManager extends ParallelBehaviour {

    private final AgUnitManager agente;
    //2.3 Escuchar cambio de fase (cíclicamente).
    SimpleBehaviour cambioFase = new SimpleBehaviour() {
        @Override
        public void action() {
            agente.receiveNotifyChangePhase();
        }

        @Override
        public boolean done() {
            return agente.getPhase()>1;
        }
    };

    public UnitManagerGameBehaviour_BehaviourManager(AgUnitManager agente) {
        this.agente=agente;
        //2.1 Mover unidades
        this.addSubBehaviour(new UnitManagerGameBehaviour_Move(agente));
        //2.2 Explotar recurso
        this.addSubBehaviour(new UnitManagerGameBehaviour_ResourceExploitation(agente));
        //2.3 Escuchar cambio de fase
        this.addSubBehaviour(cambioFase);
        //2.4 (en un futuro construir edificio)
        this.addSubBehaviour(new UnitManagerGameBehaviour_Building(agente));
        //2.5 (en un futuro crear unidades).
        this.addSubBehaviour(new UnitManagerGameBehaviour_CreateUnit(agente));
    }







}

