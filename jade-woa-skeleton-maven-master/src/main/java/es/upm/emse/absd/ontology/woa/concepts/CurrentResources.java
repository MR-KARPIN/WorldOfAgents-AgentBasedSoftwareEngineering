package es.upm.emse.absd.ontology.woa.concepts;

import jade.content.Concept;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurrentResources implements Concept {
    private Resource gold;
    private Resource stone;
    private Resource wood;
}
