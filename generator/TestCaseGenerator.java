import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * Program that generates several test cases for the elevator
 * algorithm
 * 
 * The output can be modified by changing the global values
 * 
 * The program produces 3 textFiles:
 * 
 * buildingStats.txt which contains information about the generated
 * sample building
 * 
 * floorStats.txt which shows which floor the workers are currently
 * on. This should always show all the workers on the first line
 * which corresponds to floor 0 which corresponds to correct output
 * 
 * output.txt the file that contains the data that is used by the 
 * elevator algorithm
 * 
 * @author Joseph Del Prete
 */

public class TestCaseGenerator {

    private static final int NIGHT_SHIFT_BEGIN = 2300; // 11:00pm
    private static final int NIGHT_SHIFT_END = 500; // 5:00am
    private static int buildingFloors = 10; // number of floors in building
    private static int maxCapacity; // determined by building floors
    private static double workerPercentage = 0.6; 
    private static double custodianPercentage = 0.05;
    private static int amountOfWorkers; // equals maxCapacity * workerPercentage
    private static int amountOfCustodians;
    private static int numberOfGuests;
    private static int openingTime = 900; // 9:00 am
    private static int closingTime = 1700; // 5:00 pm
    private static int daysToGenerate = 1; // how many days will be printed to output.txt
    private static int numberOfDayShifEmployees; // equivalent to amountOfWorkers
    private static int numberOfNightShiftEmployees; // maxCapacity * custodianPercentage

    /**
     * Used to represent the building in which the people travel
     */
    static ArrayList<ArrayList<JPerson>> building = new ArrayList<ArrayList<JPerson>> ();
    
    /**
     * Used to represent each individual access to the elevator
     * This list is much larger than the actual number of people
     * involved in the simulation
     */
    static ArrayList<JPerson> personList = new ArrayList<JPerson> ();

    /**
     * Initializes the building ArrayList of ArrayList<JPerson>
     * Must be called before performing operations on the global
     * variable building
     */
    public static void initBuilding () {
        for (int i = 0; i < buildingFloors; i++) {
            ArrayList<JPerson> tempList = new ArrayList<JPerson> ();
            building.add (tempList);
        }
    }

    /**
     * Calls the generateNormalPeople() method. Then it sorts the output
     * generated by the generateNormalPeople() method and copies it to a
     * local ArrayList to allow support for multiple days to 
     * be generated. Then it calls the writeOutput() method
     * to write the output
     * @param args
     */
    @SuppressWarnings("unchecked")
    public static void main (String[] args) {
        // do not necessarily need main
        // serves mainly as a caller for other methods
        // initBuilding();
        ArrayList<JPerson> testPeople = new ArrayList<JPerson> ();
        for (int i = 0; i < daysToGenerate; i++) {
            initBuilding ();
            ArrayList<JPerson> simplePeople = generateNormalPeople ();
            Collections.sort (simplePeople);
            for (JPerson person : simplePeople) {
                testPeople.add (person);
            }
            // reset everything
            // personList.clear ();
            if (i != daysToGenerate - 1) {
                personList.clear ();
                building.clear ();
            }
        }

        try {
            writeOutput (testPeople, "output.txt");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace ();
        }
    }

    /**
     * Creates Workers at the start of their shift
     * Dependent on the global variable openingTime
     * @param randInt used to simulate randomness in their entry
     */
    public static void generateWorkers (Random randInt) {
        // generate workers for the morning, they enter at 9:00am
        int count = 0;
        for (; count < amountOfWorkers; count++) {
            int randomNumber = randInt.nextInt () % 10;
            if (randomNumber < 0) {
                randomNumber *= -1;
            }
            JPerson person = new JPerson (
            // openingTime + randInt.nextInt () % 10, // time
                    openingTime + randomNumber, 1, // direction
                    0, workerBeginDayChooseFloor () // destination floor
                    , "CREATE");
            building.get (person.getDestFloor ()).add (person); // add to
                                                                // destination
                                                                // floor
            personList.add (person);
        }
    }

    public static void generateGuests (int amountToGenerate, int startingTime, Random randInt) {
        // generate guests throughout operating hours
        numberOfGuests = amountToGenerate;
        int count = 0;
        for (; count < amountToGenerate; count++) {
            int randomNumber = randInt.nextInt () % 10;
            if (randomNumber < 0) {
                randomNumber *= -1;
            }
            JPerson person = new JPerson (startingTime + randomNumber, // time
                    1, // direction
                    0, workerBeginDayChooseFloor (), // destination floor
                    "CREATE");
            building.get (person.getDestFloor ()).add (person); // add person to
                                                                // destination
                                                                // floor
            personList.add (person);
        }
    }
    
