package es.upm.emse.absd.team1.agents.platform.TribeAccountant.TribeAccountantBehaviours;

import es.upm.emse.absd.GUIUtils;
import es.upm.emse.absd.team1.agents.platform.TribeResources;
import es.upm.emse.absd.team1.agents.platform.TribeAccountant.AgTribeAccountant;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import lombok.extern.java.Log;

import java.util.HashMap;
import java.util.Map;

import static es.upm.emse.absd.team1.agents.Utils.ANSI_YELLOW;
import static es.upm.emse.absd.team1.agents.Utils.STORE_CAPACITY;

@Log
public class TribeAccountantBehaviour_Building extends CyclicBehaviour {
    AgTribeAccountant agente;
    Map<String, Float> precioTownHall = new HashMap<String, Float>() {{
        put("oro", (float) 250);
        put("piedra", (float) 150);
        put("madera", (float) 200);
    }};
    Map<String, Float> precioStore = new HashMap<String, Float>() {{
        put("oro", (float) 50);
        put("piedra", (float) 50);
        put("madera", (float) 50);
    }};

    public TribeAccountantBehaviour_Building(AgTribeAccountant agente) {
        super(agente);
        this.agente=agente;
    }

    @Override
    public void action() {
        //recibir mensaje del BuildingManager para preguntar si tiene recursos para construir un building.
        ACLMessage msg = this.getAgent().receive(MessageTemplate.MatchProtocol("tieneRecursosParaBuilding"));
        if(msg!=null) {
            log.info(ANSI_YELLOW + this.getAgent().getLocalName() + " Recibe mensaje de BuildingManager preguntando" +
                    " por los recursos para construir un building");
            Object[] resourcesInfoForBuilding;
            try { resourcesInfoForBuilding = (Object[]) msg.getContentObject(); }
            catch (UnreadableException e) { throw new RuntimeException(e); }
            AID tribu = (AID) resourcesInfoForBuilding[0];
            AID unidad = (AID) resourcesInfoForBuilding[1];
            String type = (String) resourcesInfoForBuilding[2];

            Map<String, Float> precio = type.equals("Town Hall") ? precioTownHall : precioStore;
            float precioOro = precio.get("oro");
            float precioPiedra = precio.get("piedra");
            float precioMadera = precio.get("madera");

            float oro = this.agente.getTribesResourcesMap().get(tribu).getTribeGold();
            float piedra = this.agente.getTribesResourcesMap().get(tribu).getTribeStone();
            float madera = this.agente.getTribesResourcesMap().get(tribu).getTribeWood();

            ACLMessage response = msg.createReply();
            if(oro >= precioOro && piedra >= precioPiedra && madera >= precioMadera){
                //Recursos disponibles
                TribeResources recursos = this.agente.getTribesResourcesMap().get(tribu);
                recursos.setTribeGold(-precioOro);
                recursos.setTribeStone(-precioPiedra);
                recursos.setTribeWood(-precioMadera);
                this.agente.getTribesResourcesMap().put(tribu, recursos);
                response.setPerformative(ACLMessage.AGREE);

                //LLAMAR A LA GUI ACTUALIZANDO EL SCORE
                GUIUtils.loseResource(tribu.getLocalName(), unidad.getLocalName(), "gold", (int) precioOro);
                GUIUtils.loseResource(tribu.getLocalName(), unidad.getLocalName(), "stone", (int) precioPiedra);
                GUIUtils.loseResource(tribu.getLocalName(), unidad.getLocalName(), "wood", (int) precioMadera);

                this.agente.sendCurrentResources(tribu,recursos.getTribeGold(),recursos.getTribeStone(),recursos.getTribeWood(),recursos.getTribeStorageCapacity());


            } else {
                response.setPerformative(ACLMessage.FAILURE);
            }
            response.setProtocol("tieneRecursosParaBuilding");
            this.agente.send(response);
        }

        //recibir mensaje del BuildingManager para devolver los recursos cuando el tiempo de juego termina y la
        //construcción del building falla
        ACLMessage msg2 = this.getAgent().receive(MessageTemplate.MatchProtocol("devolverRecursosBuilding"));
        if(msg2!=null) {
            log.info(ANSI_YELLOW + this.getAgent().getLocalName() + " Recibe mensaje de BuildingManager pidiendo" +
                    "devolver recursos de la construcción de un building");
            Object[] resourcesInfoForBuilding;
            try { resourcesInfoForBuilding = (Object[]) msg2.getContentObject(); }
            catch (UnreadableException e) { throw new RuntimeException(e); }
            AID tribu = (AID) resourcesInfoForBuilding[0];
            AID unidad = (AID) resourcesInfoForBuilding[1];
            String type = (String) resourcesInfoForBuilding[2];

            Map<String, Float> precio = type.equals("Town Hall") ? precioTownHall : precioStore;
            float precioOro = precio.get("oro");
            float precioPiedra = precio.get("piedra");
            float precioMadera = precio.get("madera");

            float oro= this.agente.getTribesResourcesMap().get(tribu).getTribeGold();
            float piedra= this.agente.getTribesResourcesMap().get(tribu).getTribeStone();
            float madera= this.agente.getTribesResourcesMap().get(tribu).getTribeWood();

            TribeResources recursos = this.agente.getTribesResourcesMap().get(tribu);
            recursos.setTribeGold(precioOro);
            recursos.setTribeStone(precioPiedra);
            recursos.setTribeWood(precioMadera);
            this.agente.getTribesResourcesMap().put(tribu, recursos);

            GUIUtils.gainResource(tribu.getLocalName(), unidad.getLocalName(), "gold", (int) precioOro);
            GUIUtils.gainResource(tribu.getLocalName(), unidad.getLocalName(), "stone", (int) precioPiedra);
            GUIUtils.gainResource(tribu.getLocalName(), unidad.getLocalName(), "wood", (int) precioMadera);
        }

        //recibir mensaje del BuildingManager para actualizar el número de buildings y la capacidad si se contruyó una store
        ACLMessage msg3 = this.getAgent().receive(MessageTemplate.MatchProtocol("actualizarBuildings"));
        if(msg3!=null) {
            log.info(ANSI_YELLOW + this.getAgent().getLocalName() + " Recibe mensaje de BuildingManager pidiendo" +
                    "actualizar número de buildings");
            Object[] updateBuildingsInfo;
            try { updateBuildingsInfo = (Object[]) msg3.getContentObject(); }
            catch (UnreadableException e) { throw new RuntimeException(e); }
            AID tribu = (AID) updateBuildingsInfo[0];
            String type = (String) updateBuildingsInfo[2];

            TribeResources recursos = this.agente.getTribesResourcesMap().get(tribu);
            if (type.equals("Town Hall")) {
                float nCities= this.agente.getTribesResourcesMap().get(tribu).getNCities();
                recursos.setNCities(nCities+1);
            } else if (type.equals("Store")) {
                float nStores= this.agente.getTribesResourcesMap().get(tribu).getNStores();
                float capacity= this.agente.getTribesResourcesMap().get(tribu).getTribeStorageCapacity();
                recursos.setNCities(nStores+1);
                recursos.setTribeStorageCapacity(capacity+STORE_CAPACITY);
            } else {
                log.info(ANSI_YELLOW + this.getAgent().getLocalName() + " No se reconoce el tipo de Building enviado" +
                        "por el BuildingManager");
            }
            this.agente.getTribesResourcesMap().put(tribu, recursos);
        }
    }
}
