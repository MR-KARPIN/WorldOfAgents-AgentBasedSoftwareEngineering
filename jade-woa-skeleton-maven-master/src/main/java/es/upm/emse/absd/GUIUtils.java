package es.upm.emse.absd;

import lombok.extern.java.Log;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import static es.upm.emse.absd.team1.agents.Utils.*;

@Log
public final class GUIUtils {

    public static void GameStart(ArrayList<String> tribes){
        ArrayList<String> tribesSorted=new ArrayList<>();
        if(!tribes.isEmpty()) {
            //Ordenar tribes
            int indice;
            log.info(ANSI_PURPLE+ "Ordenar tribes para la interfaz");
            for (int i = 0; i < tribes.size(); i++) {
                indice = tribes.indexOf("Team" + (i + 1));
                tribesSorted.add(tribes.get(indice));
                log.info(ANSI_PURPLE+ "Se añade a la interfaz: " + tribes.get(indice));
            }
            GameStartCall(tribesSorted);
        }
        else
            log.warning(ANSI_RED + "No se han encontrado tribus para iniciar el juego");
    }
    private static void GameStartCall(ArrayList<String> tribes){
        try {
            URL url = new URL("http://localhost:3000/api/start");
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            JSONParser parser = new JSONParser();
            JSONObject map = (JSONObject)parser.parse(new FileReader("src/main/java/es/upm/emse/absd/newMap.json"));
            JSONObject mapInit = new JSONObject();
            mapInit.put("map", map);

            mapInit.put("players", tribes);


            String payload = mapInit.toString();

            // Write payload to the connection
            try (OutputStream outputStream = connection.getOutputStream()) {
                byte[] input = payload.getBytes("utf-8");
                outputStream.write(input, 0, input.length);
            }

            // Get response code
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                System.out.println("Game started successfully!");
            } else {
                System.out.println("Failed to start the game. Response code: " + responseCode);
            }
        } catch (Exception e) {
            log.warning(ANSI_BLACK + "ERROR EN LA CONEXIÓN");
        }
    }

    public static void GameEnd(){
        GameEndCall();
    }
    private static void GameEndCall(){
        try {
            URL url = new URL("http://localhost:3000/api/end");
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            // Get response code
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                System.out.println("Game ended successfully!");
            } else {
                System.out.println("Failed to end the game. Response code: " + responseCode);
            }
        } catch (Exception e) {
            log.warning(ANSI_BLACK + "ERROR EN LA CONEXIÓN");
        }
    }

    public static void newUnit(String tribe, String unidad, int x, int y){
        newUnitCall(tribe, unidad, x, y);
    }
    private static void newUnitCall(String tribe, String unidad, int x, int y){
        try {
            log.info(ANSI_PURPLE + "Creo unidad "+unidad+" de la tribu "+tribe+" en la posición: "+x+","+y);
            JSONObject newTribeInfo = new JSONObject();
            URL url = new URL("http://localhost:3000/api/agent/create");
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            newTribeInfo.put("player_id", tribe);
            newTribeInfo.put("agent_id", unidad);
            JSONObject tileObject = new JSONObject();
            tileObject.put("x", x);
            tileObject.put("y", y);
            newTribeInfo.put("tile", tileObject);
            String payload = newTribeInfo.toString();
            // Write payload to the connection
            try (OutputStream outputStream = connection.getOutputStream()) {
                byte[] input = payload.getBytes("utf-8");
                outputStream.write(input, 0, input.length);
            }
            // Get response code
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                log.info(ANSI_PURPLE + "Pintada la unidad "+unidad+" de la tribu "+tribe);
            } else {
                log.warning(ANSI_RED + "ERROR creando la unidad "+unidad+" de la tribu "+tribe);
            }
        }
        catch (Exception e) {
            log.warning(ANSI_BLACK + "ERROR EN LA CONEXIÓN");
        }
    }

    public static void moveAgent(String tribe, String unidad, int x, int y){
        moveAgentCall(tribe, unidad, x, y);
    }
    private static void moveAgentCall(String tribe, String unidad, int x, int y ){
        try {
            log.info(ANSI_PURPLE + " Muevo la unidad "+unidad+" de la tribu "+tribe+" a la posición: "+x+","+y);
            JSONObject moveTribeInfo = new JSONObject();
            URL url = new URL("http://localhost:3000/api/agent/move");
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            moveTribeInfo.put("player_id", tribe);
            moveTribeInfo.put("agent_id", unidad);
            JSONObject tileObject = new JSONObject();
            tileObject.put("x", x);
            tileObject.put("y", y);
            moveTribeInfo.put("tile", tileObject);
            String payload = moveTribeInfo.toString();
            // Write payload to the connection
            try (OutputStream outputStream = connection.getOutputStream()) {
                byte[] input = payload.getBytes("utf-8");
                outputStream.write(input, 0, input.length);
            }
            // Get response code
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                log.info(ANSI_PURPLE + "Movida la unidad "+unidad+" de la tribu "+tribe);
            } else {
                log.warning(ANSI_RED + "ERROR moviendo la unidad "+unidad+" de la tribu "+tribe);
            }
        }
        catch (Exception e) {
            log.warning(ANSI_BLACK + "ERROR EN LA CONEXIÓN");
        }
    }

    public void dieAgent(String tribe, String unidad){
        dieAgentCall(tribe, unidad);
    }
    private void dieAgentCall(String tribe, String unidad){
        try {
            log.info(ANSI_PURPLE + " Mato a la unidad "+unidad+" de la tribu "+tribe);
            JSONObject dieTribeInfo = new JSONObject();
            URL url = new URL("http://localhost:3000/api/agent/die");
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            dieTribeInfo.put("player_id", tribe);
            dieTribeInfo.put("agent_id", unidad);
            String payload = dieTribeInfo.toString();
            // Write payload to the connection
            try (OutputStream outputStream = connection.getOutputStream()) {
                byte[] input = payload.getBytes("utf-8");
                outputStream.write(input, 0, input.length);
            }
            // Get response code
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                log.info(ANSI_PURPLE + "Muerta la unidad "+unidad+" de la tribu "+tribe);
            } else {
                log.warning(ANSI_RED + "ERROR matando la unidad "+unidad+" de la tribu "+tribe);
            }
        }
        catch (Exception e) {

            log.warning(ANSI_BLACK + "ERROR EN LA CONEXIÓN");
        }
    }

    public void startAction(String unidad, String type){
        if(type.equals("exploit") || type.equals("negotiate"))
            startActionCall(unidad, type);
        else
            log.warning(ANSI_RED + "Tipo de acción no válido");
    }
    private void startActionCall(String unidad, String type){
        try {
            log.info(ANSI_PURPLE + " Empiezo acción "+type+" con la unidad "+unidad);
            JSONObject startActionInfo = new JSONObject();
            URL url = new URL("http://localhost:3000/api/agent/start");
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            startActionInfo.put("agent_id", unidad);
            startActionInfo.put("type", type);
            String payload = startActionInfo.toString();
            // Write payload to the connection
            try (OutputStream outputStream = connection.getOutputStream()) {
                byte[] input = payload.getBytes("utf-8");
                outputStream.write(input, 0, input.length);
            }
            // Get response code
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                log.info(ANSI_PURPLE + "Iniciada la acción "+type+" con la unidad "+unidad);
            } else {
                log.warning(ANSI_RED + "ERROR iniciando la acción "+type+" con la unidad "+unidad);
            }
        }
        catch (Exception e) {
            log.warning(ANSI_BLACK + "ERROR EN LA CONEXIÓN");
        }
    }

    public void cancelAction(String unidad){
        cancelActionCall(unidad);
    }
    private void cancelActionCall(String unidad){
        try {
            log.info(ANSI_PURPLE + " Cancelo acción de la unidad "+unidad);
            JSONObject cancelActionInfo = new JSONObject();
            URL url = new URL("http://localhost:3000/api/agent/cancel");
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            cancelActionInfo.put("agent_id", unidad);
            String payload = cancelActionInfo.toString();
            // Write payload to the connection
            try (OutputStream outputStream = connection.getOutputStream()) {
                byte[] input = payload.getBytes("utf-8");
                outputStream.write(input, 0, input.length);
            }
            // Get response code
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                log.info(ANSI_PURPLE + "Cancelada la acción de la unidad "+unidad);
            } else {
                log.warning(ANSI_RED + "ERROR cancelando la acción de la unidad "+unidad);
            }
        }
        catch (Exception e) {
            log.warning(ANSI_BLACK + "ERROR EN LA CONEXIÓN");
        }
    }

    public static void gainResource(String tribe, String unidad, String resource, int amount){
        if(resource.equals("food") || resource.equals("wood") || resource.equals("gold") || resource.equals("stone"))
            gainResourceCall(tribe, unidad, resource, amount);
        else
            log.warning(ANSI_PURPLE + "Tipo de recurso no válido");
    }
    private static void gainResourceCall(String tribe, String unidad, String resource, int amount){
        try {
            log.info(ANSI_PURPLE + " Recibo "+amount+" de "+resource+" para el agente"+ unidad+" de la tribu "+tribe);
            JSONObject gainResourceInfo = new JSONObject();
            URL url = new URL("http://localhost:3000/api/resource/gain");
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            gainResourceInfo.put("player_id", tribe);
            gainResourceInfo.put("agent_id", unidad);
            gainResourceInfo.put("resource", resource);
            gainResourceInfo.put("amount", amount);
            String payload = gainResourceInfo.toString();
            // Write payload to the connection
            try (OutputStream outputStream = connection.getOutputStream()) {
                byte[] input = payload.getBytes("utf-8");
                outputStream.write(input, 0, input.length);
            }
            // Get response code
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                log.info(ANSI_PURPLE + "Recibidos "+amount+" de "+resource+" para el agente"+ unidad+" de la tribu "+tribe);
            } else {
                log.warning(ANSI_RED + "ERROR recibiendo "+amount+" de "+resource+" para el agente"+ unidad+" de la tribu "+tribe);
            }
        }
        catch (Exception e) {
            log.warning(ANSI_BLACK + "ERROR EN LA CONEXIÓN");
        }
    }

    public static void loseResource(String tribe, String unidad, String resource, int amount){
        if(resource.equals("food") || resource.equals("wood") || resource.equals("gold") || resource.equals("stone"))
            loseResourceCall(tribe, unidad, resource, amount);
        else
            log.warning(ANSI_RED + "Tipo de recurso no válido");
    }
    private static void loseResourceCall(String tribe, String unidad, String resource, int amount){
        try {
            log.info(ANSI_PURPLE + " Pierdo "+amount+" de "+resource+" para el agente"+ unidad+" de la tribu "+tribe);
            JSONObject loseResourceInfo = new JSONObject();
            URL url = new URL("http://localhost:3000/api/resource/lose");
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            loseResourceInfo.put("player_id", tribe);
            loseResourceInfo.put("agent_id", unidad);
            loseResourceInfo.put("resource", resource);
            loseResourceInfo.put("amount", amount);
            String payload = loseResourceInfo.toString();
            // Write payload to the connection
            try (OutputStream outputStream = connection.getOutputStream()) {
                byte[] input = payload.getBytes("utf-8");
                outputStream.write(input, 0, input.length);
            }
            // Get response code
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                log.info(ANSI_PURPLE + "Perdidos "+amount+" de "+resource+" para el agente"+ unidad+" de la tribu "+tribe);
            } else {
                log.warning(ANSI_RED + "ERROR perdiendo "+amount+" de "+resource+" para el agente"+ unidad+" de la tribu "+tribe);
            }
        }
        catch (Exception e) {
            log.warning(ANSI_BLACK + "ERROR EN LA CONEXIÓN");
        }
    }

    public static void depleteResource(int x, int y){
        depleteResourceCall(x, y);
    }
    private static void depleteResourceCall(int x, int y){
        try {
            log.info(ANSI_PURPLE + " Agotado recurso en la posición: "+x+y);
            JSONObject depleteResourceInfo = new JSONObject();
            URL url = new URL("http://localhost:3000/api/resource/deplete");
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            JSONObject tileObject = new JSONObject();
            tileObject.put("x", x);
            tileObject.put("y", y);
            depleteResourceInfo.put("tile", tileObject);
            String payload = depleteResourceInfo.toString();
            // Write payload to the connection
            try (OutputStream outputStream = connection.getOutputStream()) {
                byte[] input = payload.getBytes("utf-8");
                outputStream.write(input, 0, input.length);
            }
            // Get response code
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                log.info(ANSI_PURPLE + "Agotado recurso en la posición: "+x+y);
            } else {
                log.warning(ANSI_RED + "ERROR agotando recurso en la posición: "+x+y);
            }
        }
        catch (Exception e) {
            log.warning(ANSI_BLACK + "ERROR EN LA CONEXIÓN");
        }
    }

    public static void build(String unidad, String type){
        if(type.equals("Town Hall") || type.equals("Farm") || type.equals("Store"))
            buildCall(unidad, type);
        else
            log.warning(ANSI_RED + "Tipo de edificio no válido");
    }
    private static void buildCall(String unidad, String type){
        log.info(ANSI_PURPLE + " Construyo "+type+" con la unidad "+unidad);
        JSONObject buildInfo = new JSONObject();
        try {
            URL url = new URL("http://localhost:3000/api/building/create");
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            buildInfo.put("agent_id", unidad);
            buildInfo.put("type", type);
            String payload = buildInfo.toString();
            // Write payload to the connection
            try (OutputStream outputStream = connection.getOutputStream()) {
                byte[] input = payload.getBytes("utf-8");
                outputStream.write(input, 0, input.length);
            }
            // Get response code
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                log.info(ANSI_PURPLE + "Construido "+type+" con la unidad "+unidad);
            } else {
                log.warning(ANSI_RED + "ERROR construyendo "+type+" con la unidad "+unidad);
            }
        }
        catch (Exception e) {
            log.warning(ANSI_BLACK + "ERROR EN LA CONEXIÓN");
        }
    }


}
