/* vkb.js : visual keyboard : 2016-05-18 Wed : ylh

    - based originally on 2015/words 2015/kb.js written in processing.js
    - this version works with gyroscope (using "YPR" data) and touchpad (using "+")
    - gyroscopes move cross-hair to target keys, dwell click
    - same action can be achieved by moving the mouse
    
    - can also detect touchpad keys and show them on the key, click action is immediate
*/
"use strict"

var menu = [
        {
            id: "refresh",
            display: "REFRESH PORT",
            fn: serialio.refreshport
        },
        {
            id: "nextport",
            display: "NEXT PORT",
            fn: serialio.nextport
        },
        {
            id: "selectport",
            display: "SELECT PORT",
            fn: serialio.selectport
        },
        {
            id: "move",
            display: "MOVE",
            fn: move_fn
        },
        {
            id: "go",
            display: "GO",
            fn: go_fn
        },
        {
            id: "erase",
            display: "ERASE",
            fn: erase_fn
        },
        {
            id: "calibrate",
            display: "CALIBRATE",
            fn: calibrate_fn
        },
        {
            id: "save",
            display: "SAVE",
            fn: save_fn
        },
        {
            id: "retrieve",
            display: "RETRIEVE",
            fn: get_fn
        },
        {
            id: "setfrom",
            display: "SET FROM TEXTBOX",
            fn: setfrom_fn
        }
    ],
    keys = [],
    atGrid = null,
    MOKey = null,
    MOYes = false,
    MOTime = 0,
    currentFunction = null,
    currentKey = null,
    myText = "",
    C_WIDTH = 1000, // interface canvace w, h
    C_HEIGHT = 560,
    C_SHIFT = 340, // display area below
    keySize = 20,
    gridSize = 20,
    DWELL_TIME = 1000,
    REFRACTORY = 1200,
    profileJSON = null,
    startTime = null,
    outputArea = null,
    BASETEXTCOLOUR = "#6363fc",
    BACKGROUNDCOLOUR = 'rgb(255,255,255)',
    FOREGROUNDCOLOUR = 'rgb(220,220,200)'; // rgb(250,100,100)


function setup() {

    startTime = millis();

    createCanvas(C_WIDTH, C_HEIGHT + C_SHIFT);

    profileJSON = createInput("(profile here)");
    profileJSON.position(10, C_HEIGHT + C_SHIFT - 25);
    profileJSON.size(800, 20);

    outputArea = createP("---");
    outputArea.position(760, 10);
    //    outputArea.class('box');
    outputArea.id("outbox");

    serialio.web_socket();
    atGrid = matrix(C_WIDTH / gridSize + 1, (C_HEIGHT + C_SHIFT) / gridSize + 1, null);
    //    console.log(" " + (C_WIDTH / gridSize + 1) + " " + ((C_HEIGHT + C_SHIFT) / gridSize + 1));

    textSize(16);
    var i = 0;
    for (var m of menu) {
        m.el = createButton(m.display);
        m.el.value = m.display;
        m.el.position(20, 20 + i * 37);
        m.el.style('color', '#4747d0');
        m.el.style('background', BACKGROUNDCOLOUR);
        m.el.size(80, 33);
        m.el.mousePressed(m.fn);
        i = i + 1;
    }
    for (i = 0; i < 26; i++) {
        add_keys(i.toString(), 20 + i * keySize, 800);
    }

    get_fn();
    calibrate_fn();
}

function draw() {

    background('rgb(250,250,250)'); // make the screen white
    fill(BASETEXTCOLOUR);
    strokeWeight(0);
    textSize(14);
    text(serialio.thisport(), 30, 580);

    textSize(24);
    text(myText, 300, 30);

    currentFunction && currentFunction();

    doTouchpad();
    doYPR();
    drawMeters();
    scrollToTheBottom();

}

function add_keys(c, x, y) {
    let k = {};
    k.selected = false;
    k.touched = false;
    k.touchedTime = null;

    k.el = createButton(c);
    k.el.value(c);
    k.el.style('color', BASETEXTCOLOUR);
    k.el.position(x, y);
    k.el.size(keySize, keySize);
    k.el.mousePressed(keyClicked);
    k.el.mouseOver(keyMouseOver);
    k.el.mouseOut(keyMouseOut);
    k.el.gridx = toGrid(x) / gridSize;
    k.el.gridy = toGrid(y) / gridSize;
    keys.push(k);

    atGrid[k.el.gridx][k.el.gridy] = k;
}



