#include <IRrecv.h>
#include <IRremoteESP8266.h>
#include <IRsend.h>
#include <IRtimer.h>
#include <IRutils.h>
#include <ir_Argo.h>
#include <ir_Daikin.h>
#include <ir_Fujitsu.h>
#include <ir_Gree.h>
#include <ir_Haier.h>
#include <ir_Kelvinator.h>
#include <ir_LG.h>
#include <ir_Magiquest.h>
#include <ir_Midea.h>
#include <ir_Mitsubishi.h>
#include <ir_Toshiba.h>
#include <ir_Trotec.h>

#include <ESP8266WiFi.h>
#include <WiFiClient.h>
#include <ESP8266WebServer.h>
#include <FS.h>
//#include <IRremoteESP8266.h>
#include <ArduinoJson.h>

// ******* IR-Reciever *******
int SEND_PIN = 2; //an IR led is connected to GPIO pin 5 (D1)
int RECV_PIN = 0; //an IR detector/demodulatord is connected to GPIO pin 13 (D7)

IRrecv irrecv(RECV_PIN);
IRsend irsend(SEND_PIN, true);
decode_results results;

// ******* Netzwerkeinstellungen, bitte anpassen! *******
const char* ssid     = "lcservice"; // SSID des vorhandenen WLANs
const char* password = "qqeb6y8f"; // Passwort für das vorhandene WLAN

String commandstring;

File fsUploadFile;

IPAddress gateway(192,168,179,1); // IP-Adresse des WLAN-Gateways
IPAddress subnet(255,255,255,0);  // Subnetzmaske
IPAddress ip(192,168,0,221); // feste IP-Adresse für den WeMos

ESP8266WebServer server(80); // Webserver initialisieren auf Port 80

String zeitstempel() { // Betriebszeit als Stunde:Minute:Sekunde
  char stempel[10];
  int lfdStunden  = millis()/3600000;
  int lfdMinuten  = millis()/60000-lfdStunden*60;
  int lfdSekunden = millis()/1000-lfdStunden*3600-lfdMinuten*60;
  int lfdMicro    = millis()/1000-lfdStunden*3600-lfdMinuten*60-lfdSekunden*1000;
  sprintf (stempel,"%03d:%02d:%02d.%03d", lfdStunden, lfdMinuten, lfdSekunden, lfdMicro);
  return stempel;
}

void sendIR() {
  if (server.hasArg("IRCode")){
    unsigned long code = strtoul(server.arg("IRCode").c_str(), NULL, 10);
    if (server.hasArg("IRBrand")){
      String brand = server.arg("IRBrand");
      unsigned long len = 32;
      if (server.hasArg("IRLength")){
        len = strtoul(server.arg("IRLength").c_str(), NULL, 10);
      }

      Serial.print("out: ");
      Serial.print(code);
      Serial.print(", ");
      Serial.print(len);
      Serial.print(", ");
      Serial.println(server.arg("IRBrand"));
      
      server.send(200, "text/plain", server.arg("IRCode"));

      if (brand == "SAMSUNG") {
        irsend.sendSAMSUNG(code, len);
      } else if (brand == "NEC") {
        irsend.sendNEC(code, len);
      } else if (brand == "RC6") {
        irsend.sendRC6(code, len);
      }
    }
  }
}


void sendHF() {
  if (server.hasArg("HFCode")){
    unsigned long code = strtoul(server.arg("HFCode").c_str(), NULL, 10);
    
  }
}

void remoteFiles() {
  Dir dir = SPIFFS.openDir("/json");
  dir.next();
  String message = "[";
  while (true) {
    String filename = dir.fileName();
    filename = filename.substring(6, filename.length() - 5);
    message += "\"" + filename + "\"";
    if (dir.next()) {
      message += ",";
    } else {
      break;
    }
  }
  message += "]";
  server.send(200, "application/json", message);
}

