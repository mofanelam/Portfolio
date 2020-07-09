import lejos.nxt.*;
import lejos.robotics.navigation.DifferentialPilot;
import lejos.util.*;
import java.util.*;
import java.lang.Math;

public class GridNav {

  public static double rotateAxisToASensor = 2.8;
  public static int sensorAngleToAxis = 41;

  public static int steps = 0; // one step equals 0.1
  public static GridNode last_node = new GridNode();
  public static int direction = GridNode.direction_North; // N 1, E 2, S 4, W 8

  public static ArrayList<GridNode> node_list = new ArrayList<>();

  public static void main(String[] args) throws Exception {

    DifferentialPilot pilot;
    pilot = new DifferentialPilot(2.2f, 6.9f, Motor.A, Motor.C);
    LightSensor lightL = new LightSensor(SensorPort.S4);
    LightSensor lightR = new LightSensor(SensorPort.S1);

    UltrasonicSensor sonar = new UltrasonicSensor(SensorPort.S3);
    int sonicDist = sonar.getDistance();

    // Initialise light sensors
    int lightL_val = lightL.getLightValue();
    int lightR_val = lightR.getLightValue();
    pilot.setTravelSpeed(2);
    pilot.setRotateSpeed(30);
    Delay.msDelay(50); // wait for sensors stabilizing

    lightL_val = lightL.getLightValue();
    lightR_val = lightR.getLightValue();

    int light_stat = 0;
    int road_stat = 0; // |, <-, ->, -|, |-, T, -|-

    node_list.add(last_node);

    do {

      pilot.travel(0.2);
      steps += 2;
      lightL_val = lightL.getLightValue();
      lightR_val = lightR.getLightValue();

      sonicDist = sonar.getDistance();
      if (sonicDist <= 6) {
        pilot.travel(-steps / 10.0);
        steps = 0;
        last_node.node_type -= direction;
        exploreNode(pilot);
        continue;
      }

      light_stat = lightStats(lightL_val, lightR_val);

      if (light_stat == 1 || light_stat == 2) {
        light_stat = sideCheck(pilot, lightL, lightR, light_stat);
      }

      switch(light_stat) {
        case 0:
          road_stat = 0;
          break;
        case 1:
          road_stat = singleLightCheck(pilot, lightL, true, lightR);
          break;
        case 2:
          road_stat = singleLightCheck(pilot, lightR, false, lightL);
          break;
        case 3:
          if (detectCrossroad(pilot, lightL, lightR)) {
            road_stat = 6;
          } else {
            road_stat = 5;
          }
          break;
        default:
          road_stat = 0;
          break;
      }

      // Meet a node
      if (road_stat != 0) {
        pilot.travel(2.7);
        steps += 27;
        // add explored direction
        if ((~last_node.explored & direction) > 0) {
          last_node.explored += direction;
        }
        // |, <-, ->, -|, |-, T, -|-
        // 0,  1,  2,  3,  4, 5,  6
        switch (road_stat) {
          // Turn left
          case 1:
            last_node = checkNode(new GridNode(last_node, GridNode.opposite(direction), "CORNER",
                                                last_node.xPos + GridNode.dxPos(direction, steps), last_node.yPos + GridNode.dyPos(direction, steps),
                                                GridNode.left(direction)));
            if (last_node.from == GridNode.direction_South){
              last_node.type = "GOAL";
              Button.waitForAnyPress();
            }
            break;
          // Turn right
          case 2:
            last_node = checkNode(new GridNode(last_node, GridNode.opposite(direction), "CORNER",
                                                last_node.xPos + GridNode.dxPos(direction, steps), last_node.yPos + GridNode.dyPos(direction, steps),
                                                GridNode.right(direction)));
            if (last_node.from == GridNode.direction_West){
              last_node.type = "GOAL";
              Button.waitForAnyPress();
            }
            break;
          // -|
          case 3:
            last_node = checkNode(new GridNode(last_node, GridNode.opposite(direction), "JUNC",
                                                last_node.xPos + GridNode.dxPos(direction, steps), last_node.yPos + GridNode.dyPos(direction, steps),
                                                direction, GridNode.left(direction)));
            break;
          // |-
          case 4:
            last_node = checkNode(new GridNode(last_node, GridNode.opposite(direction), "JUNC",
                                                last_node.xPos + GridNode.dxPos(direction, steps), last_node.yPos + GridNode.dyPos(direction, steps),
                                                direction, GridNode.right(direction)));
            break;
          // T
          case 5:
            last_node = checkNode(new GridNode(last_node, GridNode.opposite(direction), "JUNC",
                                                last_node.xPos + GridNode.dxPos(direction, steps), last_node.yPos + GridNode.dyPos(direction, steps),
                                                GridNode.left(direction), GridNode.right(direction)));
            break;
          // -|-
          case 6:
            last_node = checkNode(new GridNode(last_node, GridNode.opposite(direction), "CROSS",
                                                last_node.xPos + GridNode.dxPos(direction, steps), last_node.yPos + GridNode.dyPos(direction, steps),
                                                direction, GridNode.left(direction), GridNode.right(direction)));
            break;
          default:
            break;
        }

        exploreNode(pilot);
      }

      LCD.clear();
      System.out.println("facing: " + GridNode.dirStr(direction));
      System.out.println("lastNode: x: " + last_node.xPos + "  y: " + last_node.yPos + "  " + last_node.type);
      System.out.println("steps: " + steps);

    } while (true);

    // LCD.clear();
    // LCD.drawInt(detectTJunctionLR(pilot, lightR, true)?1:0, 0, 0);
    // do{}while(true);

  }

