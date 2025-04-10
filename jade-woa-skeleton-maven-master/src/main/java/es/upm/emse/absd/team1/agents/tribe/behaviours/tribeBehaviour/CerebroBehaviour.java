package es.upm.emse.absd.team1.agents.tribe.behaviours.tribeBehaviour;

import es.upm.emse.absd.ontology.woa.WoaOntology;
import es.upm.emse.absd.ontology.woa.WoaOntologyVocabulary;
import es.upm.emse.absd.ontology.woa.actions.AssignNewUnit;
import es.upm.emse.absd.ontology.woa.actions.InformCurrentResources;
import es.upm.emse.absd.ontology.woa.actions.InformNewBuilding;
import es.upm.emse.absd.ontology.woa.actions.RevealCell;
import es.upm.emse.absd.ontology.woa.concepts.Building;
import es.upm.emse.absd.ontology.woa.concepts.Cell;
import es.upm.emse.absd.ontology.woa.concepts.Coordinate;
import es.upm.emse.absd.team1.agents.Utils;
import es.upm.emse.absd.team1.agents.tribe.AgTribe;
import es.upm.emse.absd.team1.agents.tribe.instructions.*;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.ParallelBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import lombok.extern.java.Log;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import static es.upm.emse.absd.team1.agents.Utils.*;
import static es.upm.emse.absd.team1.agents.Utils.newMsgWithObject;

@Log
public class CerebroBehaviour extends ParallelBehaviour {

    private final Codec codec = new SLCodec();
    private final Ontology ontology = WoaOntology.getInstance();

    private final AgTribe agent;

    private Queue<AID> libres = new LinkedList<>();


    private Coordinate posAyuntamiento = null;
    private boolean construyendoAyunta=false;
    private boolean construyendoTienda=false;
    private boolean hayNuevaUnidad = false;
    private boolean initialInstructions = false;
    private Queue<Coordinate> candidatasAyuntamiento = new LinkedList<>();
    private Queue<Coordinate> candidatasTienda = new LinkedList<>();

    //HashMap<AID, Queue<Instruction>> instrucciones = new HashMap<>();

