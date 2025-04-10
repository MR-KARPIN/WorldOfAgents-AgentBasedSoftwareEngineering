package es.upm.emse.absd.ontology.woa;

import es.upm.emse.absd.ontology.woa.actions.*;
import es.upm.emse.absd.ontology.woa.concepts.*;
import static es.upm.emse.absd.ontology.woa.WoaOntologyVocabulary.*;
import jade.content.onto.BasicOntology;
import jade.content.onto.Ontology;
import jade.content.schema.AgentActionSchema;
import jade.content.schema.ConceptSchema;
import jade.content.schema.ObjectSchema;
import jade.content.schema.TermSchema;
import lombok.extern.java.Log;


@Log
public class WoaOntology extends Ontology {
    // The singleton instance of this ontology
    private static final Ontology theInstance = new WoaOntology();
    public static Ontology getInstance() { return theInstance; }

    public WoaOntology() {
        super(ONTOLOGY_NAME, BasicOntology.getInstance());

        try {

            // adding AgentAction(s)
            AgentActionSchema informCurrentResourcesSchema = new AgentActionSchema(INFORM_CURRENT_RESOURCES);
            add(informCurrentResourcesSchema, InformCurrentResources.class);

            AgentActionSchema registerSchema = new AgentActionSchema(REGISTER);
            add(registerSchema, Register.class);

            AgentActionSchema changePhaseSchema = new AgentActionSchema(CHANGE_PHASE);
            add(changePhaseSchema, ChangePhase.class);

            AgentActionSchema assignNewUnitSchema = new AgentActionSchema(ASSIGN_NEW_UNIT);
            add(assignNewUnitSchema, AssignNewUnit.class);

            AgentActionSchema allocateUnitSchema = new AgentActionSchema(ALLOCATE_UNIT);
            add(allocateUnitSchema, AllocateUnit.class);

            AgentActionSchema distributeMapSchema = new AgentActionSchema(DISTRIBUTE_MAP);
            add(distributeMapSchema, DistributeMap.class);

            AgentActionSchema moveSchema = new AgentActionSchema(MOVE);
            add(moveSchema, Move.class);

            AgentActionSchema collectResourceSchema = new AgentActionSchema(COLLECT_RESOURCE);
            add(collectResourceSchema, CollectResource.class);

            AgentActionSchema revealCellSchema = new AgentActionSchema(REVEAL_CELL);
            add(revealCellSchema, RevealCell.class);

            AgentActionSchema constructBuildingSchema = new AgentActionSchema(CONSTRUCT_BUILDING);
            add(constructBuildingSchema, ConstructBuilding.class);

            AgentActionSchema createUnitSchema = new AgentActionSchema(CREATE_UNIT);
            add(createUnitSchema, CreateUnit.class);

            AgentActionSchema informNewBuildingSchema = new AgentActionSchema(INFORM_NEW_BUILDING);
            add(informNewBuildingSchema, InformNewBuilding.class);

            // adding Predicate(s)


            // adding Concept(s)
            ConceptSchema resourceSchema = new ConceptSchema(RESOURCE);
            add(resourceSchema, Resource.class);

            ConceptSchema currentResourcesSchema = new ConceptSchema(CURRENT_RESOURCES);
            add(currentResourcesSchema, CurrentResources.class);

            ConceptSchema storageCapacitySchema = new ConceptSchema(STORAGE_CAPACITY);
            add(storageCapacitySchema, StorageCapacity.class);

            ConceptSchema newPhaseSchema = new ConceptSchema(NEW_PHASE);
            add(newPhaseSchema, NewPhase.class);

            ConceptSchema coordinateSchema = new ConceptSchema(COORDINATE);
            add(coordinateSchema, Coordinate.class);

            ConceptSchema cellSchema = new ConceptSchema(CELL);
            add(cellSchema, Cell.class);

            ConceptSchema destinationSchema = new ConceptSchema(DESTINATION);
            add(destinationSchema, Destination.class);

            ConceptSchema directionSchema = new ConceptSchema(DIRECTION);
            add(directionSchema, Direction.class);

            ConceptSchema buildingSchema = new ConceptSchema(BUILDING);
            add(buildingSchema, Building.class);



            // adding fields
            resourceSchema.add(RESOURCE_TYPE, (TermSchema)getSchema(BasicOntology.STRING), ObjectSchema.MANDATORY);
            resourceSchema.add(RESOURCE_AMOUNT, (TermSchema)getSchema(BasicOntology.FLOAT), ObjectSchema.MANDATORY);

            currentResourcesSchema.add(RESOURCE_GOLD, resourceSchema, ObjectSchema.MANDATORY);
            currentResourcesSchema.add(RESOURCE_STONE, resourceSchema, ObjectSchema.MANDATORY);
            currentResourcesSchema.add(RESOURCE_WOOD, resourceSchema, ObjectSchema.MANDATORY);

            storageCapacitySchema.add(STORAGE_SIZE, (TermSchema)getSchema(BasicOntology.FLOAT), ObjectSchema.MANDATORY);

            newPhaseSchema.add(PHASE_ID,(TermSchema)getSchema(BasicOntology.INTEGER), ObjectSchema.MANDATORY);

            informCurrentResourcesSchema.add(TRIBE_RESOURCES, currentResourcesSchema, ObjectSchema.MANDATORY);
            informCurrentResourcesSchema.add(STORAGE, storageCapacitySchema, ObjectSchema.MANDATORY);

            coordinateSchema.add(COORDINATE_XVALUE, (TermSchema)getSchema(BasicOntology.INTEGER), ObjectSchema.MANDATORY);
            coordinateSchema.add(COORDINATE_YVALUE, (TermSchema)getSchema(BasicOntology.INTEGER), ObjectSchema.MANDATORY);

            assignNewUnitSchema.add(AID_UNIT_ID, (TermSchema)getSchema(BasicOntology.AID), ObjectSchema.MANDATORY);

            allocateUnitSchema.add(COORDINATE_INITIAL_POSITION, coordinateSchema, ObjectSchema.MANDATORY);

            distributeMapSchema.add(COORDINATE_MAP_SIZE, coordinateSchema, ObjectSchema.MANDATORY);

            directionSchema.add(DIRECTION_DIR, (TermSchema)getSchema(BasicOntology.INTEGER), ObjectSchema.MANDATORY);

            destinationSchema.add(DESTINATION_DEST, coordinateSchema, ObjectSchema.MANDATORY);

            cellSchema.add(CELL_COORD, coordinateSchema, ObjectSchema.MANDATORY);
            cellSchema.add(CELL_CONTENT, (TermSchema)getSchema(BasicOntology.STRING), ObjectSchema.MANDATORY);

            buildingSchema.add(BUILDING_TYPE, (TermSchema)getSchema(BasicOntology.STRING), ObjectSchema.MANDATORY);

            moveSchema.add(MOVE_DEST, directionSchema, ObjectSchema.MANDATORY);

            constructBuildingSchema.add(CONSTRUCT_BUILDING_BUILDING, buildingSchema, ObjectSchema.MANDATORY);

            revealCellSchema.add(REVEAL_CELL_CONTENT, cellSchema, ObjectSchema.MANDATORY);
            informNewBuildingSchema.add(INFORM_NEW_BUILDING_CELL, cellSchema, ObjectSchema.MANDATORY);

        }catch (java.lang.Exception e) {
            log.warning(e.getMessage());
        }
    }
}