  public static int lightStats(int lightL_val, int lightR_val) {
    if (lightL_val >= 50 && lightR_val >= 50) {
      return 0; // all clear
    }
    if (lightL_val < 50 && lightR_val >= 50) {
      return 1; // something on the left
    }
    if (lightL_val >= 50 && lightR_val < 50) {
      return 2; // something on the right
    }
    if (lightL_val < 50 && lightR_val < 50) {
      return 3; // both side
    }
    return -1;
  }

  // Check to the side to the single sensor
  public static int sideCheck(DifferentialPilot pilot, LightSensor lightL, LightSensor lightR, int prev_stat) {
    pilot.travel(0.2);

    if (prev_stat == 1) {
      pilot.rotate(5);
    } else if (prev_stat == 2) {
      pilot.rotate(-5);
    }

    int light_stat = lightStats(lightL.getLightValue(), lightR.getLightValue());

    if (light_stat == 0 || light_stat == 3) {
      pilot.travel(-0.2);
      return light_stat;
    }

    if (prev_stat == 1) {
      pilot.rotate(-4);
    } else if (prev_stat == 2) {
      pilot.rotate(4);
    }
    pilot.travel(-0.2);

    light_stat = checkAhead(pilot, lightL, lightR, light_stat);

    return light_stat;
  }

  // Scan ahead in case of T junction
  public static int checkAhead(DifferentialPilot pilot, LightSensor lightL, LightSensor lightR, int prev_stat) {
    double dist = 0.0;
    int light_stat = prev_stat;

    for (int i = 0; i < 4; i++) {
      pilot.travel(0.1);
      dist += 0.1;
      light_stat = lightStats(lightL.getLightValue(), lightR.getLightValue());

      if (prev_stat == 2) {
        if (light_stat == 1 || light_stat == 3) {
          pilot.rotate(-Math.toDegrees(Math.atan(dist / 1.9)));
          pilot.travel(-dist);
          return 3;
        }
      } else if (prev_stat == 1) {
        if (light_stat == 2 || light_stat == 3) {
          pilot.rotate(Math.toDegrees(Math.atan(dist / 1.9)));
          pilot.travel(-dist);
          return 3;
        }
      }
    }

    pilot.travel(-dist);
    return prev_stat;
  }

