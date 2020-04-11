/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018 FIRST. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package org.frc2020.droid;

import org.xero1425.misc.SimArgs;

import edu.wpi.first.wpilibj.RobotBase;

/**
 * Do NOT add any static variables to this class, or any initialization at all.
 * Unless you know what you are doing, do not modify this file except to
 * change the parameter class to the startRobot call.
 */
public final class Main {
    private Main() {
    }

/**
 * Main initialization function. Do not perform any initialization here.
 *
 * <p>If you change your main robot class, change the parameter type.
 */
    public static void main(final String... args) {

        int i = 0 ;
        while (i < args.length) {
            if (args[i].equals("--input")) {
                i++ ;
                if (i == args.length) {
                    System.err.println("command line argument --logfile requires an additional argument") ;
                    System.exit(2) ;                    
                }
                SimArgs.InputFileName = args[i];
            }
            else if (args[i].equals("--logfile")) {
                i++ ;
                if (i == args.length) {
                    System.err.println("command line argument --logfile requires an additional argument") ;
                    System.exit(2) ;                    
                }
                SimArgs.LogFileName = args[i] ;
            }
            else {
                System.err.println("unknown command line argument '" + args[i] + "'") ;
                System.exit(2) ;
            }

            i++ ;
        }

        RobotBase.startRobot(Droid::new);
    }
}
