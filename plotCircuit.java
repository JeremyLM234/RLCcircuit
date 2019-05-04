/**
 plotCircuit
 
 Description: Reads a file storing the position data for charges in an rlc circuit and plots the charges moving in the circuit.
 Current Version: April 2019
 Author: Jeremy Lackman-Mincoff
 */

import java.awt.*; //Contains classes for graphics
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.io.*;  //Provides for system input and output, through data streams, serialization and file systems
import java.util.Scanner;

final public class plotCircuit
{
  
  //create Color objects
  final static Color red = Color.red;
  final static Color blue = Color.cyan;
  final static Color yellow = Color.yellow;
  final static Color green = Color.green;
  final static Color orange = Color.orange;
  final static Color black = Color.black;
  
  //constants
  public static final double dt = 1.e-4; //time interval in seconds //**Verify that these are the same as in alternatingCurrentRLCCircuitV2.java
  public static final double Tmax = 4.; //simulation time in seconds
  public static final double circuitLength = 500; //length of the circuit in one x-direction (either positive or negative)
  public static final double circuitHeight = 300; //length of the circuit in one y-direction (either positive or negative)
  public static final double[] SRLCx = {25+circuitLength, 25+2*circuitLength, 25+circuitLength, 25}; //locations of the source of AC current, resistor, inductor, and capacitor respectively
  public static final double[] SRLCy = {20+2*circuitHeight, 30+circuitHeight, 20, 30+circuitHeight}; //**the top-left corner of the circuit is at (50,50), these coordinate values  in the arrays appear to be strange for aesthetic purposes
  public static final double dataIncrement = 10; //every dataIncrement data points were be printed in the output file
  
  //values to be used in multiple methods
  static double[][] s; //empty position array
  static double[][] x; //empty x-coordinate array (on the jframe)
  static double[][] y; //empty y-coordinate array (on the jframe)
  static double separationDistance = 50; //distance between consecutive charges
  static int N = (int) (Tmax / dt); //number of iterations of loop
  static boolean print = false; //for desk check
  JFrame window = new JFrame("RLC_Circuit_Alternating_Current"); //creates the JFrame object with the given name to be used throughout the code
  DrawWindow drawWindow = new DrawWindow();
  
  public static int k = 0; //a loop counter to be used in multiple loops
  public static int l = 0; //a loop counter to be used in multiple loops
  
