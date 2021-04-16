package it.polimi.ingsw.Deposit;

import it.polimi.ingsw.Enums.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;

public class Market {
    private final Resource[][] grid;   //first position row, second position column: [row][column]
    private Resource slide;
    int posSelected;        //if ==-1 not initialized
    int isRowSelected;      //if ==1 selected row, ==0 column, ==-1 not initialized

    public synchronized void selectColumn(int columnSelected) throws IndexOutOfBoundsException{
        if (columnSelected > 3)
            throw new IndexOutOfBoundsException();

        if ((isRowSelected == 0) && (posSelected == columnSelected)) //it is used just to speed up --> no toggle selection here!
            return;

        if(isRowSelected != -1)
            deselectPreviousSelection();
        isRowSelected = 0;
        posSelected = columnSelected;
        for (int i=0; i<3; i++)
            grid[i][posSelected].select();
    }

    private synchronized EnumMap<Resource, Integer> pickColumn(int chosenColumn) {
        deselectPreviousSelection();
        EnumMap<Resource, Integer> returningMap = new EnumMap<>(Resource.class);
        for (int i=0; i<3; i++)
            returningMap.put(grid[i][chosenColumn], 1 + ( (returningMap.get(grid[i][chosenColumn]) == null) ? 0 : returningMap.get(grid[i][chosenColumn]) ));
        this.shiftColumn(chosenColumn);
        return returningMap;
    }

    private synchronized void shiftColumn(int column) {
        Resource support;
        support = slide;
        slide = grid[0][column];
        grid[0][column] = grid[1][column];
        grid[1][column] = grid[2][column];
        grid[2][column] = support;
    }

    public synchronized void selectRow(int rowSelected) throws IndexOutOfBoundsException{
        if (rowSelected > 2)
            throw new IndexOutOfBoundsException();

        if ((isRowSelected == 1) && (posSelected == rowSelected)) //it is used just to speed up --> no toggle selection here!
            return;

        if(isRowSelected != -1)
            deselectPreviousSelection();
        isRowSelected = 1;
        posSelected = rowSelected;
        for (int i=0; i<4; i++)
            grid[posSelected][i].select();
    }

    private synchronized EnumMap<Resource, Integer> pickRow(int chosenRow) {
        deselectPreviousSelection();
        EnumMap<Resource, Integer> returningMap = new EnumMap<>(Resource.class);
        for (int i=0; i<4; i++)
            returningMap.put(grid[chosenRow][i], 1 + ((returningMap.get(grid[chosenRow][i]) == null) ? 0 : returningMap.get(grid[chosenRow][i])) );
        this.shiftRow(chosenRow);
        return returningMap;
    }

    private synchronized void shiftRow(int row) {
        Resource support;
        support = slide;
        slide = grid[row][0];
        grid[row][0] = grid[row][1];
        grid[row][1] = grid[row][2];
        grid[row][2] = grid[row][3];
        grid[row][3] = support;
    }

    public synchronized Resource[][] getState(){
        Resource[][] gridCopy = new Resource[3][4];
        //cloning by row
        for (int i=0; i<3; i++){
            for (int j=0; j<4; j++){
                gridCopy[i][j] = grid[i][j];
            }
        }
        return gridCopy;
    }

    public EnumMap<Resource, Integer> takeSelection() throws IllegalAccessException{
        if (isRowSelected == -1)
            throw new IllegalAccessException(); //there was no previous selection!

        EnumMap<Resource, Integer> resourcesSelected;
        if (isRowSelected == 1)
            resourcesSelected = this.pickRow(posSelected);
        else
            resourcesSelected = this.pickColumn(posSelected);
        posSelected = -1;
        isRowSelected = -1;
        return resourcesSelected;
    }

    public synchronized Resource getSlide(){
        return slide;
    }

    private synchronized void deselectPreviousSelection(){
        if (isRowSelected == 1){
            for (int i=0; i<4; i++)
                grid[posSelected][i].select();
        }
        else{
            for (int i=0; i<3; i++)
                grid[i][posSelected].select();
        }
    }

    //it can be used when the player decide to not take anything from the market but had a previous selection
    public synchronized void restoreState(){
        deselectPreviousSelection();
        isRowSelected = -1;
        posSelected = -1;
    }

    public Market() {
        grid = new Resource[3][4];
        ArrayList<Resource> pickingList = new ArrayList<>();
        int i, j;

        for (i=0; i<2; i++) pickingList.add(Resource.COIN);
        pickingList.add(Resource.FAITH);
        for (i=0; i<2; i++) pickingList.add(Resource.SERVANT);
        for (i=0; i<2; i++) pickingList.add(Resource.SHIELD);
        for (i=0; i<2; i++) pickingList.add(Resource.STONE);
        for (i=0; i<4; i++) pickingList.add(Resource.WHITE);

        Collections.shuffle(pickingList);

        //initialization by row
        for (i=0; i<3; i++){
            for (j=0; j<4; j++){
                grid[i][j] = pickingList.remove(pickingList.size()-1);               //pickingList.remove returns the removed object
            }
        }
        slide = pickingList.remove(0);
        isRowSelected = -1;
        posSelected = -1;
    }
}
