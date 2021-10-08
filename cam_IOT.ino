#include <ESP8266WiFi.h>  // WiFi
#include <PubSubClient.h> // mqtt

// 아두이노가 접속할 WiFi AP/password
const char* ssid = "networking407_2.4G"; 
const char* password = "networking407"; 

//const char* ssid = "Hyuckkjjjuuu"; 
//const char* password = "123456789asd"; 
//const char* mqtt_server = "192.168.10.117";

// mqtt 브로커 주소 & mqtt 브로커에 접속하는 클라이언트 이름
const char* mqtt_server = "192.168.15.84";
const char* clientName = "wonyong2"; // 중복되지 않도록 수정

WiFiClient espClient;            // WiFi 접속 객체
PubSubClient client(espClient);  // mqtt 클라이언트 객체

// WiFi 접속-----------------------------------
void setup_wifi() {
  delay(10);
  Serial.println();
  Serial.print("Connecting to ");
  Serial.println(ssid);
  
  WiFi.mode(WIFI_STA);
  WiFi.begin(ssid, password); // WiFi AP 접속 시도

  // WiFi AP 접속 시도
  while(WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  // WiFi AP 접속시 시리얼 모니터에 성공 메시지 출력
  Serial.println("");
  Serial.println("WiFi connected");
  Serial.println("IP address: ");
  Serial.println(WiFi.localIP());
  
}

// mqtt 브로커에 접속 시도-------------------------------
void reconnect() {
  while (!client.connected()) {
    Serial.print("Attempting MQTT connection...");
    // mqtt 브로커 접속 시도
    if (client.connect(clientName)) {
      Serial.println("connected");
    } else {
      Serial.print("failed, rc=");
      Serial.print(client.state());
      Serial.println(" try again in 5 seconds");
      delay(5000);  // 5초 마다 접속 재시도
    }
  }
}

// 아두이노 설정, WiFi AP 및 mqtt 브로커 접속-----------
void setup() {
  //A0번이 센서
  pinMode(A0, INPUT);
  Serial.begin(115200);
  setup_wifi();            // WiFi AP 접속
  client.setServer(mqtt_server, 1883); // mqtt 브로커에 접속
}

// mqtt 브로커 접속 시도-------------------------------
 void loop() {
  if (!client.connected()) {
    reconnect();
  }
  client.loop(); // mqtt 브로커로부터 토픽 청취

  int pirValue = analogRead(A0);
  Serial.println(pirValue);
  //priValue값이 900인 경우, 알람과 함께 dectection 토픽 발행
  if(pirValue > 900){
    Serial.println("detection!!");
    client.publish("detection", "1");  
  }else{
    Serial.println("no. detection!!");
  }
  
  delay(1000);
}
