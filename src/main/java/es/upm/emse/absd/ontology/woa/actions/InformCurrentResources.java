package es.upm.emse.absd.ontology.woa.actions;

import es.upm.emse.absd.ontology.woa.concepts.CurrentResources;
import es.upm.emse.absd.ontology.woa.concepts.StorageCapacity;
import jade.content.AgentAction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InformCurrentResources implements AgentAction {

    private CurrentResources tribeResources;
    private StorageCapacity storage;
}
