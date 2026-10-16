# اپلیکیشن اندروید Joyit

پروژه‌ی اندرویدی Joyit یک WebView امن و سبک برای نمایش وب‌سایت `https://joyit.ir/` است. سایت داخل خود برنامه باز می‌شود و برای نمایش بازی‌ها و محتوای آنلاین به اینترنت نیاز دارد.

## امکانات

- نمایش وب‌سایت درون برنامه و پشتیبانی از JavaScript، Local Storage و کوکی‌ها
- نوار پیشرفت بارگذاری صفحه
- صفحه‌ی خطای اتصال با امکان تلاش دوباره در همان نشانی ناموفق
- دکمه‌ی Back اندروید برای پیمایش تاریخچه‌ی صفحات
- مدیریت لینک‌های بازشونده در تب جدید و پشتیبانی محدود از لینک‌های تماس، ایمیل، پیامک و موقعیت مکانی
- فعال بودن Safe Browsing در نسخه‌های اندروید پشتیبانی‌شده
- محدود کردن دسترسی WebView به فایل‌های محلی و جلوگیری از محتوای ناامن HTTP داخل صفحات HTTPS
- غیرفعال بودن WebView debugging در نسخه‌ی Release

## تنظیمات اصلی برنامه

### ۱. آدرس وب‌سایت

در فایل زیر مقدار `HOME_URL` را تغییر بده:

`app/src/main/java/ir/joyit/app/MainActivity.java`

```java
private static final String HOME_URL = "https://joyit.ir/";
```

### ۲. نام، نسخه و شناسه‌ی برنامه

- **نام نمایشی:** `app/src/main/res/values/strings.xml` و مقدار `app_name`
- **شناسه‌ی برنامه (Application ID):** `app/build.gradle` و مقدار `applicationId`
- **فضای نام کدهای اندروید:** همان فایل و مقدار `namespace`
- **شماره و نسخه‌ی انتشار:** `app/build.gradle` و مقادیر `versionCode` و `versionName`
- **حداقل نسخه‌ی اندروید:** مقدار `minSdk` در `app/build.gradle`
- **API هدف:** مقدار `targetSdk` در `app/build.gradle`
- **رنگ پس‌زمینه و رنگ تأکیدی برنامه:** `app/src/main/res/values/colors.xml`
- **تم نوار وضعیت و نوار پایین گوشی:** `app/src/main/res/values/styles.xml`

در نسخه‌ی فعلی، `targetSdk` برابر **34 (Android 14)** است. `compileSdk` روی 35 قرار دارد؛ این مقدار نسخه‌ی SDK مورد استفاده برای کامپایل است و با API هدف برنامه تفاوت دارد. `minSdk` برابر 23 است.

> اگر برنامه قبلاً منتشر شده، برای انتشار به‌روزرسانی باید `applicationId` را ثابت نگه داری و `versionCode` را نسبت به نسخه‌ی منتشرشده افزایش بده.

## تغییر لوگوی برنامه

آیکن در چند اندازه نگهداری می‌شود تا روی گوشی‌های مختلف واضح بماند:

- **آیکن سازگار (Adaptive Icon) در اندروید 8 به بعد:**
  - تصویر پیش‌زمینه: `app/src/main/res/drawable-nodpi/ic_launcher_foreground.png`
  - رنگ پس‌زمینه: مقدار `joyit_icon_background` در `app/src/main/res/values/colors.xml`
  - اتصال این دو تصویر در `app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml` و `ic_launcher_round.xml` انجام می‌شود.
- **آیکن‌های سازگاری برای اندازه‌های مختلف:** فایل‌های `ic_launcher.png` و `ic_launcher_round.png` در پوشه‌های `mipmap-mdpi`، `mipmap-hdpi`، `mipmap-xhdpi`، `mipmap-xxhdpi` و `mipmap-xxxhdpi`.

برای تغییر کامل و یکدست لوگو، تصویر Adaptive و تمام PNGهای اندازه‌بندی‌شده را جایگزین کن. بهتر است تصویر اصلی مربعی، با وضوح بالا و دارای حاشیه‌ی امن برای برش دایره‌ای باشد. پس از جایگزینی، APK را دوباره Build کن.

## مدیریت دسترسی‌ها (Permissions)

دسترسی‌های برنامه در فایل زیر تعریف می‌شوند:

`app/src/main/AndroidManifest.xml`

در نسخه‌ی فعلی فقط این دسترسی ضروری فعال است:

| دسترسی | کاربرد |
|---|---|
| `android.permission.INTERNET` | بارگذاری سایت و بازی‌های آنلاین در WebView |

مجوز `ACCESS_NETWORK_STATE` حذف شده، چون کد فعلی مستقیماً از آن استفاده نمی‌کند. برنامه در وضعیت فعلی به موقعیت مکانی، دوربین، میکروفون، مخاطبین، فایل‌ها یا اعلان‌ها دسترسی اندرویدی درخواست نمی‌کند. مجوزهای تازه را فقط زمانی اضافه کن که قابلیت متناظر واقعاً پیاده‌سازی شده باشد.

نمونه‌ی افزودن یک مجوز (فقط در صورت نیاز واقعی قابلیت):

```xml
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

مجوزهای عادی مانند `INTERNET` معمولاً پنجره‌ی تأیید زمان اجرا ندارند؛ مجوزهای حساس مانند دوربین یا موقعیت مکانی علاوه بر Manifest به درخواست زمان اجرا و مدیریت پاسخ کاربر نیاز دارند. صرفاً اضافه کردن نام مجوز به Manifest برای تکمیل آن قابلیت کافی نیست.

## ساخت APK با GitHub Actions

1. محتوای پوشه‌ی پروژه را در یک مخزن GitHub قرار بده.
2. به بخش **Actions** برو و workflow با نام **Build Joyit APK** را اجرا کن.
3. پس از موفقیت اجرا، از بخش **Artifacts** فایل `joyit-debug-apk` را دانلود کن.
4. ZIP خروجی شامل `app-debug.apk` است.

ساخت APK با هر push به شاخه‌ی `main` یا `master` نیز اجرا می‌شود. Workflow از JDK 17، Android SDK Platform 35 و Build Tools 35.0.0 استفاده می‌کند که با `compileSdk 35` هماهنگ است.

## ساخت محلی

نیازمندی‌ها: JDK 17، Android SDK Platform 35، Android Build Tools 35.0.0 و Gradle 8.9.

در ریشه‌ی پروژه اجرا کن:

```bash
gradle --no-daemon assembleDebug
```

مسیر APK خروجی:

`app/build/outputs/apk/debug/app-debug.apk`

## پیش از انتشار عمومی

- نسخه‌ی Release را با کلید امضای متعلق به خودت امضا کن؛ APK دیباگ برای انتشار نهایی مناسب نیست.
- دامنه‌ی سایت و مجوزهای مورد نیاز را بررسی کن.
- روی نسخه‌های مختلف اندروید و ابعاد مختلف صفحه آزمایش انجام بده.
- اگر قابلیت‌هایی مانند انتخاب فایل، دوربین یا میکروفون به سایت اضافه شد، پشتیبانی WebView و مجوزهای لازم را جداگانه پیاده‌سازی کن.
