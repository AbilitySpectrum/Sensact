//
//  hospital_robot.cpp
//  stuff
//
//  Created by Colin Miyata on 2016-12-11.
//  Copyright © 2016 Colin Miyata. All rights reserved.
//

#include <hospital_robot.h>
#include <math.h>
#include "Arduino.h"
#include <avr/pgmspace.h>

// ***** Constants used in calculations *****

// radians to degrees
const float r2d=180.0/PI;

// Sine and cosine look up tables

const PROGMEM float ssin[361]={0,-0.017452,-0.034899,-0.052336,-0.069756,-0.087156,-0.10453,-0.12187,-0.13917,-0.15643,-0.17365,-0.19081,-0.20791,-0.22495,-0.24192,-0.25882,-0.27564,-0.29237,-0.30902,-0.32557,-0.34202,-0.35837,-0.37461,-0.39073,-0.40674,-0.42262,-0.43837,-0.45399,-0.46947,-0.48481,-0.5,-0.51504,-0.52992,-0.54464,-0.55919,-0.57358,-0.58779,-0.60182,-0.61566,-0.62932,-0.64279,-0.65606,-0.66913,-0.682,-0.69466,-0.70711,-0.71934,-0.73135,-0.74314,-0.75471,-0.76604,-0.77715,-0.78801,-0.79864,-0.80902,-0.81915,-0.82904,-0.83867,-0.84805,-0.85717,-0.86603,-0.87462,-0.88295,-0.89101,-0.89879,-0.90631,-0.91355,-0.9205,-0.92718,-0.93358,-0.93969,-0.94552,-0.95106,-0.9563,-0.96126,-0.96593,-0.9703,-0.97437,-0.97815,-0.98163,-0.98481,-0.98769,-0.99027,-0.99255,-0.99452,-0.99619,-0.99756,-0.99863,-0.99939,-0.99985,-1,-0.99985,-0.99939,-0.99863,-0.99756,-0.99619,-0.99452,-0.99255,-0.99027,-0.98769,-0.98481,-0.98163,-0.97815,-0.97437,-0.9703,-0.96593,-0.96126,-0.9563,-0.95106,-0.94552,-0.93969,-0.93358,-0.92718,-0.9205,-0.91355,-0.90631,-0.89879,-0.89101,-0.88295,-0.87462,-0.86603,-0.85717,-0.84805,-0.83867,-0.82904,-0.81915,-0.80902,-0.79864,-0.78801,-0.77715,-0.76604,-0.75471,-0.74314,-0.73135,-0.71934,-0.70711,-0.69466,-0.682,-0.66913,-0.65606,-0.64279,-0.62932,-0.61566,-0.60182,-0.58779,-0.57358,-0.55919,-0.54464,-0.52992,-0.51504,-0.5,-0.48481,-0.46947,-0.45399,-0.43837,-0.42262,-0.40674,-0.39073,-0.37461,-0.35837,-0.34202,-0.32557,-0.30902,-0.29237,-0.27564,-0.25882,-0.24192,-0.22495,-0.20791,-0.19081,-0.17365,-0.15643,-0.13917,-0.12187,-0.10453,-0.087156,-0.069756,-0.052336,-0.034899,-0.017452,0,0.017452,0.034899,0.052336,0.069756,0.087156,0.10453,0.12187,0.13917,0.15643,0.17365,0.19081,0.20791,0.22495,0.24192,0.25882,0.27564,0.29237,0.30902,0.32557,0.34202,0.35837,0.37461,0.39073,0.40674,0.42262,0.43837,0.45399,0.46947,0.48481,0.5,0.51504,0.52992,0.54464,0.55919,0.57358,0.58779,0.60182,0.61566,0.62932,0.64279,0.65606,0.66913,0.682,0.69466,0.70711,0.71934,0.73135,0.74314,0.75471,0.76604,0.77715,0.78801,0.79864,0.80902,0.81915,0.82904,0.83867,0.84805,0.85717,0.86603,0.87462,0.88295,0.89101,0.89879,0.90631,0.91355,0.9205,0.92718,0.93358,0.93969,0.94552,0.95106,0.9563,0.96126,0.96593,0.9703,0.97437,0.97815,0.98163,0.98481,0.98769,0.99027,0.99255,0.99452,0.99619,0.99756,0.99863,0.99939,0.99985,1,0.99985,0.99939,0.99863,0.99756,0.99619,0.99452,0.99255,0.99027,0.98769,0.98481,0.98163,0.97815,0.97437,0.9703,0.96593,0.96126,0.9563,0.95106,0.94552,0.93969,0.93358,0.92718,0.9205,0.91355,0.90631,0.89879,0.89101,0.88295,0.87462,0.86603,0.85717,0.84805,0.83867,0.82904,0.81915,0.80902,0.79864,0.78801,0.77715,0.76604,0.75471,0.74314,0.73135,0.71934,0.70711,0.69466,0.682,0.66913,0.65606,0.64279,0.62932,0.61566,0.60182,0.58779,0.57358,0.55919,0.54464,0.52992,0.51504,0.5,0.48481,0.46947,0.45399,0.43837,0.42262,0.40674,0.39073,0.37461,0.35837,0.34202,0.32557,0.30902,0.29237,0.27564,0.25882,0.24192,0.22495,0.20791,0.19081,0.17365,0.15643,0.13917,0.12187,0.10453,0.087156,0.069756,0.052336,0.034899,0.017452,0};

