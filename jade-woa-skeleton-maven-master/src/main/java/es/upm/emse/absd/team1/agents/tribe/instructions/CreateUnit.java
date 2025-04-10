package es.upm.emse.absd.team1.agents.tribe.instructions;

import lombok.Data;

import java.util.ArrayList;

@Data
public class CreateUnit implements Instruction{
    String type = "CreateUnit";
    ArrayList<Integer> directions;

    public CreateUnit(ArrayList<Integer> directions) {
        this.directions = directions;
    }
}
