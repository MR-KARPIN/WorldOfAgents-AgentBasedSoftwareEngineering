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
public class Direction implements Concept {
    private int dir;
}