    public static void moveGuestsAround (int timeToMove, Random randInt, int numberOfCalls) {
        int loopCount = 0;
        int count = personList.size() - numberOfGuests;
        for (; loopCount < numberOfGuests; loopCount++) {
            int randomNumber = randInt.nextInt () % 10;
            if (randomNumber < 0) {
                randomNumber *= -1; // make negative
            }
            JPerson temp = personList.get (loopCount + count);
            // JPerson person = new JPerson(closingTime + randInt.nextInt () %
            // 10, -1, temp.getDestFloor (), 0);
            int floorToTravel = chooseRandomFloor (temp.getDestFloor ());
            int currentFloor = temp.getDestFloor ();
            int direction = 0;
            if (floorToTravel < currentFloor) {
                direction = -1;
            } else if (floorToTravel > currentFloor) {
                direction = 1;
            } else {
                // shouldn't happen
                direction = 0;
            }
            JPerson person = new JPerson (timeToMove + randomNumber, direction,
                    currentFloor, floorToTravel, "MOVE");
            
            building.get (person.getInitialFloor ()).remove (
                    personList.get (loopCount + count)); // remove person from previous
                                             // floor
            building.get (person.getDestFloor ()).add (person); // add person to
                                                                // new floor
            personList.add (person);
        }
    }
    
    public static void makeGuestsLeave (int timeToLeave, Random randInt) {
        // custodians then leave at 5:00am
        int currentPosition = personList.size () - numberOfGuests;// -1;
        int count = 0;
        for (; count < numberOfGuests; count++) {
            int randomNumber = randInt.nextInt () % 10;
            if (randomNumber < 0) {
                randomNumber *= -1;
            }
            JPerson temp = personList.get (currentPosition + count);
            JPerson person = new JPerson (timeToLeave + randomNumber, -1,
                    temp.getDestFloor (), 0, "MOVE");
            building.get (person.getInitialFloor ()).remove (
                    personList.get (currentPosition + count)); // remove person
                                                               // from old floor
            building.get (person.getDestFloor ()).add (person); // add person to
                                                                // new floor
            personList.add (person);
        } // personList grows by += numberOfGuests
    }
    
    /**
     * Simulates workers traveling through the building
     * Requires that the generateWorkers method be called first
     * Also requires that the makeWorkersLeave method has not yet been
     * called
     * 
     * @param timeToMove time that you want the workers to start traveling
     * in the building
     * @param randInt provides randomness to their movement
     * @param numberOfCalls how many times you called this method before
     * the first time you call it, this value should be 0, the next time
     * the value should be 1, and so on. 
     */
    public static void moveWorkersAround (int timeToMove, Random randInt,
            int numberOfCalls) {
        int loopCount = 0;
        int count = 0 + ( amountOfWorkers * numberOfCalls);
        for (; loopCount < amountOfWorkers; loopCount++) {
            int randomNumber = randInt.nextInt () % 10;
            if (randomNumber < 0) {
                randomNumber *= -1; // make negative
            }
            JPerson temp = personList.get (loopCount + count);
            // JPerson person = new JPerson(closingTime + randInt.nextInt () %
            // 10, -1, temp.getDestFloor (), 0);
            int floorToTravel = chooseRandomFloor (temp.getDestFloor ());
            int currentFloor = temp.getDestFloor ();
            int direction = 0;
            if (floorToTravel < currentFloor) {
                direction = -1;
            } else if (floorToTravel > currentFloor) {
                direction = 1;
            } else {
                // shouldn't happen
                direction = 0;
            }
            JPerson person = new JPerson (timeToMove + randomNumber, direction,
                    currentFloor, floorToTravel, "MOVE");
            
            building.get (person.getInitialFloor ()).remove (
                    personList.get (loopCount + count)); // remove person from previous
                                             // floor
            building.get (person.getDestFloor ()).add (person); // add person to
                                                                // new floor
            
            personList.add (person);
        }
    }
    
    /**
     * Simulates the workers leaving the building
     * Requires that the generateWorkers() method 
     * and the moveWorkersAround() method have both been called 
     * 
     * @param randInt provides randomness to their departure time
     */
    public static void makeWorkersLeave (Random randInt) {
        // workers then leave at 5:00pm
        closingTime = 1700;
        int count = 0;
        int sizeOfList = personList.size ();
        for (; count < amountOfWorkers; count++) {
            int randomNumber = randInt.nextInt () % 10;
            if (randomNumber < 0) {
                randomNumber *= -1;
            }
            JPerson temp = personList
                    .get (sizeOfList - amountOfWorkers + count);
            // JPerson person = new JPerson(closingTime + randInt.nextInt () %
            // 10, -1, temp.getDestFloor (), 0);
            JPerson person = new JPerson (closingTime + randomNumber, -1,
                    temp.getDestFloor (), 0, "MOVE");
            building.get (person.getInitialFloor ()).remove (
                    personList.get (sizeOfList - amountOfWorkers + count)); // remove
                                                                            // person
                                                                            // from
                                                                            // previous
            // floor
            building.get (person.getDestFloor ()).add (person); // add person to
                                                                // new floor
            personList.add (person);
        } // personList grows by += amountOfWorkers
    }

