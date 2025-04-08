# ReversiAI

Bu projede Reversi oyunu için bir yapay zeka gerçekleştirilmiştir. Proje Spring Boot ve React kullanılarak geliştirilmiştir.

## Geliştirme ve Üretim Ortamları

Proje, farklı ortamlarda çalışabilecek şekilde yapılandırılmıştır.

### Backend

Backend iki farklı profilde çalışabilir:
- **dev**: Geliştirme ortamı (varsayılan)
  - URL: http://localhost:8080
  - CORS yapılandırması: http://localhost:3000 için izin verilir

- **prod**: Üretim ortamı
  - URL: https://codeyzersiserver.tail9fb8f4.ts.net
  - CORS yapılandırması: https://codeyzersi.tail9fb8f4.ts.net için izin verilir

Profil değiştirmek için:
```
# Geliştirme ortamı için (varsayılan)
java -jar target/reversi-ai-0.0.1-SNAPSHOT.jar

# Üretim ortamı için
java -jar target/reversi-ai-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

### Frontend

Frontend yapılandırması da ortama göre değişmektedir:
- **development**: Geliştirme ortamı (varsayılan)
  - Frontend URL: http://localhost:3000
  - API URL: http://localhost:8080

- **production**: Üretim ortamı
  - Frontend URL: https://codeyzersi.tail9fb8f4.ts.net
  - API URL: https://codeyzersiserver.tail9fb8f4.ts.net

Frontend komutları:
```
# Geliştirme ortamında çalıştırma
npm run dev

# Üretim ortamı için derleme
npm run prod

# Üretim ortamında servis etme
npm run serve:prod
```

## Kurulum

### Backend
```
mvn clean install
java -jar target/reversi-ai-0.0.1-SNAPSHOT.jar
```

### Frontend
```
cd reversi_frontend
npm install
npm start
```

## Oyun Kuralları

Reversi (Othello olarak da bilinir), iki kişilik bir strateji oyunudur.

- Oyun 8x8 bir tahta üzerinde oynanır.
- Oyuncular sırayla taş yerleştirirler.
- Oyuncu, en az bir rakip taşını çevirebileceği bir alana taş yerleştirmek zorundadır.
- Rakip taşı, yeni yerleştirilen taşın ve oyuncunun diğer taşının arasında kalırsa çevrilir.
- Taşlar yatay, dikey ve çapraz olarak çevrilebilir.
- Oyun, tahta dolduğunda veya hiçbir oyuncu hamle yapamaz hale geldiğinde biter.
- En çok taşa sahip olan oyuncu kazanır. 