  public static int singleLightCheck(DifferentialPilot pilot, LightSensor light, boolean isLeftLight, LightSensor other_light) {
    int light_val = light.getLightValue();
    int i = isLeftLight ? 1 : -1;
    double distance = 0.0;

    do {
      pilot.travel(0.1);
      distance += 0.1;
      light_val = light.getLightValue();
    } while (light_val < 50);

    // width of the tape in inches (measured by robot) / cos(20deg)
    if (distance < 0.7) {
      // travel through the tip of a corner
      pilot.travel(-distance - 0.4);
      steps -= 4;
      pilot.rotate(i * 5);
    } else if (distance >= 0.9) {
      pilot.travel(-distance - 0.4);
      steps -= 4;
      pilot.rotate(i * Math.toDegrees(Math.asin(0.7 / distance)));
    } else {
      pilot.travel(-distance);
      if (detectTJunctionLR(pilot, other_light, isLeftLight)) {
        // -| or |-
        return isLeftLight ? 3 : 4;
      } else {
        // a corner
        return isLeftLight ? 1 : 2;
      }
    }

    return 0;
  }


  // A sensor scan for a designated angle for tapes
  public static boolean searchArea(DifferentialPilot pilot, LightSensor light, int degree) {

    double halfDeg = degree / 2.0;
    double rotated = 0.0;

    while (rotated <= halfDeg) {
      if (light.getLightValue() < 50) {
        pilot.rotate(-rotated);
        return true;
      }
      pilot.rotate(1);
      rotated += 1.0;
    }

    pilot.rotate(-rotated);
    rotated = 0.0;

    while (rotated <= halfDeg) {
      if (light.getLightValue() < 50) {
        pilot.rotate(rotated);
        return true;
      }
      pilot.rotate(-1);
      rotated += 1.0;
    }

    pilot.rotate(rotated);
    return false;
  }

  /* The following methods work when the robot is in good alignment with the line. */
  // If false then it should be a corner
  public static boolean detectTJunctionLR(DifferentialPilot pilot, LightSensor lightOutside, boolean isJunctionLeft) {
    int i = 1;
    if (!isJunctionLeft) {
      i *= -1;
    }

    //int sensorAngleToAxis = 41;

    pilot.travel(rotateAxisToASensor / 2);

    pilot.rotate(i * sensorAngleToAxis / 2.0);
    boolean rtn = searchArea(pilot, lightOutside, 10);
    pilot.rotate(-i * sensorAngleToAxis / 2.0);

    pilot.travel(-rotateAxisToASensor / 2);

    return rtn;
  }

  // If false then it should be a T junction
  public static boolean detectCrossroad(DifferentialPilot pilot, LightSensor lightL, LightSensor lightR) {
    // pilot.travel(2);

    pilot.travel(rotateAxisToASensor / 2);

    pilot.rotate(sensorAngleToAxis / 2.0);
    boolean detectedR = searchArea(pilot, lightR, 15);
    pilot.rotate(-sensorAngleToAxis);
    boolean detectedL = searchArea(pilot, lightL, 15);
    pilot.rotate(sensorAngleToAxis / 2.0);

    // LCD.drawInt(detectedL?1:0, 2, 2);
    // LCD.drawInt(detectedR?1:0, 4, 4);

    pilot.travel(-rotateAxisToASensor / 2);

    return(detectedL && detectedR);
  }

  public static GridNode checkNode(GridNode new_node) {
    for (GridNode node : node_list) {
        if (node.near(new_node)) {
          //Set the node coordinates into the average of the two
          node.xPos = (node.xPos + new_node.xPos) / 2;
          node.yPos = (node.yPos + new_node.yPos) / 2;
          // Set node.explored such that the node
          if ((~node.explored & GridNode.opposite(direction)) > 0) {
            node.explored += GridNode.opposite(direction);
          }
          steps = 0;
          return node;
        }
    }

    node_list.add(new_node);
    steps = 0;
    return new_node;
  }

  // Turn towards first unexplored direction or parent
  public static void exploreNode(DifferentialPilot pilot) {
    int new_dir = last_node.firstUnexplored();

    // if no unexplored
    while (new_dir == 0) {
      if (last_node.parent == null) {
        Button.waitForAnyPress(); // Would have backed to the start already
      } else {
        pilot.rotate(GridNode.turnFromTo(direction, last_node.from));
        direction = last_node.from;
        int dist = last_node.distToParent();
        pilot.travel(dist / 10.0);
        // steps = 0;
        last_node = last_node.parent;
        new_dir = last_node.firstUnexplored();
      }
    }

    pilot.rotate(GridNode.turnFromTo(direction, new_dir));
    direction = new_dir;
  }

}
