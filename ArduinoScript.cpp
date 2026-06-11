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
const unsigned long holdTime   = 7000;
const unsigned long exhaleTime = 8000;

//////////////////////////////////////////////////////////
// SETUP
//////////////////////////////////////////////////////////

void setup() {
  Wire.begin();
  Wire.setWireTimeout(3000,true);

  Serial.begin(115200);

  pinMode(INHALE_PIN, OUTPUT);
  pinMode(EXHALE_PIN, OUTPUT);

  digitalWrite(INHALE_PIN, LOW);
  digitalWrite(EXHALE_PIN, LOW);

  ring.begin();
  ring.setBrightness(25);
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

  // Transition out of "Good Job" screen after 2 seconds
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

  // FIX: Continuously refresh the face during active breathing
  // so the countdown timer updates every ~100ms.
  // (Outside breathing mode drawFace is only called on state changes,
  //  so this branch is only entered when it's actually needed.)
  static unsigned long lastBreathDraw = 0;
  if (breathingMode() && !breathingFinished &&
      millis() - lastBreathDraw >= 100) {
    drawFace();
    lastBreathDraw = millis();
  }
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

  if      (cmd == "happy")    currentEmotion = HAPPY;
  else if (cmd == "calm")     currentEmotion = CALM;
  else if (cmd == "sad")      currentEmotion = SAD;
  else if (cmd == "anxious")  currentEmotion = ANXIOUS;
  else if (cmd == "depressed")currentEmotion = DEPRESSED;
  else if (cmd == "breath")   currentEmotion = BREATH;
  else                        currentEmotion = NEUTRAL;

  // Entering breathing mode fresh — reset all state
  if (previousEmotion != BREATH && currentEmotion == BREATH) {
    breathCycles      = 0;
    breathingFinished = false;
    breathState       = BREATH_IN;
    breathTimer       = millis();
  }

  drawFace();
}

//////////////////////////////////////////////////////////
// BLINK
//////////////////////////////////////////////////////////

void updateBlink() {

  // FIX: Don't blink (and overwrite the screen) during breathing
  // or while the "Good Job" message is displayed.
  if (breathingMode() || showingGoodJob) return;

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

  if (breathingFinished) return;

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

        if (breathCycles >= 3) {

          breathingFinished = true;
          showingGoodJob    = true;
          completionTimer   = millis();

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

    display.drawLine(40, 25, 52, 25, SH110X_WHITE);
    display.drawLine(76, 25, 88, 25, SH110X_WHITE);

  } else {

    display.fillCircle(46, 25, 6, SH110X_WHITE);
    display.fillCircle(82, 25, 6, SH110X_WHITE);

    display.fillCircle(48, 27, 2, SH110X_BLACK);
    display.fillCircle(84, 27, 2, SH110X_BLACK);
  }
}

void drawFace() {

  display.clearDisplay();

  // ── Good Job screen ───────────────────────────────────
  if (showingGoodJob) {

    display.setTextSize(2);
    display.setTextColor(SH110X_WHITE);
    display.setCursor(18, 12);
    display.print("GOOD");
    display.setCursor(24, 36);
    display.print("JOB!");
    display.display();
    return;
  }

  // ── FIX: Dedicated breath-guidance screen ─────────────
  // Shows the phase label centred on the top half and a large
  // countdown number on the bottom half.  No face is drawn
  // so there is plenty of room for the text.
  if (breathingMode() && !breathingFinished) {

    unsigned long elapsed = millis() - breathTimer;
    unsigned long phaseDuration;
    const char*   label;

    switch (breathState) {
      case BREATH_IN:
        label         = "Breathe In";
        phaseDuration = inhaleTime;
        break;
      case BREATH_HOLD:
        label         = "Hold";
        phaseDuration = holdTime;
        break;
      case BREATH_OUT:
      default:
        label         = "Breathe Out";
        phaseDuration = exhaleTime;
        break;
    }

    // Countdown: ceil((phaseDuration - elapsed) / 1000)
    unsigned long remaining = 0;
    if (elapsed < phaseDuration) {
      remaining = (phaseDuration - elapsed + 999) / 1000; // ceiling divide
    }

    // ── Phase label (text size 1 = 6×8 px per char) ──
    display.setTextColor(SH110X_WHITE);
    display.setTextSize(1);

    // Centre the label horizontally
    int16_t  tx, ty;
    uint16_t tw, th;
    display.getTextBounds(label, 0, 0, &tx, &ty, &tw, &th);
    display.setCursor((128 - tw) / 2, 10);
    display.print(label);

    // ── Cycle counter (small, top-right) ──────────────
    display.setTextSize(1);
    display.setCursor(100, 2);
    display.print(breathCycles + 1);
    display.print("/3");

    // ── Big countdown number (text size 4 = 24×32 px) ─
    display.setTextSize(4);
    char numBuf[4];
    itoa((int)remaining, numBuf, 10);
    display.getTextBounds(numBuf, 0, 0, &tx, &ty, &tw, &th);
    display.setCursor((128 - tw) / 2, 28);
    display.print(numBuf);

    display.display();
    return;
  }

  // ── Normal emotion faces ───────────────────────────────
  display.drawRoundRect(20, 2, 88, 60, 10, SH110X_WHITE);

  switch (currentEmotion) {

    case HAPPY:

      display.drawLine(40, 24, 52, 20, SH110X_WHITE);
      display.drawLine(76, 20, 88, 24, SH110X_WHITE);

      display.drawLine(52, 42, 58, 48, SH110X_WHITE);
      display.drawLine(58, 48, 70, 48, SH110X_WHITE);
      display.drawLine(70, 48, 76, 42, SH110X_WHITE);
      break;

    case CALM:

      display.drawLine(40, 24, 52, 24, SH110X_WHITE);
      display.drawLine(76, 24, 88, 24, SH110X_WHITE);

      display.drawLine(54, 45, 74, 45, SH110X_WHITE);
      break;

    case SAD:

      drawEyesNormal();

      display.drawLine(52, 50, 58, 44, SH110X_WHITE);
      display.drawLine(58, 44, 70, 44, SH110X_WHITE);
      display.drawLine(70, 44, 76, 50, SH110X_WHITE);
      break;

    case ANXIOUS:

      display.drawCircle(46, 25, 8, SH110X_WHITE);
      display.drawCircle(82, 25, 8, SH110X_WHITE);

      display.fillCircle(46, 25, 3, SH110X_WHITE);
      display.fillCircle(82, 25, 3, SH110X_WHITE);

      display.drawCircle(64, 46, 5, SH110X_WHITE);
      break;

    case DEPRESSED:

      display.drawLine(40, 25, 52, 25, SH110X_WHITE);
      display.drawLine(76, 25, 88, 25, SH110X_WHITE);

      display.drawLine(56, 48, 72, 48, SH110X_WHITE);
      break;

    default:  // NEUTRAL (and BREATH post-finish falls to CALM before here)

      drawEyesNormal();

      display.drawLine(54, 45, 74, 45, SH110X_WHITE);
      break;
  }

  display.display();
}

