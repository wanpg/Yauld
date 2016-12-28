.class public Lcom/wanpg/yaulddemo/DemoApplication;
.super Landroid/support/multidex/MultiDexApplication;
.source "DemoApplication.java"


# direct methods
.method public constructor <init>()V
    .registers 1

    .prologue
    .line 12
    invoke-direct {p0}, Landroid/support/multidex/MultiDexApplication;-><init>()V

    return-void
.end method


# virtual methods
.method protected attachBaseContext(Landroid/content/Context;)V
    .registers 2
    .param p1, "base"    # Landroid/content/Context;

    .prologue
    .line 16
    invoke-super {p0, p1}, Landroid/support/multidex/MultiDexApplication;->attachBaseContext(Landroid/content/Context;)V

    .line 17
    invoke-static {p1}, Lcom/wanpg/yauld/loader/Loader;->install(Landroid/content/Context;)V

    .line 18
    return-void
.end method

.method public onCreate()V
    .registers 1

    .prologue
    .line 22
    invoke-super {p0}, Landroid/support/multidex/MultiDexApplication;->onCreate()V

    .line 23
    return-void
.end method
