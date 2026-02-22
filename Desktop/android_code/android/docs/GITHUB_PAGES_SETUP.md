# GitHub Pages Kurulum Talimatları

## 📋 Adımlar

### 1. Repository'ye Push Etme
```bash
cd /Users/cakir/Desktop/android_code/android
git add docs/
git commit -m "Add legal documents (privacy policy, terms of service, cookie policy)"
git push origin main
```

### 2. GitHub Pages Ayarlarını Yapılandırma

1. **GitHub.com** adresine gidin
2. Repository'nizi açın (piksel-kosucu veya benzeri)
3. **Settings** sekmesine tıklayın
4. Sol menüden **Pages** seçin
5. **Build and deployment** bölümünde:
   - **Source**: "Deploy from a branch" seçin
   - **Branch**: `main` (veya `master`) + `/docs` klasörü seçin
6. **Save** düğmesine tıklayın

### 3. GitHub Pages URL'si

Birkaç dakika sonra aşağıdaki URL'de erişim sağlanabilecek:

```
https://[USERNAME].github.io/[REPOSITORY]/docs/
```

Örneğin:
```
https://cakir.github.io/piksel-kosucu/docs/
```

### 4. Dosyalar

GitHub Pages aşağıdaki dosyaları otomatik olarak sunacaktır:

- **index.html** - Ana sayfa (tüm belgelere bağlantılar)
- **privacy-policy.html** - Gizlilik Politikası
- **terms-of-service.html** - Hizmet Koşulları
- **cookie-policy.html** - Çerez Politikası
- **_config.yml** - Jekyll konfigürasyonu

### 5. URL'lerin Uygulamaya Eklenmesi

Android uygulamada bu belgeleri referans etmek için:

**app/src/main/res/values/strings.xml** dosyasına ekleyin:

```xml
<string name="privacy_policy_url">https://[USERNAME].github.io/[REPOSITORY]/docs/privacy-policy.html</string>
<string name="terms_of_service_url">https://[USERNAME].github.io/[REPOSITORY]/docs/terms-of-service.html</string>
<string name="cookie_policy_url">https://[USERNAME].github.io/[REPOSITORY]/docs/cookie-policy.html</string>
```

### 6. Uygulama İçinde Belgeleri Gösterme

**app/src/main/java/com/pikselkosucu/app/LegalActivity.java**:

```java
package com.pikselkosucu.app;

import android.os.Bundle;
import android.webkit.WebView;
import androidx.appcompat.app.AppCompatActivity;

public class LegalActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_legal);
        
        WebView webView = findViewById(R.id.webView);
        String url = getIntent().getStringExtra("url");
        webView.loadUrl(url);
    }
}
```

### 7. AndroidManifest.xml'e Ekle

```xml
<activity android:name=".LegalActivity" />
```

### 8. Custom Domain (İsteğe Bağlı)

Kendi domaininiz varsa:

1. **Settings → Pages → Custom domain** alanına yazın
2. DNS kayıtlarınızı GitHub'ın talimatlarına göre ayarlayın

## 🔄 Güncellemeler

Belgeleri güncellemek için:

1. `docs/` klasörü içindeki HTML dosyaları düzenleyin
2. Git ile commit ve push yapın
3. GitHub Pages otomatik olarak güncellenir (birkaç dakika içinde)

## 🚀 Domain Önerileri

- `pikselkosucu.app` (if available)
- `pikselkosucu.com`
- `pixelrunner.app`

## 📞 Destek

GitHub Pages hakkında sorular: https://docs.github.com/en/pages

---

**Oluşturma Tarihi:** 23 Şubat 2026
**Durum:** Hazır kullanım
