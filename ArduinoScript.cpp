#include <Wire.h>
#include <Adafruit_GFX.h>
#include <Adafruit_SH110X.h>
#include <Adafruit_NeoPixel.h>

#define OLED_ADDR 0x3C

#define PIXEL_PIN 2
#define NUMPIXELS 12

#define INHALE_PIN 3
#define EXHALE_PIN 4

Adafruit_SH1106G display(128, 64, &Wire, -1);
Adafruit_NeoPixel ring(NUMPIXELS, PIXEL_PIN, NEO_GRB + NEO_KHZ800);

int breathCycles = 0;
bool breathingFinished = false;
float currentR = 80;
float currentG = 80;
float currentB = 80;

float targetR = 80;
float targetG = 80;
float targetB = 80;
unsigned long completionTimer = 0;
bool showingGoodJob = false;

enum Emotion {
  NEUTRAL,
  HAPPY,
  CALM,
  SAD,
  ANXIOUS,
  DEPRESSED,
  BREATH
};

Emotion currentEmotion = NEUTRAL;

enum BreathState {
  BREATH_IN,
  BREATH_HOLD,
  BREATH_OUT
};

BreathState breathState = BREATH_IN;

unsigned long breathTimer = 0;
unsigned long blinkTimer = 0;

bool eyesClosed = false;

const unsigned long inhaleTime = 4000;
const unsigned long holdTime = 7000;
const unsigned long exhaleTime = 8000;

//////////////////////////////////////////////////////////
// SETUP
//////////////////////////////////////////////////////////

void setup() {

  Serial.begin(115200);

  pinMode(INHALE_PIN, OUTPUT);
  pinMode(EXHALE_PIN, OUTPUT);

  digitalWrite(INHALE_PIN, LOW);
  digitalWrite(EXHALE_PIN, LOW);

  ring.begin();
  ring.setBrightness(50);
  ring.show();

  if (!display.begin(OLED_ADDR, true)) {
    while (1);
  }

  drawFace();
}

//////////////////////////////////////////////////////////
// LOOP
//////////////////////////////////////////////////////////

void loop() {

  if (showingGoodJob &&
    millis() - completionTimer >= 2000) {

  showingGoodJob = false;

  currentEmotion = CALM;

  drawFace();
}

  readSerial();

  updateBlink();

  updateBreathing();

  updateNeoPixels();
}

//////////////////////////////////////////////////////////
// SERIAL
//////////////////////////////////////////////////////////

void readSerial() {

  if (!Serial.available()) return;

  String cmd = Serial.readStringUntil('\n');

  cmd.trim();
  cmd.toLowerCase();

  Emotion previousEmotion = currentEmotion;

  if (cmd == "happy")
    currentEmotion = HAPPY;

  else if (cmd == "calm")
    currentEmotion = CALM;

  else if (cmd == "sad")
    currentEmotion = SAD;

  else if (cmd == "anxious")
    currentEmotion = ANXIOUS;

  else if (cmd == "depressed")
    currentEmotion = DEPRESSED;

  else if (cmd == "breath")
    currentEmotion = BREATH;

  else
    currentEmotion = NEUTRAL;

  // entering breathing mode?
  if (!(
        previousEmotion == BREATH 
      ) &&
      (
        currentEmotion == BREATH
      )) {
    breathCycles = 0;
    breathingFinished = false;
    breathState = BREATH_IN;
    breathTimer = millis();
  }

  drawFace();
}

//////////////////////////////////////////////////////////
// BLINK
//////////////////////////////////////////////////////////

void updateBlink() {

  if (millis() - blinkTimer > 2500) {

    eyesClosed = !eyesClosed;

    drawFace();

    blinkTimer = millis();
  }
}

//////////////////////////////////////////////////////////
// BREATHING
//////////////////////////////////////////////////////////

bool breathingMode() {

  return currentEmotion == BREATH;
}

