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
public class Coordinate implements Concept {
    private int xValue;
    private int yValue;

    @Override
    public String toString(){
        return xValue + " " + yValue;
    }
}