    /**
     * Creates Custodians at the start of their shift
     * Dependent on the global variable NIGHT_SHIFT_BEGIN
     * @param randInt used to simulate randomness in their entry
     */
    public static void generateCustodians (Random randInt) {
        // generate custodians for late night shift
        // custodians come in at NIGHT_SHIFT_BEGIN for night shift
        amountOfCustodians = (int) ( maxCapacity * custodianPercentage);
        numberOfNightShiftEmployees = amountOfCustodians;
        int count = 0;
        for (; count < numberOfNightShiftEmployees; count++) {
            int randomNumber = randInt.nextInt () % 10;
            if (randomNumber < 0) {
                randomNumber *= -1;
            }
            JPerson person = new JPerson (NIGHT_SHIFT_BEGIN + randomNumber, // time
                    1, // direction
                    0, workerBeginDayChooseFloor (), // destination floor
                    "CREATE");
            building.get (person.getDestFloor ()).add (person); // add person to
                                                                // destination
                                                                // floor
            personList.add (person);
        }
    }

    /**
     * Simulates the custodians leaving the building
     * Requires that the generateCustodians() method 
     * and the moveCustodiansAround() method have both been called 
     * 
     * @param randInt provides randomness to their departure time
     */
    public static void makeCustodiansLeave (Random randInt) {
        // custodians then leave at 5:00am
        int currentPosition = personList.size () - amountOfCustodians;// -1;
        int count = 0;
        for (; count < amountOfCustodians; count++) {
            int randomNumber = randInt.nextInt () % 10;
            if (randomNumber < 0) {
                randomNumber *= -1;
            }
            JPerson temp = personList.get (currentPosition + count);
            JPerson person = new JPerson (NIGHT_SHIFT_END + randomNumber, -1,
                    temp.getDestFloor (), 0, "MOVE");
            building.get (person.getInitialFloor ()).remove (
                    personList.get (currentPosition + count)); // remove person
                                                               // from old floor
            building.get (person.getDestFloor ()).add (person); // add person to
                                                                // new floor
            personList.add (person);
        } // personList grows by += amountOfCustodians
        
    }
    
    /**
     * Simulates custodians traveling through the building
     * Requires that the generateCustodians() method be called first
     * Also requires that the makeCustodiansLeave() method has not yet been
     * called
     * 
     * @param timeToMove time that you want the custodians to start traveling
     * in the building
     * @param randInt provides randomness to their movement
     * @param numberOfCalls how many times you called this method before
     * the first time you call it, this value should be 0, the next time
     * the value should be 1, and so on. 
     */
    public static void moveCustodiansAround (int timeToMove, Random randInt, int numberOfCalls) {
        int loopCount = 0;
        int count = personList.size() - amountOfCustodians;
        for (; loopCount < amountOfCustodians; loopCount++) {
            int randomNumber = randInt.nextInt () % 10;
            if (randomNumber < 0) {
                randomNumber *= -1; // make negative
            }
            JPerson temp = personList.get (loopCount + count);
            // JPerson person = new JPerson(closingTime + randInt.nextInt () %
            // 10, -1, temp.getDestFloor (), 0);
            int floorToTravel = chooseRandomFloor (temp.getDestFloor ());
            int currentFloor = temp.getDestFloor ();
            int direction = 0;
            if (floorToTravel < currentFloor) {
                direction = -1;
            } else if (floorToTravel > currentFloor) {
                direction = 1;
            } else {
                // shouldn't happen
                direction = 0;
            }
            JPerson person = new JPerson (timeToMove + randomNumber, direction,
                    currentFloor, floorToTravel, "MOVE");

            building.get (person.getInitialFloor ()).remove (
                    personList.get (loopCount + count)); // remove person
                                                               // from old floor
            building.get (person.getDestFloor ()).add (person); // add person to
                                                                // new floor
            personList.add (person);
        }
    }
    
    /**
     * Simulates travel throughout the building for one day
     * from time t = 0, to time t = 2359
     * 
     * @return personList used in the main method to format output
     * and calculate elevator access calls
     */
    public static ArrayList<JPerson> generateNormalPeople () {
        Random randInt = new Random (1000); // seed = 1000
        maxCapacity = ( randInt.nextInt () % buildingFloors) * 100;
        if (maxCapacity < 0) {
            maxCapacity = maxCapacity * -1;
        }
        amountOfWorkers = (int) ( maxCapacity * workerPercentage);
        numberOfDayShifEmployees = amountOfWorkers;

        generateWorkers (randInt);
        
        moveWorkersAround (1100, randInt, 0);
        moveWorkersAround (1300, randInt, 1);
        moveWorkersAround (1500, randInt, 2);

        makeWorkersLeave (randInt);

        generateCustodians (randInt);
        
        moveCustodiansAround (200, randInt, 0);
        moveCustodiansAround (400, randInt, 1);
        moveCustodiansAround (430, randInt, 2);
        
        makeCustodiansLeave (randInt);
        
        return personList;
    }