function move_fn() {
    setThisMenu(this);
    currentFunction = function () {
        if (currentKey && currentKey.selected) {
            // move selected key's position
            currentKey.position(toGrid(mouseX) - gridSize / 2, toGrid(mouseY) - gridSize / 2);
        }
    };
}

function go_fn() {
    setThisMenu(this);
    currentFunction = function () {
        // check the case when spot is moved by head/gyro, not by mouse
        if (!MOYes) {
            // check if a key is on the x-hair
            let x = atGrid[g_x][g_y];

            if (x) {
                // first time on this key
                if (!MOKey) {
                    MOTime = millis();
                }
                MOKey = x.el;
            } else {
                // not on a key, check neighbourhood
                if (g_x > 0 && g_y > 0 && (g_x < C_WIDTH / gridSize) && (g_y < C_HEIGHT / gridSize)) {

                    function chk(x, y) {
                        if (atGrid[x][y] && atGrid[x][y].el == MOKey) return true;
                        return false;
                    }
                    // not on a key, but near another key
                    if (chk(g_x - 1, g_y) ||
                        chk(g_x + 1, g_y) ||
                        chk(g_x - 1, g_y - 1) ||
                        chk(g_x + 1, g_y - 1) ||
                        chk(g_x - 1, g_y + 1) ||
                        chk(g_x + 1, g_y + 1) ||
                        chk(g_x, g_y - 1) ||
                        chk(g_x, g_y + 1)
                    ) {
                        // near another key, assume that we are still at that other key
                    } else {
                        //not near another key, select
                        MOKey = null;
                        MOTime = millis();
                    }
                }
            }
        }

        if (MOKey) {
            // display key progress, then add key to buffer
            keyProgress();
        }
    };
}

function erase_fn() {
    myText = "";
    outputArea.html("");
}

// key pad key touched; action immediately, then show for FLASH_TIME
function padKeyOn(j) {
    let PAD_REFRACTORY = 300; // this must be less than FLASH_TIME
    let list = "";
    for (var k of keys) {
        if (k.el.value() == j) {
            // rising edge, just been touched OR re-touched after REFRACTORY while flashing
            if (!k.touched || (k.touched && (millis() - k.touchedTime) > PAD_REFRACTORY)) {

                // ACTION on rising edge here
                //                myText += k.el.value() + ",";
                list += k.el.value() + ",";

                k.touched = true;
                k.touchedTime = millis();
            }
        }
    }

    if (list.length > 0) {
        outputArea.html(outputArea.html() + "<br>[" + ((millis() - startTime) / 1000).toFixed(2) + "] " + list);
    }
}

function padKeyOff(k) {

}

function scrollToTheBottom() {
    let elem = document.getElementById(outputArea.id());
    elem.scrollTop = elem.scrollHeight;
}

function keyClicked() {
    if (this) {
        if (this.selected) {
            this.selected = false;
            currentKey.style('color', BASETEXTCOLOUR);
            currentKey = null;
            this.gridx = toGrid(mouseX) / gridSize;
            this.gridy = toGrid(mouseY) / gridSize;
            atGrid[this.gridx][this.gridy] = this;

        } else {
            this.selected = true;
            currentKey = this;
            currentKey.style('color', 'red');
            atGrid[this.gridx][this.gridy] = null;
        }
    }
}

function keyMouseOver() {
    //    this.style('background', FOREGROUNDCOLOUR);
    MOKey = this;
    MOYes = true;
    MOTime = millis();
}

function keyMouseOut() {
    //    this.style('background', BACKGROUNDCOLOUR);
    MOKey = null;
    MOYes = false;
}