  public static void main(String t[])
  {
    
    String filename = "acCurrentRLCCircuit.txt"; //previous output file containing position data for charges
    File file = new File (filename); //passing the data file to the File object
    
    try
    {
      s = new double[(int)(4*circuitLength/separationDistance + 4*circuitHeight/separationDistance)][N]; //defining the lengths of the 2D position array --> notice the dataIncrement + 1 term in the length since, after the complete position data for a given charge, there is a newline character
      x = new double[s.length][s[0].length]; //lengths of x and y-coordinate arrays
      y = new double[s.length][s[0].length];
      
      int currentLineNumber = 1; //the line that the scanner is reading
      
      Scanner fileScanner = new Scanner(file); //will scan each line of the input file
      
      while (fileScanner.hasNext()) //while the scanner still has more lines to read
      {
        for (int j = 0; j < s.length; j++)
        {
          for (int i = 0; i < (int)(N/dataIncrement); i++)
          {        
            String currentValue = fileScanner.next(); //the string of numbers on the line being read by the scanner will be assigned to the variable currentValue
            s[j][i] = Double.parseDouble(currentValue); //parseDouble takes a string of numbers and converts it to a double
            fileScanner.nextLine(); //advance the scanner to the next line
            currentLineNumber++; //increment the line number
            
            if (currentLineNumber == N + 1) //if the scanner is reading the line immediately after the complete position data for one charge
            {
              fileScanner.nextLine(); //scanner moves down one line
              currentLineNumber = 1; //the line number is reset
            }
            
            if (j == 1 && print)
              System.out.println(s[j][i]); //desk check
          }
        }
      }
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
    
    new plotCircuit().createJFrame();
  } //main method
  
  //createJFrame instance method which defines the JFrame for the code
  public void createJFrame()
  {
    //defining the JFrame properties
    window.getContentPane().add(drawWindow); //the content pane is a visual component of a Java program that can hold other visual components
    window.setSize((int)(2*circuitLength + 100),(int)(2*circuitHeight + 100)); //sets the size of the frame
    window.setVisible(true); //makes the frame visible
    window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //defines the closing operation for the frame
    window.setResizable(false); //makes the user unable to change the size of the frame
    window.setLocationRelativeTo(null); //centers the frame on the screen
    moveCharges();
  }
  
  // paint instance method which creates graphics in the JFrame
  class DrawWindow extends JPanel
  {
    public void paint(Graphics ga)
    {
      
      Font f = new Font("Times New Roman", Font.PLAIN, 40); //creating a Font object
      ga.setFont(f); //setting the font
      
      //drawing axes separating circuit into segments of circuitLength and circuitHeight
      ga.setColor(black);
      ga.drawLine(0,(int)(circuitHeight + 50),(int)(2*circuitLength + 100),(int)(circuitHeight + 50)); //draws a line corresponding to the x-axis //**Verify that the final point represents the length of the frame
      ga.drawLine((int)(circuitLength + 50),0,(int)(circuitLength + 50),(int)(2*circuitHeight + 100)); //draws a line corresponding to the y-axis //**Verify that the final point represents the eidth of the frame
      
      //drawing the circuit (rectangle)
      java.awt.Graphics2D gb = (java.awt.Graphics2D) ga.create();
      
      gb.setStroke(new java.awt.BasicStroke(8)); //thickness of 8
      gb.setColor(black);
      gb.drawRect(50,50,(int)(2*circuitLength),(int)(2*circuitHeight)); // (int xpos,int ypos,int length,int width)
      
      //drawing the source of AC current
      ga.setColor(orange);
      ga.fillOval((int)(SRLCx[0]),(int)(SRLCy[0]),50,50);
      ga.setColor(black);
      ga.drawString("~",(int)(SRLCx[0]+15),(int)(SRLCy[0]+35)); //the text is offset from the circle for aesthetics
      
      //drawing the resistor
      ga.setColor(blue);
      ga.fillOval((int)(SRLCx[1]),(int)(SRLCy[1]),50,50);
      ga.setColor(black);
      ga.drawString("R",(int)(SRLCx[1]+13),(int)(SRLCy[1]+35)); //the text is offset from the circle for aesthetics
      
      //drawing the inductor
      ga.setColor(green);
      ga.fillOval((int)(SRLCx[2]),(int)(SRLCy[2]),50,50);
      ga.setColor(black);
      ga.drawString("L",(int)(SRLCx[2]+15),(int)(SRLCy[2]+37)); //the text is offset from the circle for aesthetics
      
      //drawing the capacitor
      ga.setColor(red);
      ga.fillOval((int)(SRLCx[3]),(int)(SRLCy[3]),50,50);
      ga.setColor(black);
      ga.drawString("C",(int)(SRLCx[3]+12),(int)(SRLCy[3]+37)); //the text is offset from the circle for aesthetics
      
      //drawing the charges
      ga.setColor(yellow);
      
      for (l = k; l < s[0].length; k++)
      {
        for (int j = 0; j < s.length; j++) //note the reversed order of the loops since we show all charges at a single time on the circuit
        {
          ga.fillOval((int)(x[j][l]),(int)(y[j][l]),8,8);
        }
        if (l < s[0].length)
        {
          l = s[0].length;
        }
      }
      l = k;
    } //paint
  } //DrawWindow
  
  public void moveCharges()
  {
    for (l = k; l < s[0].length;)
    {
      for (int j = 0; j < s.length; j++)
      {
        if (s[j][l] >= 0 && s[j][l] <= 2*circuitLength) //charges on the bottom segment of the circuit
        {
          x[j][l] = (int)(46+s[j][l]);
          y[j][l] = (int)(46+2*circuitHeight);  //charges are slightly offset for aesthetic purposes
        }
        if (s[j][l] > 2*circuitLength && s[j][l] <= 2*circuitLength+2*circuitHeight) //charges on the right segment of the circuit
        {
          x[j][l] = (int)(46+2*circuitLength);
          y[j][l] = (int)(46+2*circuitHeight-(s[j][l]-2*circuitLength));  //charges are slightly offset for aesthetic purposes
        }
        if (s[j][l] > 2*circuitLength+2*circuitHeight && s[j][l] <= 4*circuitLength+2*circuitHeight) //charges on the top segment of the circuit
        {
          x[j][l] = (int)(46+2*circuitLength-(s[j][l]-(2*circuitLength+2*circuitHeight)));
          y[j][l] = 46;  //charges are slightly offset for aesthetic purposes
        }
        if (s[j][l] > 4*circuitLength+2*circuitHeight && s[j][l] < 4*circuitLength+4*circuitHeight) //charges on the left segment of the circuit
        {
          x[j][l] = 46;
          y[j][l] = (int)(46+(s[j][l]-(4*circuitLength+2*circuitHeight))); //charges are slightly offset for aesthetic purposes
        }
      }
      try
      {
        Thread.sleep(10);
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }
      window.repaint(); //repaint the JFrame using the new charge positions
    }
  }
}