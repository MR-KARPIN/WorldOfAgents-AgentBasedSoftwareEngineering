package es.upm.emse.absd.team1.agents.tribe.instructions;

import es.upm.emse.absd.ontology.woa.concepts.Coordinate;
import lombok.Data;

import java.util.ArrayList;

@Data
public class Move implements Instruction{
    ArrayList<Integer> directions;
    String type = "Move";
    public Move(ArrayList<Integer> directions) {
        this.directions = directions;
    }
}
