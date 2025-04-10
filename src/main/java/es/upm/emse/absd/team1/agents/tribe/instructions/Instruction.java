package es.upm.emse.absd.team1.agents.tribe.instructions;

import lombok.Data;

public interface Instruction {
    String type = "";

    String getType();
}