void uploadFile() {
  HTTPUpload& upload = server.upload();
  if(upload.status == UPLOAD_FILE_START){
    String filename = upload.filename;
    if(!filename.endsWith(".json")) return;
    
    if(!filename.startsWith("/")) filename = "/"+filename;
    if(!filename.startsWith("/json")) filename = "/json"+filename;
    Serial.print("handleFileUpload Name: "); Serial.println(filename);
    fsUploadFile = SPIFFS.open(filename, "w");
    filename = String();
  } else if(upload.status == UPLOAD_FILE_WRITE){
    Serial.print("handleFileUpload Data: "); Serial.println(upload.currentSize);
    if(fsUploadFile)
      fsUploadFile.write(upload.buf, upload.currentSize);
  } else if(upload.status == UPLOAD_FILE_END){
    if(fsUploadFile)
      fsUploadFile.close();
    Serial.print("handleFileUpload Size: "); Serial.println(upload.totalSize);
  }
}

void removeFile() {
  if (server.hasArg("file")){
    String path = server.arg("file");
    if (SPIFFS.exists(path)) {
      if (SPIFFS.remove(path)) {
        server.send(200, "text/plain", "OK");
        return;
      }
    }
  }
  server.send(200, "text/plain", "Failure");
  return;
}

void sendCommand(long value, long bits, long type) {
  if (value < 0) {
    value = value + 2^32;
  }
  //Serial.println(String(value) + "," + String(bits) + "," + String(type));
  Serial.println(String((int)value, (unsigned char)DEC) + "," + String((int)bits, (unsigned char)DEC) + "," + String((int)type, (unsigned char)DEC));
  switch ((int)type) {
    default:
    case UNKNOWN:      
      Serial.println("UNKNOWN");
      return;       
    case NEC:
      Serial.println("NEC");
      irsend.sendNEC((int)value, (int)bits);
      return;       
    case RC5:
      Serial.println("RC5");
      irsend.sendRC5((int)value, (int)bits);
      return;       
    case RC6:
      Serial.println("RC6");
      irsend.sendRC6((int)value, (int)bits);
      return;       
    case DISH:
      Serial.println("DISH");
      return;       
    case SHARP:
      Serial.println("SHARP");
      return;       
    case JVC:
      Serial.println("JVC");
      return;       
    case SANYO:
      Serial.println("SANYO");
      return;       
    case MITSUBISHI:
      Serial.println("MITSUBISHI");
      return;       
    case SAMSUNG:
      Serial.println("SAMSUNG");
      irsend.sendSAMSUNG((int)value, (int)bits);
      return;       
    case LG:
      Serial.println("LG");
      irsend.sendLG((int)value, (int)bits);
      return;       
    case WHYNTER:
      Serial.println("WHYNTER");
      return;       
    case AIWA_RC_T501:
      Serial.println("AIWA_RC_T501");
      return;       
    case PANASONIC:
      Serial.println("PANASONIC");
      return;       
    case DENON:
      Serial.println("DENON");
      return;       
  }



}







bool loadFromSpiffs(String path){
  Serial.print(path + ", ");
  
  String dataType = "text/plain";
  if (path.endsWith("/")) path += "index.htm";
  if (server.hasArg("download")){
    Serial.print("download, ");
    dataType = "application/x-download";
  } else {
    if(path.endsWith(".src")) path = path.substring(0, path.lastIndexOf("."));
    else if(path.endsWith(".htm")) dataType = "text/html";
    else if(path.endsWith(".css")) dataType = "text/css";
    else if(path.endsWith(".js")) dataType = "application/javascript";
    else if(path.endsWith(".json")) dataType = "application/json";
    else if(path.endsWith(".png")) dataType = "image/png";
    else if(path.endsWith(".gif")) dataType = "image/gif";
    else if(path.endsWith(".jpg")) dataType = "image/jpeg";
    else if(path.endsWith(".ico")) dataType = "image/x-icon";
    else if(path.endsWith(".xml")) dataType = "text/xml";
    else if(path.endsWith(".pdf")) dataType = "application/pdf";
    else if(path.endsWith(".zip")) dataType = "application/zip";
  }
  
  if (!SPIFFS.exists(path.c_str())) return false;
  File dataFile = SPIFFS.open(path.c_str(), "r");
  long time1 = millis();
  server.streamFile(dataFile, dataType);
  long time2 = millis();
  dataFile.close();

  Serial.println(time2 - time1);
  return true;
}