    public CerebroBehaviour(AgTribe a) {
        super();
        this.agent = a;


        this.addSubBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                //recibe mensajes y actualiza
                receiveInfoNewCell();
                receiveCurrentResources();
                receiveNewBuilding();
                receiveNewUnit();
                receiveProblemBuilding();
            }
        });


        this.addSubBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                //Se gestiona aqui la parte de pensar todas las cosas
                if(!initialInstructions) {
                    // PRIMERA FASE: Explorar todas las casillas
                    log.info(ANSI_CYAN + this.getAgent().getLocalName() + " THE FIRST PHASE OF OUR STRATEGY HAS BEGUN...");

                    // Unidad 1 explora el mapa
                    ArrayList<Integer> direcciones = calcularRutaExploradorInicial(a.getMapDimensions().first(), a.getMapDimensions().second());
                    agent.getInstrucciones().get(agent.getUnidades().get(0)).add(new Move(direcciones));

                    initialInstructions = true;
                }

                if (!libres.isEmpty()){
                    AID pedidor = libres.peek();
                    boolean rentableTienda = (agent.getGold()+agent.getStone()+agent.getWood())>=agent.getStorage()*0.8;
                    if (!construyendoAyunta && posAyuntamiento == null) { // no hay ayuntamiento ni se está construyendo
                        //log.info(ANSI_CYAN + this.getAgent().getLocalName() + " MISION: HACER AYUNTAMIENTO");
                        int neededWood = 200;
                        int neededStone = 150;
                        int neededGold = 250;
                        //Check si tenemos suficientes recursos para hacer ayuntamiento
                        if (agent.getWood() >= neededWood && agent.getStone() >= neededStone && agent.getGold() >= neededGold){ // Tenemos lo suficiente para hacer un ayuntamiento
                            candidatasAyuntamiento = candidatasAyuntamiento.isEmpty()? agent.getMapa().findAll("Ground") : candidatasAyuntamiento;
                            Coordinate candidata = candidatasAyuntamiento.poll();
                            //Ver si tenemos casillas candidatas
                            if(candidata!=null) {
                                log.info(ANSI_CYAN + this.getAgent().getLocalName() + " ordenamos a unidad hacer ayuntamiento");

                                //Cuento ficticiamente con que voy a tener un TownHall
                                construyendoAyunta=true;

                                Coordinate origen = agent.getPosiciones().get(pedidor);
                                ArrayList<Integer> direcciones = hacerCamino(origen, candidata);
                                agent.getInstrucciones().get(pedidor).add(new ConstructBuilding(new Building("Town Hall"), direcciones));
                                libres.poll();
                            }
                        }
                        else conseguirCantidades(pedidor, neededWood, neededStone, neededGold);
                    } else if (posAyuntamiento != null && !hayNuevaUnidad) {
                        log.info(ANSI_CYAN + this.getAgent().getLocalName() + " MISION: CREAR UNIDAD");
                        int neededGold = 150;
                        if (agent.getGold() >= neededGold){
                            log.info(ANSI_CYAN + this.getAgent().getLocalName() + " tenemos suficiente para unidad");
                            Coordinate origen = agent.getPosiciones().get(pedidor);
                            ArrayList<Integer> direcciones = hacerCamino(origen, posAyuntamiento);
                            //Cuento con que voy a tener la unidad nueva
                            hayNuevaUnidad=true;
                            agent.getInstrucciones().get(pedidor).add(new CreateUnit(direcciones));
                            libres.poll();
                        }
                        else conseguirCantidades(pedidor, 0, 0, neededGold);
                    } else if (posAyuntamiento != null && rentableTienda && !construyendoTienda){
                        log.info(ANSI_CYAN + this.getAgent().getLocalName() + " MISION: CONSTRUIR TIENDAS");
                        int neededWood = 50;
                        int neededStone = 50;
                        int neededGold = 50;
                        if (posAyuntamiento!=null && agent.getWood() >= neededWood && agent.getStone() >= neededStone && agent.getGold() >= neededGold){
                            log.info(ANSI_CYAN + this.getAgent().getLocalName() + " tenemos suficiente para tienda");

                            candidatasTienda = candidatasTienda.isEmpty()? agent.getMapa().findViableStores(): candidatasTienda;
                            Coordinate candidata = candidatasTienda.poll();
                            //Comprobar que hay candidatas
                            if(candidata!=null) {
                                //Le quito temporalmente los recursos a la tribu, para no contar con ellos.
                                Coordinate origen = agent.getPosiciones().get(pedidor);
                                ArrayList<Integer> direcciones = hacerCamino(origen, candidata);
                                agent.getInstrucciones().get(pedidor).add(new ConstructBuilding(new Building("Store"), direcciones));
                                libres.poll();
                                //Cuento con que construyo tienda
                                construyendoTienda=true;
                            }
                        }
                        else{
                            conseguirCantidades(pedidor, neededWood, neededStone, neededGold);
                        }
                    } else {
                        log.info(ANSI_CYAN + this.getAgent().getLocalName() + "MISION: SIN TAREA");
                        Coordinate origen = agent.getPosiciones().get(pedidor);
                        String material = agent.getMapa().findAll("Ore").isEmpty()? "Forest" :"Ore";
                        int cantidad = 1;
                        if(mandarRecolectar(origen, material, cantidad, pedidor)) libres.poll();
                    }
                }
            }
        });

        this.addSubBehaviour(new CyclicBehaviour() { // Camarero, le da a cada unidad una instruction cuando la pide
            @Override
            public void action() {
                if(initialInstructions) {
                    ACLMessage msg = this.getAgent().receive(MessageTemplate.MatchProtocol("libre"));
                    if (msg != null) {
                        //log.info(ANSI_CYAN + this.getAgent().getLocalName() + " La unidad "+msg.getSender().getLocalName()+" está libre");
                        AID unitAID = msg.getSender();
                        try {
                            Coordinate position = (Coordinate) msg.getContentObject();
                            agent.getPosiciones().put(unitAID, position);
                        } catch (UnreadableException e) { throw new RuntimeException(e); }
                        Instruction siguienteInstruccion = agent.getInstrucciones().get(unitAID).poll();
                        if (siguienteInstruccion != null) {
                            log.info(ANSI_CYAN + this.getAgent().getLocalName() +
                                    " Tengo la instrucción "+siguienteInstruccion.getType()+" para la unidad "+unitAID.getLocalName());
                            switch (siguienteInstruccion.getType()) {
                                case "Move" -> {
                                    ArrayList<Integer> direcciones = ((Move) siguienteInstruccion).getDirections();
                                    ACLMessage moveMsg = newMsgWithObject(this.getAgent(), ACLMessage.INFORM,
                                            direcciones, unitAID, "explora");
                                    log.info(ANSI_CYAN + this.getAgent().getLocalName() + " Explora juvenil");
                                    this.getAgent().send(moveMsg);
                                }
                                case "ExploitResource" -> {
                                    int cantidad = ((ExploitResource) siguienteInstruccion).getAmount();
                                    ArrayList<Integer> camino = ((ExploitResource) siguienteInstruccion).getDirections();
                                    Object[] contenido = {camino, cantidad};
                                    ACLMessage exploitMsg = newMsgWithObject(this.getAgent(), ACLMessage.INFORM,
                                            contenido, unitAID, "recolecta");
                                    log.info(ANSI_CYAN + this.getAgent().getLocalName() + " recolecta juvenil");
                                    this.getAgent().send(exploitMsg);
                                }
                                case "ConstructBuilding" -> {
                                    Building edificio = ((ConstructBuilding) siguienteInstruccion).getBuilding();
                                    ArrayList<Integer> camino = ((ConstructBuilding) siguienteInstruccion).getDirections();
                                    Object[] contenido = {camino, edificio};
                                    ACLMessage constructMsg = newMsgWithObject(this.getAgent(), ACLMessage.INFORM,
                                            contenido, unitAID, "construye");
                                    log.info(ANSI_CYAN + this.getAgent().getLocalName() + " construye juvenil");
                                    this.getAgent().send(constructMsg);
                                }
                                case "CreateUnit" -> {
                                    ArrayList<Integer> camino = ((CreateUnit) siguienteInstruccion).getDirections();
                                    Object[] contenido = {camino, null};
                                    ACLMessage unitMsg = newMsgWithObject(this.getAgent(), ACLMessage.INFORM,
                                            contenido, unitAID, "construye");
                                    log.info(ANSI_CYAN + this.getAgent().getLocalName() + " reproducete juvenil");
                                    this.getAgent().send(unitMsg);
                                }
                            }
                        } else{
                            if(!libres.contains(unitAID)){
                                libres.add(unitAID);
                                log.info(ANSI_CYAN + this.getAgent().getLocalName() + " Unidad "+unitAID.getLocalName()+ " añadida a lista de libres");
                            }
                            ACLMessage unitMsg = newMsgWithObject(this.getAgent(), ACLMessage.INFORM, null, unitAID, "esperate");
                            //log.info(ANSI_CYAN + this.getAgent().getLocalName() + " esperese " + unitAID.getLocalName());
                            this.getAgent().send(unitMsg);
                        }
                    }
                }
            }
        });
    }

    private void conseguirCantidades(AID pedidor, int neededWood, int neededStone, int neededGold) {
        Coordinate origen = agent.getPosiciones().get(pedidor);
        boolean found = false;
        if(agent.getStone() < neededStone){
            found = mandarRecolectar(origen, "Ore", 1, pedidor);
        }
        if(agent.getGold() < neededGold && !found){
            found = mandarRecolectar(origen, "Ore", 1, pedidor);
        }
        if (agent.getWood() < neededWood && !found ){
            mandarRecolectar(origen, "Forest", 1, pedidor);
        }
    }

    private void receiveInfoNewCell() {
        //Recibe mensaje de unitcontroller de solicitud de moverse
        ACLMessage msg = this.getAgent().receive(
                MessageTemplate.and(MessageTemplate.and(MessageTemplate.MatchLanguage(codec.getName()),
                                MessageTemplate.MatchOntology(ontology.getName())),
                        MessageTemplate.MatchProtocol(WoaOntologyVocabulary.REVEAL_CELL))
        );
        if(msg != null) {
            log.info(ANSI_CYAN + this.getAgent().getLocalName() + " Message received from "+ msg.getSender().getLocalName());
            Action actionMsg = extractMsgOntoContent(this.getAgent(), msg);
            if (actionMsg != null) {
                Cell cell = ((RevealCell) actionMsg.getAction()).getCellContent();
                Coordinate coords = cell.getCoord();
                log.info(ANSI_CYAN + this.getAgent().getLocalName() +
                        " The tribe controller knows a new cell: " +
                        coords.getXValue() + "," + coords.getYValue() + " content: " + cell.getContent());
                this.agent.getMapa().setValue(coords.getXValue(), coords.getYValue(), cell.getContent(), false);
            } else log.warning(ANSI_RED + this.getAgent().getLocalName() + " Error obtaining the info of the new cell");
        }
    }

    private void receiveCurrentResources() {
        ACLMessage msg = this.getAgent().receive(
                MessageTemplate.and(MessageTemplate.and(MessageTemplate.MatchLanguage(codec.getName()),
                                MessageTemplate.MatchOntology(ontology.getName())),
                        MessageTemplate.MatchProtocol(WoaOntologyVocabulary.INFORM_CURRENT_RESOURCES))
        );
        if(msg != null) {
            log.info(ANSI_CYAN + this.getAgent().getLocalName() + " Message received from TribeAccountant");
            Action actionMsg = extractMsgOntoContent(this.getAgent(), msg);
            if (actionMsg != null) {
                InformCurrentResources initialResources = ((InformCurrentResources) actionMsg.getAction());
                log.info(ANSI_CYAN + this.getAgent().getLocalName() +
                        " Now I have: " +
                        " Gold: " + initialResources.getTribeResources().getGold().getAmount() +
                        " Wood: " + initialResources.getTribeResources().getWood().getAmount() +
                        " Stone: " + initialResources.getTribeResources().getStone().getAmount() +
                        " Storage: " + initialResources.getStorage().getSize());
                actualizarRecursos(initialResources);
            } else
                log.warning(ANSI_RED + this.getAgent().getLocalName() + " Error obtaining the content of the current resources");
        }
    }

    private void receiveProblemBuilding() {
        ACLMessage msg = this.getAgent().receive(MessageTemplate.MatchProtocol("problemas"));
        if(msg != null) {
            log.info(ANSI_CYAN + this.getAgent().getLocalName() + " Message received from"+msg.getSender()+" with problems");
            String type = msg.getContent();
            if(type.contains("Town Hall"))
                construyendoAyunta=false;
            else if (type.contains("Store")) {
                construyendoTienda=false;
            } else if (type.contains("Unit")) {
                hayNuevaUnidad=false;
            }
            else{
                log.warning(ANSI_RED + this.getAgent().getLocalName() + " Error obtaining the type of failure");
            }
        }
    }

    private void receiveNewBuilding() {
        ACLMessage msg = this.getAgent().receive(
                MessageTemplate.and(MessageTemplate.and(MessageTemplate.MatchLanguage(codec.getName()),
                                MessageTemplate.MatchOntology(ontology.getName())),
                        MessageTemplate.MatchProtocol(WoaOntologyVocabulary.INFORM_NEW_BUILDING))
        );
        if(msg != null) {
            log.info(ANSI_CYAN + this.getAgent().getLocalName() + " Message received from " + msg.getSender().getLocalName());
            Action actionMsg = extractMsgOntoContent(this.getAgent(), msg);
            if (actionMsg != null) {
                InformNewBuilding newBuilding = (InformNewBuilding) actionMsg.getAction();
                Coordinate coords = newBuilding.getCell().getCoord();
                log.info(ANSI_CYAN + this.getAgent().getLocalName() + " Now I have a " + newBuilding.getCell().getContent());

                //Actualizamos las estructuras, si es un ayuntamiento ya sabemos donde va a estar toda la partida
                this.agent.getMapa().setValue(coords.getXValue(), coords.getYValue(), newBuilding.getCell().getContent(), false);
                if (newBuilding.getCell().getContent().equals("Town Hall")) posAyuntamiento = newBuilding.getCell().getCoord();
                if (newBuilding.getCell().getContent().equals("Store")) construyendoTienda = false;

                candidatasTienda.clear(); // al haber nuevos edificios tenemos que recalcular donde se hace una tienda
                log.info(ANSI_CYAN + this.getAgent().getLocalName() + " Reinicio candidatas tienda con el nuevo edificio");
            } else
                log.warning(ANSI_RED + this.getAgent().getLocalName() + " Error obtaining the content of the current resources");
        }
    }

    private void receiveNewUnit() {
        ACLMessage msg = this.getAgent().receive(
                MessageTemplate.and(MessageTemplate.and(MessageTemplate.MatchLanguage(codec.getName()),
                                MessageTemplate.MatchOntology(ontology.getName())),
                        MessageTemplate.MatchProtocol(WoaOntologyVocabulary.ASSIGN_NEW_UNIT))
        );
        if(msg != null) {
            log.info(ANSI_CYAN + this.getAgent().getLocalName() + " Message of new unit received from UnitManager");
            Action actionMsg = Utils.extractMsgOntoContent(this.getAgent(), msg);
            if (actionMsg != null) {
                AssignNewUnit newUnit = (AssignNewUnit) actionMsg.getAction();
                log.info(ANSI_CYAN + this.getAgent().getLocalName() + " gets the Unit AID: " + newUnit.getUnitID());
                //Envío mensaje a la unidad diciéndole que soy el padre y esperando su posición inicial
                ACLMessage soyTuPadre = Utils.newMsgWithObject(this.agent, ACLMessage.INFORM, null, newUnit.getUnitID(), "soyTuPadre");
                this.agent.send(soyTuPadre);
                ACLMessage respuesta = this.agent.blockingReceive(MessageTemplate.MatchProtocol("soyTuPadre"));
                try {
                    Coordinate posicion = (Coordinate) respuesta.getContentObject();
                    log.info(ANSI_CYAN + this.getAgent().getLocalName() +" Unit  " + newUnit.getUnitID().getLocalName() +
                            " está en la posición: " + posicion.getXValue() + " "+ posicion.getYValue());
                    agent.getPosiciones().put(newUnit.getUnitID(), posicion);
                    agent.getUnidades().add(newUnit.getUnitID());
                    agent.getInstrucciones().put(newUnit.getUnitID(), new LinkedList<>());
                } catch (UnreadableException e) { throw new RuntimeException(e); }
                hayNuevaUnidad = true;
            } else log.warning(ANSI_RED + this.getAgent().getLocalName() + " Error obtaining the content of the unit aid");
        }
    }

    private ArrayList<Integer> calcularRutaExploradorInicial(int ancho, int largo){
        ArrayList<Integer> direcciones = new ArrayList<>();
        int nFilas = (int) Math.ceil((double) largo /2);
        for (int i = 0; i < ancho; i++) {
            for (int j = 0; j < nFilas-1; j++) direcciones.add(6);
            direcciones.add(1);
        }
        return direcciones;
    }

    private boolean mandarRecolectar(Coordinate origen, String material, int cantidad, AID unidad){
        // Hacer una instrucción de que vaya a un sitio
        Coordinate destino = this.agent.getMapa().findValue(material, origen);
        if (destino != null){
            ArrayList<Integer> camino = hacerCamino(origen, destino);
            // Hacer una instrucción de que recolecte x cantidad
            this.agent.getInstrucciones().get(unidad).add(new ExploitResource(cantidad, camino));
            libres.poll();
            log.info(ANSI_CYAN + this.getAgent().getLocalName() + " Mandamos recolectar "+cantidad+ " del material "
                    + material+" a la unidad "+unidad.getLocalName()+ " en " + destino.getXValue()  +", " + destino.getYValue());
            return true;
        }
        return false;
    }

    private ArrayList<Integer> hacerCamino(Coordinate origen, Coordinate destino){
        //calculo direcciones de movimiento para llegar a la misma columna de destino
        ArrayList<Integer> camino = hacerCaminoAColumna(origen, destino);
        Coordinate newOrigen=origen;
        //calculo la posicion en la que estará la unidad tras esos movimientos.
        for(int direccion : camino){
            newOrigen= assignNewPos(newOrigen, direccion);
        }
        //calculo las direcciones de movimiento para llegar a la misma fila destino
        camino.addAll(hacerCamnioaFila(newOrigen, destino));
        return camino;
    }


    private ArrayList<Integer> hacerCaminoAColumna(Coordinate origen, Coordinate destino){
        ArrayList<Integer> camino = new ArrayList<>();
        int xO = origen.getXValue();
        int yO = origen.getYValue();
        int xD = destino.getXValue();
        int yD = destino.getYValue();
        //Comprobar si vamos por borde o no
        if (Math.abs(yD-yO) > this.agent.getMapDimensions().second()/2){    //Ir por el borde
            if (yD>yO){ // destino esta a la derecha
                for (int i = 0; i < this.agent.getMapDimensions().second() - Math.abs(yD-yO); i++) {
                    if (xD<xO){
                        camino.add(5); // destino esta arriba
                    }
                    else{
                        camino.add(4); // destino esta abajo
                    }
                }
            } else {    //destino esta a la izquierda
                for (int i = 0; i < this.agent.getMapDimensions().second() - Math.abs(yD-yO); i++) {
                    if (xD<xO){
                        camino.add(1); // destino esta arriba
                    }
                    else{
                        camino.add(2); // destino esta abajo
                    }
                }
            }
        } else{     //Se va normal, no por el borde
            if (yD>yO){ // destino esta a la derecha
                for (int i = 0; i < Math.abs(yD-yO); i++) {
                    if (xD<xO){
                        camino.add(1); // esta arriba
                    }
                    else{
                        camino.add(2); // esta abajo
                    }
                }
            } else {    //destino está a la izquierda
                for (int i = 0; i < Math.abs(yD-yO); i++) {
                    if (xD<xO){
                        camino.add(5); // esta arriba
                    }
                    else{
                        camino.add(4); // esta abajo
                    }
                }
            }
        }
        return camino;
    }


    private ArrayList<Integer> hacerCamnioaFila(Coordinate origen, Coordinate destino){
        ArrayList<Integer> camino = new ArrayList<>();
        int xO = origen.getXValue();
        int xD = destino.getXValue();
        // Comprobar si voy por el borde:
        if (Math.abs(xD-xO) > this.agent.getMapDimensions().first()/2){ //Ir por el borde
            for (int i = 0; i < (this.agent.getMapDimensions().first() - Math.abs(xD-xO))/2; i++) {
                if (xD<xO) camino.add(3); // destino esta arriba
                else camino.add(6); // destino esta abajo
            }
        } else {    //no voy por el borde
            for (int i = 0; i < (Math.abs(xD-xO))/2; i++) {
                if (xD<xO) camino.add(6); // destino esta arriba
                else camino.add(3); // destino esta abajo
            }
        }
        return camino;
    }


    private Coordinate assignNewPos(Coordinate posActual, int direction){
        //Calcular la posición nueva de la unidad
        int filaActual = posActual.getXValue();
        int columnaActual = posActual.getYValue();
        //ver las dimensiones del mapa
        int filas = (int) this.agent.getMapDimensions().first();
        int columnas = (int) this.agent.getMapDimensions().second();

        switch (direction) {
            case 1 -> { // Arriba a la derecha
                filaActual--;
                columnaActual++;
            }

            case 2-> {// Abajo a la derecha
                filaActual++;
                columnaActual++;
            }
            case 3-> filaActual += 2; // Abajo
            case 4 -> {// Abajo a la izquierda
                filaActual++;
                columnaActual--;
            }
            case 5-> {// Arriba a la izquierda
                filaActual--;
                columnaActual--;
            }
            case 6-> filaActual -= 2; // Arriba
            default-> System.out.println("Dirección no válida.");
        }
        // Ajustar la posición si se sale del mapa
        if (filaActual < 1) { // se sale por debajo
            filaActual = filaActual==0 ? filas : filas-1;
        } else if (filaActual > filas) { // se sale por arriba
            filaActual = filaActual==filas+1 ? 1 : 2;
        }
        if (columnaActual < 1) columnaActual = columnas; // se sale por debajo
        else if (columnaActual > columnas) columnaActual = 1; // se sale por arriba

        return new Coordinate(filaActual,columnaActual);
    }



    private void actualizarRecursos(InformCurrentResources recursos){
        agent.setGold(recursos.getTribeResources().getGold().getAmount());
        agent.setStone(recursos.getTribeResources().getStone().getAmount());
        agent.setWood(recursos.getTribeResources().getWood().getAmount());
        agent.setStorage(recursos.getStorage().getSize());
    }
}













