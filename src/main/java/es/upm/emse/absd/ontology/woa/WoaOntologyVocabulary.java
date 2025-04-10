package es.upm.emse.absd.ontology.woa;

public class WoaOntologyVocabulary {
    public static final String ONTOLOGY_NAME = "WoaOntology";
    //actions
    public static final String INFORM_CURRENT_RESOURCES = "informCurrentResources";
    public static final String REGISTER = "register";
    public static final String CHANGE_PHASE = "changePhase";
    public static final String ASSIGN_NEW_UNIT = "assignNewUnit";
    public static final String ALLOCATE_UNIT = "allocateUnit";
    public static final String DISTRIBUTE_MAP = "distributeMap";
    public static final String MOVE = "move";
    public static final String COLLECT_RESOURCE = "collectResource";
    public static final String REVEAL_CELL = "revealCell";
    public static final String CONSTRUCT_BUILDING = "constructBuilding";
    public static final String CREATE_UNIT = "createUnit";
    public static final String INFORM_NEW_BUILDING = "informNewBuilding";
    //concepts
    public static final String RESOURCE = "resource";
    public static final String RESOURCE_TYPE = "typeRes";
    public static final String RESOURCE_AMOUNT = "amount";

    public static final String CURRENT_RESOURCES = "currentResources";
    public static final String RESOURCE_GOLD = "gold";
    public static final String RESOURCE_STONE = "stone";
    public static final String RESOURCE_WOOD = "wood";
    public static final String TRIBE_RESOURCES = "tribeResources";
    public static final String STORAGE = "storage";

    public static final String STORAGE_CAPACITY = "storageCapacity";
    public static final String STORAGE_SIZE = "size";

    public static final String NEW_PHASE = "newPhase";
    public static final String PHASE_ID = "phase";

    public static final String COORDINATE = "coordinate";
    public static final String COORDINATE_XVALUE = "xValue";
    public static final String COORDINATE_YVALUE = "yValue";
    public static final String COORDINATE_INITIAL_POSITION = "initialPosition";
    public static final String COORDINATE_MAP_SIZE = "mapSize";
    public static final String AID_UNIT_ID = "unitID";

    public static final String CELL = "cell";
    public static final String CELL_COORD = "coord";
    public static final String CELL_CONTENT = "content";
    public static final String BUILDING_TYPE = "type";
    public static final String DESTINATION = "destination";
    public static final String DESTINATION_DEST = "newDest";
    public static final String DIRECTION = "direction";
    public static final String BUILDING = "building";
    public static final String DIRECTION_DIR = "dir";
    public static final String MOVE_DEST = "dest";
    public static final String CONSTRUCT_BUILDING_BUILDING = "building";
    public static final String REVEAL_CELL_CONTENT = "cellContent";
    public static final String INFORM_NEW_BUILDING_CELL = "cell";
    private WoaOntologyVocabulary() {}
}