const PROGMEM float ccos[361]={-1,-0.99985,-0.99939,-0.99863,-0.99756,-0.99619,-0.99452,-0.99255,-0.99027,-0.98769,-0.98481,-0.98163,-0.97815,-0.97437,-0.9703,-0.96593,-0.96126,-0.9563,-0.95106,-0.94552,-0.93969,-0.93358,-0.92718,-0.9205,-0.91355,-0.90631,-0.89879,-0.89101,-0.88295,-0.87462,-0.86603,-0.85717,-0.84805,-0.83867,-0.82904,-0.81915,-0.80902,-0.79864,-0.78801,-0.77715,-0.76604,-0.75471,-0.74314,-0.73135,-0.71934,-0.70711,-0.69466,-0.682,-0.66913,-0.65606,-0.64279,-0.62932,-0.61566,-0.60182,-0.58779,-0.57358,-0.55919,-0.54464,-0.52992,-0.51504,-0.5,-0.48481,-0.46947,-0.45399,-0.43837,-0.42262,-0.40674,-0.39073,-0.37461,-0.35837,-0.34202,-0.32557,-0.30902,-0.29237,-0.27564,-0.25882,-0.24192,-0.22495,-0.20791,-0.19081,-0.17365,-0.15643,-0.13917,-0.12187,-0.10453,-0.087156,-0.069756,-0.052336,-0.034899,-0.017452,0,0.017452,0.034899,0.052336,0.069756,0.087156,0.10453,0.12187,0.13917,0.15643,0.17365,0.19081,0.20791,0.22495,0.24192,0.25882,0.27564,0.29237,0.30902,0.32557,0.34202,0.35837,0.37461,0.39073,0.40674,0.42262,0.43837,0.45399,0.46947,0.48481,0.5,0.51504,0.52992,0.54464,0.55919,0.57358,0.58779,0.60182,0.61566,0.62932,0.64279,0.65606,0.66913,0.682,0.69466,0.70711,0.71934,0.73135,0.74314,0.75471,0.76604,0.77715,0.78801,0.79864,0.80902,0.81915,0.82904,0.83867,0.84805,0.85717,0.86603,0.87462,0.88295,0.89101,0.89879,0.90631,0.91355,0.9205,0.92718,0.93358,0.93969,0.94552,0.95106,0.9563,0.96126,0.96593,0.9703,0.97437,0.97815,0.98163,0.98481,0.98769,0.99027,0.99255,0.99452,0.99619,0.99756,0.99863,0.99939,0.99985,1,0.99985,0.99939,0.99863,0.99756,0.99619,0.99452,0.99255,0.99027,0.98769,0.98481,0.98163,0.97815,0.97437,0.9703,0.96593,0.96126,0.9563,0.95106,0.94552,0.93969,0.93358,0.92718,0.9205,0.91355,0.90631,0.89879,0.89101,0.88295,0.87462,0.86603,0.85717,0.84805,0.83867,0.82904,0.81915,0.80902,0.79864,0.78801,0.77715,0.76604,0.75471,0.74314,0.73135,0.71934,0.70711,0.69466,0.682,0.66913,0.65606,0.64279,0.62932,0.61566,0.60182,0.58779,0.57358,0.55919,0.54464,0.52992,0.51504,0.5,0.48481,0.46947,0.45399,0.43837,0.42262,0.40674,0.39073,0.37461,0.35837,0.34202,0.32557,0.30902,0.29237,0.27564,0.25882,0.24192,0.22495,0.20791,0.19081,0.17365,0.15643,0.13917,0.12187,0.10453,0.087156,0.069756,0.052336,0.034899,0.017452,6.1232e-17,-0.017452,-0.034899,-0.052336,-0.069756,-0.087156,-0.10453,-0.12187,-0.13917,-0.15643,-0.17365,-0.19081,-0.20791,-0.22495,-0.24192,-0.25882,-0.27564,-0.29237,-0.30902,-0.32557,-0.34202,-0.35837,-0.37461,-0.39073,-0.40674,-0.42262,-0.43837,-0.45399,-0.46947,-0.48481,-0.5,-0.51504,-0.52992,-0.54464,-0.55919,-0.57358,-0.58779,-0.60182,-0.61566,-0.62932,-0.64279,-0.65606,-0.66913,-0.682,-0.69466,-0.70711,-0.71934,-0.73135,-0.74314,-0.75471,-0.76604,-0.77715,-0.78801,-0.79864,-0.80902,-0.81915,-0.82904,-0.83867,-0.84805,-0.85717,-0.86603,-0.87462,-0.88295,-0.89101,-0.89879,-0.90631,-0.91355,-0.9205,-0.92718,-0.93358,-0.93969,-0.94552,-0.95106,-0.9563,-0.96126,-0.96593,-0.9703,-0.97437,-0.97815,-0.98163,-0.98481,-0.98769,-0.99027,-0.99255,-0.99452,-0.99619,-0.99756,-0.99863,-0.99939,-0.99985,-1};

