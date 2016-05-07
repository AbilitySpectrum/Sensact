
// NOT USED - click

/* ClickButton library demo
 
 Blinks a LED according to different clicks on one button.
 
 Short clicks:
 
 Single click - Toggle LED on/off
 Double click - Blink      (Toggles LED 2 times/second)
 Triple click - Fast blink (Toggles LED 5 times/second)
 
 Long clicks (hold button for one second or longer on last click):
 
 Single-click - Slow blink   (Toggles LED every second)
 Double-click - Sloow blink  (Toggles LED every other second)
 Triple-click - Slooow blink (Toggles LED every three seconds)
 
 
 The circuit:
 - LED attached from pin 10 to resistor (say 220-ish ohms), other side of resistor to GND (ground)
 - pushbutton attached from pin 4 to GND
 No pullup resistor needed, using the Arduino's (Atmega's) internal pullup resistor in this example.
 
 Based on the Arduino Debounce example.
 
 2010, 2013 raron
 
 GNU GPLv3 license
 
 #include <AACsensors.h>
 #include <AACswitch.h>
 
#include "ClickButton.h"

// the LED
const int MCledPin = SENSACT_BLUE;
int MCledState = 0;
//Switch relay( 2, 200, 0);
//Switch relay1( 3, 200, 0);
//Switch buzzer( 4, 40, 1 ); // USED buzzer = 4 or A2 +++++

// the Button
const int buttonPin1 = SENSACT_IN3;
ClickButton button1(buttonPin1, LOW, CLICKBTN_PULLUP);

// Arbitrary LED function 
int LEDfunction = 0;


void mc_setup()
{
   pinMode(MCledPin,OUTPUT);  

   // Setup button timers (all in milliseconds / ms)
   // (These are default if not set, but changeable for convenience)
   button1.debounceTime   = 20;   // Debounce timer in ms
   button1.multiclickTime = 100;  // Time limit for multi clicks
   button1.longClickTime  = 1000; // time until "held-down clicks" register

   //Bleutooth HID
   //    Serial1.begin(115200);
}


void mc_loop()
{
   // Update button state
   button1.Update();
   //  relay.Check();
   //  relay1.Check();
   //  buzzer.Check();

   //  // Save click codes in LEDfunction, as click codes are reset at next Update()
   if (button1.clicks != 0) {
      LEDfunction = button1.clicks;
      Serial.println(LEDfunction);
   }

   switch (button1.clicks) {
   case 0:

      break;
   case 1:// short click
      Keyboard.press(KEY_RIGHT_ARROW); 
      Keyboard.releaseAll();

      // Bluetooth use with ipad switch access
      //    Serial1.write(0x07); //right arrow = 0×07;
      //     relay.On(1000);
      //     buzzer.On(40);

      break;
   case 2:  
      Keyboard.press(KEY_LEFT_ARROW); 
      Keyboard.releaseAll(); 
      // Bluetooth use with ipad switch access
      //   Serial1.write(0x0B); //left arrow = 0×0B;   

      break;
   case 3:  // iPad: HOME key
      Keyboard.press(KEY_RIGHT_CTRL);
      Keyboard.press(KEY_RIGHT_ALT);
      Keyboard.press('H');
      Keyboard.releaseAll();    
      // Bluetooth 
      //      Serial1.write(0x0D); //enter key = 0×0D;
      break;
   case -1:  // iPad: one long click ,hold

      Keyboard.press(KEY_UP_ARROW); 
      Keyboard.press(KEY_DOWN_ARROW); 
      Keyboard.releaseAll();

      Keyboard.press(KEY_UP_ARROW); 
      Keyboard.press(KEY_DOWN_ARROW); 
      Keyboard.releaseAll();
      //use with ipad switch access
      //    Serial1.write(0x0E); //up arrow 
      //    relay1.On(1000);
      //    buzzer.On(40);

      break;
   case -2:
      // 1 short click then 1 long click = scroll down
      Keyboard.press(KEY_RIGHT_CTRL);
      Keyboard.press(KEY_RIGHT_ARROW);
      Keyboard.releaseAll();  
      break;


   case -3:
      // 2 short clicsk then 1 long click = scroll up
      Keyboard.press(KEY_RIGHT_CTRL);
      Keyboard.press(KEY_RIGHT_ALT);
      Keyboard.press('S');
      break;
   default:

      break;


   }

   // Simply toggle LED on single clicks
   // (Cant use LEDfunction like the others here,
   //  as it would toggle on and off all the time)
   if(button1.clicks == 1) {

      MCledState = !MCledState; 
   }

   // blink faster if double clicked
   if(LEDfunction == 2)  {

      MCledState = (millis()/500)%2;
   }

   // blink even faster if triple clicked
   if(LEDfunction == 3)  {
      MCledState = (millis()/200)%2;
   }

   // slow blink (must hold down button. 1 second long blinks)
   if(LEDfunction == -1)  {  // tap twice

      MCledState = (millis()/1000)%2;
   }

   // slower blink (must hold down button. 2 second loong blinks)
   if(LEDfunction == -2)  {  // HOME button


      MCledState = (millis()/2000)%2;
   }

   // even slower blink (must hold down button. 3 second looong blinks)
   if(LEDfunction == -3)  {
      MCledState = (millis()/3000)%2;
   }


   // update the LED
   digitalWrite(MCledPin,MCledState);
}

*/


