package es.upm.emse.absd.team1.agents.tribe;
import es.upm.emse.absd.ontology.woa.concepts.Coordinate;
import lombok.extern.java.Log;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import static es.upm.emse.absd.team1.agents.Utils.ANSI_YELLOW;

@Log
public class GameMap {
    String [][] map; //el valor de cada celda se guarda como
    int widthMap;
    int heightMap;


    public GameMap(int width, int height) {
        this.map = new String[width][height];
        widthMap=width;
        heightMap=height;
    }

    /*
    public Coordinate findValue(String value){
        for (int i = 0; i < this.widthMap; i++) {
            for (int j = 0; j < this.heightMap; j++) {
                if (this.map[i][j]!=null && this.map[i][j].equals(value)) return new Coordinate(i+1,j+1);
            }
        }
        return null;
    }
     */
    public Coordinate findValue(String value, Coordinate origen){
        if(this.map[origen.getXValue()-1][origen.getYValue()-1]!=null && this.map[origen.getXValue()-1][origen.getYValue()-1].equals(value)){
            return origen;
        }
        ArrayList<Coordinate> casillasResources = new ArrayList<>();
        for (int i = 0; i < this.widthMap; i++) {
            for (int j = 0; j < this.heightMap; j++) {
                if (this.map[i][j]!=null && this.map[i][j].equals(value)){
                   casillasResources.add(new Coordinate(i+1,j+1));
                }
            }
        }
        int masCercano=10000;
        int distancia;
        Coordinate result= null;
        for (Coordinate candidata : casillasResources) {
            distancia = Math.abs(candidata.getXValue()-origen.getXValue())+Math.abs(candidata.getYValue()-origen.getYValue());
            if(masCercano>distancia){
                masCercano=distancia;
                result=candidata;
            }
        }
        return result;
    }

    public Queue<Coordinate> findAll(String value){
        Queue<Coordinate> coordinates = new LinkedList<>();
        for (int i = 0; i < this.widthMap; i++) {
            for (int j = 0; j < this.heightMap; j++) {
                if (this.map[i][j]!=null && this.map[i][j].equals(value)){
                    coordinates.add(new Coordinate(i+1,j+1));
                }
            }
        }
        return coordinates;
    }

    public Queue<Coordinate> findViableStores(){
        Queue<Coordinate> coordinates = new LinkedList<>();
        for (int i = 0; i < this.widthMap; i++) {
            for (int j = 0; j < this.heightMap; j++) {
                if (this.map[i][j]!=null && checkStore(i,j)) coordinates.add(new Coordinate(i+1,j+1));
            }
        }
        return coordinates;
    }


        //Añade un value a las coordenadas x e y, si resourcecheck es true, mira a ver que solo se puedan meter ground, forest etc
    public int setValue(int x, int y, String value, boolean resourcheCheck){

        if (checkCoordinates(x,y)){
            if(resourcheCheck && (value.equals("Ground") || value.equals("Ore") || value.equals("Forest") || value.equals("Building"))){
                this.map[x-1][y-1] = value;
            } else if (!resourcheCheck){
                this.map[x-1][y-1] = value;
            } else return -1;
            return 0;
        }
        else return -1;
    }

    //Obtiene el value
    public String getValue(int x, int y){
        if (checkCoordinates(x,y) && map[x][y]!=null) {
            return map[x][y];
        } else {
            return "";
        }
    }
    //revisa si las coordenadas son correctas antes de las cosas
    private boolean checkCoordinates(int x, int y){
        if ((x % 2 == 0 && y % 2 == 0) || (x % 2 == 1 && y % 2 == 1)) {
            return true;
        } else {
            return false;
        }
    }

    private boolean checkStore(int x, int y) {
        String centralCellContent=getValue(x,y);
        if(centralCellContent.equals("Ground")){
            int xCoord1a=adjustCoordinateX(x+1,heightMap);
            int xCoord1b=adjustCoordinateX(x-1,heightMap);
            int xCoord2a=adjustCoordinateX(x+2,heightMap);
            int xCoord2b=adjustCoordinateX(x-2,heightMap);

            int yCoord1a=adjustCoordinateY(y+1,widthMap);
            int yCoord1b=adjustCoordinateY(y-1,widthMap);


            if(getValue(xCoord1b,yCoord1b).contains("Town Hall") || getValue(xCoord1b,yCoord1b).contains("Store") ||
               getValue(xCoord1b,yCoord1a).contains("Town Hall") || getValue(xCoord1b,yCoord1a).contains("Store") ||
               getValue(xCoord1a,yCoord1b).contains("Town Hall") || getValue(xCoord1a,yCoord1b).contains("Store") ||
               getValue(xCoord1a,yCoord1a).contains("Town Hall") || getValue(xCoord1a,yCoord1a).contains("Store") ||
               getValue(xCoord2b,y).contains("Town Hall") || getValue(xCoord2b,y).contains("Store") ||
               getValue(xCoord2a,y).contains("Town Hall") || getValue(xCoord2a,y).contains("Store"))
            {
                return true;
            }
            else
            {
                return false;
            }
        }
        else
        {
            return false;
        }

    }



    private static int adjustCoordinateX(int coord, int max) {
        coord++;
        int cordResult=coord;
        if (coord < 1) {
            cordResult= coord == 0 ? max : max - 1;
        } else if (coord > max) {
            cordResult= coord == max + 1 ? 1 : 2;
        }
        return cordResult-1;
    }
    private static int adjustCoordinateY(int coord, int max) {
        coord++;
        int cordResult=coord;
        if (coord < 1) {
            cordResult= max;
        } else if (coord > max) {
            cordResult= 1;
        }
        return cordResult-1;
    }

}
