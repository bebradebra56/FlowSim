# Add project specific ProGuard rules here.

############ APPSFLYER ############
-keep class com.appsflyer.** { *; }
-keep class com.appsflyer.internal.** { *; }
-dontwarn com.appsflyer.**

-keep class com.android.installreferrer.** { *; }
-dontwarn com.android.installreferrer.**

############ FIREBASE ############
-keep class com.google.firebase.installations.** { *; }
-keep class com.google.firebase.analytics.** { *; }
-keep class com.google.firebase.** { *; }
-keep class com.google.firebase.messaging.** { *; }
-keepclassmembers class com.google.firebase.iid.** { *; }

-keep class com.google.android.gms.ads.identifier.** { *; }
-dontwarn com.google.android.gms.ads.identifier.**

-dontwarn com.google.firebase.analytics.**
-dontwarn com.google.firebase.messaging.**
-dontwarn com.google.firebase.iid.**
-dontwarn com.google.firebase.installations.**

############ KOTLIN ############
-keep class kotlin.jvm.internal.** { *; }

############ KOIN ############
# Сохранить Koin Core
-keep class org.koin.** { *; }
-dontwarn org.koin.**

# Сохранить твои классы, которые создаются через Koin (важно!)
-keep class * implements org.koin.core.component.KoinComponent { *; }

-keep class org.koin.android.** { *; }
-dontwarn org.koin.android.**

############ ROOM ############
# Хранить аннотации
-keepattributes *Annotation*

# Сохранить DAO и сущности
-keep class androidx.room.** { *; }
-dontwarn androidx.room.**

# Сохранить модели с @Entity, чтобы поля не обфусцировались
-keep @androidx.room.Entity class * { *; }

# Сохранить абстрактный класс Database
-keep class * extends androidx.room.RoomDatabase { *; }

-keepclassmembers class * {
    @androidx.room.TypeConverter *;
}

-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature