/*
   sensact_2.0_basic_sanity
   
   Bruce Braidek, Mar 31 2016
   for Bruyere Hospital, Ottawa, Canada
   
   This is a short sketch that the user can load onto
   a Leonardo and test if the basic items ar working.  
   
   This sketch is based on melody by Tom Igoe
    http://www.arduino.cc/en/Tutorial/Tone
    
    I simply kept adding to it until all my functionality 
    was being tested.  

  - Plays a melody on the speaker
  - Flashes the RGB LEDs
  - Toggles the relays
  - Passes text using Bluetooth (dongle must be configured for 9600 baud)
  
  For later...
  - Sits and waits for a IR input and flashes it back 
  while playing the melody, flashing IR and RGB, and 
  toggling relays
  
  */
#include "pitches.h"

#define BUZZER_PIN     10

#define BLUE_LED_PIN   4
#define GREEN_LED_PIN  5
#define RED_LED_PIN    A3

#define OP1_RELAY      9
#define OP2_RELAY      11

#define IR_RECV_PIN    12
#define IR_OP_PIN      13

    int i = 0;


void setup() {
 
 /* *****Setting up for tests***** */ 
    //Initialize LED pins
    pinMode(RED_LED_PIN, OUTPUT);
    pinMode(GREEN_LED_PIN, OUTPUT);
    pinMode(BLUE_LED_PIN, OUTPUT);
    
    digitalWrite(RED_LED_PIN, LOW);
    digitalWrite(GREEN_LED_PIN, LOW);
    digitalWrite(BLUE_LED_PIN, LOW);
    
    //Initialize Relay outputs
    pinMode(OP1_RELAY, OUTPUT);
    pinMode(OP2_RELAY, OUTPUT);
    
    digitalWrite(OP1_RELAY, LOW);
    digitalWrite(OP2_RELAY, LOW);
    
    //Initializing the IR Pins
    pinMode(IR_RECV_PIN, INPUT); 
    pinMode(IR_OP_PIN, OUTPUT);

    digitalWrite(IR_OP_PIN, LOW);
   
   
   TestBuzzer();  
   
   BeeperChirpUp();
   TestRGBLed();
   BeeperChirpDown();
   
   BeeperChirpUp();
   TestRelays();
   BeeperChirpDown();
   
   BeeperChirpUp();
   TestBluetooth();
   BeeperChirpDown();
   
   //TestIRRecv();
   
   //BeeperChirpUp();
   //TestCurrentDraw();
   //BeeperChirpDown();
   BeeperChirpUp();
   TestSwitchInput();
   BeeperChirpDown();

   BeeperChirp();
   BeeperChirp();
         
}