    /**
     * Simulates a worker chooses a floor when he starts his day
     * 
     * The floor he choose can not be zero
     * @return the randomly generated floor he has been assigned to
     * go to 
     */
    public static int workerBeginDayChooseFloor () {
        int destinationFloor;
        Random randInt = new Random ();
        int randomNumber = randInt.nextInt ();
        if (randomNumber < 0) {
            randomNumber = randomNumber * -1;
        }
        destinationFloor = randInt.nextInt () % buildingFloors;
        if (destinationFloor < 0) {
            destinationFloor *= -1;
        }
        if (destinationFloor == 0) {
            destinationFloor += 1;
        }
        return destinationFloor;
    }

    
    /**
     * Simulates a floor to go to after the person is already in
     * the building
     * @param currentFloor the floor that the person is currently at
     * before calling the elevator
     * @return the floor that the person chose to go to based on the
     * randomly generated value
     */
    public static int chooseRandomFloor (int currentFloor) {
        int randomFloor;
        Random randInt = new Random ();
        int randomNumber = randInt.nextInt ();
        if (randomNumber < 0) {
            randomNumber = randomNumber * -1;
        }
        randomFloor = randInt.nextInt () % buildingFloors;
        if (randomFloor < 0) {
            randomFloor *= -1; // make it positive
        }
        
        if (randomFloor == 0) {
            randomFloor += 1;
        }

        if (randomFloor == currentFloor) {
            // change the floor by 2
            randomFloor += 2;
            if (randomFloor > ( buildingFloors - 1)) {
                randomFloor -= 4;
            } else if (randomFloor < 0) {
                randomFloor += 4;
            }
        }

        return randomFloor;
    }
    

    /**
     * Writes the output to the file with the name specified by outputFile
     * 
     * @param localPersonList local copy of the global variable personList
     * @param outputFile provides the name of the output file
     * @throws IOException in case the file is not found
     */
    public static void writeOutput (ArrayList<JPerson> localPersonList,
            String outputFile) throws IOException {
        
        /*
         * write the output to the outputFile file
         */
        File file = new File (outputFile);
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter (file);
        } catch (IOException e) {
            System.out.println ("File not found");
            e.printStackTrace ();
        }
        BufferedWriter bw = new BufferedWriter (fileWriter);
        for (int count = 0; count < localPersonList.size (); count++) {
            JPerson temp = localPersonList.get (count);
            bw.write (temp.getCreateOrMove () + " " //+ "Time: "
                    + temp.getTime () + " " + //"Direction: "
                    + temp.getDirection () + " " + //"Init Floor: "
                    + temp.getInitialFloor () + " "// + "Dest Floor: "
                    + temp.getDestFloor ());
            bw.write ("\n");
        }
        bw.flush ();

        /*
         * write the output to the buildingStats 
         */
        File statFile = new File ("buildingStats.txt");
        fileWriter = null;
        try {
            fileWriter = new FileWriter (statFile);
        } catch (IOException e) {
            System.out.println ("File not found");
            e.printStackTrace ();
        }
        bw = new BufferedWriter (fileWriter);

        bw.write ("Max Capacity: " + maxCapacity + "\n");
        bw.write ("Number of Day Shift Employees: " + numberOfDayShifEmployees
                + "\n");
        bw.write ("Number of Night Shift Employees: "
                + numberOfNightShiftEmployees + "\n");
        bw.write ("Number of Days Generated: " + daysToGenerate + "\n");
        bw.write ("Number of Elevator calls: " + personList.size ()
                * daysToGenerate);

        bw.flush ();

        /*
         * write the output to the floorStats file
         */
        File statFile2 = new File ("floorStats.txt");
        fileWriter = null;
        try {
            fileWriter = new FileWriter (statFile2);
        } catch (IOException e) {
            System.out.println ("File not found");
            e.printStackTrace ();
        }
        bw = new BufferedWriter (fileWriter);

        for (int i = 0; i < buildingFloors; i++) {
            int count = 0;
            //ArrayList<JPerson> temp = building.get (i);
            for (int j = 0; j < building.get(i).size(); j++) {
                count++;
                bw.write (count + " ");
            }
            bw.write ("\n");
        }

        /*
         * clean up after myself
         */
        bw.flush ();
        bw.close ();
        fileWriter.close ();
    }
}
