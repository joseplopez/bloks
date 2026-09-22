# Reglas específicas del proyecto Bloks

# Conservar atributos necesarios para Crashlytics (Line numbers y Source files)
-keepattributes SourceFile,LineNumberTable,*Annotation*,Signature,EnclosingMethod

# Hilt/Dagger (aunque suelen traer sus propias reglas, estas refuerzan)
-keep class dagger.hilt.android.internal.** { *; }
-keep class com.centelles.bloks.di.** { *; }

# Firebase Analytics & Crashlytics
-keep class com.google.firebase.** { *; }

# Google Billing
-keep class com.android.billingclient.** { *; }

# AdMob
-keep class com.google.android.gms.ads.** { *; }
-keep class com.google.ads.** { *; }

# Mantener modelos de datos si se usan en Serialización (JSON/DataStore)
-keep class com.centelles.bloks.engine.model.** { *; }

# Eliminar logs de producción (Opcional, pero recomendado para seguridad y tamaño)
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int w(...);
    public static int e(...);
}

# Android App Startup & WorkManager rules for AGP 9+ compatibility
-keepnames class * extends androidx.startup.Initializer

-keep class androidx.work.impl.WorkDatabase_Impl { *; }

-keep class * extends androidx.work.ListenableWorker {
    <init>(android.content.Context, androidx.work.WorkerParameters);
}