void loop() {
   // no need to repeat the melody.
}

  void TestBuzzer(void)
  {
      // notes in the melody:
      int melody[] = {
         /*NOTE_C4, NOTE_G3, NOTE_G3, NOTE_A3, NOTE_G3, 0, NOTE_B3, NOTE_C4*/
         NOTE_C7, NOTE_G6, NOTE_G6, NOTE_A6, NOTE_G6, 0, NOTE_B6, NOTE_C7
      };
      
      // note durations: 4 = quarter note, 8 = eighth note, etc.:
      int noteDurations[] = {
        4, 8, 8, 4, 4, 4, 4, 4
      };
      
     /* *****Testing the Buzzer***** */    
     // iterate over the notes of the melody:
     for (int thisNote = 0; thisNote < 8; thisNote++) {
  
       // to calculate the note duration, take one second
       // divided by the note type.
       //e.g. quarter note = 1000 / 4, eighth note = 1000/8, etc.
       int noteDuration = 1000 / noteDurations[thisNote];
       tone(BUZZER_PIN, melody[thisNote], noteDuration);     
  
       // to distinguish the notes, set a minimum time between them.
       // the note's duration + 30% seems to work well:
       int pauseBetweenNotes = noteDuration * 1.30;
       delay(pauseBetweenNotes);
       
       // stop the tone playing:
       noTone(BUZZER_PIN);
      
     }
  }
   
  void TestRGBLed(void)
  {
         
     /* *****Testing the RGB LED***** */
     for(i = 0; i != 3; i++)
     {
       digitalWrite(RED_LED_PIN, HIGH);
       delay(500);
       digitalWrite(RED_LED_PIN, LOW);
       delay(500);
       digitalWrite(GREEN_LED_PIN, HIGH);
       delay(500);
       digitalWrite(GREEN_LED_PIN, LOW);
       delay(500);
       digitalWrite(BLUE_LED_PIN, HIGH);
       delay(500);
       digitalWrite(BLUE_LED_PIN, LOW);
       delay(500); 
     }  
  }
  
  void TestRelays(void)
  {
     /* *****Testing the Relays***** */
     int select;
     
     select = OP1_RELAY;
     
     for(i=1; i < 9; i++)
     {
       
       if( i % 5 == 0)
         select = OP2_RELAY;
         
       digitalWrite(select, HIGH);
       delay(500);
       digitalWrite(select, LOW);
       delay(500);
     }
    
  }
  
  void TestIRRecv(void)
  {
    //No test developed so far
  }
  
  void BeeperChirp(void)
  {
    int duration = 1000/8;
    
    for(i = 0; i < 3; i++)
    {
      tone(BUZZER_PIN, NOTE_C7, duration);
      delay(150);
      noTone(BUZZER_PIN);
    }
    delay(300);
  }
  
  void TestBluetooth(void)
  {
      char data = '\n';
      delay(9000);

      // Open serial communications and wait for port to open:
      Serial.begin(9600);
      while (!Serial) {
        ; // wait for serial port to connect. Needed for Leonardo only
      }
  
      Serial.println("Ready!");
 
      // set the data rate for the SoftwareSerial port
 
      // for HC-05 use 38400 when poerwing with KEY/STATE set to HIGH on power on
      //mySerial.begin(9600);
      Serial1.begin(9600);
      Serial.println("Use a HC-06 to test: Use an external and terminal sofware");
      Serial.println("Pass text back and forth between Serial Monitor and External Computer");
      Serial.println(" ");
      Serial.println("Enter # int the Arduino Serial Monitor to leave Bluetooth Test");
      
      Serial.println("Ready... ");
      
      while(data != '#')
      {
        if(Serial1.available())
        {
          while(Serial1.available())
          Serial.write(Serial1.read());  
        }
  
        //read user input of available
        if (Serial.available())
        {
          data = Serial.read();
          Serial1.write(data);
        }
      }
      
      Serial.println(" ");
      Serial.println("Bleutooth Test Successfully Terminated");
  }

  void TestCurrentDraw(void)
  {

  int count = 0;
  char data = '\n';
  
   /*Teporarily adding various functions one by one*/
   /* to allow determination of current draw*/

   // Open serial communications and wait for port to open:
   Serial.begin(9600);
   while (!Serial) {
      ; // wait for serial port to connect. Needed for Leonardo only
   }
  
   Serial.println("Ready!");

   Serial.println(" ");
   Serial.println("Shutting down all systems to allow current draw readings");
    
    digitalWrite(RED_LED_PIN, LOW);
    digitalWrite(GREEN_LED_PIN, LOW);
    digitalWrite(BLUE_LED_PIN, LOW);
       
    digitalWrite(OP1_RELAY, LOW);
    digitalWrite(OP2_RELAY, LOW);

    digitalWrite(BUZZER_PIN, LOW);

    digitalWrite(IR_OP_PIN, LOW);

   Serial.println("All systems disabled, take baseline reading");
   Serial.println(" ");
   Serial.println("Enter # in the Serial Monitor to add functions one by one");

   while(1)
   {
      //Read data from the serial monitor
      while(data != '#' /*&& data != 'q'*/)
      {
        //read user input if available
        if (Serial.available())
        {
          data = Serial.read();          
        }
      }

      //if( data == '#')
      //{
        switch(count)
        {
          case 0:
            Serial.println("Enabling Red LED");
            digitalWrite(RED_LED_PIN, HIGH);
          break;
  
          case 1:
            Serial.println("Enabling Green LED");
            digitalWrite(GREEN_LED_PIN, HIGH);
          break;
  
          case 2:
            Serial.println("Enabling Blue LED");
            digitalWrite(BLUE_LED_PIN, HIGH);
          break;

          case 3:
            Serial.println("Enabling Relay 1");
            digitalWrite(OP1_RELAY, HIGH);
          break;

          case 4:
            Serial.println("Enabling Relay 2");
            digitalWrite(OP2_RELAY, HIGH);
          break;

          case 5:
            Serial.println("Enabling IR Output");
            digitalWrite(IR_OP_PIN, HIGH);
          break;          

          case 6:
            Serial.println("Enabling Buzzer");
            digitalWrite(BUZZER_PIN, HIGH);
          break;
  
          default:
            Serial.println("No other systems to activate, you are at full current draw");
            Serial.println("Delaying for 5 seconds, then exiting");
            delay(5000);
            count = 99;
          break;          
        }
      //}
      //else
        //it has to be 'q', so quit
      //  break;

      count++;
      
      if(count== 100)
        break;
                
      Serial.print("Count is ");
      Serial.println(count );
                  
      Serial.print("2 data is ");
      Serial.println(data);

      data = '\n';
     
   }  //end forever loop
  } //end funtion


  void TestSwitchInput(void)
  {

    char data = '\n';
  
    /*Teporarily adding various functions one by one*/
    /* to allow determination of current draw*/

    // Open serial communications and wait for port to open:
    Serial.begin(9600);
    while (!Serial) {
        ; // wait for serial port to connect. Needed for Leonardo only
    }
  
    Serial.println("Ready!");
    Serial.println("This will test switch inputs... Initializing D7 as Active Low input");

    pinMode( 7, INPUT);
    digitalWrite( 7, HIGH);

    Serial.println("D7 Initialized, ready for input.  Enter '#' in Serial Monitor to break out of test");

    while(data != '#')
    {
      if(digitalRead(7) == LOW)
      {
        BeepRegular();      
      }

      //read user input if available
      if (Serial.available())
      {
        data = Serial.read();          
      }
    }

    Serial.println("Switch Input Test Successfully Completed!");
  }


  
  void BeeperChirpUp(void)
  {
    int duration = 1000/8;
    
    tone(BUZZER_PIN, NOTE_A6, duration);
    delay(150);
    noTone(BUZZER_PIN);
    
    tone(BUZZER_PIN, NOTE_B6, duration);
    delay(150);
    noTone(BUZZER_PIN);
    
    tone(BUZZER_PIN, NOTE_C7, duration);
    delay(150);
    noTone(BUZZER_PIN);
    
    delay(300);
          
  }
  
  void BeeperChirpDown(void)
  {
    int duration = 1000/8;
    
    tone(BUZZER_PIN, NOTE_C7, duration);
    delay(150);
    noTone(BUZZER_PIN);
    
    tone(BUZZER_PIN, NOTE_B6, duration);
    delay(150);
    noTone(BUZZER_PIN);
    
    tone(BUZZER_PIN, NOTE_A6, duration);
    delay(150);
    noTone(BUZZER_PIN);
    
    delay(300);
          
  }

  void BeepRegular(void)
  {
    int duration = 1000/8;

    tone(BUZZER_PIN, NOTE_C7, duration);
    delay(150);
    noTone(BUZZER_PIN);
  }

  
