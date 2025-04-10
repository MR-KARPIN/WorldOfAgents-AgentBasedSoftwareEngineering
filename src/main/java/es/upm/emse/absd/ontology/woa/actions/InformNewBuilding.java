package es.upm.emse.absd.ontology.woa.actions;

import es.upm.emse.absd.ontology.woa.concepts.Cell;
import jade.content.AgentAction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InformNewBuilding implements AgentAction {
    private Cell cell;
}
