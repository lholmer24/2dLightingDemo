package com.example.newfxmain;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.event.EventHandler;

import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Line;

import java.lang.Math;
import java.util.ArrayList;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {

        Circle lPoint = new Circle(0, 0, 1); //secretly tracks start of drawn lines, have to use object with .get() and . set()
                                                       //cause for some reason an object's methods are in scope but variables aren't

        Canvas canvas = new Canvas();
        canvas.setHeight(512);
        canvas.setWidth(512);
        //create canvas

        VBox vbox = new VBox(canvas);
        Scene scene = new Scene(vbox);
        primaryStage.setScene(scene);
        primaryStage.show();
        //define scene and stuff like that

        ArrayList<Line> lineList = new ArrayList<>(); //create array of lines

        render(canvas, 256, 256, lineList);

        EventHandler<MouseEvent> lightHandler = e -> render(canvas, (int)e.getX(), (int)e.getY(),  lineList);
        scene.addEventFilter(MouseEvent.MOUSE_MOVED, lightHandler); //render whenever mouse moves with light at mouse location
        scene.addEventFilter(MouseEvent.MOUSE_DRAGGED, lightHandler);

        EventHandler<MouseEvent> lineHandler = e -> {
            lPoint.setCenterX(e.getX());
            lPoint.setCenterY(e.getY());};
        scene.addEventFilter(MouseEvent.MOUSE_PRESSED, lineHandler); //record when mouse goes down

        EventHandler<MouseEvent> lineHandler2 = e -> {
            if(e.getX() != lPoint.getCenterX() || e.getY() != lPoint.getCenterY()) {
                lineList.add(new Line(e.getX(), e.getY(), lPoint.getCenterX(), lPoint.getCenterY()));}
            render(canvas, (int)e.getX(), (int)e.getY(),  lineList);};
        scene.addEventFilter(MouseEvent.MOUSE_RELEASED, lineHandler2); //draw line using recorded position when mouse is released

        EventHandler<MouseEvent> lineHandler3 = e -> {
            if (e.getButton() == MouseButton.SECONDARY){lineList.clear();}
            render(canvas, (int)e.getX(), (int)e.getY(),  lineList);};
        scene.addEventFilter(MouseEvent.MOUSE_CLICKED, lineHandler3); //clear all lines on right click
    }
    
    public void render(Canvas canvas, int lightX, int lightY, ArrayList<Line> lineList){
        int size = 512; //size of canvas
        int xDist, yDist; //variables for differences in point
        int lightR = 500; //size of light
        int c; //shading value
        double hyp; //length of hypotenuse from light to pixel
        boolean occluded; //whether there is shadow on the pixel

        double colorScale = (double) 255/ (double) lightR; //color scaling for length of fade

        GraphicsContext graphicsContext2D = canvas.getGraphicsContext2D();

        graphicsContext2D.setStroke(Color.rgb(200,0,0));
        graphicsContext2D.setLineWidth(5.0);


        for(int i1 = 0; i1 < size; i1++){//iterate for every x value
            for(int i2 = 0; i2 < size; i2++){//iterate for every y value

                occluded = false;

                xDist = i1 - lightX;//find distance
                yDist = i2 - lightY;//find distance
                hyp = Math.sqrt((xDist * xDist) + (yDist * yDist));//find length of hypotenuse

                for (Line i : lineList){
                    if (linesIntersect(lightX,lightY, i1, i2, i.getStartX(), i.getStartY(), i.getEndX(), i.getEndY())){occluded = true;}
                } //detect occlusion for each line

                if (!occluded){c = 255 - (int) (hyp * colorScale);}else{c=0;}//shade appropriately by distance
                if (c < 0){c = 0;}//ensure values are positive
                if (i1 % 50 == 0 || i2 % 50 == 0){c += 60;}c+=20;//lighten background and add lines
                if (c > 255){c = 255;}//ensure values not too large

                Color col = Color.rgb(c,c,c);

                graphicsContext2D.getPixelWriter().setColor(i1, i2, col);//render all pixels

            }
        }
        for (Line i : lineList){
            graphicsContext2D.strokeLine(i.getStartX(), i.getStartY(), i.getEndX(), i.getEndY());
        }
    }

    private static boolean between(double x1, double y1,//starting here, everything stolen from awt.Line2D
                                 double x2, double y2,
                                  double x3, double y3)
    {if (x1 != x2) {
        return (x1 <= x3 && x3 <= x2) || (x1 >= x3 && x3 >= x2);
    }
    else {
       return (y1 <= y3 && y3 <= y2) || (y1 >= y3 && y3 >= y2);
      }
    }
    private static double area2(double x1, double y1,
                                double x2, double y2,
                                double x3, double y3)
    {
      return (x2 - x1) * (y3 - y1) - (x3 - x1) * (y2 - y1);
    }
    public static boolean linesIntersect(double x1, double y1,
                                        double x2, double y2,
                                        double x3, double y3,
                                        double x4, double y4)
    {
      double a1, a2, a3, a4;

      // deal with special cases
      if ((a1 = area2(x1, y1, x2, y2, x3, y3)) == 0.0)
      {
        // check if p3 is between p1 and p2 OR
        // p4 is collinear also AND either between p1 and p2 OR at opposite ends
        if (between(x1, y1, x2, y2, x3, y3))
        {
           return true;
          }
          else
          {
            if (area2(x1, y1, x2, y2, x4, y4) == 0.0)
            {
              return between(x3, y3, x4, y4, x1, y1)
                     || between (x3, y3, x4, y4, x2, y2);
           }
           else {
              return false;
            }
          }
        }
        else if ((a2 = area2(x1, y1, x2, y2, x4, y4)) == 0.0)
        {
          // check if p4 is between p1 and p2 (we already know p3 is not
          // collinear)
          return between(x1, y1, x2, y2, x4, y4);
        }
        if ((a3 = area2(x3, y3, x4, y4, x1, y1)) == 0.0) {
          // check if p1 is between p3 and p4 OR
          // p2 is collinear also AND either between p1 and p2 OR at opposite ends
          if (between(x3, y3, x4, y4, x1, y1)) {
            return true;
         }
          else {
         if (area2(x3, y3, x4, y4, x2, y2) == 0.0) {
           return between(x1, y1, x2, y2, x3, y3)
                  || between (x1, y1, x2, y2, x4, y4);
         }
         else {
             return false;
         }
      }
   }
    else if ((a4 = area2(x3, y3, x4, y4, x2, y2)) == 0.0) {
       // check if p2 is between p3 and p4 (we already know p1 is not
                 // collinear)
                   return between(x3, y3, x4, y4, x2, y2);
              }
             else {  // test for regular intersection
               return ((a1 > 0.0) ^ (a2 > 0.0)) && ((a3 > 0.0) ^ (a4 > 0.0));
             }
           } //end thievery


    public static void main(String[] args) {
        launch(args);
    }

}