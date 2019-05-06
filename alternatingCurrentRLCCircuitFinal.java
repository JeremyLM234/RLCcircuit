/**
 alternatingCurrentRLCCircuitV2
 
 Description: Calculates and prints the positions of charges in an rlc circuit in an output file.
 Current version: April 2019
 Author: Jeremy Lackman-Mincoff
 */

import java.io.*; //Provides for system input and output, through data streams, serialization and file systems

public class alternatingCurrentRLCCircuitFinal
{
  
  //////////////////////////////////
  //1. Declare constants
  //////////////////////////////////
  
  //time constants
  public static final double dt = 1.e-4; //time interval in seconds
  public static final double Tmax = 4.; //simulation time in seconds
  
  //circuit parameters
  //*note that we will be using the convention 1 C = 1 charge
  public static final double Imax = 100.; //maximum current of the source in charges/s
  public static final double omega = 20.*Math.PI; //angular frequency of the source in rad/s
  public static final double R = 25.; //resistance of the resistor in ohms
  public static final double L = 2.e-2; //inductance of the inductor in henrys
  public static final double C = 5.e-7; //capacitance of the capacitor in farads
  public static final double circuitLength = 500; //length of the circuit in one x-direction from the axis(either positive or negative)
  public static final double circuitHeight = 300; //length of the circuit in one y-direction from the axis(either positive or negative)
  public static final double phaseAngle = findPhaseAngle(omega, R, L, C); //calling the phaseAngle method to calculate the phase angle
  public static final int dataIncrement = 20; //every dataIncrement data points will be printed in the output file, this increment will need to be used in the plotCircuit code as well
  
  //locations of the source of AC current, resistor, inductor, and capacitor respectively
  public static final double[] SRLCx = {25+circuitLength, 25+2*circuitLength, 25+circuitLength, 25}; //locations of the source of AC current, resistor, inductor, and capacitor respectively
  public static final double[] SRLCy = {20+2*circuitHeight, 30+circuitHeight, 20, 30+circuitHeight}; //these values are strange for aesthetic purposes
  
  //main method
  public static void main (String[] args)
  {
    //////////////////////////////////
    //2. Open file to store data
    //////////////////////////////////
    
    PrintWriter outputFile = null; //prints a text output stream to represent an object, does not yet specify a file
    try //checks for an error in the loop, runs what is in the catch scope if an error is found
    {
      outputFile = new PrintWriter (new FileOutputStream("acCurrentRLCCircuit.txt", false)); //Creates a file output with the name "acCurrentRLCCircuit".txt
    }
    catch (FileNotFoundException e) //shows that an attempt to open the file has failed
    {
      System.out.println("There was a problem opening the file. The program will be terminated.");
      System.exit(0); //terminates the Java Virtual Machine
    }
    
    //////////////////////////////////////////////////
    //3. Declare initial conditions and arrays
    //////////////////////////////////////////////////
    
    int N = (int) (Tmax / dt); //number of iterations of loop
    
    double[] t = new double[N]; //array for time
    double[] I = new double[N]; //array for current of series circuit
    double separationDistance = 50; //distance between consecutive charges
    double[][] s = new double[(int)(4*circuitLength/separationDistance + 4*circuitHeight/separationDistance)][N]; //array for position of charges on a straight line representing the total length of all segments of the circuit --> the starting point of zero distance will be the bottom-left corner of the circuit
    boolean print = false; //for desk check
    
    t[0] = 0; //initialize time to zero
    I[0] = Imax*Math.sin(-phaseAngle); //knowing that I[i] varies according to Imax*Math.sin(omega*t[i] - phaseAngle), and t[0] = 0, we can initialize I[0] to Imax*Math.sin(-phaseAngle)
    
    //initializing charge positions
    for (int j = 0; j < s.length; j++)
    {
      s[j][0] = j*separationDistance;
    }
    
    if (print)
      System.out.println(s[0][0]); //desk check
    
    /////////////////////////////////////////////////////////////
    //4. Runge-Kutta method (4th order)
    /////////////////////////////////////////////////////////////
    
    for (int j = 0; j < s.length; j++)
    {
      for (int i = 1; i < N; i++)
      {
        t[i] = t[i-1] + dt; //incrementing time
        
        //incrementing current
        double k1 = rungeKutta(t[i-1],0);
        double k2 = rungeKutta(t[i-1] + dt/2,dt*k1/2);
        double k3 = rungeKutta(t[i-1] + dt/2,dt*k2/2);
        double k4 = rungeKutta(t[i-1] + dt,dt*k3); //calling the rungeKutta method to calculate the constants k1, k2, k3, k4 in the runge-kutta method
        
        I[i] = I[i-1] + dt*(k1/6 + k2/3 + k3/3 + k4/6);
        
        if (I[i] == 0) //if the current is zero
        {
          s[j][i] = s[j][i-1]; //charges do not move
        }
        if (s[j][i-1] <= 4*circuitLength + 4*circuitHeight && s[j][i-1] >= 0 && I[i-1] > 0) //if charges are not beyond the circuit boundaries and the current is positive
        {
          s[j][i] = s[j][i-1] + separationDistance*Math.abs(I[i-1])*dt; //increment position
          
          if (s[j][i] > 4*circuitLength + 4*circuitHeight) ////if charges will be past the furthest position from the start position
          {
            double temp = s[j][i]; //store the old value of the position in a temporary variable
            s[j][i] = (temp - 4*circuitLength - 4*circuitHeight); //reset position, inlcluding the remainder
          }
        }
        if (s[j][i-1] >= 0 && s[j][i-1] <= 4*circuitLength + 4*circuitHeight && I[i-1] < 0) //if charges are not beyond the circuit boundaries and the current is negative
        {
          s[j][i] = s[j][i-1] - separationDistance*Math.abs(I[i-1])*dt; //increment position
          
          if (s[j][i] < 0) //if charges will be past the start position
          {
            double temp = s[j][i]; //store the old value of the position in a temporary variable
            s[j][i] = (4*circuitLength + 4*circuitHeight + temp); //reset position, inlcuding the remainder
          }
        }
        if (j == 0 && i<3000 && print)
          System.out.println(s[j][i]); //desk check
      }
    }
    
    /////////////////////////////////////////////////////////////
    //5. Printng and plotting data
    /////////////////////////////////////////////////////////////
    
    for (int j = 0; j < s.length; j++)
    {
      for (int i = 0; i < N; i+=dataIncrement) //prints every dataIncrement data points in the output file, this increment will need to be used in the plotCircuit code as well
      {
        outputFile.println(s[j][i]); //print the position data on the output file
      }
      outputFile.println();
    }
    
    outputFile.close(); //close the output file
    plotCircuit.main(new String[0]); //plotting the output data
  } //main method
  
  //findPhaseAngle method which returns the phase angle of the circuit
  public static double findPhaseAngle(double angularFrequency, double resistance, double inductance, double capacitance)
  {
    double phaseAngle = 0;
    phaseAngle = Math.atan((angularFrequency*inductance - (1/(angularFrequency*capacitance)))/resistance); //from the geometry of the phasor diagram
    return phaseAngle;
  } //phaseAngle method
  
  //rungeKutta method which returns a constant for the runge-kutta method
  public static double rungeKutta(double time, double extraTerm)
  {
    double k = 0;
    k = omega*Imax*Math.cos(omega*time - phaseAngle) + extraTerm;
    return k;
  } //rungeKutta method
}