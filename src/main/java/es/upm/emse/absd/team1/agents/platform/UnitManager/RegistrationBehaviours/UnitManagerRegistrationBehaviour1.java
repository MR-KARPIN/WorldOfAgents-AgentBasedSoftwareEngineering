package es.upm.emse.absd.team1.agents.platform.UnitManager.RegistrationBehaviours;


import es.upm.emse.absd.team1.agents.platform.TribeUnitsInfo;
import es.upm.emse.absd.team1.agents.platform.UnitManager.AgUnitManager;
import jade.core.behaviours.ParallelBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import lombok.extern.java.Log;

import java.util.ArrayList;

/*
    1. Behaviour paralelo que se encarga de las funciones en la fase de registro,
                    1.1 Crea las 3 unidades iniciales de una nueva tribu (cíclicamente)
                    1.2 Escuchar cambio de fase (cíclicamente).
       Este behaviour acaba al terminar la fase de registro.
 */
@Log
public class UnitManagerRegistrationBehaviour1 extends ParallelBehaviour {

    private final AgUnitManager agente;
    private ArrayList<TribeUnitsInfo> tribesUnitsInfo;

    public UnitManagerRegistrationBehaviour1(AgUnitManager agente, ArrayList<TribeUnitsInfo> tribesUnitsInfo) {
        this.agente = agente;
        this.tribesUnitsInfo = tribesUnitsInfo;
        //1.1 Crea las 3 unidades iniciales de una nueva tribu (cíclicamente)
        this.addSubBehaviour(new UnitManRegistrationBehaviour1_1(agente, tribesUnitsInfo));
        //1.2 Escuchar cambio de fase (cíclicamente).
        this.addSubBehaviour(cambioFase);
    }

    SimpleBehaviour cambioFase = new SimpleBehaviour() {
        @Override
        public void action() { agente.receiveNotifyChangePhase(); }

        @Override
        public boolean done() {
            return agente.getPhase()>0;
        }
    };






}