// show key progress and action at the end of DWELL_TIME
function keyProgress() {
    let col;
    strokeWeight(0);
    if (millis() < MOTime) return;

    if ((millis() - MOTime) < DWELL_TIME) {
        let fractionFull = 1 - ((DWELL_TIME - (millis() - MOTime)) / DWELL_TIME);

        let x, y;
        if (MOKey) {
            x = MOKey.x + keySize / 2;
            y = MOKey.y + keySize / 2;
        } else {
            x = curs[0];
            y = curs[1];
        }
        col = (fractionFull / 2 + 0.5) * 255;
        fill(color(100, Math.floor(col), 100));
        arc(x, y, 60, 60, -0.5 * Math.PI, fractionFull * 2 * Math.PI - 0.5 * Math.PI);

    } else {
        // ACTION on key dwell completion (selected)
        myText += MOKey.value() + ",";
        outputArea.html(outputArea.html() + "<br>" + MOKey.value());
        //        MOKey = null;  // do not allow repeat
        MOTime = millis() + REFRACTORY;
    }
};

// show touchpad key selection progress for FLASH_TIME; already action'ed
function doTouchpad() {
    const FLASH_TIME = 400;
    for (var k of keys) {
        if (k.touched) {
            let col;
            strokeWeight(0);
            if (millis() < k.touchedTime) return;

            if ((millis() - k.touchedTime) < FLASH_TIME) {
                let fractionFull = ((FLASH_TIME - (millis() - k.touchedTime)) / FLASH_TIME);

                let x, y;

                x = k.el.x + keySize / 2;
                y = k.el.y + keySize / 2;
                col = (fractionFull / 2 + 0.5) * 255;
                fill(color(100, Math.floor(col), 100));
                let z = 10 + fractionFull * 60;
                ellipse(x, y, z, z);
                //                arc(x, y, 60, 60, -0.5 * Math.PI, fractionFull * 2 * Math.PI - 0.5 * Math.PI);

            } else {

                k.touched = null;
                k.touchedTime = millis();
                //                MOTime = millis() + REFRACTORY;
            }
        }
    }
};

let ypr = [0, 0, 0],
    oypr = [0, 0, 0],
    pypr = [0, 0, 0],
    x_ypr = [0, 0, 0],
    thisypr = [0, 0, 0],
    minypr = [0, 0, 0],
    maxypr = [0, 0, 0],
    curs = [0, 0, 0],
    gcurs = [0, 0, 0],
    g_x = 10,
    g_y = 10,
    c_centre = [0, 0, 0],
    gyro = [0, 0, 0],
    acc = [0, 0, 0],
    wacc = [0, 0, 0],
    calibStartTime = 0,
    inMotion = 0;

function doYPR() {

    let calib = (millis() - calibStartTime) < 3000;

    // use gyro to check if we've moved off - NOT WORKABLE algorith?
    //    if (Math.abs(gyro[i]) > 1 || Math.abs(gyro[2] > 1)) {
    //        inMotion = millis();
    //    }
    //    inMotion = millis(); // ylh  always GO!!

    for (var i = 0; i < 3; i++) {
        if (ypr[i] < -90) ypr[i] += 180;
        thisypr[i] = ypr[i] - oypr[i]; // + 360.0;

        if (calib) {
            // first 2 sec of calibrate, centre head
            if ((millis() - calibStartTime) < 2000) {
                fill(100, 100, 255);
                ellipse(C_WIDTH / 2, C_HEIGHT / 2, 10, 10);
                if (i == 0) {
                    minypr[i] = thisypr[i] - 12; // preset angles
                    maxypr[i] = thisypr[i] + 12;
                }
                if (i == 1) {
                    minypr[i] = thisypr[i] - 8; // preset angles
                    maxypr[i] = thisypr[i] + 8;
                }
            }
            if ((millis() - calibStartTime) < 2800) {
                fill(100, 100, 255);
                ellipse(C_WIDTH / 2, C_HEIGHT / 2, 10, 10);
                pypr[i] = 0;
                oypr[i] = ypr[i]; // reset origin
            }

            if (thisypr[i] < minypr[i]) minypr[i] = thisypr[i];
            if (thisypr[i] > maxypr[i]) maxypr[i] = thisypr[i];
            pypr[i] = (minypr[i] + maxypr[i]) / 2;

            c_centre[i] = curs[i];
        }

        let d = 500;
        if (i == 0) {
            d = C_WIDTH;
        }
        if (i == 1) {
            d = C_HEIGHT;
        }
        curs[i] = constrain(
            map(thisypr[i], minypr[i], maxypr[i], 0, d), 0, d);

        if (i == 1) curs[i] = C_HEIGHT - curs[i];

        gcurs[i] = toGrid(curs[i]);
    }

    g_x = gcurs[0] / gridSize;
    g_y = gcurs[1] / gridSize;

    //if xhair is at a key
    if (atGrid[g_x][g_y]) {
        // draw red circle around where cursor is
        // fill(128, 0, 128);
        // ellipse(curs[0], curs[1], 40, 40);

        // re-set origin here, so minor movements are compensated 
        let x = constrain(map(gcurs[0], 0, C_WIDTH, minypr[0], maxypr[0]), minypr[0], maxypr[0]) - thisypr[0];
        let y = constrain(map(C_HEIGHT - gcurs[1], 0, C_HEIGHT, minypr[1], maxypr[1]), minypr[1], maxypr[1]) - thisypr[1];
        // console.log(x.toFixed(2) + " " + y.toFixed(2) + " " + (x * x + y * y).toFixed(2));
        if ((x * x + y * y) < 0.6) { // size of the neighbourhood, value is empirical for MPU6050
            oypr[0] = oypr[0] - x;
            oypr[1] = oypr[1] - y;
        }
    }
}