void updateBreathing() {

  if (breathingFinished)
  return;

  if (!breathingMode()) {

    digitalWrite(INHALE_PIN, LOW);
    digitalWrite(EXHALE_PIN, LOW);
    breathState = BREATH_IN;
    breathTimer = millis();

    return;
  }

  unsigned long now = millis();

  switch (breathState) {

    case BREATH_IN:

      digitalWrite(INHALE_PIN, HIGH);
      digitalWrite(EXHALE_PIN, LOW);

      if (now - breathTimer >= inhaleTime) {

        breathState = BREATH_HOLD;
        breathTimer = now;
        drawFace();
      }

      break;

    case BREATH_HOLD:

      digitalWrite(INHALE_PIN, HIGH);
      digitalWrite(EXHALE_PIN, HIGH);

      if (now - breathTimer >= holdTime) {

        breathState = BREATH_OUT;
        breathTimer = now;
        drawFace();
      }

      break;

    case BREATH_OUT:

      digitalWrite(INHALE_PIN, LOW);
      digitalWrite(EXHALE_PIN, HIGH);

      if (now - breathTimer >= exhaleTime) {

        breathCycles++;

if(breathCycles >= 3) {

  breathingFinished = true;

  showingGoodJob = true;
  completionTimer = millis();

  digitalWrite(INHALE_PIN, LOW);
  digitalWrite(EXHALE_PIN, LOW);

  drawFace();
} else {

  breathState = BREATH_IN;
  breathTimer = now;

  drawFace();
}
      }

      break;
  }
}

//////////////////////////////////////////////////////////
// OLED FACE
//////////////////////////////////////////////////////////

void drawEyesNormal() {

  if (eyesClosed) {

    display.drawLine(40,25,52,25,SH110X_WHITE);
    display.drawLine(76,25,88,25,SH110X_WHITE);
  }
  else {

    display.fillCircle(46,25,6,SH110X_WHITE);
    display.fillCircle(82,25,6,SH110X_WHITE);

    display.fillCircle(48,27,2,SH110X_BLACK);
    display.fillCircle(84,27,2,SH110X_BLACK);
  }
}

void drawFace() {

  display.clearDisplay();
  if (showingGoodJob) {

  display.setTextSize(2);

  display.setCursor(18,12);
  display.setTextColor(SH110X_WHITE);
  display.print("GOOD");

  display.setCursor(24,36);
  display.print("JOB!");

  display.display();
  return;
}

  display.drawRoundRect(20,2,88,60,10,SH110X_WHITE);

  switch(currentEmotion) {

    case HAPPY:

      display.drawLine(40,24,52,20,SH110X_WHITE);
      display.drawLine(76,20,88,24,SH110X_WHITE);

      display.drawLine(52,42,58,48,SH110X_WHITE);
      display.drawLine(58,48,70,48,SH110X_WHITE);
      display.drawLine(70,48,76,42,SH110X_WHITE);

      break;

    case CALM:

      display.drawLine(40,24,52,24,SH110X_WHITE);
      display.drawLine(76,24,88,24,SH110X_WHITE);

      display.drawLine(54,45,74,45,SH110X_WHITE);

      break;

    case SAD:

      drawEyesNormal();

      display.drawLine(52,50,58,44,SH110X_WHITE);
      display.drawLine(58,44,70,44,SH110X_WHITE);
      display.drawLine(70,44,76,50,SH110X_WHITE);

      break;

    case ANXIOUS:

      display.drawCircle(46,25,8,SH110X_WHITE);
      display.drawCircle(82,25,8,SH110X_WHITE);

      display.fillCircle(46,25,3,SH110X_WHITE);
      display.fillCircle(82,25,3,SH110X_WHITE);

      display.drawCircle(64,46,5,SH110X_WHITE);

      break;

    case DEPRESSED:

      display.drawLine(40,25,52,25,SH110X_WHITE);
      display.drawLine(76,25,88,25,SH110X_WHITE);

      display.drawLine(56,48,72,48,SH110X_WHITE);

      break;

    default:

      drawEyesNormal();

      display.drawLine(54,45,74,45,SH110X_WHITE);

      break;
  }

  display.display();
}

