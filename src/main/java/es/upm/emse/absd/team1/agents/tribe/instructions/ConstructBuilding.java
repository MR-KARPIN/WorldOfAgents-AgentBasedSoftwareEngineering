package es.upm.emse.absd.team1.agents.tribe.instructions;

import es.upm.emse.absd.ontology.woa.concepts.Building;
import lombok.Data;

import java.util.ArrayList;

@Data
public class ConstructBuilding implements Instruction{
    Building building;
    String type = "ConstructBuilding";
    ArrayList<Integer> directions;
    public ConstructBuilding(Building building, ArrayList<Integer> directions) {
        this.building = building;
        this.directions = directions;
    }
}
