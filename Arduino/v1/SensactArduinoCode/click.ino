// click - based on morse program 20160412

const int seconds = 1000;
int units = 400;  // morse units
int alarmThreshold = 9*seconds; 


boolean wroteSpace = true; // write only one ' ' letter
int firstTimeMorse = 0;

int lastTransit = 0;
int timeLapse = 0;
boolean MARK = false;
boolean TRANSIT = false;

int click_action( int input) {
   if( input == 1) {
      tone( SENSACT_BUZZER, NOTE_E7, 100 );
      //if(state == SENSACT_RUN) Serial.println('1');
      ledTime = whenOn[CLICK]= currentMillis;
      onOff[CLICK] ++;
   } 
   else if( input == 2) {
      tone( SENSACT_BUZZER, NOTE_A4, 100 );
      //if(state == SENSACT_RUN) Serial.println('2');
      ledTime = whenOn[CLICK]= currentMillis;
      onOff[CLICK] ++;
   } 
   return input;
}

int mark() {

   if ( !MARK ) { //&& ((millis()-firstTimeMorse ) > 2*seconds) ) {
      timeLapse = millis() - lastTransit;
      lastTransit = millis();
      TRANSIT = true;
      MARK = true;
   }

   return click_action( check_click() );
}

int no_mark() {

   if ( MARK ) { // && ((millis()-firstTimeMorse ) > 2*seconds) ) {  
      timeLapse = millis() - lastTransit;
      lastTransit = millis();
      TRANSIT = true;
      MARK = false;
   }
   return click_action( check_click() );
}

int click_setup() {
   lastTransit = 0;
   timeLapse =0;
   MARK = false;
   TRANSIT = false;
}

int check_click() {
   int ret_value = 0;
   int sinceTransition = (millis() - lastTransit);

   //   if ( pause ) return;

   // mark =============
   if (MARK) {  
      //      print("M");
      if (TRANSIT) {  // after transition to mark (uptick) => deal with SPACE here


         if ( timeLapse > 7*units ) {
            //ret_value = 2;
         } 
         else if ( timeLapse > 3*units ) { // space between letters
            //            processLetter();
            wroteSpace = false;
         } 
         else {  // very short space - it is part of a letter; dealt with by space
            //
         }
      } 
      else {  // very long mark - short marks are handled by SPACE
         if ( sinceTransition > alarmThreshold ) {   //alarm
            //            buzzAlarm.on(2000);
            //            pauseFor( 2000, false);
            //            println("ALARM1");
         }         
         else if ( timeLapse > 3*units ) { //

            //lastTransit = millis();
            //timeLapse = 0;
         } 
      }
   } 
   else { 
      //      print("S");
      // space =============
      if (TRANSIT) {  // after transition to space (downtick) => deal with MARK here

         if ( timeLapse > alarmThreshold ) {   // alarm - should not see here
            //            buzzAlarm.on(2000);
            //            pauseFor(2000, false);
            //timeLapse = 0;
            //            morseStr="";
            //            println("ALARM2");
            ret_value = 2;
         } 
         else if ( timeLapse > 7*units ) {
            //            processDel();
            ret_value = 2;
         } 
         else if ( timeLapse > 3*units  ) {
            ret_value = 2;
            //            morseStr += "-";
            //            inDelete = 0;  //ylh
            //            if ( voiceon ) buzzDash.on(200);
         } 
         else {  // if ( yesCount > 0 ) {
            ret_value = 1;
            //            morseStr += ".";
            //            if ( voiceon ) buzzDot.on(100);
            //            inDelete = 0;  //ylh
         }
         wroteSpace = false;

      } 
      else {  // handle very long SPACE here - short spaces handled by Mark uptick
         if ( sinceTransition > 10*units ) { // was 10 units
            if ( !wroteSpace) {  
               //               ret_value = 2;
            }
            wroteSpace = true;
         } 
         else if ( sinceTransition > 3*units ) { // space between letters
            //            processLetter();
            wroteSpace = false;
         } 
         else {  // very short space - it is part of a letter; dealt with by mark
            //
         }

      }
   }
   if ( TRANSIT ) {
      TRANSIT = false;
      lastTransit = millis();
   }

   return ret_value;
}