//////////////////////////////////////////////////////////
// NEOPIXEL
//////////////////////////////////////////////////////////

void updateNeoPixels() {

  static unsigned long lastUpdate = 0;

  if (millis() - lastUpdate < 30)
    return;

  lastUpdate = millis();


  switch(currentEmotion) {

  case HAPPY:
    targetR=255;
    targetG=180;
    targetB=0;
    break;

  case BREATH:
    targetR = 255;
    targetG = 165;
    targetB = 0;
    break;

  case CALM:
    targetR=0;
    targetG=100;
    targetB=255;
    break;

  case SAD:
    targetR=0;
    targetG=30;
    targetB=255;
    break;

  case ANXIOUS:
    targetR=255;
    targetG=80;
    targetB=0;
    break;

  case DEPRESSED:
    targetR=80;
    targetG=0;
    targetB=120;
    break;

  default:
    targetR=80;
    targetG=80;
    targetB=80;
    break;
}
currentR += (targetR - currentR) * 0.08;
currentG += (targetG - currentG) * 0.08;
currentB += (targetB - currentB) * 0.08;

uint8_t r = (uint8_t)currentR;
uint8_t g = (uint8_t)currentG;
uint8_t b = (uint8_t)currentB;

  ring.clear();
  if (breathingFinished) {

  for(int i=0; i<NUMPIXELS; i++) {
    ring.setPixelColor(i, ring.Color(r,g,b));
  }

  ring.show();
  return;
}

  if (!breathingMode()) {

    for(int i=0;i<NUMPIXELS;i++) {
      ring.setPixelColor(i, ring.Color(r,g,b));
    }

    ring.show();
    return;
  }

  unsigned long elapsed;
  unsigned long now = millis();
  int ledsLit = 0;
  float progress = 0.0;


  switch(breathState) {

    //////////////////////////////////////////////////////
    // INHALE : FILL RING
    //////////////////////////////////////////////////////
    case BREATH_IN:

  progress =
    (float)(millis() - breathTimer) /
    inhaleTime;

  progress = constrain(progress, 0.0, 1.0);

  {
    float litPixels = progress * NUMPIXELS;

    int fullPixels = (int)litPixels;

    float partial = litPixels - fullPixels;

    for(int i=0; i<fullPixels; i++) {
      ring.setPixelColor(i, ring.Color(r,g,b));
    }

    if(fullPixels < NUMPIXELS) {

      ring.setPixelColor(
        fullPixels,
        ring.Color(
          r * partial,
          g * partial,
          b * partial
        )
      );
    }
  }

  break;

    //////////////////////////////////////////////////////
    // HOLD : FULL RING
    //////////////////////////////////////////////////////
    case BREATH_HOLD:

      for(int i=0;i<NUMPIXELS;i++) {
        ring.setPixelColor(i, ring.Color(r,g,b));
      }

      break;

    //////////////////////////////////////////////////////
    // EXHALE : EMPTY RING
    //////////////////////////////////////////////////////
        case BREATH_OUT:

  progress =
    (float)(millis() - breathTimer) /
    exhaleTime;

  progress = constrain(progress, 0.0, 1.0);

  {
    float litPixels =
      NUMPIXELS * (1.0 - progress);

    int fullPixels = (int)litPixels;

    float partial = litPixels - fullPixels;

    for(int i=0; i<fullPixels; i++) {
      ring.setPixelColor(i, ring.Color(r,g,b));
    }

    if(fullPixels < NUMPIXELS) {

      ring.setPixelColor(
        fullPixels,
        ring.Color(
          r * partial,
          g * partial,
          b * partial
        )
      );
    }
  }

  break;
  }

  ring.show();
}