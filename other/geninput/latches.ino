#define COUNTER_RESET_PIN  7
#define COUNTER_PIN  6
#define LATCH_PIN 4

#define RED_LED 1
#define GREEN_LED 2
#define BLUE_LED 4

#define DELAY_TIME  10 // Time in MS to wait for chips to reset, latch etc.  
// Probably can be much shorter.  Chips response times are given on ns on spec sheet.

void latch_setup() {
  pinMode(COUNTER_RESET_PIN, OUTPUT);
  pinMode(COUNTER_PIN, OUTPUT);
  pinMode(LATCH_PIN, OUTPUT);

}

void latch_loop() {
  // put your main code here, to run repeatedly:

  // Power all outputs

  setLatches(false, true, false, false);
  Serial.println("+++++ ALL ON +++++");
  delay(5000);

  setLatches(false, false, false, false);
  Serial.println("====== ALL OFF ======");
  delay(5000);

  // Now turn on an LED to show the run state
  //  ledOn(GREEN_LED);
}

void setLatches(boolean latch1, boolean latch2, boolean latch3, boolean latch4) {
  digitalWrite(COUNTER_PIN, LOW); // Ensure the right start point

  // Reset the counter
  digitalWrite(COUNTER_RESET_PIN, HIGH);
  Serial.println("COUNTER RESET HIGH");
  delay(DELAY_TIME);
  digitalWrite(COUNTER_RESET_PIN, LOW);
  Serial.println("COUNTER RESET LOW");

  // Compute the counter value required
  int value = latch1 ? 1 : 0;
  value += latch2 ? 2 : 0;
  value += latch3 ? 4 : 0;
  value += latch4 ? 8 : 0;

  // Set the counter - each low-to-high transition adds 1 to the counter
  for (int i = 0; i < value; i++) {
    digitalWrite(COUNTER_PIN, HIGH);
    Serial.println("counter high");
    delay(DELAY_TIME);
    digitalWrite(COUNTER_PIN, LOW);
    Serial.println("counter low");
    delay(DELAY_TIME);
  }

  // Latch the counter output to the Latch_Qn outputs.
  digitalWrite(LATCH_PIN, HIGH);
  delay(DELAY_TIME);
  digitalWrite(LATCH_PIN, LOW);

}

void ledOn(int LED) {
  digitalWrite(COUNTER_PIN, LOW); // Ensure the right start point

  // Reset the counter
  digitalWrite(COUNTER_RESET_PIN, HIGH);
  delay(DELAY_TIME);
  digitalWrite(COUNTER_RESET_PIN, LOW);

  // Set the counter - each low-to-high transition adds 1 to the counter
  for (int i = 0; i < LED; i++) {
    digitalWrite(COUNTER_PIN, HIGH);
    delay(DELAY_TIME);
    digitalWrite(COUNTER_PIN, LOW);
    delay(DELAY_TIME);
  }

  // Do not touch the LATCH_PIN.  This way you affect only the LED and
  // not the LATCH_Qn outputs.
}