// ***** Fast sine and cosine functions *****

float fsin(int angle) {
    return pgm_read_float_near(&ssin[angle+180]);
}

float fcos(int angle) {
    return pgm_read_float_near(&ccos[angle+180]);
}

// ***** Constructor *****

hospital_robot::hospital_robot(float length_arm1, float length_arm2, float shoulder_offset, float height_to_second_motor, float x_init, float y_init, float z_init, float dt, float v, float vclaw){
    
    // Assign robot values
    
    _length_arm_1=length_arm1;
    _length_arm_2=length_arm2;
    _shoulder_offset=shoulder_offset;
    _height_to_second_motor=height_to_second_motor;
    
    //Assign motion values
    
    _dt=dt;
    _v=v;
    _v_claw=vclaw;
    
    // Assign initial position
    
    _x=x_init;
    _y=y_init;
    _z=z_init;
    
    // Determine initial thetas
    
    _theta1=0;
    _theta2=0;
    _theta3=0;
    _theta4=0;
    _theta5=0;
    _theta6=0;
    
    inverse_kinematics();
    
    _theta4=0;
    _theta5=0;
    _theta6=0;
    
    
}

// ***** Robot motion Functions *****

// Inverse kinematics

void hospital_robot::inverse_kinematics(){
    
    // Save old theta values
    float theta1old=_theta1;
    float theta2old=_theta2;
    float theta3old=_theta3;
    float theta4old=_theta4;
    
    // Calculate theta1
    _theta1=atan2(_y,_x)*r2d;
    
    // Determine total x and y considering the shoulder offset
    float x_c=_x-_shoulder_offset*fcos(int(_theta1));
    float y_c=_y-_shoulder_offset*fsin(int(_theta1));
    
    // Calculate variable D which helps in calculations and in determining if position
    // can be reached
    float D=(x_c*x_c+y_c*y_c+(_z-_height_to_second_motor)*(_z-_height_to_second_motor)-_length_arm_1*_length_arm_1-_length_arm_2*_length_arm_2)/(2.0*_length_arm_1*_length_arm_2);
        
    //Calculate theta3
    _theta3=atan2(-sqrt(1-D*D),D)*r2d;
        
    //Calculate theta2
    _theta2=atan2(_z-_height_to_second_motor,sqrt(x_c*x_c+y_c*y_c))-atan2(_length_arm_2*fsin(int(_theta3)),_length_arm_1+_length_arm_2*fcos(int(_theta3)));
    _theta2=_theta2*r2d;
        
    //Calculate theta4 to hold position
    _theta4=theta4old-(_theta2-theta2old)-(_theta3-theta3old);
    
    /* Check if position can't be reached (D>1) or if thetas 1-3 exceed their max values, and if so then
     hold position */
    if (D>1||abs(_theta1)>90||_theta2>95||_theta2<0||_theta3>0||_theta3<-120) {
        
        // If position cannot be reached, hold position
        _theta1=theta1old;
        _theta2=theta2old;
        _theta3=theta3old;
        _theta4=theta4old;
        
    }
    
    /* Check if theta 4 command exceeds limit and if so, truncate to limit */
    if (abs(_theta4)>90) {
        if (_theta4<0) {
            _theta4=-90;
        } else {
            _theta4=90;
        }
    }
    
}

void hospital_robot::move_right(){

    _y=_y-_v*_dt/1000.0;
    _change_thetas=true;

}

void hospital_robot::move_left(){

    _y=_y+_v*_dt/1000.0;
    _change_thetas=true;

}

void hospital_robot::move_up(){

    _z=_z+_v*_dt/1000.0;
    _change_thetas=true;

}

void hospital_robot::move_down(){

    _z=_z-_v*_dt/1000.0;
    _change_thetas=true;

}

void hospital_robot::move_forward(){

    _x=_x+_v*_dt/1000.0;
    _change_thetas=true;

}

