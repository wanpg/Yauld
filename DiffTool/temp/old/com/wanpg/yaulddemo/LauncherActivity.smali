.class public Lcom/wanpg/yaulddemo/LauncherActivity;
.super Landroid/support/v7/app/AppCompatActivity;
.source "LauncherActivity.java"


# direct methods
.method public constructor <init>()V
    .registers 1

    .prologue
    .line 14
    invoke-direct {p0}, Landroid/support/v7/app/AppCompatActivity;-><init>()V

    return-void
.end method

.method static synthetic access$000(Lcom/wanpg/yaulddemo/LauncherActivity;)V
    .registers 1
    .param p0, "x0"    # Lcom/wanpg/yaulddemo/LauncherActivity;

    .prologue
    .line 14
    invoke-direct {p0}, Lcom/wanpg/yaulddemo/LauncherActivity;->toMainActivity()V

    return-void
.end method

.method private toMainActivity()V
    .registers 2

    .prologue
    .line 31
    new-instance v0, Lcom/wanpg/yaulddemo/LauncherActivity$2;

    invoke-direct {v0, p0}, Lcom/wanpg/yaulddemo/LauncherActivity$2;-><init>(Lcom/wanpg/yaulddemo/LauncherActivity;)V

    invoke-virtual {p0, v0}, Lcom/wanpg/yaulddemo/LauncherActivity;->runOnUiThread(Ljava/lang/Runnable;)V

    .line 40
    return-void
.end method


# virtual methods
.method protected onCreate(Landroid/os/Bundle;)V
    .registers 6
    .param p1, "savedInstanceState"    # Landroid/os/Bundle;
        .annotation build Landroid/support/annotation/Nullable;
        .end annotation
    .end param

    .prologue
    .line 18
    invoke-super {p0, p1}, Landroid/support/v7/app/AppCompatActivity;->onCreate(Landroid/os/Bundle;)V

    .line 19
    const v0, 0x7f04001a

    invoke-virtual {p0, v0}, Lcom/wanpg/yaulddemo/LauncherActivity;->setContentView(I)V

    .line 21
    new-instance v0, Landroid/os/Handler;

    invoke-direct {v0}, Landroid/os/Handler;-><init>()V

    new-instance v1, Lcom/wanpg/yaulddemo/LauncherActivity$1;

    invoke-direct {v1, p0}, Lcom/wanpg/yaulddemo/LauncherActivity$1;-><init>(Lcom/wanpg/yaulddemo/LauncherActivity;)V

    const-wide/16 v2, 0x7d0

    invoke-virtual {v0, v1, v2, v3}, Landroid/os/Handler;->postDelayed(Ljava/lang/Runnable;J)Z

    .line 27
    return-void
.end method