//////////////////////////////////////////////////////////
// NEOPIXEL
//////////////////////////////////////////////////////////

void updateNeoPixels() {

  static unsigned long lastUpdate = 0;

  if (millis() - lastUpdate < 30) return;

  lastUpdate = millis();

  switch (currentEmotion) {

    case HAPPY:
      targetR = 255; targetG = 180; targetB =   0; break;

    case BREATH:
      targetR = 255; targetG = 165; targetB =   0; break;

    case CALM:
      targetR =   0; targetG = 100; targetB = 255; break;

    case SAD:
      targetR =   0; targetG =  30; targetB = 255; break;

    case ANXIOUS:
      targetR = 255; targetG =  80; targetB =   0; break;

    case DEPRESSED:
      targetR =  80; targetG =   0; targetB = 120; break;

    default:
      targetR =  80; targetG =  80; targetB =  80; break;
  }

  currentR += (targetR - currentR) * 0.08f;
  currentG += (targetG - currentG) * 0.08f;
  currentB += (targetB - currentB) * 0.08f;

  uint8_t r = (uint8_t)currentR;
  uint8_t g = (uint8_t)currentG;
  uint8_t b = (uint8_t)currentB;

  ring.clear();

  // After breathing finishes (or not in breath mode) — solid colour
  if (breathingFinished || !breathingMode()) {

    for (int i = 0; i < NUMPIXELS; i++) {
      ring.setPixelColor(i, ring.Color(r, g, b));
    }
    ring.show();
    return;
  }

  // Breath-mode: animate ring fill/drain
  float    progress  = 0.0f;
  float    litPixels = 0.0f;
  int      fullPixels;
  float    partial;

  switch (breathState) {

    ////////////////////////////////////////////////////
    // INHALE: fill ring clockwise
    ////////////////////////////////////////////////////
    case BREATH_IN:

      progress  = constrain(
        (float)(millis() - breathTimer) / inhaleTime, 0.0f, 1.0f);
      litPixels = progress * NUMPIXELS;
      fullPixels = (int)litPixels;
      partial    = litPixels - fullPixels;

      for (int i = 0; i < fullPixels; i++) {
        ring.setPixelColor(i, ring.Color(r, g, b));
      }
      if (fullPixels < NUMPIXELS) {
        ring.setPixelColor(fullPixels,
          ring.Color(r * partial, g * partial, b * partial));
      }
      break;

    ////////////////////////////////////////////////////
    // HOLD: full ring
    ////////////////////////////////////////////////////
    case BREATH_HOLD:

      for (int i = 0; i < NUMPIXELS; i++) {
        ring.setPixelColor(i, ring.Color(r, g, b));
      }
      break;

    ////////////////////////////////////////////////////
    // EXHALE: drain ring
    ////////////////////////////////////////////////////
    case BREATH_OUT:

      progress  = constrain(
        (float)(millis() - breathTimer) / exhaleTime, 0.0f, 1.0f);
      litPixels  = NUMPIXELS * (1.0f - progress);
      fullPixels = (int)litPixels;
      partial    = litPixels - fullPixels;

      for (int i = 0; i < fullPixels; i++) {
        ring.setPixelColor(i, ring.Color(r, g, b));
      }
      if (fullPixels < NUMPIXELS) {
        ring.setPixelColor(fullPixels,
          ring.Color(r * partial, g * partial, b * partial));
      }
      break;
  }

  ring.show();
}
