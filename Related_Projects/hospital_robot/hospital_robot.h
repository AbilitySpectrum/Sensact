//
//  hospital_robot.h
//
//  Created by Colin Miyata on 2016-12-11.
//  Copyright © 2016 Colin Miyata. All rights reserved.
//

#include "Arduino.h"

class hospital_robot
{
public:
    
    /* — Declare an instance of the robot — */

    hospital_robot(float length_arm1, float length_arm2, float shoulder_offset, float height_to_second_motor, float x_init, float y_init, float z_init, float dt, float v, float vclaw);
    
    /* — Inverse kinematics that will move the claw to the given position by handing back the required theta angles for
     the joints by reference — */
    
    void inverse_kinematics();


   /* — Functions to print through serial — */
    
    String make_command(); // Make command for the robot

    String print_position(); // Print position of the robot for debugging
    String print_thetas(); // Print thetas for the sake of debugging

    /* — Functions to move the robot - */

    void move_right();
    void move_left();
    void move_up();
    void move_down();
    void move_forward();
    void move_backward();
    void close_claw();
    void open_claw();
    void tilt_claw_up();
    void tilt_claw_down();
    void rotate_claw_pos();
    void rotate_claw_neg();
    void get_servo_angles(int& theta1, int& theta2, int& theta3, int& theta4, int& theta5, int& theta6);
    
    
private:
    
    // Define Arm Lengths
    float _length_arm_1;
    float _length_arm_2;
    float _shoulder_offset;
    float _height_to_second_motor;
    float _x;
    float _y;
    float _z;
    float _theta1;
    float _theta2;
    float _theta3;
    float _theta4;
    float _theta5;
    float _theta6;
    float _dt;
    float _v;
    float _v_claw;
    bool _change_thetas=false;
    
};

// ***** Fast sine and cosine *****

// Functions
float fcos(int angle);
float fsin(int angle);