void handleNotFound(){
  if (loadFromSpiffs(server.uri())) return;
  String message = "File Not Detected\n\n";
  message += "URI: ";
  message += server.uri();
  message += "\nMethod: ";
  message += (server.method() == HTTP_GET)?"GET":"POST";
  message += "\nArguments: ";
  message += server.args();
  message += "\n";
  for (uint8_t i=0; i<server.args(); i++){
    message += " NAME:"+server.argName(i) + "\n VALUE:" + server.arg(i) + "\n";
  }
  server.send(404, "text/plain", message);
  Serial.println(message);
}

String encoding(decode_results &results) {
  switch (results.decode_type) {
    default:
    case UNKNOWN:      return "UNKNOWN";       
    case NEC:          return "NEC";           
    case SONY:         return "SONY";          
    case RC5:          return "RC5";           
    case RC6:          return "RC6";           
    case DISH:         return "DISH";          
    case SHARP:        return "SHARP";         
    case JVC:          return "JVC";           
    case SANYO:        return "SANYO";         
    case MITSUBISHI:   return "MITSUBISHI";    
    case SAMSUNG:      return "SAMSUNG";       
    case LG:           return "LG";            
    case WHYNTER:      return "WHYNTER";       
    case AIWA_RC_T501: return "AIWA_RC_T501";  
    case PANASONIC:    return "PANASONIC";     
    case DENON:        return "DENON";         
  }
}

void setup() {
  SPIFFS.begin(); //Start the File-System

  irrecv.enableIRIn(); // Start the receiver
  irsend.begin(); // Start the sender
  
  // Seriellen Monitor für Kontrollausgaben öffnen
  Serial.begin(115200);
  Serial.println("");
  Serial.println("WeMos-Schaltaktor");
  
  // WLAN-Verbindung herstellen
  
  //WiFi.config(ip, gateway, subnet); // auskommentieren, falls eine dynamische IP bezogen werden soll
  WiFi.begin(ssid, password);
  Serial.print("Verbindungsaufbau");

  // Verbindungsaufbau abwarten
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println(" erfolgreich!");
  Serial.println("");
  Serial.print("Verbunden mit: ");
  Serial.println(ssid);
  Serial.print("Signalstaerke: ");
  int rssi = WiFi.RSSI();
  Serial.print(rssi);
  Serial.println(" dBm");
  Serial.print("IP-Adresse: ");
  Serial.println(WiFi.localIP());
  Serial.println("");

  // HTTP-Anfragen bearbeiten
  server.on("/sendIR.htm", sendIR);
  
  server.on("/json/upload.htm", HTTP_POST, [](){ server.send(200, "text/plain", "upload ..."); }, uploadFile);
  server.on("/json/remove.htm", removeFile);
  
  server.on("/json/remoteFiles.json", remoteFiles);
  server.onNotFound(handleNotFound);
  
  // HTTP-Server starten
  server.begin();
  Serial.println(zeitstempel() + " HTTP-Server gestartet");
  Serial.println("");

  Dir dir = SPIFFS.openDir("/");
  while (dir.next()) {
    Serial.print(dir.fileName() + " ");
    File f = dir.openFile("r");
    Serial.println(f.size());
  }

  
  
  
  File remotes = SPIFFS.open("/json/remotes.json", "r");
  if (!remotes) {
      Serial.println("file open failed");
  } else {
      commandstring = remotes.readString();
      Serial.println(commandstring);
  }
}

void loop() {
  if (irrecv.decode(&results)) {

    Serial.println(String((int)results.value, (unsigned char)DEC) + "," + String((int)results.bits, (unsigned char)DEC) + "," + String((int)results.decode_type, (unsigned char)DEC));
    
    DynamicJsonBuffer jsonBuffer;
    JsonArray& commands = jsonBuffer.parseArray(commandstring);



    for (int i = 0; i < commands.size(); i++) {
      for (int j = 0; j < commands[i].size(); j++) {
        long value = commands[i][j][0];
        long bits = commands[i][j][1];
        long type = commands[i][j][2];

        if (results.value == value && results.bits == bits && results.decode_type == type) {
          sendCommand(commands[0][j][0], commands[0][j][1], commands[0][j][2]);
          goto afterLoop;          
        }
      }
    }

    afterLoop:

//    jsonBuffer.clear();

    
    
    
    
    
    
    irrecv.resume(); // Receive the next value
  }
  
  server.handleClient(); // auf HTTP-Anfragen warten
}