function unSetAllMenus() {
    for (var m of menu) {
        m.el.style('color', '#6363ff');
        m.el.style('background', BACKGROUNDCOLOUR);
    }
}

function setThisMenu(x) {
    unSetAllMenus();
    x.style('color', 'orange');
    x.style('background', FOREGROUNDCOLOUR);
    //    currentFunction = x.display;
}

// get the grid position
function toGrid(x) {
    return (Math.round(x / gridSize) * gridSize);
}

// draw cross-hair on (x,y)
function xhair(x, y) {
    let sz = 10; // 1/2 size of x-hair
    push();
    stroke(255, 0, 0);
    strokeWeight(1);
    line(x - sz, y, x + sz, y);
    line(x, y - sz, x, y + sz);
    pop();
}

function calibrate_fn() {
    calibStartTime = millis();
    c_centre[0] = C_WIDTH / 2;
    c_centre[1] = C_HEIGHT / 2;
    c_centre[2] = 100; //arbitrary for 'roll'
    for (let i = 0; i < 3; i++) {
        pypr[i] = 0;
        oypr[i] = ypr[i];
    }
}


function drawMeters() {

    push();
    fill(225);
    textSize(14);

    // draw a small green dot at the centre of the board
    strokeWeight(0);
    fill(220, 255, 220);
    ellipse(C_WIDTH / 2, C_HEIGHT / 2, 10, 10);

    //ellipse(curs[0], curs[1], 10, 10); // fill a circle around the current cursor

    xhair(curs[0], curs[1]); // draw a cross-hair at the current cursor

    // Draw the lines representing the angles
    for (var i = 0; i < 3; i++) {

        let diam = 80;
        let startx = 0,
            starty = C_HEIGHT + 80;

        drawDial(startx + 50 + 100 * i, starty, diam, radians(ypr[i] - oypr[i]));

        fill(100, 100, 255); //rgb(100,100,255)
        strokeWeight(0);

        let j = starty + 80;
        text('ypr', 10, j);
        text(parseFloat(Math.round(ypr[i] * 100) / 100).toFixed(2), 40 + 100 * i, j);
        j += 20;
        text('oypr', 10, j);
        text(parseFloat(Math.round(oypr[i] * 100) / 100).toFixed(2), 40 + 100 * i, j);
        j += 20;
        text('typr', 10, j);
        text(parseFloat(Math.round(thisypr[i] * 100) / 100).toFixed(2), 40 + 100 * i, j);
        j += 20;
        text('cur', 10, j);
        text(parseFloat(Math.round(curs[i] * 100) / 100).toFixed(2), 40 + 100 * i, j);

        j = starty + 80;
        startx = 400;

        drawDial(startx + 50 + 100 * i, starty, diam, radians((wacc[i] / 2000) * 180));

        fill(100, 180, 100);
        text('gyro', startx, j);
        text(gyro[i], startx + 40 + 100 * i, j);
        j += 20;
        text('acc', startx, j);
        text(acc[i], startx + 40 + 100 * i, j);
        j += 20;
        text('wacc', startx, j);
        text(wacc[i], startx + 40 + 100 * i, j);

        if (i == 2) continue;
        fill(100, 100, 180)
        j += 20;
        text('g-cur', startx, j);
        text(gcurs[i], startx + 40 + 100 * i, j);
    }
    pop();
}