/* derived from ASshort_longDigital v5d - 20150515
 short click ON DOWN sends iPad Select
 v5 closes pinControl port as well
 long click ON UP sends relay signal
 
 Added timer so that beyond longclickTime then
 sends relay signal
 
 const int testing = 1; // set this to 0 for final version
 
 const int nLevels = 3; // number of levels
 const int triggerA = 0; // reading at this value or less will trigger; must be less than nLevels
 const long ignoreTime = 400;  //400
 const long longclickTime = 1200;  //1200
 const long discardTime = 2500;  //2500
 
 long lastTriggeredA = 0;
 int lastReadA=1;
 long now;
 int count=0;
 
 
 void multiclick_setup() {    
 pinMode(SENSACT_IN3, INPUT);
 }
 void multiclick_loop() {
 int xA;
 //
 //   delay(10);
 //   relay.Check();
 //   buzzer.Check();
 //   control.Check();
 
 count++;
 xA = digitalRead(SENSACT_IN3);
 //   if( CapTouch ) { 
 //      xA = 1- digitalRead(pinIn); 
 //   } // invert for capacitive touch sensor
 //
 
 //xA=sD.Read();  // digital read using library routine 
 //************** DISABLE ANALOG  xA=sA.Level(nLevels);  //analog read
 
 if( testing>0 && xA!=lastReadA) {
 //Serial.print(count);
 //      Serial.print("a=");
 //      Serial.print(xA);
 //      Serial.print(" ");
 //      Serial.flush();
 }
 
 
 if( xA!=lastReadA ) {
 // state changed
 if( xA==triggerA ) {  // DOWN TICK
 now = millis();  
 if( now < (lastTriggeredA + ignoreTime) ) {  //ignore
 return;
 }
 else  {   // CHANGE HERE: trigger SHORT -------
 if( testing == 0 ) {
 // iPad click
 Keyboard.press(KEY_UP_ARROW); 
 Keyboard.press(KEY_DOWN_ARROW); 
 Keyboard.releaseAll();
 
 //               control.On(100);
 
 // app click 
 //               Keyboard.press('1');
 //               Keyboard.releaseAll();
 
 
 //               buzzer.On(10);
 }  
 else {
 Serial.print(count);
 Serial.print(" *A*\n");  // Triggered
 //               relay.On(50);
 //               buzzer.On(10);
 }       
 
 //            serialBuzzer.Go(1,1);
 lastTriggeredA = millis();
 }
 }
 else { // UP TICK
 tryTriggerLongA(1);
 }
 } 
 else {     //xA==last level
 if( xA==triggerA ) {
 tryTriggerLongA(2);
 }
 }
 lastReadA = xA; 
 }
 
 void tryTriggerLongA(int byWhat) {
 now = millis();
 if( ( now > (lastTriggeredA + longclickTime) ) &&
 ( now < (lastTriggeredA + discardTime) ) )
 {
 if( testing == 0 )  // CHANGE HERE: what to do on LONG click
 {
 //         relay.On(500);
 //         buzzer.On(500);
 //         serialBuzzer.Go (1,byWhat);
 } 
 else {
 Serial.print(count);
 if( byWhat==1 ) {
 Serial.print("*A--------A*\n");  // Triggered
 } 
 else {
 Serial.print("*A       A*\n");  // Triggered
 }
 //         if( testing == 1) {  //non silent testing
 //            relay.On(300);
 //            buzzer.On(300);
 //         }
 }
 }
 }
 
 */
