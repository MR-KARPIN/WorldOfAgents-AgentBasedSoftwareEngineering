package es.upm.emse.absd.team1.agents.platform;

import es.upm.emse.absd.team1.agents.platform.Position;
import jade.core.AID;
import lombok.Data;

import java.util.ArrayList;

@Data
public class Unidad{
    private AID unidad;
    private Position posicion;
    public Unidad(AID aid, int x, int y) {
        this.unidad=aid;
        this.posicion=new Position(x, y);
    }
}