function drawDial(x, y, len, angle) {

    push();
    strokeWeight(0);
    fill(220);
    ellipse(x, y, len, len);
    stroke(100, 100, 255);
    strokeWeight(2);
    translate(x, y);
    rotate(angle);
    line(-len / 2, 0, len / 2, 0);
    pop();
}

function save_fn() {
    localStorage.touchpadkeys = JSON.stringify(keys);
    profileJSON.value(localStorage.touchpadkeys);
    //    localStorage.menu = JSON.stringify(menu);
}


// retrieve profile from local storage
function get_fn() {
    if (localStorage.touchpadkeys) {
        profileJSON.value(localStorage.touchpadkeys);
        setit(localStorage.touchpadkeys);
    }
}

// set profile usng what's in profileJSON input area
function setfrom_fn() {
    setit(profileJSON.value());
}

function setit(p) {
    let thiskeys = JSON.parse(p);
    let i = 0;
    for (let i = 0; i < 26; i++) {
        keys[i].el.position(toGrid(thiskeys[i].el.x) - gridSize / 2, toGrid(thiskeys[i].el.y) - gridSize / 2);
        keys[i].el.gridx = toGrid(thiskeys[i].el.x) / gridSize;
        keys[i].el.gridy = toGrid(thiskeys[i].el.y) / gridSize;
        atGrid[keys[i].el.gridx][keys[i].el.gridy] = keys[i];
    }
}

//function add_fn() {
//    setThisMenu(this);
//
//    let k = {};
//    k.selected = false;
//    k.el = createButton(c);
//    k.el.value('new');
//    k.el.style('color', 'blue');
//    k.el.position(mouseX + 100, mouseY);
//    k.el.mousePressed(keyClicked);
//    k.el.mouseOver(keyMouseOver);
//    k.el.mouseOut(keyMouseOut);
//
//    keys.push(k);
//}

// readData is referenced in serialio.parseJSPSdata()
function readData(data) {
    //    console.log('data->' + data);
    var pos = 0;
    var res = data.split(/[\n\r,: ]+/); // split the data on the colons
    var len = res.length;

    while (pos < len) {
        // MPR121 touchpad
        if (res[pos] == '+') {
            padKeyOn(res[++pos]);
        } else if (res[pos] == '-') { // 3 digital pins
            padKeyOn(res[++pos]);
        } else

        // mpu-6050-i2cdevlib
        if (res[pos] == 'YPR') {
            for (var i = 0; i < 3; i++) {
                ypr[i] = res[++pos];
            }
            // ylh ******* swap yaw and roll
            let x = ypr[2];
            ypr[2] = ypr[1];
            ypr[1] = x;
        } else if (res[pos] == 'GYRO') {
            for (var i = 0; i < 3; i++) {
                gyro[i] = res[++pos];
            }
        } else if (res[pos] == 'REALA') {
            for (var i = 0; i < 3; i++) {
                acc[i] = res[++pos];
            }
        } else if (res[pos] == 'WORLDA') {
            for (var i = 0; i < 3; i++) {
                wacc[i] = res[++pos];
            }
        } else
        //RTIMULib : from ArduinoIMU.ino
        // note RTIMULIB prints out "x:" "y:" "z:" so we need to skip these with "++pos"
        if (res[pos] == 'RPY') {
            ++pos;
            ypr[1] = -res[++pos];
            ++pos;
            ypr[2] = res[++pos];
            ++pos;
            ypr[0] = res[++pos];
        } else if (res[pos] == 'Gyro') {
            for (var i = 0; i < 3; i++) {
                ++pos;
                gyro[i] = res[++pos] * 500;
            }
        } else if (res[pos] == 'Accel') {
            for (var i = 0; i < 3; i++) {
                ++pos;
                acc[i] = res[++pos] * 2000;
            }
        } else pos++;
    }
    return;
}