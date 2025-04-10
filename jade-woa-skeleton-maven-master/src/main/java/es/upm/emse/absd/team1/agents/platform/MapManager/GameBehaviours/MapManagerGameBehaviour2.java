package es.upm.emse.absd.team1.agents.platform.MapManager.GameBehaviours;

import es.upm.emse.absd.team1.agents.platform.MapManager.AgMapManager;
import jade.core.behaviours.ParallelBehaviour;
import lombok.extern.java.Log;

/*
2. Behaviour paralelo que se encarga de las funciones de la fase de juego:
    2.1 Mover unidades                                -->   GameBehaviour_Move
    2.2 Eliminar recurso explotado agotado
    2.3 Escuchar cambio de fase
    2.4 (Sprint 4) construir edificio
    2.5 (Sprint 4) creación unidad
   Este behaviour termina al terminar la fase de juego.
 */
@Log
public class MapManagerGameBehaviour2 extends ParallelBehaviour {

    public MapManagerGameBehaviour2(AgMapManager agente) {
        //2.1 Mover unidades
        this.addSubBehaviour(new MapManagerGameBehaviour_Move(agente));
        //2.2 Eliminar recurso explotado agotado
        this.addSubBehaviour(new MapManagerGameBehaviour_RemoveResource(agente));
        //2.3 Escuchar cambio de fase
        //this.addSubBehaviour();
        //2.4 (Sprint 4) construir edificio
        this.addSubBehaviour(new MapManagerGameBehaviour_Build(agente));
        this.addSubBehaviour(new MapManagerGameBehaviour_CheckCell(agente));

        //2.5 (Sprint 4) creación unidad
        this.addSubBehaviour(new MapManagerGameBehaviour_CheckNewUnit(agente));
        //this.addSubBehaviour(new MapManagerGameBehaviour_ReceiveNewUnit(agente));
    }


}
