package es.upm.emse.absd.ontology.woa.actions;

import jade.content.AgentAction;
import jade.core.AID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignNewUnit implements AgentAction {
    private AID unitID;
}