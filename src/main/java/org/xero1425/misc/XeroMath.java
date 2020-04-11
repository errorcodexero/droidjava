package org.xero1425.misc ;

import java.lang.Math ;

public class XeroMath
{
    public static double normalizeAngleDegrees(double a) {
        while (a <= -180.0)
            a += 360.0 ;

        while (a > 180.0)
            a -= 360.0 ;
        
        return a ;          
    }

    public static double normalizeAngleRadians(double a) {
        if (a <= -Math.PI)
            a += 2 * Math.PI   ;
        else if (a >Math.PI)
            a -= 2 *Math.PI   ;
        return a ;
    }

    public static double rad2deg(double r) {
        return r / Math.PI   * 180 ;
    }

    public static double deg2rad(double d) {
        return d / 180.0 * Math.PI   ;
    }

    public static boolean equalWithinPercentMargin(double target, double measured, double percentage) {
        return (Math.abs(100.0 * (measured - target)/target) <= percentage);
    }

    public static Double[] quadratic(double a, double b, double c) {
        Double[] result ;
        double tmp = b * b - 4 * a * c ;

        if (tmp == 0.0) {
            result = new Double[1] ;
            result[0] = -b/(2 * a) ;
        }
        else if (tmp > 0.0) {
            result = new Double[2] ;
            result[0] = (-b + Math.sqrt(tmp)) / (2 * a) ;
            result[1] = (-b - Math.sqrt(tmp)) / (2 * a) ;

            if (result[0] < result[1]) {
                //
                // Swap the result, the biggest should always be first
                //
                tmp = result[0] ;
                result[0] = result[1] ;
                result[1] = tmp ;
            }
        }
        else {
            result = new Double[0] ;
        }
        return result ;        
    }
}
