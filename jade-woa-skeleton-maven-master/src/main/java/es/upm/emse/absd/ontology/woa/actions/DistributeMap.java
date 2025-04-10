package es.upm.emse.absd.ontology.woa.actions;

import es.upm.emse.absd.ontology.woa.concepts.Coordinate;
import jade.content.AgentAction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DistributeMap implements AgentAction {
    private Coordinate mapSize;
}