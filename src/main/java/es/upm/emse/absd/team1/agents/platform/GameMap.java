package es.upm.emse.absd.team1.agents.platform;
import es.upm.emse.absd.ontology.woa.concepts.Coordinate;
import lombok.extern.java.Log;
import org.glassfish.pfl.basic.contain.Pair;

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

    public Coordinate findValue(String value){
        for (int i = 0; i < this.widthMap; i++) {
            for (int j = 0; j < this.heightMap; j++) {
                if (this.map[i][j].equals(value)) return new Coordinate(i,j);
            }
        }
        return null;
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
        if (checkCoordinates(x,y)) {
            return map[x-1][y-1];
        } else {
            System.out.println("coordenadas invalidas ("+x+","+y+")");
            return null;
        }
    }
    //revisa si las coordenadas son correctas antes de las cosas
    private boolean checkCoordinates(int x, int y){
        if ((x % 2 == 0 && y % 2 == 0) || (x % 2 == 1 && y % 2 == 1)) {
            return true;
        } else {
            System.out.println("coordenadas invalidas ("+x+","+y+")");
            return false;
        }
    }

    public String checkBuilding(int x, int y,String type,String tribeName){
        if (type.equals("Town Hall")){
            return checkTownHall(x,y);
        } else if (type.equals("Store")) {
            return checkStore(x,y,tribeName);
        }
        return "UnAvailable";
    }

    private String checkStore(int x, int y,String tribeName) {
        String centralCellContent=getValue(x,y);
        if(centralCellContent.equals("Ground")){
            int xCoord1a=adjustCoordinateX(x+1,heightMap);
            int xCoord1b=adjustCoordinateX(x-1,heightMap);
            int xCoord2a=adjustCoordinateX(x+2,heightMap);
            int xCoord2b=adjustCoordinateX(x-2,heightMap);

            int yCoord1a=adjustCoordinateY(y+1,widthMap);
            int yCoord1b=adjustCoordinateY(y-1,widthMap);


            if(getValue(xCoord1b,yCoord1b).contains(tribeName) || getValue(xCoord1b,yCoord1a).contains(tribeName)
                    || getValue(xCoord1a,yCoord1b).contains(tribeName) || getValue(xCoord1a,yCoord1a).contains(tribeName)
                    || getValue(xCoord2b,y).contains(tribeName) || getValue(xCoord2a,y).contains(tribeName))
            {
                log.info(ANSI_YELLOW + " Comprueba casillas de alrededor: " +
                        "La casilla "+ xCoord1b + "," + yCoord1b + " contiene: "+ getValue(xCoord1b,yCoord1b) +
                        "La casilla "+ xCoord1b + "," + yCoord1a + " contiene: "+ getValue(xCoord1b,yCoord1a) +
                        "La casilla "+ xCoord1a + "," + yCoord1b + " contiene: "+ getValue(xCoord1a,yCoord1b) +
                        "La casilla "+ xCoord1a + "," + yCoord1a + " contiene: "+ getValue(xCoord1a,yCoord1a) +
                        "La casilla "+ xCoord2b + "," + y + " contiene: "+ getValue(xCoord2b,y) +
                        "La casilla "+ xCoord2a + "," + yCoord1b + " contiene: "+ getValue(xCoord2a,y)
                );
                return "Available";
            }
            else
            {
                log.info(ANSI_YELLOW + " Comprueba casillas de alrededor: " +
                        "La casilla "+ xCoord1b + "," + yCoord1b + " contiene: "+ getValue(xCoord1b,yCoord1b) +
                        "La casilla "+ xCoord1b + "," + yCoord1a + " contiene: "+ getValue(xCoord1b,yCoord1a) +
                        "La casilla "+ xCoord1a + "," + yCoord1b + " contiene: "+ getValue(xCoord1a,yCoord1b) +
                        "La casilla "+ xCoord1a + "," + yCoord1a + " contiene: "+ getValue(xCoord1a,yCoord1a) +
                        "La casilla "+ xCoord2b + "," + y + " contiene: "+ getValue(xCoord2b,y) +
                        "La casilla "+ xCoord2a + "," + yCoord1b + " contiene: "+ getValue(xCoord2a,y)
                );
                return "UnAvailable";
            }
        }
        else
        {
            log.info(ANSI_YELLOW + "La casilla actual"+ x + "," + y + " contiene: "+ centralCellContent
            );
            return "UnAvailable";
        }

    }

    private boolean checkSingleCell(int x,int y){
        return getValue(x,y).equals("Ground") || getValue(x,y).equals("Forest") || getValue(x,y).equals("Ore");
    }

    private String checkTownHall(int x, int y) {
        String centralCellContent=getValue(x,y);
        if(centralCellContent.equals("Ground")){
            int xCoord1a=adjustCoordinateX(x+1,heightMap);
            int xCoord1b=adjustCoordinateX(x-1,heightMap);
            int xCoord2a=adjustCoordinateX(x+2,heightMap);
            int xCoord2b=adjustCoordinateX(x-2,heightMap);

            int yCoord1a=adjustCoordinateY(y+1,widthMap);
            int yCoord1b=adjustCoordinateY(y-1,widthMap);


            if(checkSingleCell(xCoord1b,yCoord1b) && checkSingleCell(xCoord1b,yCoord1a)
                    && checkSingleCell(xCoord1a,yCoord1b) && checkSingleCell(xCoord1a,yCoord1a)
                    && checkSingleCell(xCoord2b,y) && checkSingleCell(xCoord2a,y))
            {
                log.info(ANSI_YELLOW + " Comprueba casillas de alrededor: " +
                        "La casilla "+ xCoord1b + "," + yCoord1b + " contiene: "+ getValue(xCoord1b,yCoord1b) +
                        "La casilla "+ xCoord1b + "," + yCoord1a + " contiene: "+ getValue(xCoord1b,yCoord1a) +
                        "La casilla "+ xCoord1a + "," + yCoord1b + " contiene: "+ getValue(xCoord1a,yCoord1b) +
                        "La casilla "+ xCoord1a + "," + yCoord1a + " contiene: "+ getValue(xCoord1a,yCoord1a) +
                        "La casilla "+ xCoord2b + "," + y + " contiene: "+ getValue(xCoord2b,y) +
                        "La casilla "+ xCoord2a + "," + yCoord1b + " contiene: "+ getValue(xCoord2a,y)
                );
                return "Available";
            }
            else
            {
                log.info(ANSI_YELLOW + " Comprueba casillas de alrededor: " +
                        "La casilla "+ xCoord1b + "," + yCoord1b + " contiene: "+ getValue(xCoord1b,yCoord1b) +
                        "La casilla "+ xCoord1b + "," + yCoord1a + " contiene: "+ getValue(xCoord1b,yCoord1a) +
                        "La casilla "+ xCoord1a + "," + yCoord1b + " contiene: "+ getValue(xCoord1a,yCoord1b) +
                        "La casilla "+ xCoord1a + "," + yCoord1a + " contiene: "+ getValue(xCoord1a,yCoord1a) +
                        "La casilla "+ xCoord2b + "," + y + " contiene: "+ getValue(xCoord2b,y) +
                        "La casilla "+ xCoord2a + "," + yCoord1b + " contiene: "+ getValue(xCoord2a,y)
                );
                return "UnAvailable";
            }
        }
        else
        {
            log.info(ANSI_YELLOW + "La casilla actual"+ x + "," + y + " contiene: "+ centralCellContent
            );
            return "UnAvailable";
        }
    }

    private static int adjustCoordinateX(int coord, int max) {
        if (coord < 1) {
            return coord == 0 ? max : max - 1;
        } else if (coord > max) {
            return coord == max + 1 ? 1 : 2;
        }
        return coord;
    }
    private static int adjustCoordinateY(int coord, int max) {
        if (coord < 1) {
            return max;
        } else if (coord > max) {
            return 1;
        }
        return coord;
    }

}