void hospital_robot::move_backward(){

    _x=_x-_v*_dt/1000.0;
    _change_thetas=true;

}

void hospital_robot::close_claw(){

    _theta6=_theta6+_v_claw*_dt/1000.0;
    
    /* Check if theta 5 command exceeds limit and if so, truncate to limit */
    if (abs(_theta6)>90) {
        if (_theta6<0) {
            _theta6=-90;
        } else {
            _theta6=90;
        }
    }

}

void hospital_robot::open_claw(){

    _theta6=_theta6-_v_claw*_dt/1000.0;

    /* Check if theta 5 command exceeds limit and if so, truncate to limit */
    if (abs(_theta6)>90) {
        if (_theta6<0) {
            _theta6=-90;
        } else {
            _theta6=90;
        }
    }

}

void hospital_robot::tilt_claw_up(){

    _theta4=_theta4+_v_claw*_dt/1000.0;

    /* Check if theta 4 command exceeds limit and if so, truncate to limit */
    if (abs(_theta4)>90) {
        if (_theta4<0) {
            _theta4=-90;
        } else {
            _theta4=90;
        }
    }

}

void hospital_robot::tilt_claw_down(){

    _theta4=_theta4-_v_claw*_dt/1000.0;

    /* Check if theta 4 command exceeds limit and if so, truncate to limit */
    if (abs(_theta4)>90) {
        if (_theta4<0) {
            _theta4=-90;
        } else {
            _theta4=90;
        }
    }

}

void hospital_robot::rotate_claw_pos(){
    
    _theta5=_theta5+_v_claw*_dt/1000.0;
    
    /* Check if theta 4 command exceeds limit and if so, truncate to limit */
    if (abs(_theta5)>90) {
        if (_theta5<0) {
            _theta5=-90;
        } else {
            _theta5=90;
        }
    }
    
}

void hospital_robot::rotate_claw_neg(){
    
    _theta5=_theta5-_v_claw*_dt/1000.0;
    
    /* Check if theta 4 command exceeds limit and if so, truncate to limit */
    if (abs(_theta5)>90) {
        if (_theta5<0) {
            _theta5=-90;
        } else {
            _theta5=90;
        }
    }
    
}


// ***** Robot Communication Functions *****

// Make command for servo motor

String hospital_robot::make_command(){
    
    if (_change_thetas) {    
         inverse_kinematics();
         _change_thetas=false;
    }

    // Convert to pulses for servos. The bounds on the map function take into account motor offsets and direction
    int pos1= (int) map(_theta1,-90,90,500,2500);
    int pos2= (int) map(_theta2,0,90,170,100);
    pos2= map(pos2,0,180,500,2500);
    int pos3= (int) map(_theta3,-90,0,100,23);
    pos3= map(pos3,0,180,500,2500);
    int pos4= (int) map(_theta4,90,-90,500,2500);
    int pos5= (int) map(_theta5,-90,90,500,2500);
    int pos6= (int) map(_theta6,-90,90,500,2500);
    
    String com="# 1P"+String(pos1,DEC);
    com=com+" # 2P"+String(pos2,DEC);
    com=com+" # 3P"+String(pos3,DEC);
    com=com+" # 4P"+String(pos4,DEC);
    com=com+" # 5P"+String(pos5,DEC);
    com=com+" # 6P"+String(pos6,DEC);
    com=com+"T"+String(int(_dt),DEC)+"D500";
    
    return com;
    
}

// Print position of the robot for debugging

String hospital_robot::print_position(){

    String com="x "+String(_x,DEC);
    com=com+" y "+String(_y,DEC);
    com=com+" z "+String(_z,DEC);

    return com;

}

// Print thetas for the sake of debugging

String hospital_robot::print_thetas(){

    String com="theta1 "+String(_theta1,DEC);
    com=com+" theta2 "+String(_theta2,DEC);
    com=com+" theta3 "+String(_theta3,DEC);
    com=com+" theta4 "+String(_theta4,DEC);
    com=com+" theta5 "+String(_theta5,DEC);
    com=com+" theta6 "+String(_theta6,DEC);

    return com;

}

// Get the servo angles to output

void hospital_robot::get_servo_angles(int& theta1, int& theta2, int& theta3, int& theta4, int& theta5, int& theta6){
    
    if (_change_thetas) {
        inverse_kinematics();
        _change_thetas=false;
    }
    
    theta1= int(map(_theta1,-90,90,0,180));
    theta2= int(map(_theta2,0,90,170,100));
    theta3= int(map(_theta3,-90,0,100,23));
    theta4= int(map(_theta4,90,-90,0,180));
    theta5= int(map(_theta5,-90,90,0,180));
    theta6= int(map(_theta6,-90,90,0,180));
}
